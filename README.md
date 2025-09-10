## 실무같은 와방 쩌는 로그인 시스템 구현 

---


### 요구사항 list
- [x] 유저는 이메일을 통한 회원 가입을 한다. 
- [ ] 유저는 이메일 인증을 하지 않으면, 게스트 상태이고, 외부 이메일 인증 모듈에서 이벤트로 인증 확인이 되면, user로 상태가 바뀐다.
- [ ] 유저는 이메일, 비밀번호를 통해 로그인을 한다. 
- [x] 유저는 비밀번호와 이메일을 바꿀수 있는데, 이메일 변경 요청을 하면, 이메일인증을 완료하기 전까지 기존 이메일을 따른다.
- [ ] 유저의 비밀번호와 이메일 정보는  암호화 해서 저장해야한다.
- [x] 같은 메일, 같은 비밀번호라도, 다른 암호값이 나와야하며, 복호화가 가능해야한다. 
- [x] ADMIN 유저는 일반 유저를 정지를 줄수 있다.
- [x] 회원 탈퇴 요청을 하면, 3년간 정보가 저장된 뒤에 삭제가 된다. 
- [ ] 소셜로그인(카카오, 애플)로 가입이 가능해야한다.
- [x] 이용약관과 같은 정보이용 동의를 문서 번호와 함께 동의 시각까지 기록해야한다.
- [ ] Nginx, MariaDB , Docker, kafka, Redis 를 활용한 인프라를 구성한다.
- [x] auth 서버는 3대가 동시 가동 되어야 하며, nginx를 활용한 로드벨런싱이 되어야한다.
- [ ] 고정 스케쥴링 작업시에 동시에 여러 서버가 작업하지 않아야 한다. 
- [ ] outbox 패턴을 활용하여 이벤트 메세징 수/발신을 보장한다. 
---


###  구현 목표 
- 실제 실무 인것처럼 DDL Auto Created  사용하지 않는다. 
- Test 커버리지 80% 이상을 목표로 하며, 최대한 많은 테스트 커버리지를 확보한다. 
- Swagger를 통한 문서화 + 이벤트 명세를 통해 api 명세를 확실하게 작성한다.
- Nginx, MariaDB , Docker 를 활용한 인프라를 구성한다.


---

### ERD
![erd.png](images/erd.png)
---

### Test 결과 
- 2025/09/10 (version : 1.0)
[authTest_2025_09_10](authTest_2025_09_10.html)
![test1.png](images/test1.png)
![test2.png](images/test2.png)

---

### 서버 아키택처 도식도

![3-tir.png](images/3-tir.png)

- 서버는 쵝소한의 부하 분산 및 성능 향상을 위해 3티어 아키택쳐로 구성했다,
	- ip:9000 으로 들어오는 요청은, Nginx가 프록시 + 로드벨런서 역할을 수행하면서 복제된 auth 3개의 서버에 골골루 작업을 넘긴다.
	- 현제 DB 는 1개를 사용하고 있지만 바인드 마운딩 을 사용하고 있기에 프록시와 디비 서버또한 복제가 가능하다.

- nginx conf

```nginx configuration

# upstream: docker-compose에서 정의한 auth-server-1/2/3 컨테이너를 대상으로 함
upstream auth_backend {
    least_conn;
    server auth-server-1:8080;
    server auth-server-2:8080;
    server auth-server-3:8080;
}

server {
    listen 80;
    server_name _;

    access_log /var/log/nginx/access.log;
    error_log /var/log/nginx/error.log;

    # 정적 파일(필요 시)
    location /static/ {
        alias /usr/share/nginx/html/static/;
        expires 1d;
        add_header Cache-Control "public";
    }

    # 기본 프록시
    location / {
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;

        proxy_connect_timeout 5s;
        proxy_send_timeout 30s;
        proxy_read_timeout 30s;

        proxy_next_upstream error timeout invalid_header http_500 http_502 http_503 http_504;
        proxy_buffering on;
        proxy_buffers 16 16k;
        proxy_buffer_size 32k;

        proxy_pass http://auth_backend;
    }

    # 헬스체크 프록시 (앱이 /health를 제공할 때)
    location = /health {
        proxy_pass http://auth_backend/health;
        proxy_set_header Host $host;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_connect_timeout 2s;
        proxy_read_timeout 2s;
    }

    error_page 500 502 503 504 /50x.html;
    location = /50x.html {
        root /usr/share/nginx/html;
    }
}
```

- docker compose

```yaml
# docker-compose.yml — Compose 방식(비-Swarm)으로 다중 auth-server 인스턴스 정의
version: "3.7"

services:
  nginx:
    image: nginx:alpine
    container_name: nginx
    ports:
      - "9000:80"
    volumes:
      - ./nginx/conf:/etc/nginx/conf.d
      - ./nginx/logs:/var/log/nginx

    depends_on:
      - auth-server-1
      - auth-server-2
      - auth-server-3
    networks:
      - auth-network

  mariadb:
    container_name: mariadbAuth
    image: mariadb:latest
    env_file:
      - .env.prod
    ports:
      - "4000:3306"
    networks:
      - auth-network
    volumes:
      - db-data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql:ro
    healthcheck:
      test: [ "CMD", "mysqladmin", "ping", "-h", "localhost" ]
      interval: 30s
      timeout: 10s
      retries: 5



  # auth-server 인스턴스들을 개별 서비스로 정의 (Compose는 deploy.replicas를 적용하지 않으므로 이렇게 복수 서비스로 표현)
  auth-server-1:
    image: auth-server:1.0
    build:
      context: .
      dockerfile: Dockerfile
    env_file:
      - .env.prod
    container_name: auth-server-1
    networks:
      - auth-network
    depends_on:
      - mariadb

    healthcheck:
      test: [ "CMD", "curl", "-f", "http://localhost:8080/health" ]
      interval: 15s
      timeout: 5s
      retries: 3

  auth-server-2:
    image: auth-server:1.0
    build:
      context: .
      dockerfile: Dockerfile
    env_file:
      - .env.prod
    container_name: auth-server-2
    networks:
      - auth-network
    depends_on:
      - mariadb

    healthcheck:
      test: [ "CMD", "curl", "-f", "http://localhost:8080/health" ]
      interval: 15s
      timeout: 5s
      retries: 3

  auth-server-3:
    image: auth-server:1.0
    build:
      context: .
      dockerfile: Dockerfile
    env_file:
      - .env.prod
    container_name: auth-server-3
    networks:
      - auth-network
    depends_on:
      - mariadb

    healthcheck:
      test: [ "CMD", "curl", "-f", "http://localhost:8080/health" ]
      interval: 15s
      timeout: 5s
      retries: 3

networks:
  auth-network:
    driver: bridge
    name: auth-network

volumes:
  db-data:
    driver: local
    name: db-data

```


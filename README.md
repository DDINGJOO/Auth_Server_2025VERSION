# Auth Server

사용자 인증 및 회원 정보 관리를 담당하는 Spring Boot 기반의 마이크로서비스입니다.

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [주요 기능](#주요-기능)
3. [아키텍처](#아키텍처)
4. [데이터베이스 스키마](#데이터베이스-스키마)
5. [API 엔드포인트](#api-엔드포인트)
6. [기술 스택](#기술-스택)
7. [설정 및 실행](#설정-및-실행)
8. [배포](#배포)

---

## 프로젝트 개요

### 기본 정보

- **프로젝트명**: Auth Server
- **타입**: Spring Boot REST API 마이크로서비스
- **Java**: 21
- **빌드**: Gradle 8.x
- **버전**: 0.0.3_proto

### 핵심 목적

MSA 아키텍처 환경에서 사용자 인증 및 회원 관리를 전담하는 서버입니다.

- 이메일/소셜 로그인 기반 회원가입 및 인증
- JWT Access/Refresh 토큰 발급 및 갱신
- 사용자 상태 관리 및 이력 추적
- 동의 관리 시스템
- 세밀한 인가 정책은 API Gateway로 분리

---

## 주요 기능

### 1. 인증 및 회원관리

- 이메일 기반 회원가입/로그인
- 소셜 로그인 (카카오, 애플 OAuth 2.0)
- JWT Access/Refresh 토큰 발급 및 갱신
- 비밀번호 변경 및 이메일 변경
- 회원 탈퇴 (3년 보관 정책)
- 이메일 인증 코드 발송 및 검증

### 2. 사용자 상태 관리

- 다양한 상태 지원 (ACTIVE, BLOCKED, DELETED, EXPIRED, UNCONFIRMED)
- GUEST → USER 전환 (이메일 인증 후)
- ADMIN에 의한 사용자 정지 기능
- 정지 기간 관리 및 자동 해제
- 상태 변경 이력 추적 (History 테이블)

### 3. 동의 관리

- 이용약관 동의 이력 관리
- 문서 버전별 동의 시각 기록
- 필수/선택 동의 항목 구분
- ConsentId 기반 검증 시스템

### 4. 보안

- 비밀번호 BCrypt 암호화
- 이메일 AES-256 암호화 저장 (양방향 암호화)
- JWT 기반 인증 (Custom Implementation)
- Bean Validation 기반 입력값 검증
- 정규식 검증 (이메일, 비밀번호, 전화번호)

### 5. 이벤트 기반 통합

- Kafka를 통한 사용자 상태 변경 이벤트 발행
- 이메일 변경, 탈퇴, 정지 등 이벤트 전파
- 느슨한 결합으로 다른 마이크로서비스와 통합

### 6. 모니터링 및 통계

- AOP 기반 API 호출 카운트
- Redis Bitmap을 활용한 사용자별 API 통계
- 헬스체크 엔드포인트 (로드밸런서 연동)
- Spring Boot Actuator 통합

### 7. 스케줄링

- 만료된 사용자 자동 정리 (매일 오전 3시)
- Redis 통계 데이터 백업 (추후 구현 예정)
- ShedLock 분산 락 (다중 인스턴스 환경)

---

## 아키텍처

### 계층 구조

```
┌─────────────────────────────────────────┐
│         Controller Layer                │
│  (SignupController, LoginController...)  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Service Layer                   │
│  (SignupService, LoginService...)        │
│  (JwtService, EmailService...)           │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Event Layer (Kafka)                │
│  (StatusChangeEvent, WithdrawEvent...)   │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│      Repository Layer (JPA)             │
│  (AuthRepository, ConsentRepository...)  │
└──────────────┬──────────────────────────┘
               │
┌──────────────▼──────────────────────────┐
│         Entity Layer                    │
│  (Auth, Consent, History, Withdraw...)   │
└─────────────────────────────────────────┘
```

### 디자인 패턴

#### 1. 이벤트 기반 아키텍처

- Kafka를 통한 비동기 이벤트 발행
- 사용자 상태 변경 시 이벤트 발행 (탈퇴, 정지, 이메일 변경 등)
- 느슨한 결합 (Loose Coupling)
- 확장 가능한 구조

#### 2. 3-Tier 아키텍처

- Nginx Load Balancer
- 3개의 Auth Server 인스턴스
- 공유 데이터베이스 및 캐시

#### 3. AOP (Aspect-Oriented Programming)

- API 통계 수집을 위한 횡단 관심사 분리
- ApiRequestCountAspect를 통한 호출 카운트
- Redis Bitmap 기반 사용자별 통계

#### 4. DTO 패턴

- Request/Response DTO 분리
- 계층 간 데이터 전송 표준화
- Bean Validation 적용

#### 5. 템플릿 메서드 패턴

- 소셜 로그인 공통 로직 추상화
- 확장 가능한 OAuth 프로바이더 구조

---

## 데이터베이스 스키마

### 핵심 엔티티

#### 1. auth (사용자 인증 정보)

```sql
id
VARCHAR
    (255)
    PRIMARY KEY  -- 사용자 ID
email        VARCHAR(255) NOT NULL     -- 이메일 (AES-256 암호화)
password     VARCHAR(255)              -- 비밀번호 (BCrypt) - 소셜 로그인은 NULL
provider     ENUM                      -- SYSTEM, KAKAO, APPLE, GOOGLE
status       ENUM                      -- ACTIVE, BLOCKED, DELETED, EXPIRED, UNCONFIRMED
user_role    ENUM                      -- USER, ADMIN, GUEST, PLACE_OWNER
phone_number VARCHAR(255)              -- 전화번호
version      INT                       -- Optimistic Lock
created_at   DATETIME
updated_at   DATETIME
deleted_at   DATETIME
```

**Provider 타입:**

- SYSTEM: 이메일 회원가입
- KAKAO: 카카오 소셜 로그인
- APPLE: 애플 소셜 로그인
- GOOGLE: 구글 소셜 로그인 (예정)

**Status 타입:**

- ACTIVE: 활성 사용자
- BLOCKED: 정지된 사용자
- DELETED: 탈퇴한 사용자
- EXPIRED: 만료된 사용자
- UNCONFIRMED: 이메일 미인증 사용자 (GUEST)

#### 2. consents_name (동의 항목 정의)

```sql
id
VARCHAR
    (255)
    PRIMARY KEY  -- 동의 항목 ID
consent_name VARCHAR(255)              -- 동의 항목 명칭
version      VARCHAR(50)               -- 문서 버전
consent_url  TEXT                      -- 문서 URL
required     BOOLEAN -- 필수 여부
```

**초기 데이터:**

- TERMS_OF_SERVICE: 서비스 이용약관 (필수)
- PRIVACY_POLICY: 개인정보 처리방침 (필수)
- MARKETING_CONSENT: 마케팅 수신 동의 (선택)
- LOCATION_CONSENT: 위치 정보 이용 동의 (선택)

#### 3. consent (사용자 동의 이력)

```sql
id
VARCHAR
    (255)
    PRIMARY KEY
user_id      VARCHAR(255)              -- FK to auth.id
consent_id   VARCHAR(255)              -- FK to consents_name.id
consented_at DATETIME -- 동의 시각
```

#### 4. history (상태 변경 이력)

```sql
id
VARCHAR
    (255)
    PRIMARY KEY
user_id             VARCHAR(255)              -- FK to auth.id
updated_column      VARCHAR(255)              -- 변경된 컬럼명
before_column_value VARCHAR(255)              -- 변경 전 값
after_column_value  VARCHAR(255)              -- 변경 후 값
updated_at          DATETIME                  -- 변경 시각
version             INT
```

#### 5. withdraw (탈퇴 정보)

```sql
user_id
VARCHAR
    (255)
    PRIMARY KEY  -- FK to auth.id
withdraw_at     DATETIME                  -- 탈퇴 시각
withdraw_reason VARCHAR(100) -- 탈퇴 사유
```

**탈퇴 정책:**

- 3년 보관 후 자동 삭제
- auth.status는 DELETED로 변경
- 스케줄러를 통한 자동 정리

#### 6. suspend (사용자 정지)

```sql
id
VARCHAR
    (255)
    PRIMARY KEY
user_id       VARCHAR(255)              -- FK to auth.id
suspend_at    DATETIME                  -- 정지 시각
suspend_until DATE                      -- 정지 종료일
suspender     VARCHAR(255)              -- 정지 처리자 (ADMIN)
reason        VARCHAR(100)              -- 정지 사유
version       INT
```

#### 7. login_status (로그인 상태)

```sql
user_id
VARCHAR
    (255)
    PRIMARY KEY  -- FK to auth.id
login_status          BOOLEAN                   -- 로그인 여부
login_at              DATETIME                  -- 로그인 시각
refresh_token         VARCHAR(255)              -- Refresh Token
refresh_token_expires DATETIME                  -- Refresh Token 만료 시각
version               INT
```

### ERD

```
┌──────────────────┐
│ consents_name    │
│──────────────────│
│ id (PK)          │───┐
│ consent_name     │   │
│ required         │   │
└──────────────────┘   │
                       │
┌──────────────────┐   │
│      auth        │   │
│──────────────────│   │
│ id (PK)          │◄──┼───────┐
│ email (암호화)    │   │       │
│ password         │   │       │
│ provider         │   │       │
│ status           │   │       │
│ user_role        │   │       │
└─────┬────────────┘   │       │
      │ 1:1            │       │
      ▼                │       │
┌──────────────────┐   │       │
│  login_status    │   │       │
│──────────────────│   │       │
│ user_id (PK/FK)  │   │       │
│ refresh_token    │   │       │
└──────────────────┘   │       │
                       │       │
      │ 1:N            │       │
      ▼                │       │
┌──────────────────┐   │       │
│    consent       │   │       │
│──────────────────│   │       │
│ id (PK)          │   │       │
│ user_id (FK)     │───┘       │
│ consent_id (FK)  │◄──────────┘
│ consented_at     │
└──────────────────┘

      │ 1:N
      ▼
┌──────────────────┐
│    history       │
│──────────────────│
│ id (PK)          │
│ user_id (FK)     │
│ updated_column   │
│ before_value     │
│ after_value      │
└──────────────────┘

      │ 1:1
      ▼
┌──────────────────┐
│    withdraw      │
│──────────────────│
│ user_id (PK/FK)  │
│ withdraw_at      │
│ withdraw_reason  │
└──────────────────┘

      │ 1:N
      ▼
┌──────────────────┐
│    suspend       │
│──────────────────│
│ id (PK)          │
│ user_id (FK)     │
│ suspend_until    │
│ suspender        │
│ reason           │
└──────────────────┘
```

---

## API 엔드포인트

### 회원가입

#### POST /api/auth/signup

**이메일 회원가입**

```http
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "password123",
  "passwordConfirm": "password123",
  "phoneNumber": "010-1234-5678",
  "consents": [
    "TERMS_OF_SERVICE",
    "PRIVACY_POLICY",
    "MARKETING_CONSENT"
  ]
}

Response: 200 OK
{
  "id": "550e8400-e29b-41d4-a716-446655440000",
  "email": "user@example.com",
  "role": "GUEST",
  "status": "UNCONFIRMED"
}
```

**검증 규칙:**

- 이메일: RFC 5322 표준 형식
- 비밀번호: 8자 이상, 영문+숫자 조합
- 전화번호: 한국 형식 (010-XXXX-XXXX)
- 필수 동의 항목 누락 시 400 에러

### 로그인

#### POST /api/auth/login

**이메일 로그인**

```http
Content-Type: application/json

Request Body:
{
  "email": "user@example.com",
  "password": "password123"
}

Response: 200 OK
{
  "userId": "550e8400-...",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "status": "ACTIVE"
}
```

**상태 코드:**

- 200: 로그인 성공
- 400: 잘못된 요청
- 401: 이메일 또는 비밀번호 불일치
- 403: 계정 정지 또는 탈퇴

#### POST /api/auth/login/refreshToken

**토큰 갱신**

```http
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}

Response: 200 OK
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

### 소셜 로그인

#### POST /api/auth/social/kakao

**카카오 소셜 로그인**

```http
Content-Type: application/json

Request Body:
{
  "accessToken": "kakao_access_token",
  "consents": [
    "TERMS_OF_SERVICE",
    "PRIVACY_POLICY"
  ]
}

Response: 200 OK
{
  "userId": "550e8400-...",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "isNewUser": false
}
```

**동작:**

1. 카카오 액세스 토큰으로 사용자 정보 조회
2. 기존 사용자: 로그인 처리
3. 신규 사용자: 자동 회원가입 후 로그인

#### POST /api/auth/social/apple

**애플 소셜 로그인**

```http
Content-Type: application/json

Request Body:
{
  "identityToken": "apple_identity_token",
  "consents": [
    "TERMS_OF_SERVICE",
    "PRIVACY_POLICY"
  ]
}

Response: 200 OK (카카오 로그인과 동일)
```

### 사용자 관리

#### PATCH /api/auth/password

**비밀번호 변경**

```http
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "oldPassword": "password123",
  "newPassword": "newPassword456",
  "newPasswordConfirm": "newPassword456"
}

Response: 200 OK
```

#### PATCH /api/auth/email

**이메일 변경**

```http
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "newEmail": "newemail@example.com"
}

Response: 200 OK
```

**이벤트 발행:** `user-email-changed` (Kafka)

#### POST /api/auth/withdraw

**회원 탈퇴**

```http
Authorization: Bearer {accessToken}
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "password": "password123",
  "withdrawReason": "서비스 이용 불편"
}

Response: 200 OK
```

**이벤트 발행:** `user-withdrawn` (Kafka)

### 이메일 인증

#### POST /api/auth/email/code

**인증 코드 발송**

```http
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "email": "user@example.com"
}

Response: 200 OK
{
  "message": "인증 코드가 발송되었습니다."
}
```

**동작:**

- 6자리 랜덤 코드 생성
- Redis에 5분간 저장
- 이메일 발송 (추후 구현)

#### POST /api/auth/email/confirm

**인증 코드 검증**

```http
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "email": "user@example.com",
  "code": "123456"
}

Response: 200 OK
{
  "message": "이메일 인증이 완료되었습니다."
}
```

**동작:**

- Redis에서 코드 검증
- auth.status: UNCONFIRMED → ACTIVE
- auth.user_role: GUEST → USER

### 관리자 기능

#### POST /api/auth/suspend

**사용자 정지**

```http
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-...",
  "suspendUntil": "2025-12-31",
  "reason": "부적절한 콘텐츠 게시"
}

Response: 200 OK
```

**권한:** ADMIN만 가능

#### POST /api/auth/suspend/release

**정지 해제**

```http
Authorization: Bearer {adminAccessToken}
Content-Type: application/json

Request Body:
{
  "userId": "550e8400-..."
}

Response: 200 OK
```

### 헬스 체크

#### GET /health

```
200 OK
"Server is up"
```

### Swagger UI

#### GET /swagger-ui.html

**API 문서 (Swagger UI)**

모든 API 엔드포인트를 시각적으로 확인하고 테스트할 수 있습니다.

---

## 기술 스택

### Core

- **Spring Boot**: 3.5.5
- **Java**: 21 (Eclipse Temurin)
- **Gradle**: 8.x

### Database

- **Production**: MariaDB
- **Test**: H2 (in-memory)
- **JPA**: Hibernate
- **Optimistic Lock**: @Version

### Cache & Messaging

- **Redis**: spring-data-redis
- **Kafka**: spring-kafka
- **ShedLock**: 5.14.0 (분산 락)

### Security

- **BCrypt**: jBCrypt 0.4
- **JWT**: Custom Implementation
- **AES-256**: 이메일 암호화

### Validation

- **Bean Validation**: Hibernate Validator
- **Custom Validators**: Password, PhoneNumber, Consents

### Documentation

- **Swagger/OpenAPI**: springdoc-openapi 2.3.0

### Monitoring

- **Spring Boot Actuator**
- **AOP**: API 통계 수집

### Development

- **Lombok**: 코드 간소화
- **Slf4j**: 로깅

### Testing

- **JUnit 5**
- **Mockito**
- **Spring Boot Test**
- **@DataJpaTest**

---

## 설정 및 실행

### 로컬 실행 (dev 프로파일)

```bash
# 1. 환경 변수 설정
export SPRING_PROFILES_ACTIVE=dev
export DATABASE_HOST=localhost
export DATABASE_PORT=3306
export DATABASE_NAME=auth_db
export DATABASE_USER_NAME=root
export DATABASE_PASSWORD=password
export REDIS_HOST=localhost
export REDIS_PORT=6379
export KAFKA_URL1=localhost:9092
export KAFKA_URL2=localhost:9093
export KAFKA_URL3=localhost:9094
export JWT_SECRET=your-jwt-secret-key-change-in-production
export JWT_ACCESS_TOKEN_EXPIRE_TIME=3600000  # 1시간
export JWT_REFRESH_TOKEN_EXPIRE_TIME=604800000  # 7일

# 2. 데이터베이스 준비
mysql -u root -p < src/main/resources/schema.sql
mysql -u root -p < src/main/resources/data.sql

# 3. 실행
./gradlew bootRun
```

### 설정 파일

#### application.yaml

```yaml
spring:
  profiles:
    active: dev
```

#### application-dev.yaml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mariadb://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}
    username: ${DATABASE_USER_NAME}
    password: ${DATABASE_PASSWORD}

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

  kafka:
    bootstrap-servers:
      - ${KAFKA_URL1}
      - ${KAFKA_URL2}
      - ${KAFKA_URL3}

security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expire-time: ${JWT_ACCESS_TOKEN_EXPIRE_TIME}
    refresh-token-expire-time: ${JWT_REFRESH_TOKEN_EXPIRE_TIME}
  aes:
    encryption-key: default-aes-encryption-key-change-in-production
```

#### application-prod.yaml

```yaml
spring:
  jpa:
    show-sql: false
    hibernate:
      ddl-auto: none

  sql:
    init:
      mode: never
```

---

## 배포

### Docker Compose

#### 아키텍처

```
┌─────────────────┐
│  Nginx:9010     │ (로드 밸런서)
└────────┬────────┘
         │
    ┌────┴────┬────────┐
    │         │        │
┌───▼──┐  ┌───▼──┐  ┌──▼───┐
│Auth  │  │Auth  │  │Auth  │
│Server│  │Server│  │Server│
│  #1  │  │  #2  │  │  #3  │
└──┬───┘  └──┬───┘  └──┬───┘
   └─────────┼─────────┘
             │
    ┌────────▼────────┐
    │   MariaDB       │
    │   Redis         │
    │   Kafka Cluster │
    └─────────────────┘
```

#### docker-compose.yml

```yaml
services:
  auth-server-1:
    image: ddingsh9/auth-server:0.0.3
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_HOST=mariadb
      - DATABASE_PORT=3306
      - DATABASE_NAME=auth_db
      - DATABASE_USER_NAME=${DB_USER}
      - DATABASE_PASSWORD=${DB_PASS}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - KAFKA_URL1=kafka1:9091
      - KAFKA_URL2=kafka2:9092
      - KAFKA_URL3=kafka3:9093
      - JWT_SECRET=${JWT_SECRET}
      - JWT_ACCESS_TOKEN_EXPIRE_TIME=3600000
      - JWT_REFRESH_TOKEN_EXPIRE_TIME=604800000
    networks:
      - auth-network
      - infra-network
    depends_on:
      - mariadb
      - redis
      - kafka1

  auth-server-2:
    image: ddingsh9/auth-server:0.0.3
    # ... (동일 설정)

  auth-server-3:
    image: ddingsh9/auth-server:0.0.3
    # ... (동일 설정)

  nginx:
    image: nginx:alpine
    ports:
      - "9010:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf
    depends_on:
      - auth-server-1
      - auth-server-2
      - auth-server-3
    networks:
      - auth-network

networks:
  auth-network:
  infra-network:
    external: true
```

#### nginx.conf

```nginx
upstream auth_servers {
    server auth-server-1:8080;
    server auth-server-2:8080;
    server auth-server-3:8080;
}

server {
    listen 80;

    location / {
        proxy_pass http://auth_servers;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
    }

    location /health {
        proxy_pass http://auth_servers/health;
    }
}
```

#### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y ca-certificates && \
    rm -rf /var/lib/apt/lists/*

COPY build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### 배포 단계

```bash
# 1. 빌드
./gradlew clean build

# 2. Docker 이미지 생성
docker build -t ddingsh9/auth-server:0.0.3 .

# 3. 푸시
docker push ddingsh9/auth-server:0.0.3

# 4. 배포
docker-compose up -d

# 5. 헬스 체크
curl http://localhost:9010/health
```

---

## 프로젝트 구조

```
src/main/java/com/teambiund/bander/auth_server/
├── auth/
│   ├── controller/
│   │   ├── SignupController.java
│   │   ├── LoginController.java
│   │   ├── SocialLoginController.java
│   │   ├── AuthController.java
│   │   ├── ConsentController.java
│   │   ├── PasswordChangeController.java
│   │   ├── EmailConfirmController.java
│   │   ├── WithdrawController.java
│   │   ├── SuspendController.java
│   │   ├── HealthCheckController.java
│   │   └── enums/
│   │       └── ConsentsController.java
│   │
│   ├── service/
│   │   ├── signup/
│   │   │   ├── SignupService.java
│   │   │   └── SignupServiceImpl.java
│   │   ├── login/
│   │   │   ├── LoginService.java
│   │   │   └── LoginServiceImpl.java
│   │   ├── sociallogin/
│   │   │   ├── SocialLoginService.java
│   │   │   ├── KakaoLoginService.java
│   │   │   └── AppleLoginService.java
│   │   ├── consent_management/
│   │   │   ├── ConsentManagementService.java
│   │   │   └── ConsentManagementServiceImpl.java
│   │   ├── email_confirm/
│   │   │   ├── EmailConfirmService.java
│   │   │   └── EmailConfirmServiceImpl.java
│   │   ├── password_change/
│   │   │   ├── PasswordChangeService.java
│   │   │   └── PasswordChangeServiceImpl.java
│   │   ├── withdraw/
│   │   │   ├── WithdrawService.java
│   │   │   └── WithdrawServiceImpl.java
│   │   ├── suspend/
│   │   │   ├── SuspendService.java
│   │   │   └── SuspendServiceImpl.java
│   │   └── jwt/
│   │       ├── JwtService.java
│   │       └── JwtServiceImpl.java
│   │
│   ├── repository/
│   │   ├── AuthRepository.java
│   │   ├── ConsentRepository.java
│   │   ├── ConsentTableRepository.java
│   │   ├── HistoryRepository.java
│   │   ├── WithdrawRepository.java
│   │   ├── SuspendRepository.java
│   │   └── LoginStatusRepository.java
│   │
│   ├── entity/
│   │   ├── Auth.java
│   │   ├── consentsname/
│   │   │   └── ConsentsTable.java
│   │   ├── Consent.java
│   │   ├── History.java
│   │   ├── Withdraw.java
│   │   ├── Suspend.java
│   │   └── LoginStatus.java
│   │
│   ├── dto/
│   │   ├── request/
│   │   │   ├── SignupRequest.java
│   │   │   ├── LoginRequest.java
│   │   │   ├── SocialLoginRequest.java
│   │   │   ├── PasswordChangeRequest.java
│   │   │   └── ...
│   │   └── response/
│   │       ├── SignupResponse.java
│   │       ├── LoginResponse.java
│   │       └── ...
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorCode.java
│   │   ├── BusinessException.java
│   │   └── ...
│   │
│   ├── config/
│   │   ├── SwaggerConfig.java
│   │   ├── RedisConfig.java
│   │   ├── KafkaProducerConfig.java
│   │   └── ShedLockConfig.java
│   │
│   └── util/
│       ├── validator/
│       │   ├── PasswordMatchesValidator.java
│       │   ├── PhoneNumberValidator.java
│       │   └── RequiredConsentsValidator.java
│       ├── encryption/
│       │   ├── AesEncryptor.java
│       │   └── PasswordEncoder.java
│       ├── jwt/
│       │   └── JwtUtil.java
│       └── data/
│           └── ConsentTableInit.java
│
└── aop/
    └── statistics/
        ├── ApiRequestCountAspect.java
        └── ApiRequestStat.java

src/main/resources/
├── application.yaml
├── application-dev.yaml
├── application-prod.yaml
├── application-test.yaml
├── schema.sql
└── data.sql
```

---

## 주요 개선사항 (v0.0.3)

### 1. 소셜 로그인 구현

- 카카오 OAuth 2.0 연동
- 애플 Sign In 연동
- Provider 기반 사용자 관리
- 자동 회원가입 플로우

### 2. 보안 강화

- 이메일 AES-256 암호화
- Bean Validation 전면 적용
- 커스텀 Validator 구현
- 정규식 검증 체계화

### 3. 코드 품질 개선

- Java 네이밍 컨벤션 전면 준수
- 패키지 구조 표준화
- GlobalExceptionHandler 개선
- ErrorCode enum 도입

### 4. 문서화

- Swagger/OpenAPI 3.0 통합
- 11개 컨트롤러 전체 문서화
- JWT Bearer 인증 스키마
- API 요청/응답 예시 추가

### 5. 테스트 개선

- TestFixture 패턴 적용
- Service 레이어 테스트 강화
- 테스트 커버리지 측정 환경 구축

---

## 아키텍처 결정 사항

### Auth Server vs API Gateway

**Auth Server의 책임:**

- 사용자 인증 (Authentication)
- JWT 토큰 발급 및 갱신
- 회원 정보 관리

**API Gateway의 책임 (추후 구현):**

- JWT 토큰 검증
- 세밀한 인가 정책 (Authorization)
- 라우팅 및 로드밸런싱
- CORS, Rate Limiting

**이유:**

- 책임 분리 원칙 (Separation of Concerns)
- MSA 아키텍처 Best Practice
- 서비스 간 결합도 감소
- 독립적인 확장성

---

## 향후 계획

### Phase 1: 메시징 안정성 (우선순위 HIGH)

- [ ] Outbox 패턴 구현
- [ ] 이벤트 발행 실패 시 재시도 메커니즘
- [ ] Redis 통계 데이터 백업 스케줄러

### Phase 2: 성능 최적화 (우선순위 MEDIUM)

- [ ] N+1 쿼리 문제 해결
- [ ] 데이터베이스 인덱스 최적화
- [ ] 쿼리 성능 튜닝

### Phase 3: 운영 고도화 (우선순위 LOW)

- [ ] Prometheus + Grafana 모니터링
- [ ] ELK Stack 로깅 전략
- [ ] CI/CD 파이프라인 구축
- [ ] 부하 테스트 및 튜닝

### Phase 4: API Gateway 통합

- [ ] API Gateway 프로젝트 생성
- [ ] Spring Security 통합
- [ ] JWT 토큰 검증 필터 구현
- [ ] Auth Server와 연동

---

## 문서

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **API 문서 JSON**: http://localhost:8080/v3/api-docs
- **프로젝트 평가**: [docs/PROJECT_EVALUATION.md](docs/PROJECT_EVALUATION.md)
- **작성일**: 2025-10-23
- **버전**: 0.0.3_proto
- **저자**: DDING (ddingsha9@teambind.co.kr)

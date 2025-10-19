# Auth Server

인증 및 사용자 관리를 담당하는 인증 서버 프로젝트

## 프로젝트 개요

본 프로젝트는 실무 수준의 인증 시스템 구현을 목표로 하며, 이메일 기반 회원가입, 로그인, 동의서 관리, 회원탈퇴, 정지 등의 기능을 제공한다. Docker, Nginx, MariaDB, Redis, Kafka를
활용한 3티어 아키텍처로 설계되었으며, 복수의 서버 인스턴스를 통한 로드밸런싱과 고가용성을 지원한다.

## 기술 스택

- Java 21
- Spring Boot 3.5.5
- Spring Data JPA, Spring Data JDBC
- Spring Kafka
- Redis
- MariaDB
- Docker, Docker Compose
- Nginx
- ShedLock
- JUnit 5, AssertJ

## 요구사항

### 회원가입 및 인증

- 유저는 이메일을 통한 회원 가입을 한다.
- 유저는 이메일 인증을 하지 않으면 게스트 상태이고, 외부 이메일 인증 모듈에서 이벤트로 인증 확인이 되면 user로 상태가 바뀐다.
- 유저는 이메일, 비밀번호를 통해 로그인을 한다.

### 정보 변경 및 보안

- 유저는 비밀번호와 이메일을 바꿀 수 있는데, 이메일 변경 요청을 하면 이메일 인증을 완료하기 전까지 기존 이메일을 따른다.
- 유저의 비밀번호와 이메일 정보는 암호화해서 저장해야 한다.
- 같은 메일, 같은 비밀번호라도 다른 암호값이 나와야 하며, 복호화가 가능해야 한다.

### 관리 기능

- ADMIN 유저는 일반 유저를 정지 줄 수 있다.
- 회원 탈퇴 요청을 하면 3년간 정보가 저장된 뒤에 삭제된다.
- 이용약관과 같은 정보이용 동의를 문서 번호와 함께 동의 시각까지 기록해야 한다.

### 인프라

- Nginx, MariaDB, Docker, Kafka, Redis를 활용한 인프라를 구성한다.
- auth 서버는 3대가 동시 가동되어야 하며, nginx를 활용한 로드밸런싱이 되어야 한다.
- 고정 스케줄링 작업 시에 동시에 여러 서버가 작업하지 않아야 한다.

### 향후 계획

- 소셜로그인(카카오, 애플)로 가입이 가능해야 한다.
- outbox 패턴을 활용하여 이벤트 메시징 수발신을 보장한다.

## 구현 목표

- 실제 실무인 것처럼 DDL Auto Created 사용하지 않는다.
- Test 커버리지 80% 이상을 목표로 하며, 최대한 많은 테스트 커버리지를 확보한다.
- Swagger를 통한 문서화 및 이벤트 명세를 통해 API 명세를 확실하게 작성한다.
- Nginx, MariaDB, Docker를 활용한 인프라를 구성한다.

## 아키텍처

### 서버 구조

서버는 최소한의 부하 분산 및 성능 향상을 위해 3티어 아키텍처로 구성했다.

- ip:9010으로 들어오는 요청은 Nginx가 프록시 및 로드밸런서 역할을 수행하면서 복제된 auth 3개의 서버에 균등하게 작업을 넘긴다.
- 현재 DB는 1개를 사용하고 있지만 바인드 마운팅을 사용하고 있기에 프록시와 DB 서버 또한 복제가 가능하다.

![3-tir.png](images/3-tir.png)

### 네트워크 구성

Docker Compose를 통해 다음과 같이 구성된다:

- auth-network: Nginx와 Auth Server 간 통신
- infra-network: Auth Server와 외부 인프라(MariaDB, Redis, Kafka) 간 통신

각 Auth Server 인스턴스는 헬스체크를 통해 상태를 모니터링하며, 로그는 자동으로 로테이션된다.

## 데이터베이스 설계

### ERD

![erd.png](images/erd2.png)

### 주요 테이블

#### auth

사용자 인증 정보를 저장하는 메인 테이블

- id: 사용자 고유 식별자
- email: 사용자 이메일
- password: 암호화된 비밀번호
- provider: 로그인 제공자(SYSTEM, KAKAO, APPLE, GOOGLE)
- status: 사용자 상태(ACTIVE, UNCONFIRMED, BLOCKED, DELETED, EXPIRED)
- user_role: 사용자 권한(USER, GUEST, ADMIN, PLACE_OWNER)
- phone_number: 전화번호
- version: 낙관적 락을 위한 버전 필드

#### history

사용자 정보 변경 이력을 저장

- user_id: 사용자 ID (FK to auth)
- updated_column: 변경된 컬럼명
- before_column_value: 변경 전 값
- after_column_value: 변경 후 값
- updated_at: 변경 시각

#### consent

사용자의 동의서 동의 기록

- user_id: 사용자 ID (FK to auth)
- consent_type: 동의서 타입
- consent_url: 동의서 URL
- agreement_at: 동의 시각

#### consents_name

동의서 정보 관리 테이블

- id: 동의서 고유 식별자
- consent_name: 동의서 이름
- consent_url: 동의서 URL
- consent_version: 동의서 버전

#### suspend

사용자 정지 정보

- user_id: 정지된 사용자 ID (FK to auth)
- suspender: 정지를 실행한 관리자 ID
- suspend_at: 정지 시각
- suspend_until: 정지 종료일
- reason: 정지 사유

#### withdraw

회원 탈퇴 정보

- user_id: 탈퇴한 사용자 ID (FK to auth)
- withdraw_at: 탈퇴 시각
- withdraw_reason: 탈퇴 사유

#### login_status

로그인 상태 관리

- user_id: 사용자 ID
- last_login: 마지막 로그인 시각

#### shedlock

분산 환경에서 스케줄링 작업 중복 실행 방지를 위한 락 테이블

## 비즈니스 로직

### 회원가입 흐름

1. 사용자가 이메일, 비밀번호, 필수 동의서 정보를 제출
2. 이메일 중복 검증
3. 비밀번호 암호화(BCrypt)
4. auth 테이블에 UNCONFIRMED 상태로 사용자 정보 저장
5. consent 테이블에 동의 정보 저장
6. Kafka를 통해 이메일 인증 요청 이벤트 발행

### 이메일 인증 흐름

![emailConfirmLogic.png](images/emailConfirmLogic.png)

1. 외부 이메일 서비스에서 인증 완료 이벤트를 Kafka로 발행
2. Auth Server가 이벤트를 수신
3. 사용자 상태를 UNCONFIRMED에서 ACTIVE로 변경
4. 변경 이력을 history 테이블에 기록

### 로그인 흐름

1. 사용자가 이메일과 비밀번호를 제출
2. 이메일로 사용자 조회
3. 사용자 상태 검증(ACTIVE 여부 확인)
4. 비밀번호 검증
5. login_status 테이블에 마지막 로그인 시각 업데이트
6. 인증 토큰 발급 및 반환

### 정보 변경 흐름

#### 이메일 변경

1. 새 이메일 중복 검증
2. 이메일 변경 요청 정보를 임시 저장
3. 새 이메일로 인증 요청 발송
4. 인증 완료 시 auth 테이블 업데이트
5. history 테이블에 변경 이력 기록

#### 비밀번호 변경

1. 현재 비밀번호 검증
2. 새 비밀번호 암호화
3. auth 테이블 업데이트
4. history 테이블에 변경 이력 기록

### 회원 탈퇴 흐름

1. 사용자가 탈퇴 요청
2. auth 테이블의 status를 DELETED로 변경
3. withdraw 테이블에 탈퇴 정보 저장(탈퇴 시각, 사유)
4. 스케줄러가 매일 withdraw 테이블을 확인하여 3년 경과한 데이터를 완전 삭제

### 사용자 정지 흐름

1. ADMIN 권한 사용자가 정지 요청
2. 대상 사용자의 권한 검증(ADMIN은 정지 불가)
3. auth 테이블의 status를 BLOCKED로 변경
4. suspend 테이블에 정지 정보 저장
5. 스케줄러가 매일 suspend_until을 확인하여 정지 해제 처리

## 구현 특징

### 동시성 제어

#### ShedLock을 통한 스케줄 작업 중복 실행 방지

복수의 서버 인스턴스가 실행되는 환경에서 스케줄 작업이 중복 실행되지 않도록 ShedLock을 적용했다. JDBC 기반 락을 사용하여 분산 환경에서도 단 하나의 인스턴스만 작업을 수행하도록 보장한다.

#### 낙관적 락(Optimistic Locking)

auth 테이블의 version 필드를 활용하여 동시 업데이트 시 충돌을 감지하고 처리한다.

### 암호화

#### 비밀번호 암호화

BCrypt 알고리즘을 사용하여 비밀번호를 해시화한다. 같은 비밀번호라도 매번 다른 해시값이 생성되며, 솔트가 자동으로 포함되어 저장된다.

#### 이메일 암호화

민감한 개인정보인 이메일은 AES 암호화를 적용하여 저장하며, 필요 시 복호화가 가능하다.

### API 성능 측정 및 호출 통계

#### 구현 개요

각 API의 평균적인 성능, 일일 호출된 API별 호출 통계, 일일 방문 통계 같은 서비스를 제공해야 한다는 요구사항을 듣고 서비스를 구현하기 시작했다.

#### 사용 기술

Spring AOP와 Redis Bitmap을 이용하여 구현했다.

#### 기술 선택 배경

Spring AOP:
API 호출 카운트나 성능 측정과 관련된 코드들은 비즈니스 로직의 처음과 끝에 호출되어야 하는 각 메서드들이 있다. 컨트롤러의 모든 메서드들을 이렇게 바꿀 수가 없을 뿐더러, 모든 메서드의
try-catch-finally 구문을 삽입하여 가독성을 해하는 결과를 초래하기에 이를 해결하고자 AOP를 활용하여 로직을 구현하고자 했다.

Redis Bitmap:
Redis에는 여러 자료형을 값이나 키로 저장할 수 있다. 처음엔 API를 호출할 때마다 API 이름을 키로 갖고 네트워크 IO를 고려하여 incr를 활용해 즉각적인 업데이트를 고려했지만, 이는 User별 방문
통계를 내기엔 상당한 어려움이 있고, user를 키로 넣고 Sets로 저장했을 때는 일일 통계를 낼 때 상당한 작업 리소스가 들어갈 것으로 생각되어, Bitmap을 활용하여 key를 API이름-날짜, offset을
유저 아이디로 활용하여 저장하기로 구현했고, 유저 아이디가 없는 경우는 난수를 지정하여 카운트 하기로 했다.

#### 구현 결과

![실패시 레디스 넣기.png](images/실패시%20레디스%20넣기.png)
![요청 성공시.png](images/요청%20성공시.png)
![헬스체크 X.png](images/헬스체크%20X.png)

Redis 모니터링을 통해 원하는 명령어가 잘 들어갔고, 헬스체크 같이 데이터로의 의미가 없는 API는 Aspect 문법을 통해 제외했다.

#### 향후 개선 사항

- 하루 Redis에 저장된 데이터를 데이터베이스에 업로드하고 Redis 메모리를 삭제해야 하는 로직이 추가되어야 한다.
- 게스트 유저에 대한 정보는 로그인한 유저에 비해 호출별로 모두 기록하기에 게스트 유저에 대한 통계 가중치를 설정해야 한다.
- 모든 서비스에 대해 사용할 수 있게 git을 통해 배포하여 스타터 팩으로 만들어야 한다.

### 이벤트 기반 아키텍처

Kafka를 활용하여 이메일 인증 요청, 인증 완료 등의 이벤트를 비동기로 처리한다. 이를 통해 서비스 간 결합도를 낮추고 확장성을 높였다.

## 테스트

### 테스트 전략

- 단위 테스트: 각 서비스 계층의 비즈니스 로직을 검증
- 통합 테스트: Repository 계층과 데이터베이스 연동을 검증
- H2 인메모리 데이터베이스를 활용한 테스트 환경 구성
- Mock 객체를 활용한 외부 의존성 격리

### 테스트 커버리지

현재 14개의 테스트 클래스가 작성되어 있으며, 주요 비즈니스 로직에 대한 테스트를 포함한다.

### 테스트 결과

2025년 9월 10일 기준 전체 테스트 통과

![test1.png](images/test1.png)
![test2.png](images/test2.png)

## 실행 방법

### 필수 요구사항

- Docker, Docker Compose
- Java 21
- Gradle

### 로컬 실행

```bash
./gradlew bootRun
```

### Docker Compose 실행

```bash
docker-compose up -d
```

서버는 9010 포트로 접근 가능하며, Nginx를 통해 3개의 Auth Server 인스턴스로 로드밸런싱된다.

## API 엔드포인트

### 회원가입

POST /signup

### 로그인

POST /login

### 이메일 인증

POST /email/confirm

### 비밀번호 변경

PUT /password/change

### 회원 탈퇴

POST /withdraw

### 사용자 정지 (ADMIN)

POST /suspend

### 동의서 관리

GET /consents
POST /consents

## 프로젝트 구조

```
src/main/java/com/teambiund/bander/auth_server/
├── aop/              # AOP 관련(API 통계, 성능 측정)
├── config/           # 설정 클래스
├── controller/       # REST API 컨트롤러
├── dto/              # DTO 클래스
├── entity/           # JPA 엔티티
├── enums/            # Enum 타입 정의
├── event/            # 이벤트 클래스
├── exceptions/       # 커스텀 예외
├── repository/       # JPA Repository
├── service/          # 비즈니스 로직
├── util/             # 유틸리티 클래스
└── validation/       # 커스텀 밸리데이션
```

## 버전

현재 버전: 0.0.2_proto

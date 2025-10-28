# Auth Server 2025 VERSION

사용자 인증, 인가, 회원 관리를 담당하는 Spring Boot 기반 MSA 마이크로서비스입니다.

## 목차

1. [프로젝트 개요](#프로젝트-개요)
2. [주요 기능](#주요-기능)
3. [아키텍처](#아키텍처)
4. [데이터베이스 스키마](#데이터베이스-스키마)
5. [API 엔드포인트](#api-엔드포인트)
6. [기술 스택](#기술-스택)
7. [환경 설정](#환경-설정)
8. [테스트](#테스트)
9. [성능 최적화](#성능-최적화)
10. [배포](#배포)
11. [향후 계획](#향후-계획)

---

## 프로젝트 개요

### 기본 정보

- **프로젝트명**: Auth Server
- **타입**: Spring Boot Microservice
- **Java 버전**: 21 (Eclipse Temurin)
- **빌드 도구**: Gradle 8.x
- **Spring Boot 버전**: 3.5.5
- **현재 버전**: 0.0.3_proto
- **포트**: 8080 (개발), 9010 (Nginx LB)

### 핵심 목적

Auth Server는 MSA 아키텍처에서 **인증(Authentication)과 사용자 관리**를 전담하는 마이크로서비스로, 다음 책임을 수행합니다:

- 사용자 회원가입 및 인증 (이메일/소셜 로그인)
- JWT 기반 토큰 발급 및 갱신
- 사용자 생명주기 관리 (정지, 탈퇴, 복구)
- 동의서 관리 및 검증
- 사용자 이력 추적 및 감사
- 이메일 인증 및 비밀번호 관리

### 설계 원칙

- **분산 ID 생성**: Snowflake 알고리즘 기반 64-bit 고유 ID
- **이벤트 기반 통신**: Kafka를 통한 비동기 메시지 발행
- **보안 강화**: BCrypt 암호화, Redis 세션 관리, AES-256 개인정보 암호화
- **동시성 제어**: 낙관적 락(Optimistic Locking) 및 ShedLock 분산 락
- **API 문서화**: Swagger/OpenAPI 3.0 통합

### 아키텍처 결정 사항

**Auth Server의 책임:**

- 사용자 인증 (Authentication)
- JWT 토큰 발급 및 갱신
- 회원 정보 관리

**API Gateway의 책임 (추후 구현):**

- JWT 토큰 검증
- 세밀한 인가 정책 (Authorization)
- 라우팅 및 로드밸런싱
- CORS, Rate Limiting

**설계 이유:**

- 책임 분리 원칙 (Separation of Concerns)
- MSA 아키텍처 Best Practice
- 서비스 간 결합도 감소
- 독립적인 확장성

---

## 주요 기능

### 1. 회원가입 및 인증

#### 1.1 이메일 회원가입

- 이메일 중복 검증
- BCrypt 비밀번호 암호화 (Strength 12)
- 필수/선택 동의서 검증
- 이메일 인증 코드 발송 (Redis 기반, 5분 만료)
- Kafka 이벤트 발행 (회원가입 완료)

#### 1.2 소셜 로그인 (OAuth 2.0)

- **카카오 로그인**: Access Token 검증 후 자동 회원가입
- **애플 로그인**: Identity Token 검증
- 기존 회원 자동 연동 (이메일 기준)
- 소셜 계정 Provider 추적 (KAKAO, APPLE, GOOGLE)

### 2. 로그인 및 세션 관리

#### 2.1 JWT 토큰 발급

- **Access Token**: 1시간 유효
- **Refresh Token**: 7일 유효, Redis 저장
- Bearer Authentication 방식
- 토큰 갱신 API 제공

#### 2.2 로그인 상태 추적

- 최근 로그인 시간 기록
- 로그인 이력 저장 (LoginStatus 엔티티)
- Redis 기반 세션 캐싱

### 3. 동의서 관리

#### 3.1 동의서 유형

- **필수 동의**:
	- 서비스 이용약관 (TERMS_OF_SERVICE)
	- 개인정보 제3자 정보 제공 (PRIVACY_THIRD_PARTY)
- **선택 동의**:
	- 마케팅 정보 수신 (MARKETING_CONSENT)
	- 위치기반 서비스 (LOCATION_BASED_SERVICE)

#### 3.2 동의서 검증

- 회원가입 시 필수 동의 강제 검증
- 동의 철회 및 재동의 지원
- 동의 이력 추적 (Consent 엔티티)
- 동의서 버전 관리 (URL 제공)

### 4. 사용자 생명주기 관리

#### 4.1 회원 정지

- 관리자 권한 검증
- 정지 사유 및 기간 설정
- 다중 정지 이력 관리 (Suspend 엔티티)
- 정지 해제 API 제공

#### 4.2 회원 탈퇴

- 소프트 삭제 (Soft Delete) 방식
- 탈퇴 사유 기록
- 탈퇴 후 데이터 보관 (3년 보관 정책)
- 탈퇴 철회 기능 (30일 이내)

#### 4.3 이력 추적

- 모든 사용자 정보 변경 이력 자동 기록
- Before/After 값 저장
- 감사 로그 (Audit Log) 목적
- History 엔티티를 통한 추적성 보장

### 5. 이메일 인증

#### 5.1 인증 코드 발송

- 6자리 숫자 코드 생성
- Redis 저장 (5분 TTL)
- 이메일 전송 (비동기 처리 권장)
- 재발송 제한 (1분 쿨다운)

#### 5.2 인증 검증

- 코드 일치 여부 확인
- 만료 시간 검증
- 검증 완료 시 Redis 키 삭제
- 인증 상태 업데이트 (UNCONFIRMED → ACTIVE)

### 6. 비밀번호 관리

#### 6.1 비밀번호 변경

- 현재 비밀번호 확인
- 새 비밀번호 복잡도 검증
- BCrypt 재암호화
- 변경 이력 기록

#### 6.2 비밀번호 찾기

- 이메일 인증 후 임시 비밀번호 발급
- 비밀번호 재설정 토큰 발행

### 7. 통계 및 모니터링

#### 7.1 API 요청 통계

- AOP 기반 요청 횟수 추적
- Redis Bitmap을 활용한 사용자별 API 통계
- 엔드포인트별 호출 빈도 분석

#### 7.2 헬스 체크

- `/health` 엔드포인트 제공
- Nginx 로드밸런서 연동
- DB, Redis, Kafka 상태 확인

### 8. 스케줄링

- 만료된 사용자 자동 정리 (매일 오전 3시)
- Redis 통계 데이터 백업 (추후 구현 예정)
- ShedLock 분산 락 (다중 인스턴스 환경)

---

## 아키텍처

### 레이어 구조

```
┌─────────────────────────────────────────────────────────┐
│                    Presentation Layer                    │
│  (Controllers, Request/Response DTOs, Exception Handler) │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                     Service Layer                        │
│    (Business Logic, Transaction Management, Events)      │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                   Persistence Layer                      │
│       (JPA Repositories, Entity, Query Optimization)     │
└─────────────────┬───────────────────────────────────────┘
                  │
┌─────────────────▼───────────────────────────────────────┐
│                  Infrastructure Layer                    │
│  (Database, Redis, Kafka, External API, Scheduler)       │
└─────────────────────────────────────────────────────────┘
```

### 3-Tier 배포 아키텍처

```
┌─────────────────────────┐
│  Nginx Load Balancer    │ :9010
└────────┬────────────────┘
         │
    ┌────┴────┬───────────┐
    │         │           │
┌───▼──┐  ┌───▼──┐  ┌────▼─┐
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

### 주요 디자인 패턴

#### 1. 이벤트 기반 아키텍처 (Event-Driven Architecture)
- Kafka를 통한 비동기 이벤트 발행
- 사용자 상태 변경 시 이벤트 발행 (탈퇴, 정지, 이메일 변경 등)
- 느슨한 결합 (Loose Coupling)
- 확장 가능한 구조

#### 2. Strategy Pattern

- 소셜 로그인 전략 (카카오, 애플)
- ID 생성 전략 (Snowflake, UUID)

#### 3. Template Method Pattern

- 회원가입 플로우 추상화
- 로그인 검증 프로세스

#### 4. Repository Pattern

- JPA Repository를 통한 데이터 접근 추상화
- 커스텀 쿼리 메서드 정의

#### 5. DTO Pattern

- Request/Response DTO 분리
- Entity ↔ DTO 변환 레이어
- Bean Validation 적용

#### 6. AOP (Aspect-Oriented Programming)
- API 통계 수집을 위한 횡단 관심사 분리
- ApiRequestCountAspect를 통한 호출 카운트
- Redis Bitmap 기반 사용자별 통계

#### 7. Optimistic Locking

- `@Version` 어노테이션 기반 동시성 제어
- 충돌 발생 시 재시도 로직

---

## 데이터베이스 스키마

### Entity 관계도 (ERD)

```
┌─────────────────┐
│      Auth       │ (사용자 기본 정보)
│─────────────────│
│ PK: id          │
│    email        │ ← AES-256 암호화
│    password     │ ← BCrypt 해시
│    provider     │ ← ENUM (KAKAO, APPLE, GOOGLE, SYSTEM)
│    status       │ ← ENUM (ACTIVE, BLOCKED, DELETED, UNCONFIRMED)
│    user_role    │ ← ENUM (USER, ADMIN, GUEST, PLACE_OWNER)
│    version      │ ← 낙관적 락
└────┬────────────┘
     │
     ├─1:N─► ┌─────────────────┐
     │       │     History     │ (사용자 정보 변경 이력)
     │       │─────────────────│
     │       │ PK: id          │
     │       │ FK: user_id     │
     │       │    updated_at   │
     │       │    updated_column   │
     │       │    before_value │
     │       │    after_value  │
     │       └─────────────────┘
     │
     ├─1:N─► ┌─────────────────┐
     │       │     Consent     │ (동의서 기록)
     │       │─────────────────│
     │       │ PK: id          │
     │       │ FK: user_id     │
     │       │ FK: consent_id  │ ──► ConsentsTable
     │       │    consented_at │
     │       └─────────────────┘
     │
     ├─1:1─► ┌─────────────────┐
     │       │    Withdraw     │ (회원 탈퇴 정보)
     │       │─────────────────│
     │       │ PK: user_id     │
     │       │    withdraw_at  │
     │       │    withdraw_reason│
     │       └─────────────────┘
     │
     ├─1:N─► ┌─────────────────┐
     │       │     Suspend     │ (회원 정지 이력)
     │       │─────────────────│
     │       │ PK: id          │
     │       │ FK: user_id     │
     │       │    suspend_at   │
     │       │    suspend_until│
     │       │    reason       │
     │       │    suspender    │
     │       └─────────────────┘
     │
     └─1:1─► ┌─────────────────┐
             │  LoginStatus    │ (최근 로그인 정보)
             │─────────────────│
             │ PK: user_id     │
             │    last_login   │
             │    refresh_token│
             └─────────────────┘

             ┌──────────────────┐
             │  ConsentsTable   │ (동의서 마스터 테이블)
             │──────────────────│
             │ PK: id           │
             │    consent_name  │
             │    version       │
             │    consent_url   │
             │    required      │ ← 필수 동의 여부
             └──────────────────┘
```

### 핵심 테이블 상세 스펙

#### 1. auth (사용자 인증 정보)

| 컬럼명          | 타입           | 제약조건        | 설명                                             |
|--------------|--------------|-------------|------------------------------------------------|
| id           | VARCHAR(255) | PRIMARY KEY | Snowflake ID                                   |
| email        | VARCHAR(255) | NOT NULL    | AES-256 암호화                                    |
| password     | VARCHAR(255) | NULL        | BCrypt 해시 (소셜 로그인 시 NULL)                      |
| provider     | ENUM         | NOT NULL    | SYSTEM, KAKAO, APPLE, GOOGLE                   |
| status       | ENUM         | NOT NULL    | ACTIVE, BLOCKED, DELETED, EXPIRED, UNCONFIRMED |
| user_role    | ENUM         | NOT NULL    | USER, ADMIN, GUEST, PLACE_OWNER                |
| phone_number | VARCHAR(255) | NULL        | 전화번호                                           |
| version      | INT          | NULL        | Optimistic Lock                                |
| created_at   | DATETIME(6)  | NULL        | 생성 일시                                          |
| updated_at   | DATETIME(6)  | NULL        | 수정 일시                                          |
| deleted_at   | DATETIME(6)  | NULL        | 삭제 일시 (Soft Delete)                            |

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

| 컬럼명          | 타입           | 제약조건                    | 설명       |
|--------------|--------------|-------------------------|----------|
| id           | VARCHAR(255) | PRIMARY KEY             | 동의 항목 ID |
| consent_name | VARCHAR(255) | NULL                    | 동의 항목 명칭 |
| version      | VARCHAR(50)  | NULL                    | 문서 버전    |
| consent_url  | TEXT         | NULL                    | 문서 URL   |
| required     | BOOLEAN      | NOT NULL, DEFAULT FALSE | 필수 여부    |

**초기 데이터:**
- TERMS_OF_SERVICE: 서비스 이용약관 (필수)
- PRIVACY_THIRD_PARTY: 개인정보 제3자 정보 제공 (필수)
- MARKETING_CONSENT: 마케팅 정보 수신 동의 (선택)
- LOCATION_BASED_SERVICE: 위치기반 서비스 이용약관 동의 (선택)

#### 3. consent (사용자 동의 이력)

| 컬럼명          | 타입           | 제약조건                   | 설명           |
|--------------|--------------|------------------------|--------------|
| id           | VARCHAR(255) | PRIMARY KEY            | Snowflake ID |
| user_id      | VARCHAR(255) | FK to auth.id          | 사용자 ID       |
| consent_id   | VARCHAR(255) | FK to consents_name.id | 동의 항목 ID     |
| consented_at | DATETIME(6)  | NOT NULL               | 동의 시각        |

#### 4. history (상태 변경 이력)

| 컬럼명                 | 타입           | 제약조건          | 설명              |
|---------------------|--------------|---------------|-----------------|
| id                  | VARCHAR(255) | PRIMARY KEY   | Snowflake ID    |
| user_id             | VARCHAR(255) | FK to auth.id | 사용자 ID          |
| updated_column      | VARCHAR(255) | NOT NULL      | 변경된 컬럼명         |
| before_column_value | VARCHAR(255) | NULL          | 변경 전 값          |
| after_column_value  | VARCHAR(255) | NOT NULL      | 변경 후 값          |
| updated_at          | DATETIME(6)  | NULL          | 변경 시각           |
| version             | INT          | NULL          | Optimistic Lock |

#### 5. withdraw (탈퇴 정보)

| 컬럼명             | 타입           | 제약조건                       | 설명     |
|-----------------|--------------|----------------------------|--------|
| user_id         | VARCHAR(255) | PRIMARY KEY, FK to auth.id | 사용자 ID |
| withdraw_at     | DATETIME(6)  | NOT NULL                   | 탈퇴 시각  |
| withdraw_reason | VARCHAR(100) | NULL                       | 탈퇴 사유  |

**탈퇴 정책:**
- 3년 보관 후 자동 삭제
- auth.status는 DELETED로 변경
- 스케줄러를 통한 자동 정리

#### 6. suspend (사용자 정지)

| 컬럼명           | 타입           | 제약조건          | 설명              |
|---------------|--------------|---------------|-----------------|
| id            | VARCHAR(255) | PRIMARY KEY   | Snowflake ID    |
| user_id       | VARCHAR(255) | FK to auth.id | 사용자 ID          |
| suspend_at    | DATETIME(6)  | NOT NULL      | 정지 시각           |
| suspend_until | DATE         | NOT NULL      | 정지 종료일          |
| suspender     | VARCHAR(255) | NOT NULL      | 정지 처리자 (ADMIN)  |
| reason        | VARCHAR(100) | NOT NULL      | 정지 사유           |
| version       | INT          | NULL          | Optimistic Lock |

#### 7. login_status (로그인 상태)

| 컬럼명        | 타입           | 제약조건                       | 설명         |
|------------|--------------|----------------------------|------------|
| user_id    | VARCHAR(255) | PRIMARY KEY, FK to auth.id | 사용자 ID     |
| last_login | DATETIME(6)  | NULL                       | 마지막 로그인 시각 |

### 인덱싱 전략

#### Auth 테이블
```sql
-- 이메일 중복 검증 및 로그인 조회
CREATE INDEX idx_auth_email ON auth (email(191));

-- 사용자 상태별 조회 (정지/탈퇴 회원 필터링)
CREATE INDEX idx_auth_status ON auth (status);

-- 가입일 기준 정렬
CREATE INDEX idx_auth_created_at ON auth (created_at);
```

#### History 테이블

```sql
-- 사용자별 이력 조회
CREATE INDEX idx_history_user_id ON history (user_id);
```

#### Consent 테이블

```sql
-- 사용자별 동의 이력
CREATE INDEX idx_consent_user_id ON consent (user_id);

-- 동의 유형별 조회
CREATE INDEX idx_consent_consent_id ON consent (consent_id);

-- 복합 인덱스 (사용자 + 동의 유형)
CREATE INDEX idx_consent_user_consent ON consent (user_id, consent_id);
```

#### Suspend 테이블

```sql
-- 사용자별 정지 이력
CREATE INDEX idx_suspend_user_id ON suspend (user_id);

-- 정지 만료일 기준 스케줄러 조회
CREATE INDEX idx_suspend_until ON suspend (suspend_until);
```

#### ConsentsTable 테이블

```sql
-- 동의서 이름 기준 조회
CREATE INDEX idx_consents_name_name ON consents_name (consent_name(191));

-- 필수 동의서 필터링
CREATE INDEX idx_consents_name_required ON consents_name (required);
```

---

## API 엔드포인트

### 회원가입

#### POST `/api/auth/signup`

이메일 기반 회원가입

**Request:**
```http
POST /api/auth/signup HTTP/1.1
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123",
  "passwordConfirm": "password123",
  "phoneNumber": "010-1234-5678",
  "consents": [
    "TERMS_OF_SERVICE",
    "PRIVACY_THIRD_PARTY",
    "MARKETING_CONSENT"
  ]
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "id": "1234567890123456789",
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

**Error Cases:**

- `400 Bad Request`: 필수 동의 누락, 잘못된 이메일 형식, 비밀번호 불일치
- `409 Conflict`: 이미 존재하는 이메일

---

### 로그인

#### POST `/api/auth/login`

이메일/비밀번호 로그인

**Request:**
```http
POST /api/auth/login HTTP/1.1
Content-Type: application/json

{
  "email": "user@example.com",
  "password": "password123"
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "role": "USER",
  "status": "ACTIVE"
}
```

**상태 코드:**

- `200 OK`: 로그인 성공
- `400 Bad Request`: 잘못된 요청 형식
- `401 Unauthorized`: 이메일 또는 비밀번호 불일치
- `403 Forbidden`: 계정 정지 또는 탈퇴

---

#### POST `/api/auth/login/refreshToken`

Refresh Token을 이용한 Access Token 갱신

**Request:**
```http
POST /api/auth/login/refreshToken HTTP/1.1
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9..."
}
```

---

### 소셜 로그인

#### POST `/api/auth/social/kakao`

카카오 소셜 로그인

**Request:**
```http
POST /api/auth/social/kakao HTTP/1.1
Content-Type: application/json

{
  "accessToken": "kakao_access_token",
  "consents": [
    "TERMS_OF_SERVICE",
    "PRIVACY_THIRD_PARTY"
  ]
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "isNewUser": false
}
```

**동작:**
1. 카카오 액세스 토큰으로 사용자 정보 조회
2. 기존 사용자: 로그인 처리
3. 신규 사용자: 자동 회원가입 후 로그인

---

#### POST `/api/auth/social/apple`

애플 소셜 로그인

**Request:**
```http
POST /api/auth/social/apple HTTP/1.1
Content-Type: application/json

{
  "identityToken": "apple_identity_token",
  "consents": [
    "TERMS_OF_SERVICE",
    "PRIVACY_THIRD_PARTY"
  ]
}
```

**Response:**
```http
HTTP/1.1 200 OK (카카오 로그인과 동일)
```

---

### 이메일 인증

#### POST `/api/auth/email/code`

인증 코드 발송

**Request:**
```http
POST /api/auth/email/code HTTP/1.1
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "email": "user@example.com"
}
```

**Response:**
```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "인증 코드가 발송되었습니다.",
  "expiresIn": 300
}
```

**동작:**
- 6자리 랜덤 코드 생성
- Redis에 5분간 저장
- 이메일 발송 (추후 구현)

---

#### POST `/api/auth/email/confirm`

인증 코드 검증

**Request:**
```http
POST /api/auth/email/confirm HTTP/1.1
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "email": "user@example.com",
  "code": "123456"
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "이메일 인증이 완료되었습니다.",
  "verified": true
}
```

**동작:**
- Redis에서 코드 검증
- auth.status: UNCONFIRMED → ACTIVE
- auth.user_role: GUEST → USER

---

### 동의서 관리

#### GET `/api/auth/consents`

사용자의 동의 이력 조회

**Request:**

```http
GET /api/auth/consents HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "consents": [
    {
      "consentId": "TERMS_OF_SERVICE",
      "consentName": "서비스 이용약관 동의",
      "version": "v1.0",
      "consentedAt": "2025-01-15T10:30:00",
      "required": true
    },
    {
      "consentId": "MARKETING_CONSENT",
      "consentName": "마케팅 정보 수신 동의",
      "version": "v1.0",
      "consentedAt": "2025-01-15T10:30:00",
      "required": false
    }
  ]
}
```

---

#### POST `/api/auth/consents/withdraw`

특정 동의 철회

**Request:**

```http
POST /api/auth/consents/withdraw HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "consentId": "MARKETING_CONSENT"
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "동의가 철회되었습니다."
}
```

---

### 사용자 관리

#### PATCH `/api/auth/password`

비밀번호 변경

**Request:**

```http
PATCH /api/auth/password HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "oldPassword": "password123",
  "newPassword": "newPassword456",
  "newPasswordConfirm": "newPassword456"
}
```

**Response:**

```http
HTTP/1.1 200 OK
```

---

#### PATCH `/api/auth/email`

이메일 변경

**Request:**

```http
PATCH /api/auth/email HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "newEmail": "newemail@example.com"
}
```

**Response:**

```http
HTTP/1.1 200 OK
```

**이벤트 발행:** `user-email-changed` (Kafka)

---

#### POST `/api/auth/withdraw`

회원 탈퇴

**Request:**

```http
POST /api/auth/withdraw HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "password": "password123",
  "withdrawReason": "서비스 이용 불편"
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "회원 탈퇴가 완료되었습니다.",
  "deletedAt": "2025-01-20T14:30:00"
}
```

**이벤트 발행:** `user-withdrawn` (Kafka)

---

### 관리자 기능

#### POST `/api/auth/suspend`

사용자 정지 (관리자 전용)

**Request:**
```http
POST /api/auth/suspend HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (ADMIN)
Content-Type: application/json

{
  "userId": "1234567890123456789",
  "suspendUntil": "2025-12-31",
  "reason": "부적절한 콘텐츠 게시"
}
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: application/json

{
  "message": "회원이 정지되었습니다.",
  "suspendId": "9876543210987654321"
}
```

**권한:** ADMIN만 가능

---

#### POST `/api/auth/suspend/release`

정지 해제

**Request:**
```http
POST /api/auth/suspend/release HTTP/1.1
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9... (ADMIN)
Content-Type: application/json

{
  "userId": "1234567890123456789"
}
```

**Response:**

```http
HTTP/1.1 200 OK
```

---

### 헬스 체크

#### GET `/health`

서버 상태 확인

**Request:**

```http
GET /health HTTP/1.1
```

**Response:**

```http
HTTP/1.1 200 OK
Content-Type: text/plain

Server is up
```

---

### API 문서

#### GET `/swagger-ui.html`

Swagger UI를 통한 API 문서 확인

**접근 URL:**

- `http://localhost:8080/swagger-ui.html`
- `http://localhost:8080/v3/api-docs` (JSON 형식)

모든 API 엔드포인트를 시각적으로 확인하고 테스트할 수 있습니다.

---

## 기술 스택

### Core Framework
- **Spring Boot**: 3.5.5
- **Java**: 21 (Eclipse Temurin)
- **Gradle**: 8.x

### Persistence

- **Spring Data JPA**: ORM 레이어
- **Hibernate**: JPA 구현체
- **MariaDB 11.x**: 프로덕션 데이터베이스
- **H2 Database**: 테스트용 인메모리 DB

### Cache & Session

- **Spring Data Redis**: 세션 캐싱, 인증 코드 저장
- **Redis 7.x**: 인메모리 데이터 스토어

### Messaging

- **Spring Kafka**: 비동기 이벤트 발행
- **Apache Kafka**: 메시지 브로커

### Security

- **BCrypt**: 비밀번호 암호화 (jBCrypt 0.4)
- **JWT**: 토큰 기반 인증 (Custom Implementation)
- **AES-256**: 개인정보(이메일) 암호화

### Validation
- **Bean Validation**: Hibernate Validator
- **Custom Validators**: Password, PhoneNumber, Consents

### Monitoring & Observability

- **Spring Boot Actuator**: 헬스 체크, 메트릭 수집
- **AOP**: API 통계 수집

### Documentation

- **Springdoc OpenAPI**: 2.3.0
- **Swagger UI**: API 문서 시각화

### Distributed Systems

- **ShedLock**: 5.14.0 (분산 스케줄러 락)
- **Snowflake Algorithm**: 분산 ID 생성

### Testing

- **JUnit 5**: 단위 테스트 프레임워크
- **Mockito**: 모킹 프레임워크
- **AssertJ**: 유창한 단언문 (Fluent Assertions)
- **Spring Boot Test**: 통합 테스트 지원
- **@DataJpaTest**: JPA 테스트

### Development Tools

- **Lombok**: 보일러플레이트 코드 제거
- **Slf4j**: 로깅 프레임워크
- **Spring DevTools**: 핫 리로드

---

## 환경 설정

### 환경 변수

```bash
# Database Configuration
export DATABASE_HOST=localhost
export DATABASE_PORT=3306
export DATABASE_NAME=auth_db
export DATABASE_USER_NAME=auths
export DATABASE_PASSWORD=your_password

# Redis Configuration
export REDIS_HOST=localhost
export REDIS_PORT=6379

# Kafka Configuration
export KAFKA_URL1=localhost:9092
export KAFKA_URL2=localhost:9093
export KAFKA_URL3=localhost:9094

# JWT Configuration
export JWT_SECRET=your-jwt-secret-key-change-in-production
export JWT_ACCESS_TOKEN_EXPIRE_TIME=3600000    # 1시간
export JWT_REFRESH_TOKEN_EXPIRE_TIME=604800000 # 7일

# AES Encryption (이메일 암호화)
export AES_ENCRYPTION_KEY=default-aes-encryption-key-change-in-production

# Spring Profile
export SPRING_PROFILES_ACTIVE=dev
```

### application.yml

```yaml
spring:
  profiles:
    active: ${SPRING_PROFILES_ACTIVE:dev}
```

### application-dev.yaml

```yaml
server:
  port: 8080

spring:
  datasource:
    url: jdbc:mariadb://${DATABASE_HOST}:${DATABASE_PORT}/${DATABASE_NAME}
    username: ${DATABASE_USER_NAME}
    password: ${DATABASE_PASSWORD}
    driver-class-name: org.mariadb.jdbc.Driver

  jpa:
    hibernate:
      ddl-auto: validate
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true

  data:
    redis:
      host: ${REDIS_HOST}
      port: ${REDIS_PORT}

  kafka:
    bootstrap-servers:
      - ${KAFKA_URL1}
      - ${KAFKA_URL2}
      - ${KAFKA_URL3}
    producer:
      key-serializer: org.apache.kafka.common.serialization.StringSerializer
      value-serializer: org.apache.kafka.common.serialization.StringSerializer

security:
  jwt:
    secret: ${JWT_SECRET}
    access-token-expire-time: ${JWT_ACCESS_TOKEN_EXPIRE_TIME}
    refresh-token-expire-time: ${JWT_REFRESH_TOKEN_EXPIRE_TIME}
  aes:
    encryption-key: ${AES_ENCRYPTION_KEY}

logging:
  level:
    com.teambiund.bander.auth_server: DEBUG
    org.hibernate.SQL: DEBUG
    org.hibernate.type.descriptor.sql.BasicBinder: TRACE
```

### application-prod.yaml

```yaml
server:
  port: 9010

spring:
  jpa:
    hibernate:
      ddl-auto: none
    show-sql: false

  sql:
    init:
      mode: never

logging:
  level:
    com.teambiund.bander.auth_server: INFO
    org.hibernate.SQL: WARN
```

### 로컬 실행

```bash
# 1. 데이터베이스 준비
mysql -u root -p < src/main/resources/schema.sql

# 2. Redis 실행 (Docker)
docker run -d -p 6379:6379 redis:7-alpine

# 3. Kafka 실행 (Docker Compose)
docker-compose -f docker-compose-kafka.yml up -d

# 4. 애플리케이션 실행
./gradlew bootRun

# 또는
./gradlew build
java -jar build/libs/Auth_Server-0.0.3_proto.jar
```

---

## 테스트

### 테스트 실행

```bash
# 전체 테스트 실행
./gradlew test

# 특정 테스트 클래스 실행
./gradlew test --tests SignupServiceImplTest

# 테스트 커버리지 리포트 생성
./gradlew jacocoTestReport
```

### 테스트 구조

```
src/test/java/
├── entity/                    # Entity 테스트
│   ├── AuthTest.java
│   └── HistoryTest.java
├── service/                   # Service 레이어 테스트
│   ├── signup/
│   │   └── SignupServiceImplTest.java
│   ├── login/
│   │   └── LoginServiceImplTest.java
│   └── consent/
│       └── ConsentManagementServiceImplTest.java
├── controller/                # Controller 테스트
│   ├── SignupControllerTest.java
│   └── LoginControllerTest.java
└── integration/               # 통합 테스트
    └── AuthFlowIntegrationTest.java
```

### 테스트 커버리지 목표

| 레이어                  | 목표 커버리지 | 설명         |
|----------------------|---------|------------|
| **Service Layer**    | 90% 이상  | 핵심 비즈니스 로직 |
| **Controller Layer** | 80% 이상  | API 엔드포인트  |
| **Repository Layer** | 70% 이상  | 데이터 접근 계층  |
| **전체 프로젝트**          | 80% 이상  | 라인 커버리지    |

### TestFixture 패턴

```java
public class AuthFixture {
	public static Auth createDefaultAuth() {
		return Auth.builder()
				.id("1234567890123456789")
				.email("test@example.com")
				.password("encrypted_password")
				.provider(Provider.SYSTEM)
				.status(Status.ACTIVE)
				.userRole(Role.USER)
				.createdAt(LocalDateTime.now())
				.build();
	}
	
	public static Auth createGuestAuth() {
		return Auth.builder()
				.id("9876543210987654321")
				.email("guest@example.com")
				.password("encrypted_password")
				.provider(Provider.SYSTEM)
				.status(Status.UNCONFIRMED)
				.userRole(Role.GUEST)
				.createdAt(LocalDateTime.now())
				.build();
	}
}
```

---

## 성능 최적화

### 1. 데이터베이스 최적화

#### N+1 문제 해결

- `@EntityGraph` 또는 `JOIN FETCH` 사용
- 연관 관계 로딩 전략 최적화 (Lazy Loading)

```java

@Query("SELECT a FROM Auth a JOIN FETCH a.consents WHERE a.id = :id")
Auth findByIdWithConsents(@Param("id") String id);
```

#### 인덱스 전략

- 조회 빈도가 높은 컬럼에 인덱스 생성
- 복합 인덱스 활용 (예: user_id + consent_id)
- 실행 계획 분석 (`EXPLAIN ANALYZE`)

```sql
-- 실행 계획 분석 예시
EXPLAIN ANALYZE
    SELECT * FROM auth WHERE email = 'user@example.com';
```

### 2. 캐싱 전략

#### Redis 캐싱

- 자주 조회되는 사용자 정보 캐싱
- TTL 설정으로 데이터 신선도 유지
- Cache Aside 패턴 적용

```java

@Cacheable(value = "user", key = "#userId", unless = "#result == null")
public Auth getUserById(String userId) {
	return authRepository.findById(userId).orElse(null);
}

@CacheEvict(value = "user", key = "#userId")
public void updateUser(String userId, Auth auth) {
	authRepository.save(auth);
}
```

### 3. 동시성 제어

#### 낙관적 락 (Optimistic Locking)

- `@Version` 필드를 통한 충돌 감지
- 재시도 로직 구현

```java

@Transactional
public void updateUserWithRetry(String userId, Auth updatedAuth) {
	int maxRetries = 3;
	for (int i = 0; i < maxRetries; i++) {
		try {
			authRepository.save(updatedAuth);
			return;
		} catch (OptimisticLockException e) {
			if (i == maxRetries - 1) throw e;
			// 재시도 전 최신 데이터 로드
			updatedAuth = authRepository.findById(userId).orElseThrow();
		}
	}
}
```

#### 분산 락 (ShedLock)

- 스케줄러 중복 실행 방지
- Redis 기반 락 구현

```java

@Scheduled(cron = "0 0 3 * * ?")
@SchedulerLock(name = "cleanupExpiredUsers",
		lockAtMostFor = "10m",
		lockAtLeastFor = "5m")
public void cleanupExpiredUsers() {
	// 만료된 사용자 정리 로직
}
```

### 4. 비동기 처리

#### Kafka 이벤트 발행

- 회원가입, 로그인 이벤트 비동기 발행
- 메인 트랜잭션과 분리하여 성능 향상

```java

@Async
public void publishSignupEvent(SignupEvent event) {
	kafkaTemplate.send("auth.signup", event);
}
```

### 5. 성능 목표

| 지표               | 목표값        | 측정 방법             |
|------------------|------------|-------------------|
| **로그인 응답 시간**    | < 200ms    | JMeter, Gatling   |
| **회원가입 처리 시간**   | < 500ms    | API 부하 테스트        |
| **동시 접속 처리량**    | 1000 req/s | wrk, Apache Bench |
| **DB 커넥션 풀 활용률** | < 70%      | HikariCP 메트릭      |
| **Redis 히트율**    | > 80%      | Redis INFO stats  |

---

## 배포

### Docker 이미지 빌드

```bash
# 1. 빌드
./gradlew clean build

# 2. Docker 이미지 생성
docker build -t ddingsh9/auth-server:0.0.3 .

# 3. Docker Hub 푸시
docker push ddingsh9/auth-server:0.0.3
```

### Dockerfile

```dockerfile
FROM eclipse-temurin:21-jre-jammy

RUN apt-get update && \
    apt-get install -y ca-certificates && \
    rm -rf /var/lib/apt/lists/*

WORKDIR /app

COPY build/libs/*.jar /app/app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]
```

### Docker Compose 배포

#### docker-compose.yml

```yaml
version: '3.8'

services:
  auth-server-1:
    image: ddingsh9/auth-server:0.0.3
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DATABASE_HOST=mariadb
      - DATABASE_PORT=3306
      - DATABASE_NAME=auth_db
      - DATABASE_USER_NAME=${DB_USER}
      - DATABASE_PASSWORD=${DB_PASS}
      - REDIS_HOST=redis
      - REDIS_PORT=6379
      - KAFKA_URL1=kafka1:9092
      - KAFKA_URL2=kafka2:9092
      - KAFKA_URL3=kafka3:9092
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
    ports:
      - "8081:8080"
    # ... (동일 설정)

  auth-server-3:
    image: ddingsh9/auth-server:0.0.3
    ports:
      - "8082:8080"
    # ... (동일 설정)

  nginx:
    image: nginx:alpine
    ports:
      - "9010:80"
    volumes:
      - ./nginx.conf:/etc/nginx/nginx.conf:ro
    depends_on:
      - auth-server-1
      - auth-server-2
      - auth-server-3
    networks:
      - auth-network

  mariadb:
    image: mariadb:11.3
    environment:
      MYSQL_ROOT_PASSWORD: ${DB_ROOT_PASS}
      MYSQL_DATABASE: auth_db
      MYSQL_USER: ${DB_USER}
      MYSQL_PASSWORD: ${DB_PASS}
    volumes:
      - mariadb_data:/var/lib/mysql
      - ./src/main/resources/schema.sql:/docker-entrypoint-initdb.d/schema.sql:ro
    networks:
      - infra-network

  redis:
    image: redis:7-alpine
    ports:
      - "6379:6379"
    networks:
      - infra-network

networks:
  auth-network:
  infra-network:

volumes:
  mariadb_data:
```

### Nginx 설정

#### nginx.conf

```nginx
upstream auth_servers {
    server auth-server-1:8080;
    server auth-server-2:8080;
    server auth-server-3:8080;
}

server {
    listen 80;
    server_name localhost;

    location / {
        proxy_pass http://auth_servers;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }

    location /health {
        proxy_pass http://auth_servers/health;
        access_log off;
    }

    location /swagger-ui.html {
        proxy_pass http://auth_servers/swagger-ui.html;
    }
}
```

### 배포 단계

```bash
# 1. 환경 변수 설정
export DB_USER=auths
export DB_PASS=your_password
export DB_ROOT_PASS=root_password
export JWT_SECRET=your-256-bit-jwt-secret-key

# 2. 배포
docker-compose up -d

# 3. 로그 확인
docker-compose logs -f auth-server-1

# 4. 헬스 체크
curl http://localhost:9010/health

# 5. Swagger UI 접속
open http://localhost:9010/swagger-ui.html
```

### Kubernetes 배포 (선택)

```yaml
apiVersion: apps/v1
kind: Deployment
metadata:
  name: auth-server
  labels:
    app: auth-server
spec:
  replicas: 3
  selector:
    matchLabels:
      app: auth-server
  template:
    metadata:
      labels:
        app: auth-server
    spec:
      containers:
        - name: auth-server
          image: ddingsh9/auth-server:0.0.3
          ports:
            - containerPort: 8080
          env:
            - name: SPRING_PROFILES_ACTIVE
              value: "prod"
            - name: DATABASE_HOST
              valueFrom:
                configMapKeyRef:
                  name: auth-config
                  key: db.host
            - name: DATABASE_PASSWORD
              valueFrom:
                secretKeyRef:
                  name: auth-secrets
                  key: db.password
          resources:
            requests:
              memory: "512Mi"
              cpu: "500m"
            limits:
              memory: "1Gi"
              cpu: "1000m"
          livenessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 30
            periodSeconds: 10
          readinessProbe:
            httpGet:
              path: /health
              port: 8080
            initialDelaySeconds: 10
            periodSeconds: 5
---
apiVersion: v1
kind: Service
metadata:
  name: auth-server-service
spec:
  type: LoadBalancer
  ports:
    - port: 80
      targetPort: 8080
  selector:
    app: auth-server
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
│   │   ├── consent/
│   │   │   └── impl/
│   │   │       └── ConsentManagementServiceImpl.java
│   │   ├── email_confirm/
│   │   ├── password_change/
│   │   ├── withdrawal/
│   │   ├── suspension/
│   │   └── jwt/
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
│   │   └── response/
│   │
│   ├── exception/
│   │   ├── GlobalExceptionHandler.java
│   │   ├── ErrorCode.java
│   │   └── BusinessException.java
│   │
│   ├── config/
│   │   ├── SwaggerConfig.java
│   │   ├── RedisConfig.java
│   │   ├── GenerateKeyConfig.java
│   │   ├── CipherConfig.java
│   │   └── ShedLockConfig.java
│   │
│   ├── util/
│   │   ├── validator/
│   │   │   ├── PasswordMatchesValidator.java
│   │   │   ├── PhoneNumberValidator.java
│   │   │   └── RequiredConsentsValidator.java
│   │   ├── encryption/
│   │   │   ├── AesEncryptor.java
│   │   │   └── PasswordEncoder.java
│   │   ├── generator/
│   │   │   ├── key/
│   │   │   │   └── impl/
│   │   │   │       └── Snowflake.java
│   │   │   └── generate_code/
│   │   │       └── EmailCodeGenerator.java
│   │   └── data/
│   │       └── ConsentTableInit.java
│   │
│   └── aop/
│       └── statistics/
│           ├── ApiRequestCountAspect.java
│           └── ApiRequestStat.java

src/main/resources/
├── application.yaml
├── application-dev.yaml
├── application-prod.yaml
├── application-test.yaml
├── schema.sql
└── data.sql (선택)
```

---

## 향후 계획

### Phase 1: 메시징 안정성 (우선순위 HIGH)

- [ ] **Outbox 패턴 구현**
	- 이벤트 발행 실패 시 재시도 메커니즘
	- DB 트랜잭션과 이벤트 발행 원자성 보장
	- 예상 소요: 3-4일

- [ ] **Redis 통계 데이터 백업 스케줄러**
	- 메모리 누수 방지 및 데이터 영속화
	- 일별 통계 집계 및 저장
	- 예상 소요: 2-3일

### Phase 2: 성능 최적화 (우선순위 MEDIUM)

- [ ] **N+1 쿼리 문제 해결**
	- EntityGraph 적용
	- Fetch Join 최적화

- [ ] **데이터베이스 인덱스 최적화**
	- 실행 계획 분석
	- 복합 인덱스 설계

- [ ] **쿼리 성능 튜닝**
	- Bulk Insert/Update 최적화
	- 부하 테스트 수행

### Phase 3: 운영 고도화 (우선순위 LOW)

- [ ] **Prometheus + Grafana 모니터링**
	- 메트릭 수집 및 시각화
	- 알람 설정

- [ ] **ELK Stack 로깅 전략**
	- 중앙 집중식 로그 관리
	- 로그 분석 및 검색

- [ ] **CI/CD 파이프라인 구축**
	- GitHub Actions 또는 Jenkins
	- 자동 빌드, 테스트, 배포

- [ ] **부하 테스트 및 튜닝**
	- JMeter, Gatling 시나리오 작성
	- 병목 지점 식별 및 개선

### Phase 4: API Gateway 통합

- [ ] **API Gateway 프로젝트 생성**
	- Spring Cloud Gateway
	- Spring Security 통합

- [ ] **JWT 토큰 검증 필터 구현**
	- Gateway 레벨에서 토큰 검증
	- Auth Server와 연동

- [ ] **라우팅 규칙 정의**
	- 마이크로서비스 라우팅
	- Rate Limiting 구현

---

## 주요 개선사항 (v0.0.3_proto)

### 1. 소셜 로그인 구현
- 카카오 OAuth 2.0 연동
- 애플 Sign In 연동
- Provider 기반 사용자 관리
- 자동 회원가입 플로우

### 2. 보안 강화
- 이메일 AES-256 암호화
- Bean Validation 전면 적용
- 커스텀 Validator 구현 (Password, PhoneNumber, Consents)
- 정규식 검증 체계화

### 3. 코드 품질 개선
- Java 네이밍 컨벤션 전면 준수
- 패키지 구조 표준화 (consents_name → consentsname)
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

## 참고 문서

- **Swagger UI**: `http://localhost:8080/swagger-ui.html`
- **API 문서 JSON**: `http://localhost:8080/v3/api-docs`
- **프로젝트 평가**: [docs/PROJECT_EVALUATION.md](docs/PROJECT_EVALUATION.md)
- **PK 타입 분석**: [docs/PK_TYPE_ANALYSIS.md](docs/PK_TYPE_ANALYSIS.md)

---

## 라이선스

MIT License

---

**버전**: 0.0.3_proto
**최종 업데이트**: 2025-01-20
**작성자**: DDING (ddingsha9@teambind.co.kr)
**팀**: TeamBiund Development Team

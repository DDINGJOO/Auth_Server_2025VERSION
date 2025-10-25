# Auth Server API 명세서

**버전**: v1.0
**최종 업데이트**: 2025-10-25
**Base URL**: `http://localhost:8080`

---

## 1. 회원가입

**경로**: `POST /api/v1/auth/signup`

**요청 타입**: `application/json`

**요청 예시**:

```json
{
  "email": "user@example.com",
  "password": "Password123!",
  "passwordConfirm": "Password123!",
  "consentReqs": [
    {
      "consentId": "1",
      "consented": true
    },
    {
      "consentId": "2",
      "consented": true
    }
  ]
}
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 2. 로그인

**경로**: `POST /api/v1/auth/login`

**요청 타입**: `application/json`

**요청 예시**:

```json
{
  "email": "user@example.com",
  "password": "Password123!"
}
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk4NzY5MDMyfQ.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk5MzcwMjMyfQ.signature",
  "deviceId": "abc-1234"
}
```

---

## 3. 토큰 갱신

**경로**: `POST /api/v1/auth/login/refreshToken`

**요청 타입**: `application/json`

**요청 예시**:

```json
{
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk5MzcwMjMyfQ.signature",
  "deviceId": "abc-1234"
}
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_access_token",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.new_refresh_token",
  "deviceId": "abc-1234"
}
```

---

## 4. 카카오 소셜 로그인

**경로**: `POST /api/v1/auth/social/kakao`

**요청 타입**: `application/json`

**요청 예시**:

```json
{
  "accessToken": "kakao_oauth_access_token_here"
}
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk4NzY5MDMyfQ.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk5MzcwMjMyfQ.signature",
  "deviceId": "abc-1234"
}
```

---

## 5. 애플 소셜 로그인

**경로**: `POST /api/v1/auth/social/apple`

**요청 타입**: `application/json`

**요청 예시**:

```json
{
  "accessToken": "apple_identity_token_here"
}
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk4NzY5MDMyfQ.signature",
  "refreshToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJzdWIiOiIxMjM0NSIsInJvbGUiOiJVU0VSIiwiZGV2aWNlSWQiOiJhYmMtMTIzNCIsImlhdCI6MTY5ODc2NTQzMiwiZXhwIjoxNjk5MzcwMjMyfQ.signature",
  "deviceId": "abc-1234"
}
```

---

## 6. 사용자 인증 정보 조회

**경로**: `GET /api/v1/auth/{userId}`

**요청 타입**: N/A (Path Variable)

**요청 예시**:

```
GET /api/v1/auth/12345
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{
  "userId": "12345",
  "status": "ACTIVE",
  "provider": "SYSTEM",
  "createdAt": "2025-10-01T10:30:00",
  "updatedAt": "2025-10-25T15:45:00"
}
```

---

## 7. 이메일 인증 확인

**경로**: `GET /api/v1/auth/emails/{email}`

**요청 타입**: N/A (Query Parameter)

**요청 예시**:

```
GET /api/v1/auth/emails/user@example.com?code=123456
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 8. 이메일 인증 코드 발급

**경로**: `POST /api/v1/auth/emails/{email}`

**요청 타입**: N/A (Path Variable)

**요청 예시**:

```
POST /api/v1/auth/emails/user@example.com
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{}
```

---

## 9. 비밀번호 변경

**경로**: `POST /api/v1/auth/password`

**요청 타입**: N/A (Query Parameters)

**요청 예시**:

```
POST /api/v1/auth/password?email=user@example.com&newPassword=NewPass123!&passConfirm=NewPass123!
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 10. 동의 정보 변경

**경로**: `PUT /api/v1/auth/consent/{userId}`

**요청 타입**: `application/json`

**요청 예시**:

```json
[
  {
    "consentId": "1",
    "consented": true
  },
  {
    "consentId": "2",
    "consented": true
  },
  {
    "consentId": "3",
    "consented": false
  }
]
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 11. 동의서 목록 조회

**경로**: `GET /api/v1/auth/consents`

**요청 타입**: N/A (Query Parameter)

**요청 예시**:

```
GET /api/v1/auth/consents?all=false
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
{
  "1": {
    "consentId": "1",
    "consentName": "이용약관 동의",
    "isRequired": true,
    "consentContent": "이용약관 내용..."
  },
  "2": {
    "consentId": "2",
    "consentName": "개인정보 처리방침 동의",
    "isRequired": true,
    "consentContent": "개인정보 처리방침 내용..."
  }
}
```

---

## 12. 회원 탈퇴

**경로**: `POST /api/v1/auth/withdraw/{userId}`

**요청 타입**: N/A (Query Parameter)

**요청 예시**:

```
POST /api/v1/auth/withdraw/12345?withdrawReason=서비스가 마음에 들지 않음
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 13. 회원 탈퇴 철회

**경로**: `POST /api/v1/auth/withdraw/withdrawRetraction`

**요청 타입**: N/A (Query Parameter)

**요청 예시**:

```
POST /api/v1/auth/withdraw/withdrawRetraction?email=user@example.com
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 14. 사용자 정지 (관리자)

**경로**: `POST /api/admin/v1/auth/suspend`

**요청 타입**: `application/json`

**요청 예시**:

```json
{
  "suspendedUserId": "12345",
  "suspenderUserId": "admin123",
  "suspendReason": "부적절한 게시물 작성",
  "suspendDay": 7
}
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 15. 사용자 정지 해제 (관리자)

**경로**: `GET /api/admin/v1/auth/suspend/release`

**요청 타입**: N/A (Query Parameter)

**요청 예시**:

```
GET /api/admin/v1/auth/suspend/release?userId=12345
```

**응답 타입**: `application/json`

**응답 예시** (200 OK):

```json
true
```

---

## 16. 헬스체크

**경로**: `GET /health`

**요청 타입**: N/A

**요청 예시**:

```
GET /health
```

**응답 타입**: `text/plain`

**응답 예시** (200 OK):

```
Server is up and running
```

---

## 공통 에러 응답

### CustomException 에러 응답

**응답 타입**: `application/json`

**응답 예시**:

```json
{
  "status": 400,
  "errCode": "INVALID_CREDENTIALS",
  "message": "Invalid credentials"
}
```

### Validation 에러 응답

**응답 타입**: `application/json`

**응답 예시**:

```json
{
  "status": 400,
  "error": "Validation Failed",
  "message": "입력값 검증에 실패했습니다",
  "fieldErrors": {
    "email": "올바른 이메일 형식이 아닙니다",
    "password": "비밀번호는 8자 이상이며 영문, 숫자, 특수문자를 포함해야 합니다"
  }
}
```

---

## JWT 토큰 형식

**Header**:

```http
Authorization: Bearer eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...
```

**Payload 구조**:

```json
{
  "sub": "12345",
  "role": "USER",
  "deviceId": "abc-1234",
  "iat": 1698765432,
  "exp": 1698769032
}
```

**토큰 유효 기간**:

- Access Token: 60분
- Refresh Token: 7일 (10080분)

---

## 주요 에러 코드

| 에러 코드                           | HTTP 상태 | 설명              |
|---------------------------------|---------|-----------------|
| `INVALID_TOKEN`                 | 400     | 유효하지 않은 JWT 토큰  |
| `EXPIRED_TOKEN`                 | 400     | 만료된 JWT 토큰      |
| `INVALID_CREDENTIALS`           | 400     | 이메일 또는 비밀번호 불일치 |
| `EMAIL_NOT_FOUND`               | 404     | 등록되지 않은 이메일     |
| `USER_NOT_FOUND`                | 404     | 사용자를 찾을 수 없음    |
| `EMAIL_ALREADY_EXISTS`          | 400     | 이미 존재하는 이메일     |
| `NOT_CONFIRMED_EMAIL`           | 400     | 이메일 인증 미완료      |
| `USER_IS_SUSPENDED`             | 400     | 정지된 계정          |
| `USER_IS_BLOCKED`               | 400     | 차단된 계정          |
| `USER_IS_DELETED`               | 400     | 탈퇴한 계정          |
| `INVALID_CODE`                  | 400     | 유효하지 않은 인증 코드   |
| `REQUIRED_CONSENT_NOT_PROVIDED` | 400     | 필수 동의 항목 미제공    |
| `NOT_ADMIN`                     | 403     | 관리자 권한 필요       |
| `SOCIAL_LOGIN_FAILED`           | 401     | 소셜 로그인 실패       |

전체 에러 코드: `src/main/java/com/teambiund/bander/auth_server/auth/exception/ErrorCode/AuthErrorCode.java`

---

## 사용자 상태 (Status)

| 상태          | 설명                   |
|-------------|----------------------|
| `GUEST`     | 이메일 인증 미완료 (회원가입 직후) |
| `ACTIVE`    | 정상 활성 사용자            |
| `SUSPENDED` | 정지된 계정 (일시적)         |
| `BLOCKED`   | 차단된 계정 (영구)          |
| `DELETED`   | 탈퇴한 계정 (3년 보관)       |
| `SLEEPING`  | 휴면 계정                |

---

## 인증 제공자 (Provider)

| 제공자      | 설명            |
|----------|---------------|
| `SYSTEM` | 이메일/비밀번호 회원가입 |
| `KAKAO`  | 카카오 소셜 로그인    |
| `APPLE`  | 애플 소셜 로그인     |

---

## Swagger UI

전체 API 문서는 Swagger UI에서 확인 가능합니다:

```
http://localhost:8080/swagger-ui.html
```

**작성일**: 2025-10-25

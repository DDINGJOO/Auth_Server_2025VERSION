# 소셜 로그인 설정 가이드

## 개요

카카오와 애플 소셜 로그인을 지원합니다. 각 플랫폼에서 발급받은 Access Token 또는 Identity Token을 사용하여 로그인합니다.

## 아키텍처

### 로그인 흐름

1. 클라이언트가 카카오/애플 SDK를 통해 Access Token 또는 Identity Token 획득
2. 클라이언트가 Auth Server에 Token과 함께 로그인 요청
3. Auth Server가 해당 플랫폼 API로 사용자 정보 조회
4. 이메일 기반으로 기존 사용자 확인
   - 신규 사용자: 회원가입 처리 후 로그인
   - 기존 사용자: 로그인 처리
5. JWT Access Token 및 Refresh Token 발급

### 구조

```
Client
  ↓ (카카오/애플 Access Token)
SocialLoginController
  ↓
SocialLoginService
  ├─ KakaoOAuthClient (카카오 사용자 정보 조회)
  ├─ AppleOAuthClient (애플 사용자 정보 조회)
  ├─ SignupService (신규 사용자 회원가입)
  └─ LoginService (로그인 처리 및 토큰 발급)
```

## 카카오 로그인 설정

### 1. 카카오 개발자 콘솔 설정

1. Kakao Developers 접속
   - https://developers.kakao.com/

2. 애플리케이션 추가
   - 내 애플리케이션 → 애플리케이션 추가하기
   - 앱 이름, 사업자명 입력

3. 플랫폼 설정
   - 플랫폼 → Web 플랫폼 등록
   - 사이트 도메인 입력 (예: https://yourdomain.com)

4. 카카오 로그인 활성화
   - 제품 설정 → 카카오 로그인
   - 카카오 로그인 활성화 ON
   - Redirect URI 등록 (예: https://yourdomain.com/oauth/kakao/callback)

5. 동의항목 설정
   - 제품 설정 → 카카오 로그인 → 동의항목
   - 이메일 (필수 동의)
   - 프로필 정보 (선택 동의)

6. REST API 키 확인
   - 앱 설정 → 앱 키
   - REST API 키 복사 (클라이언트에서 사용)

### 2. 환경 변수 설정

```bash
# application-dev.yaml 또는 application-prod.yaml
oauth:
  kakao:
    user-info-uri: https://kapi.kakao.com/v2/user/me
```

### 3. API 호출 예시

```bash
POST /api/auth/social/kakao
Content-Type: application/json

{
  "accessToken": "{카카오 Access Token}",
  "deviceId": "optional-device-id"
}
```

## 애플 로그인 설정

### 1. Apple Developer 설정

1. Apple Developer 접속
   - https://developer.apple.com/account/

2. Certificates, Identifiers & Profiles 접근
   - Identifiers → App IDs 선택
   - 새 App ID 등록 또는 기존 App ID 선택

3. Sign in with Apple 활성화
   - App ID 편집
   - Capabilities → Sign in with Apple 체크

4. Services ID 생성
   - Identifiers → Services IDs
   - 새 Services ID 등록
   - Sign in with Apple 활성화
   - Domains and Subdomains 입력
   - Return URLs 입력 (예: https://yourdomain.com/oauth/apple/callback)

5. Key 생성
   - Keys → 새 Key 등록
   - Sign in with Apple 선택
   - Configure 클릭하여 Primary App ID 선택
   - Key 다운로드 (.p8 파일)
   - Key ID 확인

### 2. 환경 변수 설정

```bash
# application-dev.yaml 또는 application-prod.yaml
oauth:
  apple:
    issuer: https://appleid.apple.com
```

### 3. API 호출 예시

```bash
POST /api/auth/social/apple
Content-Type: application/json

{
  "accessToken": "{애플 Identity Token}",
  "deviceId": "optional-device-id"
}
```

## API 명세

### 카카오 로그인

```
POST /api/auth/social/kakao
```

#### Request Body
```json
{
  "accessToken": "string (required) - 카카오 Access Token",
  "deviceId": "string (optional) - 클라이언트 Device ID"
}
```

#### Response
```json
{
  "accessToken": "string - JWT Access Token",
  "refreshToken": "string - JWT Refresh Token",
  "deviceId": "string - Device ID (요청에 포함되지 않은 경우 자동 생성)"
}
```

#### Error Response
- `400 SOCIAL_LOGIN_FAILED`: 소셜 로그인 실패
- `400 EMAIL_ALREADY_EXISTS`: 이미 다른 방식으로 가입된 이메일
- `401 UNAUTHORIZED`: 유효하지 않은 Access Token

### 애플 로그인

```
POST /api/auth/social/apple
```

#### Request Body
```json
{
  "accessToken": "string (required) - 애플 Identity Token",
  "deviceId": "string (optional) - 클라이언트 Device ID"
}
```

#### Response
```json
{
  "accessToken": "string - JWT Access Token",
  "refreshToken": "string - JWT Refresh Token",
  "deviceId": "string - Device ID (요청에 포함되지 않은 경우 자동 생성)"
}
```

#### Error Response
- `400 SOCIAL_LOGIN_FAILED`: 소셜 로그인 실패
- `400 EMAIL_ALREADY_EXISTS`: 이미 다른 방식으로 가입된 이메일
- `401 UNAUTHORIZED`: 유효하지 않은 Identity Token

## 주의사항

### 보안

1. Access Token은 HTTPS를 통해서만 전송해야 합니다
2. Identity Token의 검증은 서버에서 수행됩니다
3. 클라이언트에서 받은 Token을 재검증하지 않고 신뢰하지 마세요

### Provider 정책

1. 동일한 이메일로 다른 Provider로 가입할 수 없습니다
   - 예: kakao@email.com으로 카카오 가입 후, 동일 이메일로 애플 로그인 불가
   - `EMAIL_ALREADY_EXISTS` 에러 반환

2. 신규 사용자는 자동으로 회원가입 처리됩니다
   - Provider 정보와 이메일만 저장됩니다
   - 비밀번호는 저장되지 않습니다
   - 프로필 생성 이벤트가 발행됩니다

### 테스트

1. 개발 환경에서 테스트 시 실제 카카오/애플 Token이 필요합니다
2. 단위 테스트에서는 Mock을 사용합니다
3. 통합 테스트 시 환경 변수 설정을 확인하세요

## 트러블슈팅

### 카카오 로그인 실패

**증상**: `SOCIAL_LOGIN_FAILED` 에러 발생

**원인 및 해결**:
1. Access Token 만료
   - 카카오 SDK에서 새로운 Token 재발급

2. 카카오 API 호출 실패
   - 네트워크 연결 확인
   - 카카오 서버 상태 확인 (https://status.kakao.com/)

3. 이메일 정보 없음
   - 카카오 개발자 콘솔에서 이메일 동의항목 필수로 설정
   - 사용자에게 이메일 제공 동의 요청

### 애플 로그인 실패

**증상**: `SOCIAL_LOGIN_FAILED` 에러 발생

**원인 및 해결**:
1. Identity Token 형식 오류
   - JWT 형식 확인 (header.payload.signature)
   - Base64 URL 인코딩 확인

2. Identity Token 파싱 실패
   - Token 만료 여부 확인
   - 애플 SDK 설정 확인

3. 이메일 또는 sub 정보 없음
   - 애플 Developer 콘솔에서 Sign in with Apple 설정 확인
   - 클라이언트 요청 시 scope에 email 포함 확인

### Provider 불일치 에러

**증상**: `EMAIL_ALREADY_EXISTS` 에러 발생

**해결**:
1. 사용자에게 기존 가입 방식 안내
2. 기존 계정으로 로그인 유도
3. 필요 시 계정 연동 기능 구현 고려

## 참고 문서

- 카카오 로그인 API: https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api
- Sign in with Apple: https://developer.apple.com/documentation/sign_in_with_apple
- JWT 디코딩: https://jwt.io/

# 보안 설정 가이드

## 전화번호 암호화 설정

### 1. application.yml 설정

`src/main/resources/application.yml` 파일에 다음 설정을 추가하세요:

```yaml
security:
  phone:
    # 전화번호 암호화 키 (32자 이상 권장)
    # ⚠️ 절대 Git에 커밋하지 마세요!
    encryption-key: ${PHONE_ENCRYPTION_KEY:default-phone-encryption-key-change-in-production}
```

### 2. 환경변수 설정

#### 로컬 개발 환경 (.env 파일)
```bash
PHONE_ENCRYPTION_KEY=your-local-development-encryption-key-32-chars-minimum
```

#### 프로덕션 환경
운영 환경에서는 반드시 환경변수 또는 KMS를 사용하세요:

```bash
export PHONE_ENCRYPTION_KEY="your-production-encryption-key-use-kms"
```

### 3. Docker 환경
```yaml
# docker-compose.yml
services:
  auth-server:
    environment:
      - PHONE_ENCRYPTION_KEY=${PHONE_ENCRYPTION_KEY}
```

### 4. Kubernetes 환경
```yaml
# secret.yaml
apiVersion: v1
kind: Secret
metadata:
  name: auth-server-secrets
type: Opaque
data:
  phone-encryption-key: <base64-encoded-key>
```

## 보안 권장사항

### ✅ DO (권장)
- ✅ 최소 32자 이상의 강력한 암호화 키 사용
- ✅ 환경별로 다른 암호화 키 사용 (dev, staging, prod)
- ✅ 프로덕션 환경에서는 AWS KMS, Azure Key Vault, GCP KMS 등 사용
- ✅ 암호화 키를 정기적으로 교체 (Key Rotation)
- ✅ 암호화 키 접근 권한을 최소한으로 제한

### ❌ DON'T (금지)
- ❌ 암호화 키를 코드에 하드코딩
- ❌ 암호화 키를 Git에 커밋
- ❌ 개발/프로덕션 환경에서 동일한 키 사용
- ❌ 기본 키(default-phone-encryption-key-change-in-production) 사용
- ❌ 암호화 키를 로그에 출력

## 암호화 키 생성 방법

### OpenSSL 사용
```bash
openssl rand -base64 32
```

### Python 사용
```python
import secrets
print(secrets.token_urlsafe(32))
```

### Node.js 사용
```javascript
require('crypto').randomBytes(32).toString('base64')
```

## 마이그레이션 가이드

### 기존 평문 전화번호 데이터가 있는 경우

1. **백업 필수**: 데이터베이스 백업
2. **마이그레이션 스크립트 작성**:

```sql
-- 기존 데이터 조회
SELECT id, phone_number FROM auth WHERE phone_number IS NOT NULL;

-- Java 애플리케이션에서 암호화 후 업데이트
-- (SQL로 직접 암호화는 불가, 애플리케이션 레벨에서 처리)
```

3. **마이그레이션 서비스 구현** (예시):
```java
@Service
public class PhoneNumberMigrationService {

    @Transactional
    public void migrateAllPhoneNumbers() {
        List<Auth> allUsers = authRepository.findAll();
        for (Auth user : allUsers) {
            if (user.getPhoneNumber() != null
                && !isEncrypted(user.getPhoneNumber())) {
                String encrypted = phoneNumberCipher.encrypt(user.getPhoneNumber());
                user.setPhoneNumber(encrypted);
            }
        }
        authRepository.saveAll(allUsers);
    }

    private boolean isEncrypted(String phoneNumber) {
        // 암호화된 데이터는 Base64 형식
        return phoneNumber.matches("^[A-Za-z0-9+/=]+$")
            && phoneNumber.length() > 15;
    }
}
```

## 모니터링

암호화/복호화 실패를 모니터링하세요:

```java
@Aspect
@Component
public class EncryptionMonitoringAspect {

    @AfterThrowing(
        pointcut = "execution(* com.teambiund.bander.auth_server.util.cipher.*.*(..))",
        throwing = "ex"
    )
    public void logEncryptionFailure(JoinPoint joinPoint, Exception ex) {
        log.error("Encryption/Decryption failed: {}",
            joinPoint.getSignature(), ex);
        // 알림 발송 (Slack, PagerDuty 등)
    }
}
```

## 문제 해결

### Q: "ENCRYPTION_ERROR" 발생
- 암호화 키가 올바르게 설정되었는지 확인
- 환경변수가 애플리케이션에 주입되었는지 확인

### Q: "DECRYPTION_ERROR" 발생
- 저장된 데이터가 올바른 키로 암호화되었는지 확인
- 키가 변경되지 않았는지 확인
- Base64 형식이 손상되지 않았는지 확인

### Q: 암호화 키를 분실한 경우
- ⚠️ 암호화된 데이터는 복구 불가
- 백업에서 키를 복구하거나
- 사용자에게 전화번호 재등록 요청

## 참고 자료

- [OWASP Cryptographic Storage Cheat Sheet](https://cheatsheetseries.owasp.org/cheatsheets/Cryptographic_Storage_Cheat_Sheet.html)
- [개인정보보호법 가이드라인](https://www.privacy.go.kr/)
- [AWS KMS](https://aws.amazon.com/kms/)
- [Azure Key Vault](https://azure.microsoft.com/en-us/services/key-vault/)

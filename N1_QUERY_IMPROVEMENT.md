# N+1 쿼리 문제 개선 방안

## 🔴 즉시 개선 필요 사항

### 1. AuthRepository에 추가할 Fetch Join 메서드

```java
// LoginService를 위한 메서드
@Query("select a from Auth a left join fetch a.loginStatus where a.email = :email")
Optional<Auth> findByEmailWithLoginStatus(@Param("email") String email);

@Query("select a from Auth a left join fetch a.loginStatus where a.id = :id")
Optional<Auth> findByIdWithLoginStatus(@Param("id") String id);

// WithdrawalService를 위한 메서드
@Query("select a from Auth a left join fetch a.withdraw where a.email = :email")
Optional<Auth> findByEmailWithWithdraw(@Param("email") String email);

// SuspendRelease를 위한 메서드
@Query("select distinct s from Suspend s join fetch s.suspendedUser a left join fetch a.suspensions where s.suspendUntil < :date")
List<Suspend> findAllWithAuthAndSuspensions(@Param("date") LocalDate date);
```

### 2. 서비스 레이어 수정

#### LoginServiceImpl.java 수정
```java
// 변경 전
Auth auth = authRepository.findByEmail(encryptedEmail)...

// 변경 후
Auth auth = authRepository.findByEmailWithLoginStatus(encryptedEmail)
    .or(() -> authRepository.findByEmailWithLoginStatus(email))
    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
```

#### WithdrawalManagementServiceImpl.java 수정
```java
// 변경 전
Auth auth = authRepository.findByEmail(encryptedEmail)...

// 변경 후
Auth auth = authRepository.findByEmailWithWithdraw(encryptedEmail)
    .or(() -> authRepository.findByEmailWithWithdraw(email))
    .orElseThrow(() -> new CustomException(AuthErrorCode.USER_NOT_FOUND));
```

#### SuspendRelease.java 수정
```java
// 변경 전
List<Suspend> suspends = suspendRepository.findAllBySuspendUntilIsBefore(LocalDate.now().minusDays(1));
suspends.forEach(suspend -> {
    var auth = suspend.getSuspendedUser();  // N+1 발생
    auth.getSuspensions().remove(suspend);   // N+1 발생
});

// 변경 후
List<Suspend> suspends = suspendRepository.findAllWithAuthAndSuspensions(LocalDate.now().minusDays(1));
suspends.forEach(suspend -> {
    var auth = suspend.getSuspendedUser();  // 이미 로드됨
    auth.getSuspensions().remove(suspend);   // 이미 로드됨
});
```

## 🟡 선택적 최적화

### 1. @EntityGraph 활용
```java
@EntityGraph(attributePaths = {"loginStatus"})
Optional<Auth> findWithLoginStatusByEmail(String email);

@EntityGraph(attributePaths = {"withdraw"})
Optional<Auth> findWithWithdrawByEmail(String email);

@EntityGraph(attributePaths = {"consent", "consent.consentsTable"})
Optional<Auth> findWithConsentById(String id);
```

### 2. Batch Size 설정
```yaml
spring:
  jpa:
    properties:
      hibernate:
        default_batch_fetch_size: 10  # IN 절로 최대 10개씩 묶어서 조회
```

### 3. 데이터베이스 인덱스 추가
```sql
-- 자주 조회되는 컬럼에 인덱스 추가
CREATE INDEX idx_auth_email ON auth(email);
CREATE INDEX idx_auth_status ON auth(status);
CREATE INDEX idx_suspend_suspend_until ON suspend(suspend_until);
CREATE INDEX idx_consent_user_id ON consent(user_id);
CREATE INDEX idx_history_user_id ON history(user_id);
```

## 📈 예상 성능 개선

### Before (N+1 발생)
- LoginService: 1(Auth) + 1(LoginStatus) = 2 queries
- WithdrawService: 1(Auth) + 1(Withdraw) = 2 queries
- SuspendRelease (100개 정지): 1(Suspend list) + 100(Auth) + 100(Suspensions) = 201 queries ⚠️

### After (Fetch Join 적용)
- LoginService: 1 query ✓
- WithdrawService: 1 query ✓
- SuspendRelease (100개 정지): 1 query ✓

### 성능 개선율
- **SuspendRelease**: 201 → 1 queries (99.5% 감소) 🚀
- **LoginService**: 2 → 1 queries (50% 감소)
- **WithdrawService**: 2 → 1 queries (50% 감소)

## 🎯 구현 우선순위

1. **높음**: SuspendRelease 개선 (가장 큰 성능 영향)
2. **중간**: LoginService 개선 (자주 호출되는 API)
3. **낮음**: WithdrawService 개선 (호출 빈도 낮음)

## 테스트 방법

### 1. 쿼리 로그 활성화
```yaml
spring:
  jpa:
    show-sql: true
    properties:
      hibernate:
        format_sql: true
        use_sql_comments: true
```

### 2. P6Spy 라이브러리 활용
```gradle
implementation 'p6spy:p6spy:3.9.1'
```

### 3. 성능 테스트
```java
@Test
void testN1QueryResolution() {
    // given
    createTestDataWith100Suspends();

    // when
    long startTime = System.currentTimeMillis();
    suspendRelease.release();
    long endTime = System.currentTimeMillis();

    // then
    assertThat(endTime - startTime).isLessThan(1000); // 1초 이내
}
```
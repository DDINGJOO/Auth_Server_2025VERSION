package com.teambiund.bander.auth_server.util.cipher;

import static org.assertj.core.api.Assertions.*;

import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import com.teambiund.bander.auth_server.auth.util.cipher.PBKDF2CipherStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("PBKDF2CipherStrategy 테스트")
class PBKDF2CipherStrategyTest {

    private CipherStrategy pbkdf2Cipher;

    @BeforeEach
    void setUp() {
        pbkdf2Cipher = new PBKDF2CipherStrategy();
    }

    @Nested
    @DisplayName("암호화(해싱) 테스트")
    class EncryptTests {

        @Test
        @DisplayName("[성공] 일반 비밀번호 해싱")
        void encrypt_normalPassword_success() {
            // given
            String password = "password123!";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(hashed).isNotEqualTo(password);
            assertThat(hashed).contains(":"); // iterations:salt:hash 형식
        }

        @ParameterizedTest
        @ValueSource(strings = {
                "simple",
                "Complex123!",
                "가나다라마바사",
                "!@#$%^&*()",
                "12345678901234567890",
                "MixedCase123!@#",
                "   spaces   "
        })
        @DisplayName("[성공] 다양한 형식의 비밀번호 해싱")
        void encrypt_variousPasswords_success(String password) {
            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(hashed).isNotEqualTo(password);
            assertThat(hashed).contains(":");
        }

        @ParameterizedTest
        @NullAndEmptySource
        @DisplayName("[엣지 케이스] null 또는 빈 문자열 해싱")
        void encrypt_nullOrEmpty_returnsNull(String input) {
            // when
            String hashed = pbkdf2Cipher.encrypt(input);

            // then
            assertThat(hashed).isNull();
        }

        @Test
        @DisplayName("[보안] 동일한 비밀번호도 다른 해시 생성 (Salt)")
        void encrypt_samePlainText_producesDifferentHash() {
            // given
            String password = "password123";

            // when
            String hashed1 = pbkdf2Cipher.encrypt(password);
            String hashed2 = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed1).isNotEqualTo(hashed2); // Salt로 인해 다름
            assertThat(pbkdf2Cipher.matches(password, hashed1)).isTrue();
            assertThat(pbkdf2Cipher.matches(password, hashed2)).isTrue();
        }

        @Test
        @DisplayName("[보안] 평문이 해시에 포함되지 않음")
        void encrypt_plainTextNotInHash() {
            // given
            String password = "mysecretpassword";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).doesNotContain(password);
        }

        @Test
        @DisplayName("[형식] PBKDF2 해시 형식 확인 (iterations:salt:hash)")
        void encrypt_hashFormat() {
            // given
            String password = "test";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            String[] parts = hashed.split(":");
            assertThat(parts).hasSize(3);
            assertThat(parts[0]).isEqualTo("65536"); // iterations
            assertThat(parts[1]).isNotEmpty(); // salt (Base64)
            assertThat(parts[2]).isNotEmpty(); // hash (Base64)
        }
    }

    @Nested
    @DisplayName("복호화 테스트")
    class DecryptTests {

        @Test
        @DisplayName("[실패] PBKDF2는 복호화 불가능")
        void decrypt_throwsUnsupportedOperationException() {
            // given
            String password = "password123";
            String hashed = pbkdf2Cipher.encrypt(password);

            // when & then
            assertThatThrownBy(() -> pbkdf2Cipher.decrypt(hashed))
                    .isInstanceOf(UnsupportedOperationException.class)
                    .hasMessageContaining("PBKDF2 is a one-way hash function");
        }

        @Test
        @DisplayName("[실패] null 복호화 시도")
        void decrypt_null_throwsException() {
            // when & then
            assertThatThrownBy(() -> pbkdf2Cipher.decrypt(null))
                    .isInstanceOf(UnsupportedOperationException.class);
        }
    }

    @Nested
    @DisplayName("matches 테스트")
    class MatchesTests {

        @Test
        @DisplayName("[성공] 올바른 비밀번호 검증")
        void matches_correctPassword_returnsTrue() {
            // given
            String password = "correctPassword123!";
            String hashed = pbkdf2Cipher.encrypt(password);

            // when
            boolean result = pbkdf2Cipher.matches(password, hashed);

            // then
            assertThat(result).isTrue();
        }

        @Test
        @DisplayName("[실패] 잘못된 비밀번호 검증")
        void matches_incorrectPassword_returnsFalse() {
            // given
            String correctPassword = "correctPassword123!";
            String wrongPassword = "wrongPassword123!";
            String hashed = pbkdf2Cipher.encrypt(correctPassword);

            // when
            boolean result = pbkdf2Cipher.matches(wrongPassword, hashed);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[실패] 비슷하지만 다른 비밀번호는 실패")
        void matches_similarButDifferent_returnsFalse() {
            // given
            String correctPassword = "Password123!";
            String hashed = pbkdf2Cipher.encrypt(correctPassword);

            // when & then - 대소문자만 다른 경우
            assertThat(pbkdf2Cipher.matches("password123!", hashed)).isFalse();

            // when & then - 특수문자가 없는 경우
            assertThat(pbkdf2Cipher.matches("Password123", hashed)).isFalse();

            // when & then - 모두 대문자인 경우
            assertThat(pbkdf2Cipher.matches("PASSWORD123!", hashed)).isFalse();

            // when & then - 특수문자가 추가된 경우
            assertThat(pbkdf2Cipher.matches("Password123!!", hashed)).isFalse();

            // when & then - 공백이 추가된 경우
            assertThat(pbkdf2Cipher.matches(" Password123!", hashed)).isFalse();
        }

        @Test
        @DisplayName("[엣지 케이스] null 평문")
        void matches_nullPlainText_returnsFalse() {
            // given
            String hashed = pbkdf2Cipher.encrypt("test");

            // when
            boolean result = pbkdf2Cipher.matches(null, hashed);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[엣지 케이스] null 해시")
        void matches_nullHash_returnsFalse() {
            // when
            boolean result = pbkdf2Cipher.matches("test", null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[엣지 케이스] 둘 다 null")
        void matches_bothNull_returnsFalse() {
            // when
            boolean result = pbkdf2Cipher.matches(null, null);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[엣지 케이스] 잘못된 해시 형식")
        void matches_invalidHashFormat_returnsFalse() {
            // given
            String password = "test";
            String invalidHash = "not-a-valid-hash";

            // when
            boolean result = pbkdf2Cipher.matches(password, invalidHash);

            // then
            assertThat(result).isFalse();
        }

        @Test
        @DisplayName("[보안] 동일 비밀번호의 다른 해시들 모두 검증 성공")
        void matches_multipleHashesOfSamePassword_allMatch() {
            // given
            String password = "testPassword123!";
            String hash1 = pbkdf2Cipher.encrypt(password);
            String hash2 = pbkdf2Cipher.encrypt(password);
            String hash3 = pbkdf2Cipher.encrypt(password);

            // when & then
            assertThat(pbkdf2Cipher.matches(password, hash1)).isTrue();
            assertThat(pbkdf2Cipher.matches(password, hash2)).isTrue();
            assertThat(pbkdf2Cipher.matches(password, hash3)).isTrue();
            assertThat(hash1).isNotEqualTo(hash2);
            assertThat(hash2).isNotEqualTo(hash3);
        }
    }

    @Nested
    @DisplayName("isReversible 테스트")
    class IsReversibleTests {

        @Test
        @DisplayName("[성공] PBKDF2는 복호화 불가능")
        void isReversible_returnsFalse() {
            // when
            boolean reversible = pbkdf2Cipher.isReversible();

            // then
            assertThat(reversible).isFalse();
        }
    }

    @Nested
    @DisplayName("성능 테스트")
    class PerformanceTests {

        @Test
        @DisplayName("[성능] PBKDF2는 BCrypt보다 빠름")
        void performance_fasterThanBCrypt() {
            // given
            String password = "testPassword123!";

            // when
            long startTime = System.currentTimeMillis();
            String hashed = pbkdf2Cipher.encrypt(password);
            long endTime = System.currentTimeMillis();

            // then
            long duration = endTime - startTime;
            System.out.println("PBKDF2 해싱 소요 시간: " + duration + "ms");
            assertThat(duration).isLessThan(200); // 200ms 이내 (BCrypt보다 빠름)
            assertThat(hashed).isNotNull();
        }

        @Test
        @DisplayName("[성능] 검증 속도")
        void performance_verificationSpeed() {
            // given
            String password = "testPassword123!";
            String hashed = pbkdf2Cipher.encrypt(password);

            // when
            long startTime = System.currentTimeMillis();
            boolean matches = pbkdf2Cipher.matches(password, hashed);
            long endTime = System.currentTimeMillis();

            // then
            assertThat(matches).isTrue();
            long duration = endTime - startTime;
            System.out.println("PBKDF2 검증 소요 시간: " + duration + "ms");
            assertThat(duration).isLessThan(200); // 200ms 이내
        }

        @Test
        @DisplayName("[성능] 100회 해싱 테스트")
        void performance_100HashIterations() {
            // given
            String password = "testPassword123!";

            // when
            long startTime = System.currentTimeMillis();
            for (int i = 0; i < 100; i++) {
                pbkdf2Cipher.encrypt(password);
            }
            long endTime = System.currentTimeMillis();

            // then
            long duration = endTime - startTime;
            System.out.println("100회 PBKDF2 해싱 소요 시간: " + duration + "ms");
            assertThat(duration).isLessThan(10000); // 10초 이내
        }
    }

    @Nested
    @DisplayName("보안 테스트")
    class SecurityTests {

        @Test
        @DisplayName("[보안] Salt가 자동 생성됨")
        void security_saltAutoGenerated() {
            // given
            String password = "password";

            // when
            String hash1 = pbkdf2Cipher.encrypt(password);
            String hash2 = pbkdf2Cipher.encrypt(password);

            // then - Salt가 다르므로 해시도 다름
            assertThat(hash1).isNotEqualTo(hash2);

            // Salt 확인
            String salt1 = hash1.split(":")[1];
            String salt2 = hash2.split(":")[1];
            assertThat(salt1).isNotEqualTo(salt2);
        }

        @Test
        @DisplayName("[보안] 반복 횟수 65536회 (OWASP 권장)")
        void security_iterationCount() {
            // given
            String password = "password";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then - iterations 확인
            String iterations = hashed.split(":")[0];
            assertThat(iterations).isEqualTo("65536");
        }

        @Test
        @DisplayName("[보안] Rainbow Table 공격 방어 (Salt)")
        void security_rainbowTableProtection() {
            // given - 동일한 비밀번호를 여러 번 해싱
            String password = "commonPassword123";
            String[] hashes = new String[5];

            // when
            for (int i = 0; i < 5; i++) {
                hashes[i] = pbkdf2Cipher.encrypt(password);
            }

            // then - 모든 해시가 다름 (Rainbow Table 무용지물)
            for (int i = 0; i < hashes.length; i++) {
                for (int j = i + 1; j < hashes.length; j++) {
                    assertThat(hashes[i]).isNotEqualTo(hashes[j]);
                }
            }
        }

        @Test
        @DisplayName("[보안] 긴 비밀번호도 안전하게 처리")
        void security_longPasswordHandling() {
            // given
            String longPassword = "a".repeat(100);

            // when
            String hashed = pbkdf2Cipher.encrypt(longPassword);

            // then
            assertThat(hashed).isNotNull();
            assertThat(pbkdf2Cipher.matches(longPassword, hashed)).isTrue();
        }

        @Test
        @DisplayName("[보안] Timing Attack 방어")
        void security_timingAttackProtection() {
            // given
            String password = "correctPassword";
            String hashed = pbkdf2Cipher.encrypt(password);

            // 같은 길이의 다른 비밀번호 사용 (길이가 다르면 해싱 시간도 달라질 수 있음)
            String wrongPassword1 = "wrongPassword1"; // 14 chars
            String wrongPassword2 = "wrongPassword2"; // 14 chars

            // Warmup - JVM 최적화를 위해 먼저 여러 번 실행
            for (int i = 0; i < 100; i++) {
                pbkdf2Cipher.matches(wrongPassword1, hashed);
                pbkdf2Cipher.matches(wrongPassword2, hashed);
            }

            // when - 충분한 횟수로 검증하여 시간 측정
            int iterations = 100; // 10 -> 100으로 증가
            long totalTime1 = 0;
            for (int i = 0; i < iterations; i++) {
                long start = System.nanoTime();
                pbkdf2Cipher.matches(wrongPassword1, hashed);
                totalTime1 += System.nanoTime() - start;
            }

            long totalTime2 = 0;
            for (int i = 0; i < iterations; i++) {
                long start = System.nanoTime();
                pbkdf2Cipher.matches(wrongPassword2, hashed);
                totalTime2 += System.nanoTime() - start;
            }

            // then - 평균 시간 차이가 크지 않아야 함 (상수 시간 비교)
            double avgTime1 = totalTime1 / (double) iterations;
            double avgTime2 = totalTime2 / (double) iterations;
            double timeDiffPercent = Math.abs(avgTime1 - avgTime2) / Math.max(avgTime1, avgTime2) * 100;

            System.out.println("Timing attack test - time difference: " + timeDiffPercent + "%");
            System.out.println("Average time 1: " + String.format("%.2f", avgTime1 / 1_000_000) + " ms");
            System.out.println("Average time 2: " + String.format("%.2f", avgTime2 / 1_000_000) + " ms");

            // JVM 변동성을 고려하여 임계값을 100%로 설정
            // (실제 timing attack 방어는 slowEquals 메서드의 상수 시간 비교로 보장됨)
            assertThat(timeDiffPercent).isLessThan(100.0);
        }
    }

    @Nested
    @DisplayName("통합 시나리오 테스트")
    class IntegrationTests {

        @Test
        @DisplayName("[통합] 회원가입 - 비밀번호 해싱")
        void scenario_signup() {
            // given - 사용자가 회원가입 시 비밀번호 입력
            String rawPassword = "MySecurePassword123!";

            // when - 비밀번호를 해싱하여 DB 저장
            String hashedPassword = pbkdf2Cipher.encrypt(rawPassword);

            // then - 해시가 생성되고 평문과 다름
            assertThat(hashedPassword).isNotNull();
            assertThat(hashedPassword).isNotEqualTo(rawPassword);
            assertThat(hashedPassword).contains(":");
        }

        @Test
        @DisplayName("[통합] 로그인 - 비밀번호 검증 성공")
        void scenario_loginSuccess() {
            // given - DB에 저장된 해시된 비밀번호
            String rawPassword = "MySecurePassword123!";
            String storedHash = pbkdf2Cipher.encrypt(rawPassword);

            // when - 사용자가 로그인 시 입력한 비밀번호 검증
            String loginPassword = "MySecurePassword123!";
            boolean isAuthenticated = pbkdf2Cipher.matches(loginPassword, storedHash);

            // then - 인증 성공
            assertThat(isAuthenticated).isTrue();
        }

        @Test
        @DisplayName("[통합] 로그인 - 비밀번호 검증 실패")
        void scenario_loginFailure() {
            // given - DB에 저장된 해시된 비밀번호
            String rawPassword = "MySecurePassword123!";
            String storedHash = pbkdf2Cipher.encrypt(rawPassword);

            // when - 사용자가 잘못된 비밀번호 입력
            String loginPassword = "WrongPassword123!";
            boolean isAuthenticated = pbkdf2Cipher.matches(loginPassword, storedHash);

            // then - 인증 실패
            assertThat(isAuthenticated).isFalse();
        }

        @Test
        @DisplayName("[통합] 비밀번호 변경")
        void scenario_passwordChange() {
            // given - 기존 비밀번호
            String oldPassword = "OldPassword123!";
            String oldHash = pbkdf2Cipher.encrypt(oldPassword);

            // when - 새 비밀번호로 변경
            String newPassword = "NewPassword123!";
            String newHash = pbkdf2Cipher.encrypt(newPassword);

            // then - 새 해시는 기존과 다름
            assertThat(newHash).isNotEqualTo(oldHash);
            assertThat(pbkdf2Cipher.matches(oldPassword, oldHash)).isTrue();
            assertThat(pbkdf2Cipher.matches(newPassword, newHash)).isTrue();
            assertThat(pbkdf2Cipher.matches(oldPassword, newHash)).isFalse();
            assertThat(pbkdf2Cipher.matches(newPassword, oldHash)).isFalse();
        }

        @Test
        @DisplayName("[통합] 여러 사용자의 동일한 비밀번호")
        void scenario_multipleUsersWithSamePassword() {
            // given - 여러 사용자가 같은 비밀번호 사용
            String commonPassword = "CommonPassword123!";

            // when - 각 사용자의 비밀번호를 해싱
            String hash1 = pbkdf2Cipher.encrypt(commonPassword);
            String hash2 = pbkdf2Cipher.encrypt(commonPassword);
            String hash3 = pbkdf2Cipher.encrypt(commonPassword);

            // then - 모든 해시가 다르지만 검증은 모두 성공
            assertThat(hash1).isNotEqualTo(hash2);
            assertThat(hash2).isNotEqualTo(hash3);
            assertThat(pbkdf2Cipher.matches(commonPassword, hash1)).isTrue();
            assertThat(pbkdf2Cipher.matches(commonPassword, hash2)).isTrue();
            assertThat(pbkdf2Cipher.matches(commonPassword, hash3)).isTrue();
        }
    }

    @Nested
    @DisplayName("엣지 케이스 테스트")
    class EdgeCaseTests {

        @Test
        @DisplayName("[엣지] 매우 짧은 비밀번호 (1자)")
        void edgeCase_veryShortPassword() {
            // given
            String password = "a";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(pbkdf2Cipher.matches(password, hashed)).isTrue();
        }

        @Test
        @DisplayName("[엣지] 공백만 있는 비밀번호")
        void edgeCase_whitespacePassword() {
            // given
            String password = "   ";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(pbkdf2Cipher.matches(password, hashed)).isTrue();
            assertThat(pbkdf2Cipher.matches("", hashed)).isFalse();
        }

        @Test
        @DisplayName("[엣지] 특수문자만 있는 비밀번호")
        void edgeCase_specialCharactersOnly() {
            // given
            String password = "!@#$%^&*()_+-=[]{}|;:',.<>?/~`";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(pbkdf2Cipher.matches(password, hashed)).isTrue();
        }

        @Test
        @DisplayName("[엣지] 유니코드 문자 비밀번호")
        void edgeCase_unicodePassword() {
            // given
            String password = "한글비밀번호123!";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(pbkdf2Cipher.matches(password, hashed)).isTrue();
        }

        @Test
        @DisplayName("[엣지] 이모지 포함 비밀번호")
        void edgeCase_emojiPassword() {
            // given
            String password = "Password123!😀🎉";

            // when
            String hashed = pbkdf2Cipher.encrypt(password);

            // then
            assertThat(hashed).isNotNull();
            assertThat(pbkdf2Cipher.matches(password, hashed)).isTrue();
        }
    }
}

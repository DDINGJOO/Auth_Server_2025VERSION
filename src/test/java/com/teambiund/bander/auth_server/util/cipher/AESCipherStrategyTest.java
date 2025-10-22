package com.teambiund.bander.auth_server.util.cipher;

import static org.assertj.core.api.Assertions.*;

import com.teambiund.bander.auth_server.auth.exception.CustomException;
import com.teambiund.bander.auth_server.auth.util.cipher.AESCipherStrategy;
import com.teambiund.bander.auth_server.auth.util.cipher.CipherStrategy;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

@DisplayName("AESCipherStrategy 테스트")
class AESCipherStrategyTest {

  private static final String ENCRYPTION_KEY = "test-encryption-key-for-unit-testing";
  private CipherStrategy aesCipher;

  @BeforeEach
  void setUp() {
    aesCipher = new AESCipherStrategy(ENCRYPTION_KEY);
  }

  @Nested
  @DisplayName("암호화 테스트")
  class EncryptTests {

    @Test
    @DisplayName("[성공] 일반 문자열 암호화")
    void encrypt_normalString_success() {
      // given
      String plainText = "01012345678";

      // when
      String encrypted = aesCipher.encrypt(plainText);

      // then
      assertThat(encrypted).isNotNull();
      assertThat(encrypted).isNotEqualTo(plainText);
      assertThat(encrypted).isNotEmpty();
    }

    @Test
    @DisplayName("[성공] 이메일 주소 암호화")
    void encrypt_email_success() {
      // given
      String email = "test@example.com";

      // when
      String encrypted = aesCipher.encrypt(email);

      // then
      assertThat(encrypted).isNotNull();
      assertThat(encrypted).isNotEqualTo(email);
    }

    @ParameterizedTest
    @ValueSource(
        strings = {
          "010-1234-5678",
          "abc@example.com",
          "서울특별시 강남구",
          "123456",
          "!@#$%^&*()",
          "가나다라마바사",
          "Hello World 123"
        })
    @DisplayName("[성공] 다양한 형식의 문자열 암호화")
    void encrypt_variousFormats_success(String plainText) {
      // when
      String encrypted = aesCipher.encrypt(plainText);

      // then
      assertThat(encrypted).isNotNull();
      assertThat(encrypted).isNotEqualTo(plainText);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("[엣지 케이스] null 또는 빈 문자열 암호화")
    void encrypt_nullOrEmpty_returnsNull(String input) {
      // when
      String encrypted = aesCipher.encrypt(input);

      // then
      assertThat(encrypted).isNull();
    }

    @Test
    @DisplayName("[일관성] 동일한 평문은 다른 결과를 생성하지 않음 (ECB 모드)")
    void encrypt_samePlainText_produceSameResult() {
      // given
      String plainText = "01012345678";

      // when
      String encrypted1 = aesCipher.encrypt(plainText);
      String encrypted2 = aesCipher.encrypt(plainText);

      // then
      assertThat(encrypted1).isEqualTo(encrypted2);
    }

    @Test
    @DisplayName("[보안] 평문이 암호문에 포함되지 않음")
    void encrypt_plainTextNotInCipherText() {
      // given
      String plainText = "sensitive-data";

      // when
      String encrypted = aesCipher.encrypt(plainText);

      // then
      assertThat(encrypted).doesNotContain(plainText);
    }
  }

  @Nested
  @DisplayName("복호화 테스트")
  class DecryptTests {

    @Test
    @DisplayName("[성공] 암호화된 문자열 복호화")
    void decrypt_encryptedString_success() {
      // given
      String plainText = "01012345678";
      String encrypted = aesCipher.encrypt(plainText);

      // when
      String decrypted = aesCipher.decrypt(encrypted);

      // then
      assertThat(decrypted).isEqualTo(plainText);
    }

    @Test
    @DisplayName("[성공] 이메일 복호화")
    void decrypt_encryptedEmail_success() {
      // given
      String email = "test@example.com";
      String encrypted = aesCipher.encrypt(email);

      // when
      String decrypted = aesCipher.decrypt(encrypted);

      // then
      assertThat(decrypted).isEqualTo(email);
    }

    @ParameterizedTest
    @NullAndEmptySource
    @DisplayName("[엣지 케이스] null 또는 빈 문자열 복호화")
    void decrypt_nullOrEmpty_returnsNull(String input) {
      // when
      String decrypted = aesCipher.decrypt(input);

      // then
      assertThat(decrypted).isNull();
    }

    @Test
    @DisplayName("[실패] 잘못된 Base64 형식 복호화")
    void decrypt_invalidBase64_throwsException() {
      // given
      String invalidEncrypted = "not-a-valid-base64-string!!!";

      // when & then
      assertThatThrownBy(() -> aesCipher.decrypt(invalidEncrypted))
          .isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("[실패] 다른 키로 암호화된 데이터 복호화")
    void decrypt_encryptedWithDifferentKey_throwsException() {
      // given
      CipherStrategy anotherCipher = new AESCipherStrategy("different-key");
      String plainText = "01012345678";
      String encrypted = anotherCipher.encrypt(plainText);

      // when & then
      assertThatThrownBy(() -> aesCipher.decrypt(encrypted)).isInstanceOf(CustomException.class);
    }

    @Test
    @DisplayName("[왕복 테스트] 암호화 후 복호화하면 원본과 동일")
    void encryptThenDecrypt_returnsOriginal() {
      // given
      String[] testData = {
        "01012345678", "test@example.com", "서울특별시 강남구 테헤란로 123", "!@#$%^&*()", "1234567890"
      };

      for (String plainText : testData) {
        // when
        String encrypted = aesCipher.encrypt(plainText);
        String decrypted = aesCipher.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
      }
    }
  }

  @Nested
  @DisplayName("matches 테스트")
  class MatchesTests {

    @Test
    @DisplayName("[성공] 평문과 암호문이 일치")
    void matches_correctPlainText_returnsTrue() {
      // given
      String plainText = "01012345678";
      String encrypted = aesCipher.encrypt(plainText);

      // when
      boolean result = aesCipher.matches(plainText, encrypted);

      // then
      assertThat(result).isTrue();
    }

    @Test
    @DisplayName("[실패] 평문과 암호문이 불일치")
    void matches_incorrectPlainText_returnsFalse() {
      // given
      String plainText = "01012345678";
      String wrongPlainText = "01087654321";
      String encrypted = aesCipher.encrypt(plainText);

      // when
      boolean result = aesCipher.matches(wrongPlainText, encrypted);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("[엣지 케이스] null 평문")
    void matches_nullPlainText_returnsFalse() {
      // given
      String encrypted = aesCipher.encrypt("test");

      // when
      boolean result = aesCipher.matches(null, encrypted);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("[엣지 케이스] null 암호문")
    void matches_nullEncryptedText_returnsFalse() {
      // when
      boolean result = aesCipher.matches("test", null);

      // then
      assertThat(result).isFalse();
    }

    @Test
    @DisplayName("[엣지 케이스] 둘 다 null")
    void matches_bothNull_returnsFalse() {
      // when
      boolean result = aesCipher.matches(null, null);

      // then
      assertThat(result).isFalse();
    }
  }

  @Nested
  @DisplayName("isReversible 테스트")
  class IsReversibleTests {

    @Test
    @DisplayName("[성공] AES는 복호화 가능")
    void isReversible_returnsTrue() {
      // when
      boolean reversible = aesCipher.isReversible();

      // then
      assertThat(reversible).isTrue();
    }
  }

  @Nested
  @DisplayName("키 생성 테스트")
  class KeyGenerationTests {

    @Test
    @DisplayName("[성공] 다양한 길이의 키로 암호화 가능")
    void differentKeyLengths_workCorrectly() {
      // given
      String[] keys = {
        "short",
        "medium-length-key-12345",
        "very-long-key-with-many-characters-12345678901234567890"
      };

      for (String key : keys) {
        // when
        CipherStrategy cipher = new AESCipherStrategy(key);
        String plainText = "test";
        String encrypted = cipher.encrypt(plainText);
        String decrypted = cipher.decrypt(encrypted);

        // then
        assertThat(decrypted).isEqualTo(plainText);
      }
    }

    @Test
    @DisplayName("[일관성] 동일한 키는 동일한 암호화 결과 생성")
    void sameKey_producesSameEncryption() {
      // given
      String key = "test-key";
      CipherStrategy cipher1 = new AESCipherStrategy(key);
      CipherStrategy cipher2 = new AESCipherStrategy(key);
      String plainText = "test";

      // when
      String encrypted1 = cipher1.encrypt(plainText);
      String encrypted2 = cipher2.encrypt(plainText);

      // then
      assertThat(encrypted1).isEqualTo(encrypted2);
    }

    @Test
    @DisplayName("[보안] 다른 키는 다른 암호화 결과 생성")
    void differentKeys_produceDifferentEncryption() {
      // given
      CipherStrategy cipher1 = new AESCipherStrategy("key1");
      CipherStrategy cipher2 = new AESCipherStrategy("key2");
      String plainText = "test";

      // when
      String encrypted1 = cipher1.encrypt(plainText);
      String encrypted2 = cipher2.encrypt(plainText);

      // then
      assertThat(encrypted1).isNotEqualTo(encrypted2);
    }
  }

  @Nested
  @DisplayName("성능 테스트")
  class PerformanceTests {

    @Test
    @DisplayName("[성능] 1000번 암호화/복호화 실행")
    void performance_1000Iterations() {
      // given
      String plainText = "01012345678";

      // when
      long startTime = System.currentTimeMillis();
      for (int i = 0; i < 1000; i++) {
        String encrypted = aesCipher.encrypt(plainText);
        String decrypted = aesCipher.decrypt(encrypted);
        assertThat(decrypted).isEqualTo(plainText);
      }
      long endTime = System.currentTimeMillis();

      // then
      long duration = endTime - startTime;
      System.out.println("1000번 암호화/복호화 소요 시간: " + duration + "ms");
      assertThat(duration).isLessThan(5000); // 5초 이내
    }

    @Test
    @DisplayName("[성능] 긴 문자열 암호화")
    void performance_longString() {
      // given
      String longText = "a".repeat(10000);

      // when
      long startTime = System.currentTimeMillis();
      String encrypted = aesCipher.encrypt(longText);
      String decrypted = aesCipher.decrypt(encrypted);
      long endTime = System.currentTimeMillis();

      // then
      assertThat(decrypted).isEqualTo(longText);
      long duration = endTime - startTime;
      System.out.println("10000자 암호화/복호화 소요 시간: " + duration + "ms");
      assertThat(duration).isLessThan(1000); // 1초 이내
    }
  }

  @Nested
  @DisplayName("통합 시나리오 테스트")
  class IntegrationTests {

    @Test
    @DisplayName("[통합] 전화번호 암호화 시나리오")
    void scenario_phoneNumberEncryption() {
      // given - 사용자가 회원가입 시 전화번호 입력
      String phoneNumber = "010-1234-5678";

      // when - 전화번호 암호화하여 DB 저장
      String encryptedPhone = aesCipher.encrypt(phoneNumber);
      assertThat(encryptedPhone).isNotEqualTo(phoneNumber);

      // then - DB에서 조회 후 복호화
      String decryptedPhone = aesCipher.decrypt(encryptedPhone);
      assertThat(decryptedPhone).isEqualTo(phoneNumber);

      // then - 검증
      assertThat(aesCipher.matches(phoneNumber, encryptedPhone)).isTrue();
    }

    @Test
    @DisplayName("[통합] 이메일 암호화 시나리오")
    void scenario_emailEncryption() {
      // given - 사용자가 이메일로 로그인
      String email = "user@example.com";

      // when - 이메일 암호화
      String encryptedEmail = aesCipher.encrypt(email);

      // then - 검색을 위해 복호화
      String decryptedEmail = aesCipher.decrypt(encryptedEmail);
      assertThat(decryptedEmail).isEqualTo(email);
    }

    @Test
    @DisplayName("[통합] 여러 사용자 데이터 암호화")
    void scenario_multipleUsersEncryption() {
      // given
      String[][] userData = {
        {"010-1111-2222", "user1@test.com"},
        {"010-3333-4444", "user2@test.com"},
        {"010-5555-6666", "user3@test.com"}
      };

      // when & then
      for (String[] data : userData) {
        String phone = data[0];
        String email = data[1];

        String encryptedPhone = aesCipher.encrypt(phone);
        String encryptedEmail = aesCipher.encrypt(email);

        assertThat(aesCipher.decrypt(encryptedPhone)).isEqualTo(phone);
        assertThat(aesCipher.decrypt(encryptedEmail)).isEqualTo(email);
      }
    }
  }
}

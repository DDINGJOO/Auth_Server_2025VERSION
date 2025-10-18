package com.teambiund.bander.auth_server.util.cipher;

/**
 * 암호화 전략 인터페이스 (Strategy Pattern)
 * - 다양한 암호화 방식을 추상화
 * - 대칭키 암호화 (AES): 전화번호, 이메일 등 복호화가 필요한 데이터
 * - 단방향 해시 (BCrypt): 비밀번호 등 복호화가 불필요한 데이터
 *
 * 설계 의도:
 * - 암호화 방식을 쉽게 교체 가능 (전략 패턴)
 * - 단일 책임 원칙 (SRP): 각 구현체는 하나의 암호화 방식만 담당
 * - 개방-폐쇄 원칙 (OCP): 새로운 암호화 방식 추가 시 기존 코드 수정 불필요
 */
public interface CipherStrategy {

    /**
     * 암호화
     * @param plainText 평문
     * @return 암호화된 문자열
     */
    String encrypt(String plainText);

    /**
     * 복호화
     * @param encryptedText 암호화된 문자열
     * @return 평문
     * @throws UnsupportedOperationException 단방향 해시인 경우 (BCrypt 등)
     */
    String decrypt(String encryptedText);

    /**
     * 검증 (단방향 해시용)
     * - 비밀번호처럼 복호화가 불필요하고 검증만 필요한 경우
     * @param plainText 평문
     * @param encryptedText 암호화된 문자열
     * @return 일치 여부
     */
    boolean matches(String plainText, String encryptedText);

    /**
     * 복호화 가능 여부
     * @return true: 복호화 가능 (대칭키), false: 복호화 불가능 (단방향 해시)
     */
    boolean isReversible();
}

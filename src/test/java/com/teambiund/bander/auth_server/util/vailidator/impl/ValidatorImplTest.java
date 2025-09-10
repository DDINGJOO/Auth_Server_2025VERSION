package com.teambiund.bander.auth_server.util.vailidator.impl;

import com.teambiund.bander.auth_server.exceptions.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.fail;



// 정규식 표현 검증 테스트
class ValidatorImplTest {
    private final  ValidatorImpl validator = new ValidatorImpl();

    @Test
    @DisplayName("이메일 유효성 검사 : 정상 이메일")
    void emailValid() throws CustomException {
        try{
             validator.emailValid("soh1001@teambind.co.kr");
         }catch (CustomException e){
             fail();
         }
    }
    @Test
    @DisplayName("이메일 유효성 검사 : case1 : 빈값")
    void emailValid_case1() throws CustomException {
        assertThrows(CustomException.class, () -> validator.emailValid(""));
    }

    @Test
    @DisplayName("이메일 유효성 검사 : case2 : @없는 문자열")
    void emailValid_case2() throws CustomException {
        assertThrows(CustomException.class, () -> validator.emailValid("soh1001teambind.co.kr"));
    }
    @Test
    @DisplayName("이메일 유효성 검사 : case3 : 기타 이메일 형식 위배")
    void emailValid_case3() throws CustomException {
        assertThrows(CustomException.class, () -> validator.emailValid("soh1001@teambind.co.kr."));
        assertThrows(CustomException.class, () -> validator.emailValid("soh1001@.kr"));
    }
    @Test
    @DisplayName("비밀번호 유효성 검사 : 정상 비밀번호")
    void passwordVaild() throws CustomException {
        try{
            validator.passwordValid("hoss1001!");
        }catch (CustomException e){
            fail();
        }
    }
    @Test
    @DisplayName("비밀번호 유효성 검사 : 너무 짧은 비밀번호")
    void passwordVaild_case1() throws CustomException {
        assertThrows(CustomException.class, () -> validator.passwordValid("ho1001"));
    }
    @Test
    @DisplayName("비밀번호 유효성 검사 : 경계 값 테스트 (7글자 테스트")
    void passwordVaild_case2() throws CustomException {
        assertThrows(CustomException.class, () -> validator.passwordValid("aaaaa1!"));
    }
    @Test
    @DisplayName("비밀번호 유효성 검사 : 경계 값 테스트 (9글자 테스트")
    void passwordVaild_case3() throws CustomException {
        try{
            validator.passwordValid("aaaaaa!1a");
        }catch (CustomException e){
            fail();
        }
    }
    @Test
    @DisplayName("비밀번호 유효성 검사 : 숫자만으로 이루어진 비밀번호 테스트")
    void passwordVaild_case4() throws CustomException {
        assertThrows(CustomException.class, () -> validator.passwordValid("123123123"));
    }

    @Test
    @DisplayName("비밀번호 유효성 검사 : 문자만으로 이루어진 비밀번호 테스트")
    void passwordVaild_case5() throws CustomException {
        assertThrows(CustomException.class, () -> validator.passwordValid("aaaㅁㅁㅁㅁㅁㅁaaa"));
    }

    @Test
    @DisplayName("비밀번호 확인 인증 유효성 검사 : 비밀번호 != 비밀번호 확인 테스트")
    void passConfirmValid() throws CustomException {
        assertThrows(CustomException.class, () -> validator.passConfirmValid("hoss1001!", "hoss1001"));
    }
    @Test
    @DisplayName("비밀번호 확인 인증 유효성 검사 : 비밀번호 = 비밀번호 확인")
    void passConfirmValid_case1() throws CustomException {
        try{
            validator.passConfirmValid("hoss1001!", "hoss1001!");
        }catch (CustomException e){
            fail();
        }
    }

}

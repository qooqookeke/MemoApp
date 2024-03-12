package com.qooke.memoapp.model;

public class User {

    // 네트워크로 보낼 데이터 묶음 처리(클래스)
//    {
//        "email":"fff@naver.com",
//            "password":"1234",
//            "nickname":"영수"
//    }

    public String email;
    public String password;
    public String nickname;


    // 디폴트 생성자도 만들어줌
    public User(){

    }

    // 회원가입 생성자 만들기
    public User(String email, String password, String nickname) {
        this.email = email;
        this.password = password;
        this.nickname = nickname;
    }

    // 로그인 생성자 만들기
    public User(String email, String password) {
        this.email = email;
        this.password = password;
    }
}

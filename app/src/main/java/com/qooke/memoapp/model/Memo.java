package com.qooke.memoapp.model;

import java.io.Serializable;

public class Memo implements Serializable{

    public int id;
    public String title;
    public String date;
    public String content;


    // 디폴트 생성자
    public Memo() {

    }

    // 메모생성 생성자
    public Memo(String title, String date, String content) {
        this.title = title;
        this.date = date;
        this.content = content;
    }
}

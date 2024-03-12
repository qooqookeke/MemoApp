package com.qooke.memoapp.model;

import java.util.ArrayList;

public class MemoList {

//    {
//        "result": "success",
//        "items": [
//              {
//                "id": 2,
//                "title": "카페",
//                "date": "2023-12-08T15:30:00",
//                "content": "김나나랑 커피 마시러 감"
//              },
//              {
//                "id": 3,
//                "title": "아침식사",
//                "date": "2023-12-09T08:00:00",
//                "content": "아침에는 샐러드 먹음"
//              },
//              {
//                "id": 4,
//                "title": "쇼핑",
//                "date": "2023-12-09T20:30:00",
//                "content": "백화점에 쇼핑하러 감"
//              }
//          ],
//        "count": 3
//    }

    public String result;
    public ArrayList<Memo> items;
    public int count;
}

package com.qooke.memoapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ProgressBar;

import com.qooke.memoapp.adapter.MemoAdapter;
import com.qooke.memoapp.api.MemoApi;
import com.qooke.memoapp.api.NetworkClient;
import com.qooke.memoapp.config.Config;
import com.qooke.memoapp.model.Memo;
import com.qooke.memoapp.model.MemoList;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MainActivity extends AppCompatActivity {

    Button btnAdd;
    ProgressBar progressBar;


    // 페이징 처리를 위한 변수들
    int offset = 0;
    int limit = 5;
    int count = 0;


    // 리사이클러뷰 관련 변수들
    RecyclerView recyclerView;
    MemoAdapter adapter;
    ArrayList<Memo> memoArrayList = new ArrayList<>();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 로그인한 유저인지 아닌지를 파악해서 회원가입 액티비티를 띄울 것인지, 메인 액티비티를 띄울것인지 처리한다.

        // 로그인 했다는 것은 sharedpreferences에 JWT 토큰이 있는지 없는지로 파악하면 된다.
        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");

        if(token.isEmpty()) {
            // 토큰이 없다는 것은 로그인한 적이 없는 것이므로 회원가입 액티비티를 띄우도록 한다.
            Intent intent =new Intent(MainActivity.this, RegisterActivity.class);
            startActivity(intent);
            finish();
            return;
        }

        btnAdd = findViewById(R.id.btnAdd);
        progressBar = findViewById(R.id.progressBar);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setHasFixedSize(true); // 고정사이즈:true, 변동사이즈:false
        recyclerView.setLayoutManager(new LinearLayoutManager(MainActivity.this));


        // 리사이클러뷰 페이징 처리하는 함수
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                int lastPosition = ((LinearLayoutManager)recyclerView.getLayoutManager()).findLastCompletelyVisibleItemPosition();
                int totalCount = recyclerView.getAdapter().getItemCount();

                if(lastPosition + 1 == totalCount) {
                    // 네트워크 통해서 데이터를 더 불러온다.
                    if (limit == count) {
                        // DB에 데이터가 더 존재할 수 있으니까, 데이터를 불러온다.
                        // 네트워크 통하는 함수 만들기
                        addNetworkData();
                    }

                }

            }
        });


        // 메모 생성 버튼 누르면 실행
        btnAdd.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, AddActivity.class);
                startActivity(intent);
            }
        });

    }

    @Override
    protected void onResume() {
        super.onResume();

        getNetworkData();
    }

    // 네트워크에서 데이터 가져오는 클래스(retrofit 라이브러리 사용)
    private void getNetworkData() {

        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);
        MemoApi api = retrofit.create(MemoApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        Call<MemoList> call = api.getMemoList(token, offset, limit);
        call.enqueue(new Callback<MemoList>() {
            @Override
            public void onResponse(Call<MemoList> call, Response<MemoList> response) {
                progressBar.setVisibility(View.GONE);

                if(response.isSuccessful()) {

                    // 초기화 코드
                    offset = 0;
                    count = 0;
                    memoArrayList.clear();

                    // 데이터 준비(메모리스트 클래스와 메모 어레이리스트에 데이터 추가하기)
                    MemoList memoList = response.body();
                    memoArrayList.addAll(memoList.items);
                    count = memoList.count;

                    // 어댑터 만들어서 리사이클러뷰에 적용
                    adapter = new MemoAdapter(MainActivity.this, memoArrayList);
                    recyclerView.setAdapter(adapter);

                } else {

                }
            }

            @Override
            public void onFailure(Call<MemoList> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
                // 유저한테 알리기
                return;
            }
        });
    }


    // 데이터 페이징하는 함수 만들어 주기
    private void addNetworkData() {

        progressBar.setVisibility(View.VISIBLE);

        Retrofit retrofit = NetworkClient.getRetrofitClient(MainActivity.this);
        MemoApi api = retrofit.create(MemoApi.class);

        SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
        String token = sp.getString("token", "");
        token = "Bearer " + token;

        // 오프셋을 count만큼 증가시킬 수 있도록 셋팅
        offset = offset + count;

        Call<MemoList> call = api.getMemoList(token, offset, limit);
        call.enqueue(new Callback<MemoList>() {
            @Override
            public void onResponse(Call<MemoList> call, Response<MemoList> response) {
                progressBar.setVisibility(View.GONE);

                if(response.isSuccessful()) {

                    MemoList memoList = response.body();
                    memoArrayList.addAll(memoList.items);
                    count = memoList.count;
                    adapter.notifyDataSetChanged();

                } else {

                }
            }

            @Override
            public void onFailure(Call<MemoList> call, Throwable t) {
                progressBar.setVisibility(View.GONE);
            }
        });
    }

}
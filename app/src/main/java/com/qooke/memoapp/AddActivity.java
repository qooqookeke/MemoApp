package com.qooke.memoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TimePicker;
import android.widget.Toast;

import com.qooke.memoapp.api.MemoApi;
import com.qooke.memoapp.api.NetworkClient;
import com.qooke.memoapp.config.Config;
import com.qooke.memoapp.model.Memo;
import com.qooke.memoapp.model.Res;

import java.util.Calendar;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class AddActivity extends AppCompatActivity {

    EditText editTitle;
    Button btnDate;
    Button btnTime;
    EditText editContent;
    Button btnSave;

    String date = "";
    String time = "";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add);

        editTitle = findViewById(R.id.editTitle);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        editContent = findViewById(R.id.editContent);
        btnSave = findViewById(R.id.btnSave);

        // 날짜 선택 버튼 눌렀을때 실행하기
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(AddActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {

                        // 1월이 0으로 되어 있어서 월 정보는 +1 해줘야 한다.
                        int month = i1 + 1;

                        // 월이나 일은 한자리면 왼쪽에 0 붙여야한다.
                        // 월정보 수정
                        String strMonth;
                        if(month < 10) {
                            strMonth = "0" + month;
                        } else {
                            strMonth = "" + month;
                        }

                        // 일정보 수정
                        String strDay;
                        if(i2 < 10) {
                            strDay = "0" + i2;
                        } else {
                            strDay = "" + i2;
                        }

                        date = i + "-" + strMonth + "-"  + strDay;

                        btnDate.setText(date);
                    }
                },
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                );

                //화면에 보여주기
                dialog.show();
            }
        });

        // 시간 선택 버튼 눌렀을때 실행하기
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(AddActivity.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker timePicker, int i, int i1) {
                        String strHour;
                        if(i < 10){
                            strHour = "0" + i;
                        } else {
                            strHour = "" + i;
                        }

                        String strMin;
                        if(i1 < 10){
                            strMin = "0" + i1;
                        } else {
                            strMin = "" + i1;
                        }

                        time = strHour + ":" + strMin;
                        btnTime.setText(time);
                    }
                },
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true
                );

                dialog.show();
            }
        });

        // 메모 저장 버튼 눌렀을때 실행하기
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString().trim();
                String datetime = date + " " + time;
                String content = editContent.getText().toString().trim();

                if(title.isEmpty() || date.isEmpty() || time.isEmpty() || content.isEmpty()) {
                    Toast.makeText(AddActivity.this, "항목을 모두 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                showProgress();
                Retrofit retrofit = NetworkClient.getRetrofitClient(AddActivity.this);
                MemoApi api = retrofit.create(MemoApi.class);

                // 토큰 가져오기
                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                String token = sp.getString("token", "");
                token = "Bearer " + token;

                // body에 보낼 json을 자바의 객체로 생성
                Memo memo = new Memo(title, datetime, content);

                Call<Res> call = api.addMemo(token, memo);
                call.enqueue(new Callback<Res>() {
                    @Override
                    public void onResponse(Call<Res> call, Response<Res> response) {
                        dismissProgress();

                        if(response.isSuccessful()) {
                            finish();
                            return;

                        } else {
                            // 유저한테 알리고 리턴
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<Res> call, Throwable t) {
                        dismissProgress();
                    }
                });

            }
        });

    }

    Dialog dialog;
    private void showProgress() {
        dialog = new Dialog(this);
        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(new ProgressBar(this));
        dialog.setCancelable(false);
        dialog.setCanceledOnTouchOutside(false);
        dialog.show();
    }

    private void dismissProgress() {
        dialog.dismiss();
    }

}
package com.qooke.memoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.TimePickerDialog;
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

public class UpdateActivity extends AppCompatActivity {

    EditText editTitle;
    Button btnDate;
    Button btnTime;
    EditText editContent;
    Button btnSave;

    // 날짜와 시간을 멤버변수로 만들어줌
    String date;
    String time;

    int memoId;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update);

        // Mainactivity에서 데이터 받아오는 코드 작성
        Memo memo = (Memo) getIntent().getSerializableExtra("memo");
        // 메모 아이디 추출하기
        memoId = memo.id;

        editTitle = findViewById(R.id.editTitle);
        btnDate = findViewById(R.id.btnDate);
        btnTime = findViewById(R.id.btnTime);
        editContent = findViewById(R.id.editContent);
        btnSave = findViewById(R.id.btnSave);

        editTitle. setText(memo.title);
        editContent.setText(memo.content);

        // memo.date 에는 "2023-08-03T11:30:00" 처럼 데이터가 들어있다. 날짜와 시간 분리
        String[] dateArray = memo.date.split("T");

        date = dateArray[0];
        time = dateArray[1].substring(0, 4+1);

        btnDate.setText(date);
        btnTime.setText(time);

        // 날짜 버튼 눌렀을 때 처리
        btnDate.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                DatePickerDialog dialog = new DatePickerDialog(UpdateActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker datePicker, int i, int i1, int i2) {
                        int month = i1 + 1;

                        String strMonth;
                        if(month < 10) {
                            strMonth = "0" + month;
                        } else {
                            strMonth = "" + month;
                        }

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
                        // 현재 폰에 설정된 기준 연, 월, 일로 셋팅되어 달력이 뜨게 함
                        Calendar.getInstance().get(Calendar.YEAR),
                        Calendar.getInstance().get(Calendar.MONTH),
                        Calendar.getInstance().get(Calendar.DAY_OF_MONTH)
                );

                dialog.show();
            }
        });

        // 시간 버튼 눌렀을 때 처리
        btnTime.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TimePickerDialog dialog = new TimePickerDialog(UpdateActivity.this, new TimePickerDialog.OnTimeSetListener() {
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
                        // 현재 폰에 설정된 기준 시,분으로 셋팅되어 시계가 뜨게 함
                        Calendar.getInstance().get(Calendar.HOUR_OF_DAY),
                        Calendar.getInstance().get(Calendar.MINUTE),
                        true
                );
                dialog.show();
            }
        });


        // 메모 수정 버튼 눌렀을 때 처리
        btnSave.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String title = editTitle.getText().toString().trim();
                String datetime = date + " " + time;
                String content = editContent.getText().toString().trim();

                if(title.isEmpty() || date.isEmpty() || time.isEmpty() || content.isEmpty()) {
                    Toast.makeText(UpdateActivity.this, "항목을 모두 입력해주세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 새로 저장할 때 저장하는 과정을 표시하는 다이얼로그 실행(화면이 살짝 어두워지며 프로그래스바가 돌아감)
                showProgress();

                // 네트워크 통신으로 API호출 하여 데이터를 수정함
                Retrofit retrofit = NetworkClient.getRetrofitClient(UpdateActivity.this);
                MemoApi api = retrofit.create(MemoApi.class);

                SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                String token = sp.getString("token", "");
                token = "Bearer " + token;

                Memo memo = new Memo(title, datetime, content);

                Call<Res> call = api.updateMemo(memoId, token, memo);
                call.enqueue(new Callback<Res>() {
                    @Override
                    public void onResponse(Call<Res> call, Response<Res> response) {
                        dismissProgress();

                        if(response.isSuccessful()) {
                            finish();
                        } else {

                        }
                    }

                    @Override
                    public void onFailure(Call<Res> call, Throwable t) {
                        dismissProgress();
                    }
                }
                );
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
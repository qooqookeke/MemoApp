package com.qooke.memoapp;

import androidx.appcompat.app.AppCompatActivity;

import android.app.Dialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.qooke.memoapp.api.NetworkClient;
import com.qooke.memoapp.api.UserApi;
import com.qooke.memoapp.config.Config;
import com.qooke.memoapp.model.User;
import com.qooke.memoapp.model.UserRes;

import java.util.regex.Pattern;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class RegisterActivity extends AppCompatActivity {

    EditText editEmail;
    EditText editPassword;
    EditText editNickname;
    Button btnRegister;
    TextView txtLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        editEmail = findViewById(R.id.editEmail);
        editPassword = findViewById(R.id.editPassword);
        editNickname = findViewById(R.id.editNickname);
        btnRegister = findViewById(R.id.btnRegister);
        txtLogin = findViewById(R.id.txtLogin);


        // 로그인 텍스트뷰 눌렀을때
        txtLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(RegisterActivity.this, LoginActivity.class);
                startActivity(intent);
                finish();
            }
        });


        // 회원가입 버튼을 눌렀을때(네트워크 연결 필요)
        btnRegister.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String email = editEmail.getText().toString().trim();
                String password = editPassword.getText().toString().trim();
                String nickname = editNickname.getText().toString().trim();

                // 필수 항목을 비어 있는지 확인하고 버튼을 누르면 에러창 보여주기
                if (email.isEmpty() || password.isEmpty() || nickname.isEmpty()) {
                    Toast.makeText(RegisterActivity.this, "필수항목을 모두 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 이메일 형식이 맞는지 체크(이메일 형식이 맞는지 확인하는 라이브러리 사용, 템플릿)
                Pattern pattern = Patterns.EMAIL_ADDRESS;
                if (pattern.matcher(email).matches() == false) {
                    Toast.makeText(RegisterActivity.this, "이메일을 정확히 입력하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 비밀번호 길이 체크 4~12자 까지만 허용
                if (password.length() < 4 || password.length() > 12) {
                    Toast.makeText(RegisterActivity.this, "비밀번호 길이를 확인하세요", Toast.LENGTH_SHORT).show();
                    return;
                }

                // 네트워크로 회원가입 API를 호출
                // 0. 다이얼로그를 화면에 보여준다.
                showProgress();

                // 1. retrofit 변수 생성
                Retrofit retrofit = NetworkClient.getRetrofitClient(RegisterActivity.this);

                // 2. api 패키지에 있는 interface 생성
                UserApi api = retrofit.create(UserApi.class);

                // 3. 보낼 데이터 만든다. => 묶음처리 : 클래스의 객체 생성
                User user = new User(email, password, nickname);

                // 4. API 호출
                Call<UserRes> call = api.register(user);

                // 5. 서버로부터 받은 응답을 처리하는 코드 작성
                call.enqueue(new Callback<UserRes>() {
                    @Override
                    public void onResponse(Call<UserRes> call, Response<UserRes> response) {
                        // 통신 성공했을때
                        // 서버에서 보낸 응답이 200 ok일때 처리하는 코드

                        dismissProgress();

                        Log.i("AAA", "응답 code: " + response.code());

                        if (response.isSuccessful()) {
                            UserRes userRes = response.body();

                            Log.i("AAA", "result: " + userRes.result);
                            Log.i("AAA", "access_token: " + userRes.access_token);

                            SharedPreferences sp = getSharedPreferences(Config.PREFERENCE_NAME, MODE_PRIVATE);
                            SharedPreferences.Editor editor = sp.edit();
                            editor.putString("token", userRes.access_token);
                            editor.apply();

                            // 데이터가 이상 없으므로 메인 액티비티를 실행한다.
                            Intent intent = new Intent(RegisterActivity.this, MainActivity.class);
                            startActivity(intent);
                            finish();

                        } else if (response.code() == 400) {
                            // 정상동작 안할때 코드 작성(에러코드로 구분, api명세서에서 확인)
                            Toast.makeText(RegisterActivity.this, "이메일과 비밀번호 형식이 올바르지 않습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        } else if (response.code() == 500) {
                            Toast.makeText(RegisterActivity.this, "데이터 베이스에 문제가 있습니다.", Toast.LENGTH_SHORT).show();
                            return;
                        } else {
                            Toast.makeText(RegisterActivity.this, "잠시후 다시 이용하세요.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    @Override
                    public void onFailure(Call<UserRes> call, Throwable t) {
                        dismissProgress();
                        // 유저한테 네트워크 통신 실패했다고 알려준다.(토스트나 스낵바 작성)
                        Toast.makeText(RegisterActivity.this, "네트워크 연결 오류", Toast.LENGTH_SHORT).show();
                        return;
                    }
                });

            }
        });
    }


    // 네트워크로 데이터를 처리할때 사용할 다이얼로그
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
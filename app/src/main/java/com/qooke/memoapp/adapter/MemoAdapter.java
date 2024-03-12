package com.qooke.memoapp.adapter;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.cardview.widget.CardView;
import androidx.recyclerview.widget.RecyclerView;

import com.qooke.memoapp.MainActivity;
import com.qooke.memoapp.R;
import com.qooke.memoapp.UpdateActivity;
import com.qooke.memoapp.api.MemoApi;
import com.qooke.memoapp.api.NetworkClient;
import com.qooke.memoapp.config.Config;
import com.qooke.memoapp.model.Memo;
import com.qooke.memoapp.model.Res;

import java.util.ArrayList;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;

public class MemoAdapter extends RecyclerView.Adapter<MemoAdapter.ViewHolder>{

    Context context;
    ArrayList<Memo> memoArrayList;

    // 생성자 생성(메인 액티비티에서 사용)
    public MemoAdapter(Context context, ArrayList<Memo> memoArrayList) {
        this.context = context;
        this.memoArrayList = memoArrayList;
    }

    @NonNull
    @Override
    public MemoAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.memo_row, parent, false);
        return new MemoAdapter.ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        Memo memo = memoArrayList.get(position);
        holder.txtTitle.setText(memo.title);

        // 날짜는 ISO 포맷으로 온다. 따라서 날짜를 가공해서 화면에 표시할 것!
        // "2023-08-03T11:30:00" -> "2023-08-03 11:30"
        String date = memo.date.replace("T", " ").substring(0, 15+1);

        holder.txtDate.setText(date);
        holder.txtContent.setText(memo.content);
    }

    @Override
    public int getItemCount() {
        return memoArrayList.size();
    }


    // 뷰홀더 만들기
    public class ViewHolder extends RecyclerView.ViewHolder {

        TextView txtTitle;
        TextView txtDate;
        TextView txtContent;
        ImageView imgDelete;
        CardView cardView;

        int index;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);

            txtTitle = itemView.findViewById(R.id.txtTitle);
            txtDate = itemView.findViewById(R.id.txtDate);
            txtContent = itemView.findViewById(R.id.txtContent);
            imgDelete = itemView.findViewById(R.id.imgDelete);
            cardView = itemView.findViewById(R.id.cardView);

            // 카드뷰 눌렀을때 처리
            cardView.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    // 어떤 카드뷰 눌렀는지와 데이터 보내는 코드 등 작성
                    int index = getAdapterPosition();
                    Memo memo = memoArrayList.get(index);

                    Intent intent = new Intent(context, UpdateActivity.class);

                    intent.putExtra("index", index);
                    intent.putExtra("memo", memo); // memo 클래스 직렬화(Serializable) 해줘야 함
                    context.startActivity(intent);

                }
            });

            // 이미지뷰 실행했을때 처리
            imgDelete.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showAlertDialog();
                }
            });
        }

        // 삭제시 알림창 띄우기
        private void showAlertDialog() {
            AlertDialog.Builder builder = new AlertDialog.Builder(context);
            builder.setCancelable(false);
            builder.setTitle("삭제");
            builder.setMessage("정말 삭제하시겠습니까?");
            builder.setPositiveButton("Yes", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    // 몇번째 데이터를 선택했는지 알아야한다.
                    index = getAdapterPosition();

                    // 데이터 베이스에서 삭제
                    Memo memo = memoArrayList.get(index);

                    int memoId = memo.id;

                    SharedPreferences sp = context.getSharedPreferences(Config.PREFERENCE_NAME, Context.MODE_PRIVATE);
                    String token = sp.getString("token", "");
                    token = "Bearer " + token;

                    Retrofit retrofit = NetworkClient.getRetrofitClient(context);
                    MemoApi api = retrofit.create(MemoApi.class);

                    Call<Res> call = api.deleteMemo(memoId, token);
                    call.enqueue(new Callback<Res>() {
                        @Override
                        public void onResponse(Call<Res> call, Response<Res> response) {
                            if(response.isSuccessful()){
                                memoArrayList.remove(index);
                                notifyDataSetChanged();
                            } else {

                            }
                        }

                        @Override
                        public void onFailure(Call<Res> call, Throwable t) {

                        }
                    });


                }
            });
            builder.show();
        }

    }
}

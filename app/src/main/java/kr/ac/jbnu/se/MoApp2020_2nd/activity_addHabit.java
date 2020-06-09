package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.HashMap;
import java.util.Map;

public class activity_addHabit extends BaseActivity {
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, String> habits = new HashMap<String, String>();
    EditText name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_addhabits);

        name = findViewById(R.id.edit_habit);
        Button ok = findViewById(R.id.okBtn);
        Button cancel = findViewById(R.id.cancelBtn);

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showProgressDialog();
                sendData();

                FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                db.collection("Habit List").document(currentUser.getDisplayName()).set(habits).addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        hideProgressDialog();
                        toastMessage("서버에 정상적으로 데이터를 전송하였습니다.");
                        Intent intent = new Intent(getApplicationContext(), activity_main.class);
                        startActivity(intent);
                        finish();
                    }
                })

                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                hideProgressDialog();
                                toastMessage("서버에 데이터를 전송하지 못했습니다." + e);
                            }
                        });
            }
        });
    }

    protected void sendData(){
        if(!name.getText().toString().equals("")){
            habits.put("Habit", name.getText().toString());
        }

        if(name.getText().toString().equals("")){
            toastMessage("목표 이름을 설정하세요.");
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

package kr.ac.jbnu.se.MoApp2020_2nd;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class dialog_register extends BaseActivity {
    private EditText nickname, birth, phone;
    private String Str_name, Str_birth, Str_gender, Str_phone;
    private RadioGroup gender;
    private Button signup;
    Map<String, String> User = new HashMap<String, String>();
    Calendar mCalendar = Calendar.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    DatePickerDialog.OnDateSetListener mDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_register);

        nickname = findViewById(R.id.Popup_NicknameField);
        phone = findViewById(R.id.Popup_PhoneField);
        birth = findViewById(R.id.tutorial_birth);
        gender = findViewById(R.id.genderGroup);
        signup = findViewById(R.id.Popup_confirm);

        birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(dialog_register.this, mDatePicker, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        signup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int id = gender.getCheckedRadioButtonId();

                if(id == -1){
                    toastMessage("성별을 선택하세요.");
                }

                else{
                    RadioButton Radio_gender = (RadioButton) findViewById(id);
                    Str_gender = Radio_gender.getText().toString();

                    Register();
                }
            }
        });

    }

    protected void Register(){
        showProgressDialog();

        Str_name = nickname.getText().toString();
        Str_phone = phone.getText().toString();
        Str_birth = birth.getText().toString();

        if(!Str_name.equals("") && !Str_phone.equals("") && !Str_birth.equals("")){
            showProgressDialog();
            User.put("phone", Str_phone);
            User.put("gender", Str_gender);
            User.put("name", Str_name);
            User.put("birth", Str_birth);

            db.collection("Users").document(Str_name)
                    .set(User, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Sign-up", "Send User Data to Server was Successfully completed");
                            toastMessage("사용자 데이터를 서버에 정상적으로 저장하였습니다.");
                            Intent intent = new Intent(dialog_register.this, activity_tutorialFinish.class);
                            startActivity(intent);
                            finish();
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d("Sign-up", "Send User Data to Server was not completed.");
                            toastMessage("사용자 데이터를 서버로 전송하지 못하였습니다.\n나중에 마이페이지에서 다시시도하십시오.");
                        }
                    });
        }

        else{
            hideProgressDialog();
            toastMessage("모든 필드를 채워주세요.");
        }
    }

    private void updateLabel() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);

        birth.setText(sdf.format(mCalendar.getTime()));
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

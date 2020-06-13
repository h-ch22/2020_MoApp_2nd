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

public class activity_tutorialRegister extends BaseActivity{
    private static final String TAG = "Sign-Up Process";
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private EditText name, email, phone, birth, password;
    private RadioGroup gender;
    private RadioButton male, female;
    private Button Signin, Signup;
    private String Str_email, Str_password, Str_name, Str_birth, Str_gender, Str_phone;
    Map<String, String> User = new HashMap<String, String>();
    Calendar mCalendar = Calendar.getInstance();

    DatePickerDialog.OnDateSetListener mDatePicker = new DatePickerDialog.OnDateSetListener() {
        @Override
        public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
            mCalendar.set(Calendar.YEAR, year);
            mCalendar.set(Calendar.MONTH, month);
            mCalendar.set(Calendar.DAY_OF_MONTH, dayOfMonth);
            updateLabel();
        }
    };

    protected void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.layout_tutorial_register);

        name = findViewById(R.id.tutorial_name);
        email = findViewById(R.id.tutorial_email);
        phone = findViewById(R.id.tutorial_phone);
        birth = findViewById(R.id.tutorial_birth);
        password = findViewById(R.id.tutorial_password);
        gender = findViewById(R.id.genderGroup);
        Signup = findViewById(R.id.btn_SignUp);
        Signin = findViewById(R.id.btn_SignIn);

        birth.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(activity_tutorialRegister.this, mDatePicker, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

        Signup.setOnClickListener(new View.OnClickListener() {
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

        Signin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_tutorialRegister.this, activity_signin.class);
                startActivity(intent);
                finish();
            }
        });
    }

    protected void Register(){
        showProgressDialog();

        Str_email = email.getText().toString();
        Str_password = password.getText().toString();
        Str_name = name.getText().toString();
        Str_phone = phone.getText().toString();
        Str_birth = birth.getText().toString();

        if(!Str_email.equals("") && !Str_password.equals("") && !Str_name.equals("") && !Str_phone.equals("") && !Str_birth.equals("")){
            showProgressDialog();
            User.put("email", Str_email);
            User.put("phone", Str_phone);
            User.put("gender", Str_gender);
            User.put("name", Str_name);
            User.put("birth", Str_birth);

            db.collection("Users").document(Str_name)
                    .set(User, SetOptions.merge())
                    .addOnSuccessListener(new OnSuccessListener<Void>(){
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d(TAG, "Send User Data to Server was Successfully completed");
                            toastMessage("사용자 데이터를 서버에 정상적으로 저장하였습니다.");
                        }
                    })

                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Log.d(TAG, "Send User Data to Server was not completed.");
                            toastMessage("사용자 데이터를 서버로 전송하지 못하였습니다.\n나중에 마이페이지에서 다시시도하십시오.");
                        }
                    });

            mAuth.createUserWithEmailAndPassword(Str_email, Str_password)
                    .addOnCompleteListener(activity_tutorialRegister.this, new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if(task.isSuccessful()){
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                                        .setDisplayName(Str_name).build();

                                user.updateProfile(profileUpdates)
                                        .addOnCompleteListener(new OnCompleteListener<Void>(){
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                if(task.isSuccessful()){
                                                    Log.d(TAG, "User profile updated.");
                                                }
                                            }
                                        });

                                hideProgressDialog();
                                toastMessage("회원가입 완료");
                                Intent intent = new Intent(activity_tutorialRegister.this, activity_tutorialFinish.class);
                                startActivity(intent);
                                finish();
                            }

                            else{
                                hideProgressDialog();
                                toastMessage("회원가입에 실패하였습니다.\n나중에 다시시도하시거나 관리자에게 문의하십시오.");
                                return;
                            }
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

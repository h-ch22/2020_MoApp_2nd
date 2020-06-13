package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_EditProfile extends AppCompatActivity {
    private static final String TAG ="EDITPROFILETag";
    private Button profilebtn, savebtn, backbtn, logout, delete;
    private ImageButton birthbtn;
    private EditText email, phone;
    private TextView birth;
    private RadioGroup radioGroup;
    private CircleImageView profile;

    private Uri filePath;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference mStorageRef;

    private static final int GALLERY_CODE = 10;
    activity_main activity;
    private String gender;
    Calendar mCalendar = Calendar.getInstance();
    String Displayname;

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
    protected void onCreate(Bundle savedInstanceState) {

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 0);
        }

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_editprofile);

        profile = findViewById(R.id.edit_profile);
        profilebtn = findViewById(R.id.edit_changeprofile);
        birthbtn = findViewById(R.id.edit_birthbtn);
        savebtn = findViewById(R.id.edit_changebtn);
        backbtn = findViewById(R.id.edit_backbtn);
        email = findViewById(R.id.edit_email);
        phone = findViewById(R.id.edit_phone);
        birth = findViewById(R.id.edit_birth);
        delete = findViewById(R.id.userdelete);
        logout = findViewById(R.id.logout);


        mStorageRef = FirebaseStorage.getInstance().getReference();

        radioGroup = (RadioGroup) findViewById(R.id.edit_genderradio);
        radioGroup.setOnCheckedChangeListener(radioGroupButtonChangeListener);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Displayname = user.getDisplayName();

        StorageReference ref = FirebaseStorage.getInstance().getReference("images/"+Displayname);
        Glide.with(this).load(ref).into(profile);

        profilebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                SelectImage();

            }
        });

        savebtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String newnumber = phone.getText().toString().trim();
                String newbirth = birth.getText().toString().trim();
                String newemail = email.getText().toString().trim();

                Map<String, Object> UserInfo = new HashMap<>();
                UserInfo.put("name", Displayname);
                UserInfo.put("email", newemail);
                UserInfo.put("phone", newnumber);
                UserInfo.put("gender", gender);
                UserInfo.put("birth", newbirth);

                if(!newnumber.equals("") && !newemail.equals("") && !newbirth.equals("") && !gender.equals("")) {
                    user.updateEmail(newemail)
                            .addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                    if (task.isSuccessful()) {
                                        Log.d(TAG, "User email address updated.");
                                    }
                                }
                            });

                    db.collection("Users").document(Displayname).set(UserInfo, SetOptions.merge())
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {
                                    Log.d(TAG, "DocumentSnapshot successfully updated!");
                                }
                            });

                    Toast.makeText(activity_EditProfile.this, "저장되었습니다", Toast.LENGTH_SHORT).show();
                }
                else
                    Toast.makeText(activity_EditProfile.this, "필드를 채워주세요", Toast.LENGTH_SHORT).show();
            }
        });

        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mAuth.signOut();
                Intent intent = new Intent(activity_EditProfile.this, activity_tutorial.class);
                finish();
                startActivity(intent);
            }
        });

        delete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AlertDialog.Builder alert_confirm = new AlertDialog.Builder(activity_EditProfile.this);
                alert_confirm.setMessage("정말 계정을 삭제 할까요?").setCancelable(false).setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
                                user.delete()
                                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                                            @Override
                                            public void onComplete(@NonNull Task<Void> task) {
                                                Toast.makeText(activity_EditProfile.this, "계정이 삭제 되었습니다.", Toast.LENGTH_LONG).show();
                                                Intent intent = new Intent(activity_EditProfile.this, activity_tutorial.class);
                                                startActivity(intent);
                                                finish();
                                            }
                                        });
                            }
                        }
                );
                alert_confirm.setNegativeButton("취소", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        Toast.makeText(activity_EditProfile.this, "취소", Toast.LENGTH_LONG).show();
                    }
                });
                alert_confirm.show();
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_EditProfile.this, activity_main.class);
                finish();
                startActivity(intent);
            }
        });

        birthbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new DatePickerDialog(activity_EditProfile.this, mDatePicker, mCalendar.get(Calendar.YEAR), mCalendar.get(Calendar.MONTH),
                        mCalendar.get(Calendar.DAY_OF_MONTH)).show();
            }
        });

    }

    private void SelectImage()
    {   // Defining Implicit Intent to mobile gallery
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(Intent.createChooser(intent, "Select Image from here..."), GALLERY_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GALLERY_CODE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();
            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
                profile.setImageBitmap(bitmap);
                uploadImage();
            }
            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }

    private void uploadImage() {
        if (filePath != null) {
            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageRef.child("images/" + Displayname);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(activity_EditProfile.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(activity_EditProfile.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                // Progress Listener for loading
                // percentage on the dialog box
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

    RadioGroup.OnCheckedChangeListener radioGroupButtonChangeListener =
            new RadioGroup.OnCheckedChangeListener(){
                @Override public void onCheckedChanged(RadioGroup radioGroup, @IdRes int i) {
                    if(i == R.id.edit_manbtn){
                        gender = "남성";
                        Toast.makeText(activity_EditProfile.this, "남성", Toast.LENGTH_SHORT).show();
                    } else if(i == R.id.edit_womanbtn){
                        gender = "여성";
                        Toast.makeText(activity_EditProfile.this, "여성", Toast.LENGTH_SHORT).show(); }
                }
            };

    private void updateLabel() {
        String myFormat = "yyyy/MM/dd";
        SimpleDateFormat sdf = new SimpleDateFormat(myFormat, Locale.KOREA);
        birth.setText(sdf.format(mCalendar.getTime()));
    }
}

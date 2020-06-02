package kr.ac.jbnu.se.MoApp2020_2nd;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.ClipData;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class activity_createTimeCapsule extends BaseActivity {
    Button create, add_people, add_data, sel_map, refresh;
    EditText pick_date, pick_time, timecapsule_name;
    private static int RESULT_LOAD_IMAGE = 1;
    private DatePickerDialog datePicker;
    final Calendar selDate = Calendar.getInstance();
    private SharedPreferences Data;
    private int hour, minute;
    private String open_date, capsuleName;
    private int open_hour, open_minute;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> timeCapsule = new HashMap<String, Object>();
    ArrayList<Uri> imageUri = new ArrayList<Uri>();
    private ClipData mClipData;
    activity_map map = new activity_map();
    HashMap<String, String> enableTimeCapsule = new HashMap<String, String>();
    public String location_full;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_maketimecapsule);

        create = findViewById(R.id.btn_create);
        add_people = findViewById(R.id.addPeople);
        add_data = findViewById(R.id.addData);
        pick_date = findViewById(R.id.pick_date);
        pick_time = findViewById(R.id.pick_time);
        timecapsule_name = findViewById(R.id.CapsuleName);
        sel_map = findViewById(R.id.selFromMap);
        refresh = findViewById(R.id.refresh);

        Data = getSharedPreferences("Data", MODE_PRIVATE);
        loadData();

        sel_map.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                toastMessage("마커 설정 후 새로고침 버튼을 클릭해주세요.");
                Intent intent = new Intent(activity_createTimeCapsule.this, activity_map.class);
                startActivity(intent);
            }
        });

        refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(activity_map.longitude != null && activity_map.latitude != null){
                    TextView loc = findViewById(R.id.showMarker);

                    loc.setText("선택한 위치 : " + map.latitude + ", " + map.longitude);
                    location_full = map.latitude + ", " + map.longitude;
                }
            }
        });

        for(String s : activity_addFriend.Checked){
            if(!s.equals("")){
                LinearLayout friendList = findViewById(R.id.addedPeopleLayout);

                final TextView friend = new TextView(getApplicationContext());
                friend.setText(s + "\n");
                friendList.addView(friend);
            }
        }

        add_people.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                saveData();
                Intent intent = new Intent(activity_createTimeCapsule.this, activity_addFriend.class);
                startActivity(intent);
                finish();
            }
        });

        add_data.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), RESULT_LOAD_IMAGE);
            }
        });

        setDateTimeField();
        pick_date.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                datePicker.show();
            }
        });

        pick_time.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar time = Calendar.getInstance();
                hour = time.get(Calendar.HOUR_OF_DAY);
                minute = time.get(Calendar.MINUTE);

                TimePickerDialog timePicker;

                timePicker = new TimePickerDialog(activity_createTimeCapsule.this, new TimePickerDialog.OnTimeSetListener() {
                    @Override
                    public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                        pick_time.setText(hourOfDay + " : " + minute);
                    }
                }, hour, minute, true);

                timePicker.setTitle("시각을 설정해주세요.");
                timePicker.show();
            }
        });

        create.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(!timecapsule_name.equals("") && !pick_date.getText().toString().equals("") && !pick_time.getText().toString().equals("")){
                    showProgressDialog();

                    Date current = Calendar.getInstance().getTime();

                    SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd");
                    String formattedDate = df.format(current);

                    timeCapsule.put("Name", timecapsule_name.getText().toString());
                    timeCapsule.put("Friends", activity_addFriend.Checked);
                    timeCapsule.put("Create Date", formattedDate);
                    timeCapsule.put("Open Locaton", location_full);
                    timeCapsule.put("Open Date", pick_date.getText().toString() + " " + pick_time.getText().toString());
                    enableTimeCapsule.put("Time Capsule", timecapsule_name.getText().toString());

                    for(int i = 0; i < imageUri.size(); i++){
                        uploadData(imageUri.get(i), i);
                    }

                    db.collection("Users").document(mAuth.getCurrentUser().getDisplayName()).set(enableTimeCapsule, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            Log.d("Set TimeCapsule", "Success");
                            toastMessage("타임캡슐 유저 설정이 완료되었습니다.");
                        }
                    });

                    for(String s : activity_addFriend.Checked){
                        if(!s.equals("")){
                            db.collection("Users").document(s).set(enableTimeCapsule, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void aVoid) {

                                }
                            });
                        }
                    }

                    db.collection("Time Capsule").document(timecapsule_name.getText().toString()).set(timeCapsule).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            toastMessage("타임캡슐이 성공적으로 생성되었습니다.");
                        }
                    })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    hideProgressDialog();
                                    toastMessage("타임캡슐이 생성되지 않았습니다. 나중에 다시시도하세요.");
                                }
                            });
                }

                else{
                    toastMessage("모든 필드를 채워주세요.");
                }

            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
            mClipData = intent.getClipData();

            if (intent.getClipData() != null) {
                for (int i = 0; i < mClipData.getItemCount(); i++) {
                    ClipData.Item item = mClipData.getItemAt(i);
                    Uri uri = item.getUri();
                    imageUri.add(uri);

                    ImageView imageView = new ImageView(this);
                    imageView.setImageURI(uri);
                    LinearLayout linearLayout = findViewById(R.id.addedDataLayout);
                    ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(200, 200);
                    imageView.setLayoutParams(params);

                    linearLayout.addView(imageView);
                }
            }
        }
    }

    private void updateLabel(){
        String format = "yyyy-MM-dd";
        SimpleDateFormat sdf = new SimpleDateFormat(format, Locale.KOREA);

        pick_date.setText(sdf.format(selDate.getTime()));

        open_date = pick_date.getText().toString();
    }

    private void saveData(){
        SharedPreferences.Editor editor = Data.edit();

        editor.putString("TimeCapsule_Name", timecapsule_name.getText().toString());
        editor.putInt("Open_hour", hour);
        editor.putInt("Open_minute", minute);
        editor.putString("Open_date", open_date);

        editor.apply();
    }

    private void loadData(){
        capsuleName = Data.getString("TimeCapsule_Name", "");
        open_date = Data.getString("Open_date", "");
        open_hour = Data.getInt("Open_hour", 0);
        open_minute = Data.getInt("Open_minute", 0);

        timecapsule_name.setText(capsuleName);
        pick_date.setText(open_date);
        pick_time.setText(open_hour + " : " + open_minute + " : ");
    }

    private void uploadData(Uri uri, int i){
        FirebaseStorage storage = FirebaseStorage.getInstance();
        StorageReference storageRef = storage.getReference();
        StorageReference imgRef = storageRef.child("Time Capsule_" + timecapsule_name.getText().toString() + "/" + "Data" + i + ".jpg");

        imgRef.putFile(uri).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
            @Override
            public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                toastMessage("데이터 업로드가 완료되었습니다.");
                Log.d("Upload : ", "Successful.");
            }
        })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("Upload Failed : " , e.getMessage());
                        toastMessage("데이터 업로드를 완료하지 못했습니다. 나중에 다시 시도 해주세요.");
                    }
                })

                .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                    @Override
                    public void onProgress(@NonNull UploadTask.TaskSnapshot taskSnapshot) {
                        double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                        toastMessage("업로드 중... (" + (int) progress + "%)");
                    }
                });
    }

    private void setDateTimeField(){
        datePicker = new DatePickerDialog(this, new DatePickerDialog.OnDateSetListener() {
            @Override
            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                Calendar newDate = Calendar.getInstance();
                newDate.set(year, month, dayOfMonth);

                SimpleDateFormat sdf = new SimpleDateFormat("yyyy/MM/dd");
                final Date startDate = newDate.getTime();
                String fDate = sdf.format(startDate);

                pick_date.setText(fDate);
            }
        }, selDate.get(Calendar.YEAR), selDate.get(Calendar.MONTH), selDate.get(Calendar.DAY_OF_MONTH));
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

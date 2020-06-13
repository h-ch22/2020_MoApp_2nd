package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.app.ActivityManager;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_diary extends BaseActivity implements ActivityCompat.OnRequestPermissionsResultCallback {

    GridView gridView;
    ArrayList<File> list;
    Button upload, file;
    EditText editContents;
    private static int RESULT_LOAD_IMAGE = 1;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    Map<String, Object> Title = new HashMap<String, Object>();
    Map<String, String> Contents = new HashMap<String, String>();
    public static String selYear, selMonth, selDay;
    private static String fullDate;
    private static String year, month, day;
    private ArrayList<String> storyList = new ArrayList<String>();
    Context context = this;
    private LinearLayout childLL;
    LinearLayout contentsLL;
    private FirebaseStorage storage = FirebaseStorage.getInstance();
    String name = mAuth.getCurrentUser().getDisplayName();
    InputStream stream = null;
    private static BufferedReader buf;
    private StorageReference mStorageRef;
    private Uri filePath;
    private String uploaddate;
    private CircleImageView profile1[] = new CircleImageView[20];
    private String feeddate[] = new String[20];
    private String feedcontent[] = new String[20];
    private TextView name_1[] = new TextView[20];
    private TextView date_1[] = new TextView[20];
    private TextView name_2[] = new TextView[20];
    private ImageView[] datePhoto_1 = new ImageView[10];
    private Switch publicswitch;
    private String checked = "off";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_diary);

        Date currentDate = Calendar.getInstance().getTime();
        SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
        SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
        SimpleDateFormat dayFormat = new SimpleDateFormat("dd");
        contentsLL = findViewById(R.id.contentsLayout);

        year = yearFormat.format(currentDate);
        month = monthFormat.format(currentDate);
        day = dayFormat.format(currentDate);

        selYear = year;
        selMonth = month;
        selDay = day;

        fullDate = year + "." + month + "." + day;

        loadPreviousStory(fullDate);
        final SimpleDateFormat format = new SimpleDateFormat("MM/dd");

        Button more = findViewById(R.id.moreMemory);
        editContents = findViewById(R.id.diary_contents);
        upload = findViewById(R.id.uploadbtn);
        publicswitch = findViewById(R.id.sharing_switch);
        file = findViewById(R.id.fileBtn);

        checkPermissions();

        final CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                if(!storyList.isEmpty()){
                    storyList.clear();
                }

                contentsLL.removeAllViewsInLayout();
                selYear = String.valueOf(year);
                selMonth = String.valueOf(month + 1);
                if(selMonth.length() == 1){
                    selMonth = "0" + selMonth;
                }

                selDay = String.valueOf(dayOfMonth);
                if(selDay.length() == 1){
                    selDay = "0" + selDay;
                }

                loadPreviousStory(selYear + "." + selMonth + "." + selDay);
            }
        });

        publicswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checked = "On"; //"나만보기"
                }
                else {
                    checked = "OFF"; //"공유하기
                }
            }
        });

        more.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_diary.this, activity_memory.class);
                startActivity(intent);
                finish();
            }
        });

        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), RESULT_LOAD_IMAGE);
            }
        });

        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contents = editContents.getText().toString();

                if(!contents.equals("")){
                    showProgressDialog();

                    FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                    Calendar calendar = Calendar.getInstance();
                    DateFormat format = new SimpleDateFormat("YYYY.MM.dd, HH:mm");
                    String Date = format.format(Calendar.getInstance().getTime());
                    uploaddate = Date;
                    if(checked.equals("On")){
                        Contents.put(uploaddate,contents+"º"+"(hide)");
                    }else{
                        Contents.put(uploaddate, contents);
                    }
                    uploadImage();

                    db.collection("Diary").document(currentUser.getDisplayName()).set(Contents, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            toastMessage("컨텐츠가 성공적으로 저장되었습니다.");
                            hideProgressDialog();
                        }
                    })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    toastMessage("컨텐츠가 저장되지 않았습니다. 나중에 다시시도하세요.");
                                    hideProgressDialog();
                                }
                            });
                }
            }
        });
    }

    private void checkPermissions(){
        if (Build.VERSION.SDK_INT >= 23) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 1);
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case 1: {
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                }

                return;
            }
        }
    }

    private ActivityManager.MemoryInfo getMemoryInfo() {
        ActivityManager activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        activityManager.getMemoryInfo(memoryInfo);
        return memoryInfo;
    }

    private void loadPreviousStory(String date) {
        DocumentReference docRef = db.collection("Diary").document(mAuth.getCurrentUser().getDisplayName());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    if(task.getResult().getData() != null){
                        Map<String, Object> storyMap = task.getResult().getData();

                        String Split_date = null;

                        List<String> timeList = new ArrayList<>();

                        if(storyMap != null){
                            for(Map.Entry<String, Object> entry : storyMap.entrySet()){
                                String[] splitKey = entry.getKey().split(",");

                                if(splitKey[0].equals(date)) {
                                    Split_date = splitKey[1];
                                    timeList.add(splitKey[1]);
                                    storyList.add((String) storyMap.get(entry.getKey()));
                                    Log.d("Story", entry.getValue().toString());
                                }
                            }

                            for(int i = 0; i < storyList.size(); i++){
                                LinearLayout parentLL = new LinearLayout(context);
                                LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                                lp.setMargins(0, 30, 0, 20);
                                parentLL.setLayoutParams(lp);
                                parentLL.setPadding(10,10,10,10);
                                parentLL.setBackgroundResource(R.drawable.btn_rounded);
                                parentLL.setOrientation(LinearLayout.HORIZONTAL);

                                profile1[i] = new CircleImageView(context);
                                profile1[i].setLayoutParams(new LinearLayout.LayoutParams(200, 200));
                                parentLL.addView(profile1[i]);

                                LinearLayout childLL = new LinearLayout(context);
                                childLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                childLL.setOrientation(LinearLayout.VERTICAL);

                                LinearLayout childLLL = new LinearLayout(context);
                                childLLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                childLLL.setOrientation(LinearLayout.HORIZONTAL);

                                name_1[i] = new TextView(context);
                                name_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
                                name_1[i].setTextColor(Color.parseColor("#000000"));
                                name_1[i].setPadding(15, 0, 0, 10);
                                name_1[i].setTextSize(14);
                                name_1[i].setTypeface(null, Typeface.BOLD);
                                name_1[i].setText(mAuth.getCurrentUser().getDisplayName());

                                date_1[i] = new TextView(context);
                                date_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
                                date_1[i].setPadding(15, 0, 0, 10);
                                date_1[i].setTextSize(12);
                                date_1[i].setTextColor(Color.parseColor("#4C4C4C"));
                                date_1[i].setText(feeddate[i]);

                                StorageReference ref2 = FirebaseStorage.getInstance().getReference("Diary/" + mAuth.getCurrentUser().getDisplayName() + "/" + selYear + "." + selMonth + "."
                                        + selDay + "," + timeList.get(i));

                                name_2[i] = new TextView(context);
                                name_2[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
                                name_2[i].setTextColor(Color.parseColor("#000000"));
                                name_2[i].setPadding(15, 10, 0, 10);
                                name_2[i].setTextSize(14);
                                name_2[i].setText(timeList.get(i) + " : " + storyList.get(i));

                                childLLL.addView(name_1[i]);
                                childLLL.addView(date_1[i]);
                                childLL.addView(childLLL);

                                datePhoto_1[i] = new ImageView(context);
                                datePhoto_1[i].setPadding(15,0,0,10);
                                datePhoto_1[i].setLayoutParams(new LinearLayout.LayoutParams(1000, LinearLayout.LayoutParams.WRAP_CONTENT));
                                Glide.with(context).load(ref2).into(datePhoto_1[i]);
                                childLL.addView(datePhoto_1[i]);

                                childLL.addView(name_2[i]);
                                parentLL.addView(childLL);
                                contentsLL.addView(parentLL);

                                StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + mAuth.getCurrentUser().getDisplayName());
                                Glide.with(context).load(ref).into(profile1[i]);
                            }
                        }

                        else{
                            toastMessage("저장한 데이터가 없습니다.");
                        }
                    }

                    else{
                        toastMessage("저장한 데이터가 없습니다.");
                    }
                }
            }
        });
    }

    private void uploadImage() {
        if (filePath != null) {
            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageRef.child("Diary/" + name+"/"+uploaddate);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(activity_diary.this, "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(activity_diary.this, "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), filePath);
            }
            catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}
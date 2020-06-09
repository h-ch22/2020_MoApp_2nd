package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
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
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;

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

        fullDate = year + "." + month + "." + day;

        loadPreviousStory(fullDate);
        final SimpleDateFormat format = new SimpleDateFormat("MM/dd");

        Button more = findViewById(R.id.moreMemory);
        editContents = findViewById(R.id.diary_contents);
        upload = findViewById(R.id.uploadbtn);

        file = findViewById(R.id.fileBtn);

        checkPermissions();

        final CalendarView calendarView = findViewById(R.id.calendarView);

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                if(!storyList.isEmpty()){
                    storyList.clear();
                }

                childLL.removeAllViewsInLayout();
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
                    Contents.put(Date, contents);

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

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
            try {
                stream = this.getApplicationContext().getContentResolver().openInputStream(intent.getData());
                Bitmap original = BitmapFactory.decodeStream(stream);
                ImageView imageView = new ImageView(this);
                imageView.setImageBitmap(original);
                LinearLayout linearLayout = findViewById(R.id.homeLL);
                linearLayout.addView(imageView);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
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
                    Map<String, Object> storyMap = task.getResult().getData();

                    for(Map.Entry<String, Object> entry : storyMap.entrySet()){
                        String[] splitKey = entry.getKey().split(",");

                        if(splitKey[0].equals(date)) {
                            String date = splitKey[1];
                            storyList.add((String) storyMap.get(entry.getKey()));
                            Log.d("Story", entry.getValue().toString());

                            for (int i = 0; i < storyList.size(); i++) {
                                childLL = new LinearLayout(context);
                                childLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
                                childLL.setOrientation(LinearLayout.VERTICAL);

                                TextView[] contents = new TextView[storyList.size()];
                                contents[i] = new TextView(context);
                                contents[i].setBackgroundResource(R.drawable.btn_rounded);
                                contents[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
                                contents[i].setTextColor(Color.parseColor("#000000"));
                                contents[i].setTextSize(20);
                                contents[i].setText(date + " : " + storyList.get(i));

                                childLL.addView(contents[i]);
                                contentsLL.addView(childLL);
                            }
                        }
                    }
                }
            }
        });
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

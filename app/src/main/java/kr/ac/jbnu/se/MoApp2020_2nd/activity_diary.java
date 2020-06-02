package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.app.ActivityManager;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.CalendarView;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
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

    @RequiresApi(api = Build.VERSION_CODES.Q)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_diary);

        final SimpleDateFormat format = new SimpleDateFormat("MM/dd");

        final TextView memory = findViewById(R.id.memory);
        final CalendarView calendarView = findViewById(R.id.calendarView);
        editContents = findViewById(R.id.diary_contents);
        upload = findViewById(R.id.uploadbtn);

        String defYear = Integer.toString(Calendar.getInstance().get(Calendar.YEAR));
        String defMonth = Integer.toString(Calendar.getInstance().get(Calendar.MONTH) + 1);
        String defDay = Integer.toString(Calendar.getInstance().get(Calendar.DAY_OF_MONTH));

        file = findViewById(R.id.fileBtn);

        memory.setText(defYear + "년 " + defMonth + "월 " + defDay + "일에 남긴 흔적들");

        calendarView.setOnDateChangeListener(new CalendarView.OnDateChangeListener() {
            @Override
            public void onSelectedDayChange(@NonNull CalendarView view, int year, int month, int dayOfMonth) {
                String selYear = String.valueOf(year);
                String selMonth = String.valueOf(month + 1);
                String selDay = String.valueOf(dayOfMonth);

                memory.setText(selYear + "년 " + selMonth + "월 " + selDay + "일에 남긴 흔적들");
            }
        });

        checkPermissions();

        list = getImageFiles(Environment.getExternalStorageDirectory());

        gridView = (GridView) findViewById(R.id.GalleryGrid);
        gridView.setAdapter(new GridAdapter());

        gridView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                startActivity(new Intent(getApplicationContext(), activity_viewImage.class).putExtra("img", list.get(i).toString()));
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
                    DateFormat format = new SimpleDateFormat("YYYY-MM-dd, HH:mm");
                    String Date = format.format(Calendar.getInstance().getTime());
                    Contents.put("Date", Date);
                    Contents.put("Contents", contents);
                    Title.put(Date, Contents);
                    db.collection("Diary").document(currentUser.getDisplayName()).set(Title).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                            hideProgressDialog();
                            toastMessage("컨텐츠가 성공적으로 저장되었습니다.");
                        }
                    })

                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    hideProgressDialog();
                                    toastMessage("컨텐츠가 저장되지 않았습니다. 나중에 다시시도하세요.");
                                }
                            });
                }
            }
        });
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        InputStream stream = null;
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

    class GridAdapter extends BaseAdapter {

        @Override
        public int getCount() {
            return list.size();
        }

        @Override
        public Object getItem(int i) {
            return list.get(i);
        }

        @Override
        public long getItemId(int i) {
            return 0;
        }

        @Override
        public View getView(int i, View view, ViewGroup viewGroup) {
            view = getLayoutInflater().inflate(R.layout.layout_gridimage, viewGroup, false);
            ImageView imageView = (ImageView) view.findViewById(R.id.imageView);

            Glide.with(getApplicationContext())
                    .load(getItem(i).toString())
                    .into(imageView);
            return view;
        }
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    ArrayList<File> getImageFiles(File root) {
        ArrayList<File> list = new ArrayList();
        File[] files = root.listFiles();

        for (int i = 0; i < files.length; i++) {
            if(files[i].exists()){
                ExifInterface exif = null;

                try{
                    exif = new ExifInterface(files[i]);

                    if(exif != null){
                        String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
                        Date currentDate = Calendar.getInstance().getTime();
                        SimpleDateFormat df = new SimpleDateFormat("yyyy-mm-dd");
                        String date = df.format(currentDate);

                        if(dateString.equals(date) && files[i].getName().endsWith(".jpg") || files[i].getName().endsWith(".png") || files[i].getName().endsWith(".gif")){
                            list.add(files[i]);
                        }
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

//            if(files[i].isDirectory()) {
//
//            }
//
//            else {
//                if(files[i].getName().endsWith(".jpg") || files[i].getName().endsWith(".png") || files[i].getName().endsWith(".gif")) {
//                    list.add(files[i]);
//                }
//            }
        }
        return  list;
    }

    private void checkPermissions(){
        if (Build.VERSION.SDK_INT >= 23) {
            if(checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED){
                //Requesting permission.
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

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bumptech.glide.Glide;
import com.firebase.ui.storage.images.FirebaseImageLoader;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FileDownloadTask;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;

public class activity_inTimeCapsule extends BaseActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> contentsList = new ArrayList<>();
    FirebaseStorage storage = FirebaseStorage.getInstance();
    StorageReference storageRef = storage.getReference();
    private static String Str_name;
    ImageView contentsView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_in_timecapsule);

        LinearLayout contents = findViewById(R.id.contents);
        TextView timecapsule_name = findViewById(R.id.timeCapsule_name);
        String name = mAuth.getCurrentUser().getDisplayName().toString();
        Button startAR = findViewById(R.id.startAR);
        contentsView = new ImageView(this);
        LinearLayout ll = new LinearLayout(this);

        DocumentReference docRef = db.collection("Users").document(name);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    assert document != null;
                    if(document.exists() && !Objects.equals(document.getString("Time Capsule"), "")){
                        Str_name = document.getString("Time Capsule");
                        timecapsule_name.setText(Str_name);

                        DocumentReference contentsRef = db.collection("Time Capsule").document(Str_name)
                                .collection("Time Capsule Data List").document(Str_name);

                        contentsRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                            @Override
                            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                                if(task.isSuccessful()){
                                    DocumentSnapshot document = task.getResult();

                                    if(document.exists()){
                                        Map<String, Object> contentsMap = document.getData();

                                        if(contentsMap != null){
                                            for(Map.Entry<String, Object> contentsEntry : contentsMap.entrySet()){
                                                contentsList.add(contentsEntry.getValue().toString());
                                            }

                                            for(int i = 0; i < contentsList.size(); i++){
                                                StorageReference rootStorage = storageRef.child("Time Capsule_" + Str_name);
                                                StorageReference contentsStorage = rootStorage.child("Data" + i + ".jpg");

                                                Log.d("Set Storage : ", "Success");

                                                try {
                                                    File[] localFile = new File[contentsList.size()];
                                                    localFile[i] = File.createTempFile("images", "jpg");

                                                    int finalI = i;
                                                    contentsStorage.getFile(localFile[i]).addOnSuccessListener(new OnSuccessListener<FileDownloadTask.TaskSnapshot>() {
                                                        @Override
                                                        public void onSuccess(FileDownloadTask.TaskSnapshot taskSnapshot) {
                                                            File contentsFile = new File(localFile[finalI].getPath());

                                                            if(contentsFile.exists()){
                                                                for(int j = 0; j < contentsList.size(); j++){
                                                                    ImageView imageView[] = new ImageView[contentsList.size()];
                                                                    Bitmap bitmap = BitmapFactory.decodeFile(contentsFile.getAbsolutePath());
                                                                    imageView[j] = new ImageView(getApplicationContext());
                                                                    imageView[j].setImageBitmap(bitmap);
                                                                    contents.addView(imageView[j]);
                                                                }

//                                                                ll.setOrientation(LinearLayout.VERTICAL);
//                                                                LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
//                                                                contentsView.setLayoutParams(params);
//
                                                            }
                                                        }
                                                    }).addOnFailureListener(new OnFailureListener() {
                                                        @Override
                                                        public void onFailure(@NonNull Exception e) {
                                                            Log.d("Download : ", "Failed" + e);
                                                            toastMessage("이미지 로드 중 문제가 발생하였습니다." + e);
                                                        }
                                                    });

                                                } catch (IOException e) {
                                                    e.printStackTrace();
                                                    Log.d("Download : ", "Failed with IOException" + e);
                                                }
                                            }
                                        }
                                    }
                                }
                            }
                        });
                    }
                }
            }
        });

        startAR.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_inTimeCapsule.this, activity_arStart.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

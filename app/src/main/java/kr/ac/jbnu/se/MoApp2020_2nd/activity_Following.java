package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import de.hdodenhof.circleimageview.CircleImageView;

public class activity_Following extends AppCompatActivity {
    private int myfollowing;
    private CircleImageView profile1[] = new CircleImageView[10];
    private String name[] = new String[10];
    private String record[] = new String[30];

    private Button name_1[] = new Button[10];
    private TextView name_2[] = new TextView[10];
    private int k = 1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String openDate_full;
    private String username;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_following);

        Intent intent = getIntent();
        username = intent.getExtras().getString("following");

        DocumentReference docRefuser = db.collection("Users").document(username);
        docRefuser.collection("Friend").document("Following").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        myfollowing = document.getLong("number").intValue();
                        for(int i = 1; i<myfollowing+1; i++){
                            name[i] = document.getString(Integer.toString(i));
                        }
                        loadfollowing();
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
    }
    private void loadfollowing() {
        for(int j=1; j<myfollowing+1;j++) {
            DocumentReference docfriend = db.collection("Users").document(String.valueOf(name[j]));
            docfriend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            openDate_full = document.getString("accessrecord");
                            add(openDate_full);
                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }

                }
            });
        }

    }

    private void add(String openDate_full) {
        String open_date_temp[] = openDate_full.split(" ");
        String open_date = open_date_temp[0];
        String open_time = open_date_temp[1];
        record[k] = open_date + open_time ;
        k++;
        if(k-1 == myfollowing){
            loadstory();
        }
    }

    private void loadstory() {
        final LinearLayout topLL = findViewById(R.id.followingList);
        for(int i = 1; i<myfollowing+1; i++){
            LinearLayout parentLL = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 30, 0, 0);
            parentLL.setLayoutParams(lp);
            parentLL.setOrientation(LinearLayout.HORIZONTAL);

            profile1[i] = new CircleImageView(this);
            profile1[i].setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            parentLL.addView(profile1[i]);

            LinearLayout childLL = new LinearLayout(this);
            childLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, 1));
            childLL.setOrientation(LinearLayout.VERTICAL);

            name_1[i] = new Button(this);
            name_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
            name_1[i].setTextColor(Color.parseColor("#000000"));
            name_1[i].setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            name_1[i].setBackground(null);
            name_1[i].setPadding(15, 0, 0, 10);
            name_1[i].setTextSize(14);
            name_1[i].setTypeface(null, Typeface.BOLD);
            name_1[i].setText(name[i]);
            String editname = name[i];
            name_1[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v ) {
                    Intent intent = new Intent(activity_Following.this, ViewProfile.class);
                    intent.putExtra("name", editname);
                    startActivity(intent);
                    finish();
                }
            });

            name_2[i] = new TextView(this);
            name_2[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            name_2[i].setTextColor(Color.parseColor("#000000"));
            name_2[i].setPadding(15, 0, 0, 0);
            name_2[i].setTextSize(14);
            name_2[i].setText("최근접속기록 " + record[i]);

            childLL.addView(name_1[i]);
            childLL.addView(name_2[i]);
            parentLL.addView(childLL);
            topLL.addView(parentLL);

            StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + name[i]);
            Glide.with(this).load(ref).into(profile1[i]);
        }
    }
}
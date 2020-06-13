package kr.ac.jbnu.se.MoApp2020_2nd;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

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

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.ac.jbnu.se.MoApp2020_2nd.ui.MyPage.MyPageViewModel;

public class ViewProfile extends AppCompatActivity {
    private static final String TAG ="MYPAGETag";
    private TextView titleT, birthT, genderT, emailT;
    private Button follower, following;
    private CircleImageView myprofile;
    private String username;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    private CircleImageView profile1[] = new CircleImageView[20];
    private String name[] = new String[20];
    private String feeddate[] = new String[20];
    private String feedcontent[] = new String[20];
    private TextView name_1[] = new TextView[20];
    private TextView date_1[] = new TextView[20];
    private TextView name_2[] = new TextView[20];
    private Map<String, Object> diarymap = new HashMap<String, Object>();
    private ImageView[] datePhoto_1 =new ImageView[50];

    public void onCreate(Bundle savedInstancestate){


        super.onCreate(savedInstancestate);
        setContentView(R.layout.activity_view_profile);

        titleT = findViewById(R.id.viewprofiletitle);
        birthT = findViewById(R.id.viewprofile_birth);
        genderT = findViewById(R.id.viewprofile_gender);
        emailT = findViewById(R.id.viewprofile_email);
        follower = findViewById(R.id.viewprofile_followers);
        following = findViewById(R.id.viewprofile_following);
        myprofile = findViewById(R.id.viewprofileproflie);
        mAuth = FirebaseAuth.getInstance();

        Intent intent = getIntent();
        username = intent.getExtras().getString("name");


        StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + username);
        Glide.with(this).load(ref).into(myprofile);

        DocumentReference docRefuser = db.collection("Users").document(username);
        docRefuser.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data:" +
                                " " + document.getData());
                        titleT.setText(document.getString("name"));
                        birthT.setText(document.getString("birth"));
                        genderT.setText(document.getString("gender"));
                        emailT.setText(document.getString("email"));

                    } else {
                        Log.d("TAG", "No such document");

                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        follower.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(ViewProfile.this, activity_Follower.class);
                intent.putExtra("follower", username);
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(ViewProfile.this, activity_Following.class);
                intent.putExtra("following", username);
                startActivity(intent);
            }
        });

        docRefuser.collection("Friend").document("Follower").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data:" +
                                " " + document.getData());
                        follower.setText("Follower " +document.getLong("number").toString());
                    } else {
                        Log.d("TAG", "No such document");
                        follower.setText("follower 0");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });

        docRefuser.collection("Friend").document("Following").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data:" +
                                " " + document.getData());
                        following.setText("Following " +document.getLong("number").toString());
                    } else {
                        Log.d("TAG", "No such document");
                        following.setText("Following 0");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
            }
        });
        loadfeed();
    }

    private void loadfeed(){
        DocumentReference docfriend = db.collection("Diary").document(username);
        docfriend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Map<String, Object> feeddata = document.getData();
                        if(feeddata != null){
                            for(Map.Entry<String, Object> entry : feeddata.entrySet()){
                                String datefull = (String)entry.getValue();
                                String[] check_shared = datefull.split("º");
                                if(check_shared.length == 2){
                                }else {
                                    Map<String, String> namefeed = new HashMap<String, String>();
                                    namefeed.put(username, (String) entry.getValue());
                                    diarymap.put(entry.getKey(), namefeed);
                                }
                            }
                        }
                    } else {
                        Log.d("TAG", "No such document");
                    }
                } else {
                    Log.d("TAG", "get failed with ", task.getException());
                }
                createlayout();
            }
        });
    }

    private void createlayout() {
        final LinearLayout topLL = findViewById(R.id.viewprofile_viewfeed);
        int ii =1;
        TreeMap<String,Object> tm = new TreeMap<String,Object>(diarymap);
        Set<String> keyset = diarymap.keySet();
        Iterator<String> keyiterator = tm.descendingKeySet().iterator(); //키값 내림차순 정렬

        while(keyiterator.hasNext()){
            feeddate[ii] = keyiterator.next();
            Map<String,String> ttt = (Map<String, String>) tm.get(feeddate[ii]);
            for(Map.Entry<String, String> entry2 : ttt.entrySet()){
                feedcontent[ii] = entry2.getValue();
            }
            ii++;
        }

        for(int i = 1; i<tm.size()+1; i++){
            LinearLayout parentLL = new LinearLayout(this);
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 30, 0, 20);
            parentLL.setLayoutParams(lp);
            parentLL.setPadding(10,10,10,10);
            parentLL.setBackgroundResource(R.drawable.btn_rounded);
            parentLL.setOrientation(LinearLayout.HORIZONTAL);

            profile1[i] = new CircleImageView(this);
            profile1[i].setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            parentLL.addView(profile1[i]);

            LinearLayout childLL = new LinearLayout(this);
            childLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            childLL.setOrientation(LinearLayout.VERTICAL);

            LinearLayout childLLL = new LinearLayout(this);
            childLLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            childLLL.setOrientation(LinearLayout.HORIZONTAL);

            name_1[i] = new Button(this);
            name_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
            name_1[i].setTextColor(Color.parseColor("#000000"));
            name_1[i].setBackgroundColor(Color.parseColor("#FFFFFFFF"));
            name_1[i].setBackground(null);
            name_1[i].setPadding(15, 0, 0, 10);
            name_1[i].setTextSize(14);
            name_1[i].setTypeface(null, Typeface.BOLD);
            name_1[i].setText(username);
            String editname = username;
            name_1[i].setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v ) {
                    Intent intent = new Intent(ViewProfile.this, ViewProfile.class);
                    intent.putExtra("name", editname);
                    startActivity(intent);
                    finish();
                }
            });

            date_1[i] = new TextView(this);
            date_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
            date_1[i].setPadding(15, 0, 0, 10);
            date_1[i].setTextSize(12);
            date_1[i].setTextColor(Color.parseColor("#4C4C4C"));
            date_1[i].setText(feeddate[i]);

            StorageReference ref2 = FirebaseStorage.getInstance().getReference("Diary/" + username+"/"+ feeddate[i]);

            name_2[i] = new TextView(this);
            name_2[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            name_2[i].setTextColor(Color.parseColor("#000000"));
            name_2[i].setPadding(15, 10, 0, 10);
            name_2[i].setTextSize(14);
            name_2[i].setText(feedcontent[i]);

            childLLL.addView(name_1[i]);
            childLLL.addView(date_1[i]);
            childLL.addView(childLLL);

            datePhoto_1[i] = new ImageView(this);
            datePhoto_1[i].setPadding(15,0,0,10);
            datePhoto_1[i].setLayoutParams(new LinearLayout.LayoutParams(1000, LinearLayout.LayoutParams.WRAP_CONTENT));
            Glide.with(this).load(ref2).into(datePhoto_1[i]);
            childLL.addView(datePhoto_1[i]);

            childLL.addView(name_2[i]);
            parentLL.addView(childLL);
            topLL.addView(parentLL);

            StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + username);
            Glide.with(this).load(ref).into(profile1[i]);
        }
    }
}
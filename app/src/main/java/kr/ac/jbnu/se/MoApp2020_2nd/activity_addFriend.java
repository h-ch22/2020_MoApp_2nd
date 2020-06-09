package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.HashMap;
import java.util.Map;

public class activity_addFriend extends AppCompatActivity{
    private static final String TAG = "AddFriendtag";
    private Button backbtn, addbtn;
    private EditText findid;
    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String Displayname;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_friend);

        backbtn = findViewById(R.id.add_backbtn);
        addbtn = findViewById(R.id.add_addbtn);
        findid = findViewById(R.id.add_findID);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Displayname =user.getDisplayName();

        addbtn.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {
                final String editid = findid.getText().toString();
                if (editid.equals(Displayname)){
                    toastMessage("자기 자신은 인생의 영원한 친구입니다.");
                }

                DocumentReference docfriend = db.collection("Users").document(editid);
                docfriend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                    @Override
                    public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                        if (task.isSuccessful()) {
                            DocumentSnapshot document = task.getResult();
                            if (document.exists()) {
                                Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                                overlapcheck(editid);
                            } else {
                                Log.d(TAG, "No such document");
                                Toast.makeText(activity_addFriend.this, "가입된 사용자가 없습니다", Toast.LENGTH_SHORT).show();
                            }
                        } else {
                            Log.d(TAG, "get failed with ", task.getException());
                        }
                    }
                });
            }
        });

        backbtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_addFriend.this, activity_main.class);
                startActivity(intent);
                finish();
            }
        });
    }

    private void addfriend(String checkID) {
        final String addid = checkID;

        final DocumentReference docfollower = db.collection("Users").document(addid).collection("Friend").document("Follower");

        docfollower.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {


            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        Map<String, Long> Followernum = new HashMap<>();
                        Long followernumber = document.getLong("number") + 1;
                        Followernum.put("number", followernumber);
                        docfollower.set(Followernum, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    }
                                });
                        Map<String, String> add = new HashMap<>();
                        add.put(followernumber.toString(), Displayname);
                        docfollower.set(add, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    }
                                });

                    } else {
                        Log.d(TAG, "No such document");
                        Map<String, Long> Followernum = new HashMap<>();
                        Long followernumber = Long.valueOf(1);
                        Followernum.put("number", followernumber);
                        docfollower.set(Followernum, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    }
                                });
                        Map<String, String> add = new HashMap<>();
                        add.put(followernumber.toString(), Displayname);
                        docfollower.set(add, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    }
                                });
                    }
                    Toast.makeText(activity_addFriend.this, "추가되었습니다.", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void overlapcheck(String editid) {
        final String checkID = editid;

        final DocumentReference docfollowing = db.collection("Users").document(Displayname).collection("Friend").document("Following");
        docfollowing.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d(TAG, "DocumentSnapshot data: " + document.getData());
                        if(document.getData().containsValue(checkID) == false) {
                            document.get(checkID);
                            Map<String, Long> Followingnum = new HashMap<>();
                            Long followingnumber = document.getLong("number") + 1;
                            Followingnum.put("number", followingnumber);
                            docfollowing.set(Followingnum, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                        }
                                    });
                            Map<String, String> add = new HashMap<>();
                            add.put(followingnumber.toString(), checkID);
                            docfollowing.set(add, SetOptions.merge())
                                    .addOnSuccessListener(new OnSuccessListener<Void>() {
                                        @Override
                                        public void onSuccess(Void aVoid) {
                                            Log.d(TAG, "DocumentSnapshot successfully updated!");
                                            addfriend(checkID);
                                        }
                                    });
                        }else{
                            Toast.makeText(activity_addFriend.this, "중복된친구입니다.", Toast.LENGTH_SHORT).show();
                        }
                    } else {
                        Map<String, Long> Followingnum = new HashMap<>();
                        Long followingnumber = Long.valueOf(1);
                        Followingnum.put("number", followingnumber);
                        docfollowing.set(Followingnum, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                    }
                                });
                        Map<String, String> add = new HashMap<>();
                        add.put(followingnumber.toString(), checkID);
                        docfollowing.set(add, SetOptions.merge())
                                .addOnSuccessListener(new OnSuccessListener<Void>() {
                                    @Override
                                    public void onSuccess(Void aVoid) {
                                        Log.d(TAG, "DocumentSnapshot successfully updated!");
                                        addfriend(checkID);
                                    }
                                });
                        Log.d(TAG, "No such document");
                    }


                } else {
                    Log.d(TAG, "get failed with ", task.getException());
                }
            }
        });
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}


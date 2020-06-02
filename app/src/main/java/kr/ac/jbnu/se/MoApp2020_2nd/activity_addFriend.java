package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class activity_addFriend extends BaseActivity {
    LinearLayout friendList;
    Button ok, cancel;
    FirebaseAuth mAuth = FirebaseAuth.getInstance();
    FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> FriendList = new ArrayList<>();
    public static List<String> selFriendList = new ArrayList<>();
    public static List<String> Checked = new ArrayList<String>();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_getfriends);

        friendList = findViewById(R.id.friendList);
        ok = findViewById(R.id.okBtn);
        cancel = findViewById(R.id.cancelBtn);

        DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getDisplayName());
        docRef.collection("Friend").document("Following").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                DocumentSnapshot document = task.getResult();

                if(document.exists()){
                    Map<String, Object> FriendMap = document.getData();

                    if(FriendMap != null){
                        for(Map.Entry<String, Object> entry : FriendMap.entrySet()){
                            FriendList.add(entry.getValue().toString());
                        }
                    }

                    for(String s : FriendList){
                        Log.d("Friend : ", s);
                        final CheckBox box = new CheckBox(getApplicationContext());
                        box.setText(s + "\n");
                        friendList.addView(box);

                        for(int i = 0; i < FriendList.size(); i++){
                            box.setId(i);
                            Log.d("Checkbox : ", String.valueOf(box.getId()));
                        }

                        box.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
                            @Override
                            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                                List<CheckBox> items = new ArrayList<CheckBox>();

                                if(box.isChecked()){
                                    Checked.add(box.getText().toString());

                                    for(String s : Checked){
                                        Log.d("Checked : ", s);
                                    }
                                }

                                if(!isChecked){
                                    Checked.remove(box.getText().toString());
                                }
                            }
                        });
                    }
                }

                if(!document.exists()){
                    Log.d("Friend : ", "No Friend.");
                }
            }
        });

        ok.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_addFriend.this, activity_createTimeCapsule.class);
                startActivity(intent);
            }
        });
    }
}

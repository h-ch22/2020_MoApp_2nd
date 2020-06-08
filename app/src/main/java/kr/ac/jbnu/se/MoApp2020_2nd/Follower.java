package kr.ac.jbnu.se.MoApp2020_2nd;

import android.os.Bundle;
import android.util.Log;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.CollectionReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

public class Follower extends AppCompatActivity {
    private static final String TAG ="Friend";

    private Button addFriend;
    private TextView Friend_1, Friend_2, Friend_3,Friend_4,Friend_5,Friend_6;
    private TextView history_1, history_2, history_3,history_4,history_5,history_6;
    private ImageView profile_1, profile_2, profile_3, profile_4, profile_5, profile_6;

    String[] friendlist = new String[10];
    String[] friendphoto = new String[10];

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    final FirebaseStorage storage = FirebaseStorage.getInstance();
    private StorageReference storageRef = storage.getReference();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_follower);

        Friend_1 = findViewById(R.id.friend_1);
        Friend_2 = findViewById(R.id.friend_2);
        Friend_3 = findViewById(R.id.friend_3);
        Friend_4 = findViewById(R.id.friend_4);
        Friend_5 = findViewById(R.id.friend_5);
        Friend_6 = findViewById(R.id.friend_6);

        profile_1 = findViewById(R.id.profile_1);
        profile_2 = findViewById(R.id.profile_2);
        profile_3 = findViewById(R.id.profile_3);
        profile_4 = findViewById(R.id.profile_4);
        profile_5 = findViewById(R.id.profile_5);
        profile_6 = findViewById(R.id.profile_6);

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();

        CollectionReference rankRef = db.collection("Users");
        rankRef.orderBy("friend", com.google.firebase.firestore.Query.Direction.DESCENDING).get().addOnSuccessListener(new OnSuccessListener<QuerySnapshot>() {
            @Override
            public void onSuccess(QuerySnapshot task) {
                int count = 0;
                for(QueryDocumentSnapshot doc : task){
                    Log.d(TAG, doc.getId() + "->" + doc.getString("friend"));
                    friendlist[count] = doc.getString("friend");
                    count++;
                }

                Friend_1.setText(friendlist[0]);
                Friend_2.setText(friendlist[1]);
                Friend_3.setText(friendlist[2]);
                Friend_4.setText(friendlist[3]);
                Friend_5.setText(friendlist[4]);
                Friend_6.setText(friendlist[5]);

                glidephoto();
            }
        });

    }

    private void glidephoto() {
        StorageReference ref1 = FirebaseStorage.getInstance().getReference("images/"+friendlist[0]);
        StorageReference ref2 = FirebaseStorage.getInstance().getReference("images/"+friendlist[1]);
        StorageReference ref3 = FirebaseStorage.getInstance().getReference("images/"+friendlist[2]);
        StorageReference ref4 = FirebaseStorage.getInstance().getReference("images/"+friendlist[3]);
        StorageReference ref5 = FirebaseStorage.getInstance().getReference("images/"+friendlist[4]);
        StorageReference ref6 = FirebaseStorage.getInstance().getReference("images/"+friendlist[5]);

        Glide.with(this).load(ref1).into(profile_1);
        Glide.with(this).load(ref2).into(profile_2);
        Glide.with(this).load(ref3).into(profile_3);
        Glide.with(this).load(ref4).into(profile_4);
        Glide.with(this).load(ref5).into(profile_5);
        Glide.with(this).load(ref6).into(profile_6);
    }
}
package kr.ac.jbnu.se.MoApp2020_2nd.ui.MyPage;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

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
import kr.ac.jbnu.se.MoApp2020_2nd.activity_addFriend;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_EditProfile;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_Follower;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_Following;
import kr.ac.jbnu.se.MoApp2020_2nd.R;

public class MyPageFragment extends Fragment {
    private static final String TAG ="MYPAGETag";
    private MyPageViewModel myPageViewModel;
    private View root = null;
    private TextView titleT, birthT, genderT, emailT;
    private Button addfriend, editprofile, follower, following;
    private CircleImageView myprofile;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    String Displayname;

    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        myPageViewModel = ViewModelProviders.of(this).get(MyPageViewModel.class);
        root = inflater.inflate(R.layout.fragment_mypage, container, false);
        titleT = root.findViewById(R.id.mypagetitle);
        birthT = root.findViewById(R.id.my_birth);
        genderT = root.findViewById(R.id.my_gender);
        emailT = root.findViewById(R.id.my_email);
        addfriend = root.findViewById(R.id.addfriend);
        editprofile = root.findViewById(R.id.edit_profile);
        follower = root.findViewById(R.id.mypage_followers);
        following = root.findViewById(R.id.mypage_following);
        myprofile = root.findViewById(R.id.mypageproflie);
        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Displayname = user.getDisplayName();

        StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + Displayname);
        Glide.with(this).load(ref).into(myprofile);

        DocumentReference docRefuser = db.collection("Users").document(Displayname);
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


        follower.setOnClickListener( new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(), activity_Follower.class);
                startActivity(intent);
            }
        });

        following.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_Following.class);
                startActivity(intent);
            }
        });

        editprofile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_EditProfile.class);
                startActivity(intent);
            }
        });

        addfriend.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_addFriend.class);
                startActivity(intent);
            }
        });

        return root;
    }
}

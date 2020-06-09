package kr.ac.jbnu.se.MoApp2020_2nd.ui.Home;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
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

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.ac.jbnu.se.MoApp2020_2nd.R;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_diary;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root = null;
    private static int RESULT_LOAD_IMAGE = 1;
    private Uri filePath;
    TextView year;
    TextView date;
    Button calendar, upload, file;
    EditText contents;
    CircleImageView myprofile;

    private int myfollwing;
    CircleImageView profile1[] = new CircleImageView[10];
    String name[] = new String[10];
    TextView name_1[] = new TextView[10];
    TextView name_2[] = new TextView[10];
    Map<String, Object> diarymap = new HashMap<String, Object>();

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    List<String> diarylist = new ArrayList<>();
    String Displayname;

    public void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel.class);
        root = inflater.inflate(R.layout.fragment_home, container, false);
        year = root.findViewById(R.id.homeDateYear);
        date = root.findViewById(R.id.homeDate);
        calendar = root.findViewById(R.id.calendarBtn);
        contents = root.findViewById(R.id.home_contents);
        upload = root.findViewById(R.id.uploadbtn);
        file = root.findViewById(R.id.fileBtn);
        myprofile = root.findViewById(R.id.home_myprofile);

        String date_String = Calendar.getInstance().get(Calendar.MONTH) + 1 + "월 " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "일";
        int date_btn = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String day_String = Integer.toString(date_btn);
        int year_btn = Calendar.getInstance().get(Calendar.YEAR);
        String year_String = Integer.toString(year_btn) + "년";

        year.setText(year_String);
        date.setText(date_String);
        calendar.setText(day_String);


        file.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
                intent.setType("image/*");
                intent.setAction(Intent.ACTION_GET_CONTENT);
                startActivityForResult(Intent.createChooser(intent, "이미지를 선택하세요."), RESULT_LOAD_IMAGE);
            }
        });

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_diary.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Displayname = user.getDisplayName();
        DocumentReference docRefuser = db.collection("Users").document(Displayname);
        docRefuser.collection("Friend").document("Following").get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()) {
                    DocumentSnapshot document = task.getResult();
                    if (document.exists()) {
                        Log.d("TAG", "DocumentSnapshot data: " + document.getData());
                        myfollwing = document.getLong("number").intValue();
                        for(int i = 1; i<myfollwing+1; i++){
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

        StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + Displayname);
        Glide.with(this).load(ref).into(myprofile);

        return root;
    }

    private void loadfollowing() {
        for(int j=1; j<myfollwing+1;j++) {
            DocumentReference docfriend = db.collection("Diary").document(String.valueOf(name[j]));
            docfriend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {
                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {

                        } else {
                            Log.d("TAG", "No such document");
                        }
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }
                }
            });
        }
        loadstory();
        glidephoto();
    }


    private void loadstory() {
        final LinearLayout topLL = root.findViewById(R.id.storyboard);
        for(int i = 1; i<myfollwing+1; i++){
            LinearLayout parentLL = new LinearLayout(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);
            lp.setMargins(0, 30, 0, 0);
            parentLL.setLayoutParams(lp);
            parentLL.setOrientation(LinearLayout.HORIZONTAL);

            profile1[i] = new CircleImageView(getContext());
            profile1[i].setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            parentLL.addView(profile1[i]);

            LinearLayout childLL = new LinearLayout(getContext());
            childLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT, 1));
            childLL.setOrientation(LinearLayout.VERTICAL);

            name_1[i] = new TextView(getContext());
            name_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            name_1[i].setTextColor(Color.parseColor("#000000"));
            name_1[i].setPadding(15, 0, 0, 10);
            name_1[i].setTextSize(14);
            name_1[i].setText(name[i]);

            name_2[i] = new TextView(getContext());
            name_2[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            name_2[i].setTextColor(Color.parseColor("#000000"));
            name_2[i].setPadding(15, 0, 0, 0);
            name_2[i].setTextSize(14);
            name_2[i].setText("testcontent");

            childLL.addView(name_1[i]);
            childLL.addView(name_2[i]);
            parentLL.addView(childLL);
            topLL.addView(parentLL);
        }
    }

    private void glidephoto() {
        for(int j = 1; j<myfollwing+1;j++) {
            StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + name[j]);
            Glide.with(this).load(ref).into(profile1[j]);
        }
    }

    public void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        InputStream stream = null;
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && null != intent) {
            try {
                stream = getActivity().getApplicationContext().getContentResolver().openInputStream(intent.getData());
                Bitmap original = BitmapFactory.decodeStream(stream);

                ImageView imageView = new ImageView(getContext());
                imageView.setImageBitmap(original);
                LinearLayout linearLayout = root.findViewById(R.id.homeLL);
                linearLayout.addView(imageView);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
        }
    }
}

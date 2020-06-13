package kr.ac.jbnu.se.MoApp2020_2nd.ui.Home;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.bumptech.glide.Glide;
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
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import de.hdodenhof.circleimageview.CircleImageView;
import kr.ac.jbnu.se.MoApp2020_2nd.R;
import kr.ac.jbnu.se.MoApp2020_2nd.ViewProfile;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_diary;

import static android.app.Activity.RESULT_OK;

public class HomeFragment extends Fragment {
    private HomeViewModel homeViewModel;
    private View root = null;
    private static int RESULT_LOAD_IMAGE = 1;
    private Switch publicswitch;
    private String checked = "Off";
    private TextView year;
    private TextView date;
    private Button calendar, upload, file;
    private EditText contents;
    private CircleImageView myprofile;
    private Uri filePath;

    private int myfollwing;
    private CircleImageView profile1[] = new CircleImageView[50];
    private String name[] = new String[50];
    private String feeddate[] = new String[50];
    private String feedcontent[] = new String[50];
    private Button name_1[] = new Button[50];
    private TextView date_1[] = new TextView[50];
    private TextView name_2[] = new TextView[50];
    private Map<String, Object> diarymap = new HashMap<String, Object>();
    private Map<String, String> Contents = new HashMap<String, String>();
    private String Date;
    private ImageView[] datePhoto_1 =new ImageView[50];
    private int k =1;

    private FirebaseAuth mAuth;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private StorageReference mStorageRef;
    String Displayname;
    private String uploaddate;


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
        publicswitch = root.findViewById(R.id.sharing_switch);

        mStorageRef = FirebaseStorage.getInstance().getReference();

        String date_String = Calendar.getInstance().get(Calendar.MONTH) + 1 + "월 " + Calendar.getInstance().get(Calendar.DAY_OF_MONTH) + "일";
        int date_btn = Calendar.getInstance().get(Calendar.DAY_OF_MONTH);
        String day_String = Integer.toString(date_btn);
        int year_btn = Calendar.getInstance().get(Calendar.YEAR);
        String year_String = Integer.toString(year_btn) + "년";

        year.setText(year_String);
        date.setText(date_String);
        calendar.setText(day_String);

        publicswitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if(isChecked) {
                    checked = "On"; //"나만보기"
                }
                else {
                    checked = "Off"; //"공유하기
                }
            }
        });


        upload.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String contentss = contents.getText().toString();

                if(!contents.equals("")){

                    FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
                    Calendar calendar = Calendar.getInstance();
                    DateFormat format = new SimpleDateFormat("YYYY.MM.dd, HH:mm");
                    String Date = format.format(Calendar.getInstance().getTime());
                    uploaddate = Date;
                    if(checked.equals("On")){
                        Contents.put(uploaddate,contentss+"º"+"(hide)");
                    }else{
                        Contents.put(uploaddate, contentss);
                    }
                    uploadImage();
                    db.collection("Diary").document(currentUser.getDisplayName()).set(Contents, SetOptions.merge()).addOnSuccessListener(new OnSuccessListener<Void>() {
                        @Override
                        public void onSuccess(Void aVoid) {
                        }
                    })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                }
                            });
                }
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

        calendar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_diary.class);
                startActivity(intent);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        final FirebaseUser user = mAuth.getCurrentUser();
        Displayname =user.getDisplayName();
        accessrecord();
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

    private void accessrecord() {
        Map<String, String> Contents = new HashMap<String, String>();
        DateFormat format = new SimpleDateFormat("YYYY.MM.dd, HH:mm");
        Date = format.format(Calendar.getInstance().getTime());
        Contents.put("accessrecord", Date);
        db.collection("Users").document(Displayname)
                .set(Contents, SetOptions.merge())
                .addOnSuccessListener(new OnSuccessListener<Void>(){
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "Send User Data to Server was Successfully completed");

                    }
                })

                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("TAG", "Send User Data to Server was not completed.");
                    }
                });
    }


    private void loadfollowing() {
        for(int j=1; j<myfollwing+1;j++) {
            DocumentReference docfriend = db.collection("Diary").document(String.valueOf(name[j]));
            final String yourname = name[j];
            docfriend.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
                @Override
                public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                    if (task.isSuccessful()) {

                        DocumentSnapshot document = task.getResult();
                        if (document.exists()) {
                            Map<String, Object> feeddata = document.getData();
                            if(feeddata != null) {
                                for (Map.Entry<String, Object> entry : feeddata.entrySet()) {
                                    String datefull = (String) entry.getValue();
                                    String[] check_shared = datefull.split("º");
                                    if (check_shared.length == 2) {
                                    } else {
                                        Map<String, String> namefeed = new HashMap<String, String>();
                                        namefeed.put(yourname, (String) entry.getValue());
                                        diarymap.put(entry.getKey(), namefeed);
                                    }
                                }
                            }
                        } else {
                            Log.d("TAG", "No such document");
                        }
                        if(myfollwing == k){
                            loadstory();
                        }
                        k++;
                    } else {
                        Log.d("TAG", "get failed with ", task.getException());
                    }

                }
            });

        }
    }

    private void loadstory() {
        final LinearLayout topLL = root.findViewById(R.id.storyboard);
        int ii =1;
        TreeMap<String,Object> tm = new TreeMap<String,Object>(diarymap);
        Iterator<String> keyiterator = tm.descendingKeySet().iterator(); //키값 내림차순 정렬

        while(keyiterator.hasNext()){
            feeddate[ii] = keyiterator.next();
            Map<String,String> ttt = (Map<String, String>) tm.get(feeddate[ii]);
            for(Map.Entry<String, String> entry2 : ttt.entrySet()){
                name[ii] = entry2.getKey();
                feedcontent[ii] = entry2.getValue();
            }
            ii++;
        }

        for(int i = 1; i<tm.size()+1; i++){
            LinearLayout parentLL = new LinearLayout(getContext());
            LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            lp.setMargins(0, 30, 0, 20);
            parentLL.setLayoutParams(lp);
            parentLL.setPadding(10,10,10,10);
            parentLL.setBackgroundResource(R.drawable.btn_rounded);
            parentLL.setOrientation(LinearLayout.HORIZONTAL);

            profile1[i] = new CircleImageView(getContext());
            profile1[i].setLayoutParams(new LinearLayout.LayoutParams(200, 200));
            parentLL.addView(profile1[i]);

            LinearLayout childLL = new LinearLayout(getContext());
            childLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            childLL.setOrientation(LinearLayout.VERTICAL);

            LinearLayout childLLL = new LinearLayout(getContext());
            childLLL.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            childLLL.setOrientation(LinearLayout.HORIZONTAL);

            name_1[i] = new Button(getContext());
            name_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
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
                    Intent intent = new Intent(getActivity(), ViewProfile.class);
                    intent.putExtra("name", editname);
                    startActivity(intent);
                }
            });

            date_1[i] = new TextView(getContext());
            date_1[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT,1));
            date_1[i].setPadding(15, 0, 0, 10);
            date_1[i].setTextSize(12);
            date_1[i].setTextColor(Color.parseColor("#4C4C4C"));
            date_1[i].setText(feeddate[i]);

            StorageReference ref2 = FirebaseStorage.getInstance().getReference("Diary/" + name[i]+"/"+ feeddate[i]);

            name_2[i] = new TextView(getContext());
            name_2[i].setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
            name_2[i].setTextColor(Color.parseColor("#000000"));
            name_2[i].setPadding(15, 10, 0, 10);
            name_2[i].setTextSize(14);
            name_2[i].setText(feedcontent[i]);

            childLLL.addView(name_1[i]);
            childLLL.addView(date_1[i]);
            childLL.addView(childLLL);

            datePhoto_1[i] = new ImageView(getContext());
            datePhoto_1[i].setPadding(15,0,0,10);
            datePhoto_1[i].setLayoutParams(new LinearLayout.LayoutParams(1000, LinearLayout.LayoutParams.WRAP_CONTENT));
            Glide.with(this).load(ref2).into(datePhoto_1[i]);
            childLL.addView(datePhoto_1[i]);

            childLL.addView(name_2[i]);
            parentLL.addView(childLL);
            topLL.addView(parentLL);

            StorageReference ref = FirebaseStorage.getInstance().getReference("images/" + name[i]);
            Glide.with(this).load(ref).into(profile1[i]);

        }
    }

    private void uploadImage() {
        if (filePath != null) {
            // Code for showing progressDialog while uploading
            final ProgressDialog progressDialog = new ProgressDialog(getActivity());
            progressDialog.setTitle("Uploading...");
            progressDialog.show();

            StorageReference ref = mStorageRef.child("Diary/" + Displayname+"/"+uploaddate);
            ref.putFile(filePath).addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Image Uploaded!!", Toast.LENGTH_SHORT).show();
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    progressDialog.dismiss();
                    Toast.makeText(getActivity(), "Failed " + e.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }).addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                // Progress Listener for loading
                // percentage on the dialog box
                @Override
                public void onProgress(UploadTask.TaskSnapshot taskSnapshot) {
                    double progress = (100.0 * taskSnapshot.getBytesTransferred() / taskSnapshot.getTotalByteCount());
                    progressDialog.setMessage("Uploaded " + (int) progress + "%");
                }
            });
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == RESULT_LOAD_IMAGE && resultCode == RESULT_OK && data != null && data.getData() != null) {
            filePath = data.getData();

            try {
                // Setting image on image view using Bitmap
                Bitmap bitmap = MediaStore.Images.Media.getBitmap(getActivity().getApplicationContext().getContentResolver(), filePath);
                ImageView imageView = new ImageView(getContext());
                imageView.setImageBitmap(bitmap);
                imageView.setLayoutParams(new LinearLayout.LayoutParams(300, 300));
                LinearLayout linearLayout = root.findViewById(R.id.homeLL);
                linearLayout.addView(imageView);
            }
            catch (IOException e) {
                // Log the exception
                e.printStackTrace();
            }
        }
    }
}

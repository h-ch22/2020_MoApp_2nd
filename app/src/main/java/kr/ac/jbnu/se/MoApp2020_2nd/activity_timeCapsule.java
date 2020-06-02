package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

import kr.ac.jbnu.se.MoApp2020_2nd.ui.TimeCapsule.TimeCapsuleFragment;

public class activity_timeCapsule extends BaseActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private static String timecapsule_name, openDate_full, open_date, open_time;
    public String currentTime;
    private String[] open_date_temp;
    DateFormat format = new SimpleDateFormat("yyyyMMddHHmmss");
    Date d = null;
    public TextView DateRemain, TimeRemain;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.fragment_timecapsule);

        DateRemain = findViewById(R.id.DateRemain);
        TimeRemain = findViewById(R.id.TimeRemain);
        final TextView setTime = findViewById(R.id.setTime);

        DocumentReference timecapsuleRef = db.collection("Time Capsule").document(TimeCapsuleFragment.timeCapsule_Name);
        timecapsuleRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    openDate_full = document.getString("Open Date");

                    open_date_temp = openDate_full.split(" ");

                    open_date = open_date_temp[0];

                    open_time = open_date_temp[1] + open_date_temp[2] + open_date_temp[3];

                    setTime.setText(open_date + " " + open_time);

                    try{
                        SimpleDateFormat ymd = new SimpleDateFormat("yyyy.MM.dd");
                        Date date = ymd.parse(open_date);
                        CounterClass timer = new CounterClass(date.getTime(), 1000);
                        d = format.parse(open_time);
                        timer.start();
                    }
                    catch(ParseException e){
                        e.printStackTrace();
                    }

//                    CounterClass timer = new CounterClass(d.getTime(), 1000);

                }
            }
        });
    }
}

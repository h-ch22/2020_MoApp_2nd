package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.Objects;

public class activity_inTimeCapsule extends BaseActivity {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();

    String Str_name;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_in_timecapsule);

        LinearLayout contents = findViewById(R.id.contents);
        TextView timecapsule_name = findViewById(R.id.timeCapsule_name);
        String name = mAuth.getCurrentUser().getDisplayName().toString();
        Button startAR = findViewById(R.id.startAR);
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
}

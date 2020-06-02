package kr.ac.jbnu.se.MoApp2020_2nd.ui.TimeCapsule;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import kr.ac.jbnu.se.MoApp2020_2nd.R;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_createTimeCapsule;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_timeCapsule;

public class TimeCapsuleFragment extends Fragment {
    public static String timeCapsule_Name;
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private TimeCapsuleViewModel timeCapsuleViewModel;
    Button make;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        timeCapsuleViewModel = ViewModelProviders.of(this).get(TimeCapsuleViewModel.class);

        DocumentReference docRef = db.collection("Users").document(mAuth.getCurrentUser().getDisplayName());
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if(document.exists() && !document.getString("Time Capsule").equals("")){
                        timeCapsule_Name = document.getString("Time Capsule");
                        Intent intent = new Intent(getContext(), activity_timeCapsule.class);
                        startActivity(intent);
                    }
                }
            }
        });

        View root = inflater.inflate(R.layout.fragment_timecapsule_first, container, false);

        make = root.findViewById(R.id.btn_makeTimeCapsule);

        make.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_createTimeCapsule.class);
                startActivity(intent);
            }
        });

        return root;
    }
}

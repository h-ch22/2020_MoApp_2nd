package kr.ac.jbnu.se.MoApp2020_2nd.ui.HabitTracker;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.textview.MaterialTextView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;

import kr.ac.jbnu.se.MoApp2020_2nd.R;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_addHabit;

public class HabitTrackerFragment extends Fragment {
    Button createHabit, previousDay, nextDay;
    LinearLayout contentsLL;
    TextView title, date;
    Calendar calendar = Calendar.getInstance();
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private List<String> habitList = new ArrayList<>();
    private int i = 0;

    private HabitTrackerViewModel habitTrackerViewModel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        habitTrackerViewModel = ViewModelProviders.of(this).get(HabitTrackerViewModel.class);
        View root = inflater.inflate(R.layout.fragment_habittracker, container, false);
        createHabit = root.findViewById(R.id.Btn_NewHabit);
        previousDay = root.findViewById(R.id.previousDay);
        nextDay = root.findViewById(R.id.nextDay);
        contentsLL = root.findViewById(R.id.contentsLL);
        title = root.findViewById(R.id.titleHabit);
        date = root.findViewById(R.id.date);

        calendar.setTime(new Date());
        SimpleDateFormat format = new SimpleDateFormat("MM. dd");
        SimpleDateFormat fullFormat = new SimpleDateFormat("yyyy. MM. dd");

        date.setText(fullFormat.format(calendar.getTime()));
        title.setText(format.format(calendar.getTime()) + ", 목표를 실현하셨나요?");

        createHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_addHabit.class);
                startActivity(intent);
            }
        });

        previousDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DATE, -1);
                date.setText(fullFormat.format(calendar.getTime()));
                title.setText(format.format(calendar.getTime()) + ", 목표를 실현하셨나요?");
            }
        });

        nextDay.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                calendar.add(Calendar.DATE, +1);
                date.setText(fullFormat.format(calendar.getTime()));
                title.setText(format.format(calendar.getTime()) + ", 목표를 실현하셨나요?");
            }
        });

        LinearLayout childLL = new LinearLayout(getContext());
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT);

        lp.setMargins(0, 30, 0, 0);
        childLL.setLayoutParams(lp);
        childLL.setOrientation(LinearLayout.HORIZONTAL);

        Button previousHabit = new Button(getContext());
        TextView habitName = new TextView(getContext());
        Button nextHabit = new Button(getContext());

        previousHabit.setText("←");
        previousHabit.setBackgroundColor(Color.parseColor("#ffffffff"));

        db.collection("Habit List").document(mAuth.getCurrentUser().getDisplayName()).get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()){
                        Map<String, Object> habitMap = document.getData();

                        if(habitMap != null){
                            for(Map.Entry<String, Object> entry : habitMap.entrySet()){
                                habitList.add(entry.getValue().toString());
                            }
                        }
                    }
                }
            }
        });

        nextHabit.setText("→");
        nextHabit.setBackgroundColor(Color.parseColor("#ffffffff"));

        if(habitList.isEmpty()){
            habitName.setText("설정된 목표가 없습니다. 지금 설정해보세요!");
        }

        else{
            habitName.setText(habitList.get(i));
        }

        nextHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i++;

                if(i >= habitList.size()){
                    toastMessage("다음 목표가 없습니다.");
                }

                else{
                    habitName.setText(habitList.get(i));
                }
            }
        });

        previousHabit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                i--;

                if(i < 0){
                    toastMessage("다음 목표가 없습니다.");
                }

                else{
                    habitName.setText(habitList.get(i));
                }
            }
        });

        childLL.addView(previousHabit);
        childLL.addView(habitName);
        childLL.addView(nextHabit);

        LinearLayout buttonLL = new LinearLayout(getContext());
        buttonLL.setLayoutParams(lp);
        buttonLL.setOrientation(LinearLayout.HORIZONTAL);

        ImageButton check = new ImageButton(getContext());
        ImageButton ignore = new ImageButton(getContext());

        check.setImageResource(R.drawable.imgbtn_check);
        check.setBackgroundColor(Color.parseColor("#ffffffff"));
        ignore.setImageResource(R.drawable.imgbtn_ignore);
        ignore.setBackgroundColor(Color.parseColor("#ffffffff"));

        buttonLL.addView(check);
        buttonLL.addView(ignore);

        contentsLL.addView(childLL);
        contentsLL.addView(buttonLL);

        return root;
    }

    private void toastMessage(String message){
        Toast.makeText(getContext(), message, Toast.LENGTH_SHORT).show();
    }
}

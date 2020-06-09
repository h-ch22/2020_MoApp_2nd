package kr.ac.jbnu.se.MoApp2020_2nd.ui.AR_Camera;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import kr.ac.jbnu.se.MoApp2020_2nd.R;
import kr.ac.jbnu.se.MoApp2020_2nd.activity_ar;

public class ARCameraFragment extends Fragment {
    Button startBtn;

    private ARCameraViewModel ARCameraViewModel;

    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        ARCameraViewModel = ViewModelProviders.of(this).get(ARCameraViewModel.class);
        View root = inflater.inflate(R.layout.fragment_ar_start, container, false);

        startBtn = root.findViewById(R.id.start_btn);

        startBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(getActivity(), activity_ar.class);
                startActivity(intent);
            }
        });

        return root;
    }
}

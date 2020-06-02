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

import kr.ac.jbnu.se.MoApp2020_2nd.R;

public class MyPageFragment extends Fragment {
    private MyPageViewModel myPageViewModel;
    TextView time;

    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState){
        myPageViewModel = ViewModelProviders.of(this).get(MyPageViewModel.class);
        View root = inflater.inflate(R.layout.fragment_mypage, container, false);




        return root;
    }
}

package kr.ac.jbnu.se.MoApp2020_2nd.ui.Home;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;

import com.google.api.Distribution;

import java.io.FileNotFoundException;
import java.io.InputStream;
import java.util.Calendar;

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

        return root;
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

package kr.ac.jbnu.se.MoApp2020_2nd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

public class activity_tutorialFinish extends Activity {
    private Button start;

    protected void onCreate(Bundle savedInstancestate) {
        super.onCreate(savedInstancestate);
        setContentView(R.layout.layout_tutorial_finish);

        start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_tutorialFinish.this, activity_main.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

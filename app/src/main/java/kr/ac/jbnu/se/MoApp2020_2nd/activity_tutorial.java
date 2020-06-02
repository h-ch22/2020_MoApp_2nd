package kr.ac.jbnu.se.MoApp2020_2nd;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;

import com.google.firebase.auth.FirebaseAuth;

public class activity_tutorial extends Activity {
    FirebaseAuth mAuth = FirebaseAuth.getInstance();

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_firstrun);

        if(mAuth.getCurrentUser() != null){
            Intent intent = new Intent(activity_tutorial.this, activity_main.class);
            startActivity(intent);
            finish();
        }

        Button Start;

        Start = findViewById(R.id.btn_Start);

        Start.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(activity_tutorial.this, activity_tutorialRegister.class);
                startActivity(intent);
                finish();
            }
        });
    }
}

package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.location.Location;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;

import org.w3c.dom.Document;

import java.sql.Time;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.TimeZone;
import java.util.concurrent.TimeUnit;

import io.opencensus.stats.Aggregation;
import kr.ac.jbnu.se.MoApp2020_2nd.ui.TimeCapsule.TimeCapsuleFragment;

public class activity_timeCapsule extends BaseActivity implements OnMapReadyCallback, interface_timeCapsule_googleMap {
    private FirebaseAuth mAuth = FirebaseAuth.getInstance();
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private String timecapsule_name, openDate_full, open_date, open_time;
    private FrameLayout frameLayout;
    private ImageView img_Capsule;
    private String[] open_date_temp;
    public TextView TimeRemain, DateRemain, descript, timeCapsule_name;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    DocumentReference docRef = db.collection("Time Capsule").document(TimeCapsuleFragment.timeCapsule_Name);
    String Str_latitude, Str_longitude;
    double latitude, longitude;
    private static ArrayList<String> friendList;
    long latitudeL, longitudeL;
    ImageView check;
    private Button open;
    Context context = this;

    protected void onCreate(Bundle saveInstanceState) {
        super.onCreate(saveInstanceState);
        setContentView(R.layout.fragment_timecapsule);
        final TextView friend = new TextView(this);

        TimeRemain = findViewById(R.id.TimeRemain);
        DateRemain = findViewById(R.id.DateRemain);
        frameLayout = findViewById(R.id.frameLayout);
        LinearLayout friendsList = findViewById(R.id.friendsList);
        img_Capsule = findViewById(R.id.img_status_timecapsule);
        check = findViewById(R.id.img_status_check);
        open = findViewById(R.id.btn_openTimeCapsule);
        descript = findViewById(R.id.descript_timecapsule);
        timeCapsule_name = findViewById(R.id.title_timecapsule);

        timeCapsule_name.setText(TimeCapsuleFragment.timeCapsule_Name);

        open.setVisibility(View.GONE);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        final TextView setTime = findViewById(R.id.setTime);

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    if(document.exists()){
                        friendList = (ArrayList<String>) document.get("Friends");

                        for(String s : friendList){
                            if(!s.equals("")){
                                friend.setText(s + "\n");
                            }
                        }

                        friendsList.addView(friend);

                    }

                }
            }
        });

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    openDate_full = document.getString("Open Date");

                    open_date_temp = openDate_full.split(" ");

                    Log.d("Open : ", openDate_full);

                    open_date = open_date_temp[0];

                    open_time = open_date_temp[1] + open_date_temp[2] + open_date_temp[3] + open_date_temp[4] + open_date_temp[5];

                    setTime.setText(open_date + " " + open_time);

                    SimpleDateFormat format = new SimpleDateFormat("yyyy/MM/dd HH : mm : ss", Locale.KOREA);

                    try {
                        Date currentTime = new Date();
                        String date = format.format(currentTime);
                        long curMillis = currentTime.getTime();
                        Log.d("current time : ", date);

                        Date setDate = format.parse(openDate_full);
                        long setMillis = setDate.getTime();

                        long diff = setMillis - curMillis;

                        Log.d("Difference : ", String.valueOf(diff));
                        CounterClass timer = new CounterClass(diff, 1000);
                        timer.start();

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }

                }
            }
        });
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if(task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();

                    latitudeL = document.getLong("latitude");
                    longitudeL = document.getLong("longitude");
                    latitude =  (double) latitudeL;
                    longitude = (double) longitudeL;
                }
            }
        });

        MarkerOptions markerOptions = new MarkerOptions();

        Str_latitude = Long.toString(latitudeL);
        Str_longitude = Long.toString(longitudeL);

        markerOptions.snippet(Str_latitude + ", " + Str_longitude);
        markerOptions.position(new LatLng(latitude, longitude));

        googleMap.addMarker(markerOptions);

        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng latLng) {

                if (android.os.Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (ContextCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        buildGoogleApiClient();
                        googleMap.setMyLocationEnabled(true);
                    } else {
                        checkLocationPermission();
                    }
                }
                else {
                    buildGoogleApiClient();
                    googleMap.setMyLocationEnabled(true);
                }
            }
        });
    }

    protected synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) context)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) context)
                .addApi(LocationServices.API)
                .build();
        mGoogleApiClient.connect();
    }

    @Override
    public void onConnected(@Nullable Bundle bundle) {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(1000);
        mLocationRequest.setFastestInterval(1000);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_BALANCED_POWER_ACCURACY);
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, mLocationRequest, (LocationListener) this);
        }
    }

    public static final int MY_PERMISSIONS_REQUEST_LOCATION = 99;
    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.ACCESS_FINE_LOCATION)) {

                new AlertDialog.Builder(this)
                        .setTitle("권한 상승 요구")
                        .setMessage("계속 하려면 위치 정보 액세스 권한이 필요합니다.")
                        .setPositiveButton("확인", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialogInterface, int i) {
                                //Prompt the user once explanation has been shown
                                ActivityCompat.requestPermissions(activity_timeCapsule.this,
                                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                                        MY_PERMISSIONS_REQUEST_LOCATION );
                            }
                        })
                        .create()
                        .show();


            } else {
                // No explanation needed, we can request the permission.
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        MY_PERMISSIONS_REQUEST_LOCATION );
            }
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                    if (ContextCompat.checkSelfPermission(this,
                            Manifest.permission.ACCESS_FINE_LOCATION)
                            == PackageManager.PERMISSION_GRANTED) {

                        if (mGoogleApiClient == null) {
                            buildGoogleApiClient();
                        }
                        mMap.setMyLocationEnabled(true);
                    }

                }

                else {
                    toastMessage("위치 정보 액세스 권한이 거부되었습니다.");
                }
                return;
            }
        }
    }

    public class CounterClass extends CountDownTimer {
        public CounterClass(long millisInFuture, long countDownInterval) {
            super(millisInFuture, countDownInterval);
        }

        @Override
        public void onFinish() {
            frameLayout.removeView(DateRemain);
            frameLayout.removeView(TimeRemain);
            img_Capsule.setImageResource(R.drawable.img_timecapsule_done);
            check.setImageResource(R.drawable.ic_check);
            descript.setText("타임캡슐을 개봉할 준비가 완료되었습니다.\n개봉할 장소에서 친구와 함께 접속해주세요!");
            descript.setTextColor(Color.parseColor("#8C32a852"));
            open.setVisibility(View.VISIBLE);

            open.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(activity_timeCapsule.this, activity_inTimeCapsule.class);
                    startActivity(intent);
                    finish();
                }
            });
        }

        @Override
        public void onTick(long millisUntilFinished) {
            long millis = millisUntilFinished;
            Date date = new Date();
            date.setTime(millis);

            long days = TimeUnit.MILLISECONDS.toDays(millis);
            millis -= TimeUnit.DAYS.toMillis(days);

            long hours = TimeUnit.MILLISECONDS.toHours(millis);
            millis -= TimeUnit.HOURS.toMillis(hours);

            long minutes = TimeUnit.MILLISECONDS.toMinutes(millis);
            millis -= TimeUnit.MINUTES.toMillis(minutes);

            long seconds = TimeUnit.MILLISECONDS.toSeconds(millis);

            DateRemain.setText(days + "일");
            TimeRemain.setText(hours + " : " + minutes + " : " + seconds);
        }
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

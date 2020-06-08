package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.CalendarView;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.exifinterface.media.ExifInterface;

import com.bumptech.glide.Glide;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentSnapshot;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class activity_memory extends activity_diary implements OnMapReadyCallback, interface_timeCapsule_googleMap{
    private GridView gridView;
    private static ArrayList<String> listOfAllImages = new ArrayList<String>();
    private ArrayList<String> images;
    private static String fullDate;
    private static String year, month, day;
    private GoogleMap mMap;
    GoogleApiClient mGoogleApiClient;
    Location mLastLocation;
    LocationRequest mLocationRequest;
    Map<String, String> latitudeMap = new HashMap<String, String>();
    Map<String, String> longitudeMap = new HashMap<String, String>();
    private String exif_latitude, exif_longitude, ref_latitude, ref_longitude;
    private Float float_latitude, float_longitude;
    private String Str_latitude;
    private String Str_longitude;
    double latitude, longitude;
    long latitudeL, longitudeL;
    Context context = this;
    int cnt = 0;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_memory);
        final TextView memory = findViewById(R.id.memory);

        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        gridView = (GridView) findViewById(R.id.GalleryGrid);
        try {
            gridView.setAdapter(new ImageAdapter(this));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }

        if(activity_diary.selYear == null || activity_diary.selMonth == null || activity_diary.selDay == null){
            Date currentDate = Calendar.getInstance().getTime();
            SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
            SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
            SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

            year = yearFormat.format(currentDate);
            month = monthFormat.format(currentDate);
            day = dayFormat.format(currentDate);

            fullDate = year + "/" + month + "/" + day;
            memory.setText(year + "년 " + month + "월 " + day + "일에 남긴 흔적들");
        }

        else{
            memory.setText(activity_diary.selYear + "년 " + activity_diary.selMonth + "월 " + activity_diary.selDay + "일에 남긴 흔적들");
            fullDate = activity_diary.selYear + "/" + activity_diary.selMonth + "/" + activity_diary.selDay;
        }
    }

    private class ImageAdapter extends BaseAdapter {
        private Activity context;

        public ImageAdapter(Activity localContext) throws IOException, ParseException {
            context = localContext;
            images = getAllShownImagesPath(context);
        }

        @Override
        public int getCount() {
            return images.size();
        }

        @Override
        public Object getItem(int position) {
            return position;
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            ImageView picturesView;

            if(convertView == null){
                picturesView = new ImageView(context);
                picturesView.setScaleType(ImageView.ScaleType.FIT_CENTER);
                picturesView.setLayoutParams(new GridView.LayoutParams(270, 270));
            }

            else{
                picturesView = (ImageView) convertView;
            }

            Glide.with(context).load(images.get(position)).placeholder(R.drawable.ic_launcher_foreground).centerCrop().into(picturesView);

            return picturesView;
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mMap = googleMap;

        MarkerOptions markerOptions = new MarkerOptions();

        for(int i = 0; i < latitudeMap.size(); i++){
            for(String key : latitudeMap.keySet()){
                Str_latitude = latitudeMap.get(key);
                if(!Str_latitude.equals(null)){
                    latitude = Double.parseDouble(Str_latitude);
                }
            }

            for(String key_longitude : longitudeMap.keySet()){
                Str_longitude = longitudeMap.get(key_longitude);
                if(!Str_longitude.equals(null)){
                    longitude = Double.parseDouble(Str_longitude);
                }
            }

            markerOptions.snippet(Str_latitude + ", " + Str_longitude);

            markerOptions.position(new LatLng(latitude, longitude));

            googleMap.addMarker(markerOptions);
        }


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
                                ActivityCompat.requestPermissions(activity_memory.this,
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

    private ArrayList<String> getAllShownImagesPath(Activity activity) throws IOException, ParseException {
        Uri uri;
        Cursor cursor;
        int column_index_data, column_index_folder_name;
        String absolutePathOfImage = null;

        uri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;

        String[] projection = {MediaStore.MediaColumns.DATA, MediaStore.Images.Media.BUCKET_DISPLAY_NAME};
        cursor = activity.getContentResolver().query(uri, projection, null, null, null);
        column_index_data = cursor.getColumnIndexOrThrow(MediaStore.MediaColumns.DATA);
        column_index_folder_name = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.BUCKET_DISPLAY_NAME);

        while(cursor.moveToNext()){
            absolutePathOfImage = cursor.getString(column_index_data);
            ExifInterface exifInterface = new ExifInterface(cursor.getString(column_index_data));
            String date;
            Log.d("loc : ", exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE) + exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE));

            if(activity_diary.selYear == null || activity_diary.selMonth == null || activity_diary.selDay == null){
                Date currentDate = Calendar.getInstance().getTime();
                SimpleDateFormat yearFormat = new SimpleDateFormat("yyyy");
                SimpleDateFormat monthFormat = new SimpleDateFormat("MM");
                SimpleDateFormat dayFormat = new SimpleDateFormat("dd");

                String curYear = yearFormat.format(currentDate);
                String curMonth = monthFormat.format(currentDate);
                String curDay = dayFormat.format(currentDate);

                date = curYear + "/" + curMonth + "/" + curDay;
            }

            else{
                date = activity_diary.selYear + "/" + activity_diary.selMonth + "/" + activity_diary.selDay;
            }

            Date exif;
            SimpleDateFormat format = new SimpleDateFormat("yyyy/M/d");
            String captureDate = exifInterface.getAttribute(ExifInterface.TAG_DATETIME);
            String[] partDate = captureDate.split(":");
            String newDate = partDate[0] + "/" + partDate[1] + "/" + partDate[2] + ":" + partDate[3] + ":" + partDate[4];


            exif = format.parse(newDate);
            String newCaptureDate = format.format(exif);

            if(newCaptureDate.equals(date)){
                listOfAllImages.add(absolutePathOfImage);

                exif_latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE);
                exif_longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE);
                ref_latitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LATITUDE_REF);
                ref_longitude = exifInterface.getAttribute(ExifInterface.TAG_GPS_LONGITUDE_REF);

                if(exif_latitude != null && exif_longitude != null && ref_latitude != null && ref_longitude != null){
                    if(ref_latitude.equals("N")){
                        float_latitude = convertToDegree(exif_latitude);
                    }

                    else{
                        float_latitude = 0 - convertToDegree(exif_latitude);
                    }

                    if(ref_longitude.equals("E")){
                        float_longitude = convertToDegree(exif_longitude);
                    }

                    else{
                        float_longitude = 0 - convertToDegree(exif_longitude);
                    }
                }

                Log.d("loc : ", String.valueOf(float_latitude) + String.valueOf(float_longitude));
                latitudeMap.put("latitude" + cnt, String.valueOf(float_latitude));
                longitudeMap.put("longitude" + cnt, String.valueOf(float_longitude));

                cnt++;
            }
        }

        return listOfAllImages;
    }

    private Float convertToDegree(String Str_DMS){
        Float result = null;
        String[] DMS = Str_DMS.split(",", 3);
        String[] stringD = DMS[0].split("/", 2);

        Double D0 = new Double(stringD[0]);
        Double D1 = new Double(stringD[1]);
        Double FloatD = D0 / D1;

        String[] stringM = DMS[1].split("/", 2);
        Double M0 = new Double(stringM[0]);
        Double M1 = new Double(stringM[1]);
        Double FloatM = M0 / M1;

        String[] stringS = DMS[2].split("/", 2);
        Double S0 = new Double(stringS[0]);
        Double S1 = new Double(stringS[1]);
        Double FloatS = S0 / S1;

        result = new Float(FloatD + (FloatM / 60) + (FloatS / 3600));

        return result;
    }

    private void toastMessage(String message){
        Toast.makeText(this,message, Toast.LENGTH_SHORT).show();
    }
}

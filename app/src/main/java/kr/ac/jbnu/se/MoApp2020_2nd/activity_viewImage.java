package kr.ac.jbnu.se.MoApp2020_2nd;

import android.content.Intent;
import android.media.ExifInterface;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;

import com.bumptech.glide.Glide;
import com.drew.imaging.ImageMetadataReader;
import com.drew.imaging.ImageProcessingException;
import com.drew.metadata.Directory;
import com.drew.metadata.Metadata;
import com.drew.metadata.Tag;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;

public class activity_viewImage extends AppCompatActivity {
    private static String file;
    ImageView imageView;
    TextView Metadata_date;

    @RequiresApi(api = Build.VERSION_CODES.Q)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.layout_viewimage);

        Intent intent = getIntent();
        file = intent.getStringExtra("img");

        imageView = findViewById(R.id.imageView);
        Metadata_date = findViewById(R.id.metaData_Date);

        Glide.with(getApplicationContext()).load(file).into(imageView);
        File filePath = new File(file);

        if(filePath.exists()){
            ExifInterface exif = null;

            try{
                exif = new ExifInterface(filePath);

                if(exif != null){
                    String dateString = exif.getAttribute(ExifInterface.TAG_DATETIME);
                    Metadata_date.setText("촬영한 날짜 : " + dateString);
                }

                if(exif == null){
                    Date lastModDate = new Date(filePath.lastModified());
                    Metadata_date.setText("촬영한 날짜 : " + lastModDate.toString());
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

//    private String getImageMetaData(Uri image1) {
//        try {
//            InputStream inputStream = FILE_CONTEXT.getContentResolver().openInputStream(image1);
//            try {
//                image1.getEncodedUserInfo();
//                Metadata metadata = ImageMetadataReader.readMetadata(inputStream);
//
//                for (Directory directory1 : metadata.getDirectories()) {
//                    if (directory1.getName().equals("Exif IFD0")) {
//                        for (Tag tag : directory1.getTags()) {
//                            if (tag.getTagName().equals("Date/Time")) {
//                                return tag.getDescription();
//                            }
//                        }
//                    }
//                }
//            } catch (ImageProcessingException | IOException e) {
//                e.printStackTrace();
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return null;
//    }
}

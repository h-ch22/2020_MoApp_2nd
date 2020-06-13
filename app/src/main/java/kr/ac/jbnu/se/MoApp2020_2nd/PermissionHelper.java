package kr.ac.jbnu.se.MoApp2020_2nd;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class PermissionHelper {
  private static final String CAMERA_PERMISSION = Manifest.permission.CAMERA;
  private static final int CAMERA_PERMISSION_CODE = 0;
  private static final String STORAGE_PERMISSION = Manifest.permission.WRITE_EXTERNAL_STORAGE;
  private static final int STORAGE_PERMISSION_CODE = 99;

  public static boolean hasCameraPermission(Activity activity) {
    return ContextCompat.checkSelfPermission(activity, CAMERA_PERMISSION) ==
            PackageManager.PERMISSION_GRANTED;

  }

  public static void requestCameraPermission(Activity activity) {
    ActivityCompat.requestPermissions(activity, new String[]{CAMERA_PERMISSION},
            CAMERA_PERMISSION_CODE);
  }

  public static boolean hasStoragePermission(Activity activity){
    return ContextCompat.checkSelfPermission(activity, STORAGE_PERMISSION) == PackageManager.PERMISSION_GRANTED;
  }

  public static void requestStoragePermission(Activity activity){
    ActivityCompat.requestPermissions(activity, new String[]{STORAGE_PERMISSION}, STORAGE_PERMISSION_CODE);
  }
}

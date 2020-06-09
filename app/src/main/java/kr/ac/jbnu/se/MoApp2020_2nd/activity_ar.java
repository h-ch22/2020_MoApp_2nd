package kr.ac.jbnu.se.MoApp2020_2nd;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.graphics.Bitmap;
import android.opengl.GLES20;
import android.opengl.GLSurfaceView;
import android.opengl.Matrix;
import android.os.Bundle;
import android.os.Environment;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.view.GestureDetectorCompat;

import com.google.android.material.snackbar.Snackbar;
import com.google.ar.core.ArCoreApk;
import com.google.ar.core.Camera;
import com.google.ar.core.Config;
import com.google.ar.core.Frame;
import com.google.ar.core.Session;
import com.google.ar.core.TrackingState;
import com.google.ar.core.exceptions.CameraNotAvailableException;
import com.google.ar.core.exceptions.UnavailableApkTooOldException;
import com.google.ar.core.exceptions.UnavailableArcoreNotInstalledException;
import com.google.ar.core.exceptions.UnavailableSdkTooOldException;
import com.google.ar.core.exceptions.UnavailableUserDeclinedInstallationException;
import kr.ac.jbnu.se.MoApp2020_2nd.rendering.*;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import javax.microedition.khronos.egl.EGLConfig;
import javax.microedition.khronos.opengles.GL10;
import javax.vecmath.Vector2f;
import javax.vecmath.Vector3f;

public class activity_ar extends AppCompatActivity implements GLSurfaceView.Renderer, GestureDetector.OnGestureListener,
        GestureDetector.OnDoubleTapListener {

        private static final String TAG = activity_ar.class.getSimpleName();

        private GLSurfaceView mSurfaceView;
        private Config mDefaultConfig;
        private Session mSession;
        private BackgroundRenderer mBackgroundRenderer = new BackgroundRenderer();
        private LineShaderRenderer mLineShaderRenderer = new LineShaderRenderer();
        private Frame mFrame;

        private int mWidth, mHeight;
        private boolean capturePicture = false;
        private float[] projmtx = new float[16];
        private float[] viewmtx = new float[16];
        private float[] mZeroMatrix = new float[16];

        private boolean mPaused = false;

        private float mScreenWidth = 0;
        private float mScreenHeight = 0;

        private BiquadFilter biquadFilter;
        private Vector3f mLastPoint;
        private AtomicReference<Vector2f> lastTouch = new AtomicReference<>();

        private GestureDetectorCompat mDetector;

        private LinearLayout mSettingsUI;
        private LinearLayout mButtonBar;

        private SeekBar mLineWidthBar;
        private SeekBar mLineDistanceScaleBar;
        private SeekBar mSmoothingBar;


        private float mLineWidthMax = 0.33f;
        private float mDistanceScale = 0.0f;
        private float mLineSmoothing = 0.1f;

        private float[] mLastFramePosition;

        private AtomicBoolean bIsTracking = new AtomicBoolean(true);
        private AtomicBoolean bReCenterView = new AtomicBoolean(false);
        private AtomicBoolean bTouchDown = new AtomicBoolean(false);
        private AtomicBoolean bClearDrawing = new AtomicBoolean(false);
        private AtomicBoolean bLineParameters = new AtomicBoolean(false);
        private AtomicBoolean bUndo = new AtomicBoolean(false);
        private AtomicBoolean bNewStroke = new AtomicBoolean(false);

        private ArrayList<ArrayList<Vector3f>> mStrokes;

        private DisplayRotationHelper mDisplayRotationHelper;
        private Snackbar mMessageSnackbar;

        private boolean bInstallRequested;

        private TrackingState mState;

        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            final SharedPreferences sharedPref = this.getPreferences(Context.MODE_PRIVATE);

            setContentView(R.layout.layout_ar);

            mSurfaceView = findViewById(R.id.surfaceview);
            mSettingsUI = findViewById(R.id.strokeUI);
            mButtonBar = findViewById(R.id.button_bar);

            mLineDistanceScaleBar = findViewById(R.id.distanceScale);
            mLineWidthBar = findViewById(R.id.lineWidth);
            mSmoothingBar = findViewById(R.id.smoothingSeekBar);

            mLineDistanceScaleBar.setProgress(sharedPref.getInt("mLineDistanceScale", 1));
            mLineWidthBar.setProgress(sharedPref.getInt("mLineWidth", 10));
            mSmoothingBar.setProgress(sharedPref.getInt("mSmoothing", 50));

            mDistanceScale = LineUtils.map((float) mLineDistanceScaleBar.getProgress(), 0, 100, 1, 200, true);
            mLineWidthMax = LineUtils.map((float) mLineWidthBar.getProgress(), 0f, 100f, 0.1f, 5f, true);
            mLineSmoothing = LineUtils.map((float) mSmoothingBar.getProgress(), 0, 100, 0.01f, 0.2f, true);

            SeekBar.OnSeekBarChangeListener seekBarChangeListener = new SeekBar.OnSeekBarChangeListener() {

                @Override
                public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                    SharedPreferences.Editor editor = sharedPref.edit();

                    if (seekBar == mLineDistanceScaleBar) {
                        editor.putInt("mLineDistanceScale", progress);
                        mDistanceScale = LineUtils.map((float) progress, 0f, 100f, 1f, 200f, true);
                    } else if (seekBar == mLineWidthBar) {
                        editor.putInt("mLineWidth", progress);
                        mLineWidthMax = LineUtils.map((float) progress, 0f, 100f, 0.1f, 5f, true);
                    } else if (seekBar == mSmoothingBar) {
                        editor.putInt("mSmoothing", progress);
                        mLineSmoothing = LineUtils.map((float) progress, 0, 100, 0.01f, 0.2f, true);
                    }
                    mLineShaderRenderer.bNeedsUpdate.set(true);

                    editor.apply();

                }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {
                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {
                }
            };

            mLineDistanceScaleBar.setOnSeekBarChangeListener(seekBarChangeListener);
            mLineWidthBar.setOnSeekBarChangeListener(seekBarChangeListener);
            mSmoothingBar.setOnSeekBarChangeListener(seekBarChangeListener);

            mSettingsUI.setVisibility(View.GONE);

            mDisplayRotationHelper = new DisplayRotationHelper(/*context=*/ this);
            Matrix.setIdentityM(mZeroMatrix, 0);

            mLastPoint = new Vector3f(0, 0, 0);

            bInstallRequested = false;

            mSurfaceView.setPreserveEGLContextOnPause(true);
            mSurfaceView.setEGLContextClientVersion(2);
            mSurfaceView.setEGLConfigChooser(8, 8, 8, 8, 16, 0);
            mSurfaceView.setRenderer(this);
            mSurfaceView.setRenderMode(GLSurfaceView.RENDERMODE_CONTINUOUSLY);

            mDetector = new GestureDetectorCompat(this, this);
            mDetector.setOnDoubleTapListener(this);
            mStrokes = new ArrayList<>();


        }

        private void addStroke(Vector2f touchPoint) {
            Vector3f newPoint = LineUtils.GetWorldCoords(touchPoint, mScreenWidth, mScreenHeight, projmtx, viewmtx);
            addStroke(newPoint);
        }

        private void addPoint(Vector2f touchPoint) {
            Vector3f newPoint = LineUtils.GetWorldCoords(touchPoint, mScreenWidth, mScreenHeight, projmtx, viewmtx);
            addPoint(newPoint);
        }

        private void addStroke(Vector3f newPoint) {
            biquadFilter = new BiquadFilter(mLineSmoothing);
            for (int i = 0; i < 1500; i++) {
                biquadFilter.update(newPoint);
            }
            Vector3f p = biquadFilter.update(newPoint);
            mLastPoint = new Vector3f(p);
            mStrokes.add(new ArrayList<Vector3f>());
            mStrokes.get(mStrokes.size() - 1).add(mLastPoint);
        }

        private void addPoint(Vector3f newPoint) {
            if (LineUtils.distanceCheck(newPoint, mLastPoint)) {
                Vector3f p = biquadFilter.update(newPoint);
                mLastPoint = new Vector3f(p);
                mStrokes.get(mStrokes.size() - 1).add(mLastPoint);
            }
        }

        @Override
        protected void onResume() {
            super.onResume();

            if (mSession == null) {
                Exception exception = null;
                String message = null;
                try {
                    switch (ArCoreApk.getInstance().requestInstall(this, !bInstallRequested)) {
                        case INSTALL_REQUESTED:
                            bInstallRequested = true;
                            return;
                        case INSTALLED:
                            break;
                    }

                    if (!PermissionHelper.hasCameraPermission(this)) {
                        PermissionHelper.requestCameraPermission(this);
                        return;
                    }

                    if(!PermissionHelper.hasStoragePermission(this)){
                        PermissionHelper.requestStoragePermission(this);
                        return;
                    }

                    mSession = new Session(/* context= */ this);
                } catch (UnavailableArcoreNotInstalledException
                        | UnavailableUserDeclinedInstallationException e) {
                    message = "ARCore 애플리케이션 설치 후 다시 시도해주세요.";
                    exception = e;
                } catch (UnavailableApkTooOldException e) {
                    message = "계속 하려면 ARCore 애플리케이션을 업데이트해주세요.";
                    exception = e;
                } catch (UnavailableSdkTooOldException e) {
                    message = "계속 하려면 이 소프트웨어를 업데이트해주세요.";
                    exception = e;
                } catch (Exception e) {
                    message = "이 디바이스는 AR을 지원하지 않습니다.";
                    exception = e;
                }

                if (message != null) {
                    Log.e(TAG, "Exception creating session", exception);
                    return;
                }

                Config config = new Config(mSession);
                if (!mSession.isSupported(config)) {
                    Log.e(TAG, "Exception creating session Device Does Not Support ARCore", exception);
                }
                mSession.configure(config);
            }

            try {
                mSession.resume();
            } catch (CameraNotAvailableException e) {
                e.printStackTrace();
            }
            mSurfaceView.onResume();
            mDisplayRotationHelper.onResume();
            mPaused = false;
        }

        @Override
        public void onPause() {
            super.onPause();

            if (mSession != null) {
                mDisplayRotationHelper.onPause();
                mSurfaceView.onPause();
                mSession.pause();
            }

            mPaused = false;


            DisplayMetrics displayMetrics = new DisplayMetrics();
            getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
            mScreenHeight = displayMetrics.heightPixels;
            mScreenWidth = displayMetrics.widthPixels;
        }


        @Override
        public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] results) {
            if (!PermissionHelper.hasCameraPermission(this)) {
                Toast.makeText(this,
                        "계속 하려면 카메라 권한이 필요합니다.", Toast.LENGTH_LONG).show();
                finish();
            }

            if(!PermissionHelper.hasStoragePermission(this)){
                Toast.makeText(this, "계속 하려면 저장소 권한이 필요합니다.", Toast.LENGTH_LONG).show();

                finish();
            }
        }

        @Override
        public void onWindowFocusChanged(boolean hasFocus) {
            super.onWindowFocusChanged(hasFocus);
            if (hasFocus) {
                getWindow().getDecorView().setSystemUiVisibility(
                        View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                                | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                                | View.SYSTEM_UI_FLAG_FULLSCREEN
                                | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
                getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
            }
        }

        @Override
        public void onSurfaceCreated(GL10 gl, EGLConfig config) {

            if (mSession == null) {
                return;
            }

            GLES20.glClearColor(0.1f, 0.1f, 0.1f, 1.0f);
            mBackgroundRenderer.createOnGlThread(/*context=*/this);

            try {

                mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());
                mLineShaderRenderer.createOnGlThread(this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        @Override
        public void onSurfaceChanged(GL10 gl, int width, int height) {
            GLES20.glViewport(0, 0, width, height);
            mDisplayRotationHelper.onSurfaceChanged(width, height);
            mScreenWidth = width;
            mScreenHeight = height;
            mWidth = width;
            mHeight = height;
        }

        private void update() {

            if (mSession == null) {
                return;
            }

            mDisplayRotationHelper.updateSessionIfNeeded(mSession);

            try {

                mSession.setCameraTextureName(mBackgroundRenderer.getTextureId());

                mFrame = mSession.update();
                Camera camera = mFrame.getCamera();

                mState = camera.getTrackingState();

                if (mState == TrackingState.TRACKING && !bIsTracking.get()) {
                    bIsTracking.set(true);
                } else if (mState== TrackingState.STOPPED && bIsTracking.get()) {
                    bIsTracking.set(false);
                    bTouchDown.set(false);
                }
                camera.getProjectionMatrix(projmtx, 0, dialog_ARSettings.getNearClip(), dialog_ARSettings.getFarClip());
                camera.getViewMatrix(viewmtx, 0);

                float[] position = new float[3];
                camera.getPose().getTranslation(position, 0);

                if (mLastFramePosition != null) {
                    Vector3f distance = new Vector3f(position[0], position[1], position[2]);
                    distance.sub(new Vector3f(mLastFramePosition[0], mLastFramePosition[1], mLastFramePosition[2]));

                    if (distance.length() > 0.15) {
                        bTouchDown.set(false);
                    }
                }
                mLastFramePosition = position;

                Matrix.multiplyMM(viewmtx, 0, viewmtx, 0, mZeroMatrix, 0);


                if (bNewStroke.get()) {
                    bNewStroke.set(false);
                    addStroke(lastTouch.get());
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                } else if (bTouchDown.get()) {
                    addPoint(lastTouch.get());
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                }

                if (bReCenterView.get()) {
                    bReCenterView.set(false);
                    mZeroMatrix = getCalibrationMatrix();
                }

                if (bClearDrawing.get()) {
                    bClearDrawing.set(false);
                    clearDrawing();
                    mLineShaderRenderer.bNeedsUpdate.set(true);
                }

                if (bUndo.get()) {
                    bUndo.set(false);
                    if (mStrokes.size() > 0) {
                        mStrokes.remove(mStrokes.size() - 1);
                        mLineShaderRenderer.bNeedsUpdate.set(true);
                    }
                }
                mLineShaderRenderer.setDrawDebug(bLineParameters.get());
                if (mLineShaderRenderer.bNeedsUpdate.get()) {
                    mLineShaderRenderer.setColor(dialog_ARSettings.getColor());
                    mLineShaderRenderer.mDrawDistance = dialog_ARSettings.getStrokeDrawDistance();
                    mLineShaderRenderer.setDistanceScale(mDistanceScale);
                    mLineShaderRenderer.setLineWidth(mLineWidthMax);
                    mLineShaderRenderer.clear();
                    mLineShaderRenderer.updateStrokes(mStrokes);
                    mLineShaderRenderer.upload();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onDrawFrame(GL10 gl) {
            if (mPaused) return;

            update();

            GLES20.glClear(GLES20.GL_COLOR_BUFFER_BIT | GLES20.GL_DEPTH_BUFFER_BIT);

            if (mFrame == null) {
                return;
            }

            mBackgroundRenderer.draw(mFrame);

            if (mFrame.getCamera().getTrackingState() == TrackingState.TRACKING) {
                mLineShaderRenderer.draw(viewmtx, projmtx, mScreenWidth, mScreenHeight, dialog_ARSettings.getNearClip(), dialog_ARSettings.getFarClip());
            }

            if(capturePicture){
                capturePicture = false;
                try {
                    SavePicture();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        public void onSavePicture(View view){
            this.capturePicture = true;
        }

        public void SavePicture() throws IOException{
            int pixelData[] = new int[mWidth * mHeight];

            IntBuffer buf = IntBuffer.wrap(pixelData);
            buf.position(0);
            GLES20.glReadPixels(0, 0, mWidth, mHeight, GLES20.GL_RGBA, GLES20.GL_UNSIGNED_BYTE, buf);

            final File output = new File(Environment.getExternalStoragePublicDirectory(
                    Environment.DIRECTORY_PICTURES) + "/Memory For You", "img" + Long.toHexString(System.currentTimeMillis()) + ".png");

            if(!output.getParentFile().exists()){
                output.getParentFile().mkdirs();
            }

            int bitmapData[] = new int[pixelData.length];

            for(int i = 0; i < mHeight; i++){
                for(int j = 0; j < mWidth; j++){
                    int p = pixelData[i * mWidth + j];
                    int b = (p & 0x00ff0000) >> 16;
                    int r = (p & 0x000000ff) << 16;
                    int ga = p & 0xff00ff00;

                    bitmapData[(mHeight - i - 1) * mWidth + j] = ga | r | b;
                }
            }

            Bitmap bmp = Bitmap.createBitmap(bitmapData, mWidth, mHeight, Bitmap.Config.ARGB_8888);

            FileOutputStream fos = new FileOutputStream(output);
            bmp.compress(Bitmap.CompressFormat.PNG, 100, fos);
            fos.flush();
            fos.close();
        }

        public float[] getCalibrationMatrix() {
            float[] t = new float[3];
            float[] m = new float[16];

            mFrame.getCamera().getPose().getTranslation(t, 0);
            float[] z = mFrame.getCamera().getPose().getZAxis();
            Vector3f zAxis = new Vector3f(z[0], z[1], z[2]);
            zAxis.y = 0;
            zAxis.normalize();

            double rotate = Math.atan2(zAxis.x, zAxis.z);

            Matrix.setIdentityM(m, 0);
            Matrix.translateM(m, 0, t[0], t[1], t[2]);
            Matrix.rotateM(m, 0, (float) Math.toDegrees(rotate), 0, 1, 0);
            return m;
        }

        public void clearDrawing() {
            mStrokes.clear();
            mLineShaderRenderer.clear();
        }

        public void onClickUndo(View button) {
            bUndo.set(true);
        }

        public void onClickLineDebug(View button) {
            bLineParameters.set(!bLineParameters.get());
        }

        public void onClickSettings(View button) {
            ImageButton settingsButton = findViewById(R.id.settingsButton);

            if (mSettingsUI.getVisibility() == View.GONE) {
                mSettingsUI.setVisibility(View.VISIBLE);
                mLineDistanceScaleBar = findViewById(R.id.distanceScale);
                mLineWidthBar = findViewById(R.id.lineWidth);

                settingsButton.setColorFilter(getResources().getColor(R.color.active));
            } else {
                mSettingsUI.setVisibility(View.GONE);
                settingsButton.setColorFilter(getResources().getColor(R.color.gray));
            }
        }

        public void onClickClear(View button) {

            AlertDialog.Builder builder = new AlertDialog.Builder(this)
                    .setMessage("모두 지우시겠습니까?");

            builder.setPositiveButton("예", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    bClearDrawing.set(true);
                }
            });
            builder.setNegativeButton("아니오", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    dialog.cancel();
                }
            });

            builder.show();
        }

        public void onClickRecenter(View button) {
            bReCenterView.set(true);
        }

        @Override
        public boolean onTouchEvent(MotionEvent tap) {
            this.mDetector.onTouchEvent(tap);

            if (tap.getAction() == MotionEvent.ACTION_DOWN ) {
                lastTouch.set(new Vector2f(tap.getX(), tap.getY()));
                bTouchDown.set(true);
                bNewStroke.set(true);
                return true;
            } else if (tap.getAction() == MotionEvent.ACTION_MOVE || tap.getAction() == MotionEvent.ACTION_POINTER_DOWN) {
                lastTouch.set(new Vector2f(tap.getX(), tap.getY()));
                bTouchDown.set(true);
                return true;
            } else if (tap.getAction() == MotionEvent.ACTION_UP || tap.getAction() == MotionEvent.ACTION_CANCEL) {
                bTouchDown.set(false);
                lastTouch.set(new Vector2f(tap.getX(), tap.getY()));
                return true;
            }

            return super.onTouchEvent(tap);
        }


        @Override
        public boolean onSingleTapConfirmed(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDoubleTap(MotionEvent e) {
            if (mButtonBar.getVisibility() == View.GONE) {
                mButtonBar.setVisibility(View.VISIBLE);
            } else {
                mButtonBar.setVisibility(View.GONE);
            }
            return false;
        }

        @Override
        public boolean onDoubleTapEvent(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onDown(MotionEvent e) {
            return false;
        }

        @Override
        public void onShowPress(MotionEvent tap) {
        }

        @Override
        public boolean onSingleTapUp(MotionEvent e) {
            return false;
        }

        @Override
        public boolean onScroll(MotionEvent e1, MotionEvent e2, float distanceX, float distanceY) {
            return false;
        }

        @Override
        public void onLongPress(MotionEvent e) {

        }

        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            return false;
        }

    }

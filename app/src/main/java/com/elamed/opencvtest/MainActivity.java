package com.elamed.opencvtest;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import android.Manifest;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.SurfaceView;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import org.jetbrains.annotations.NotNull;
import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

import java.util.EventListener;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2, View.OnTouchListener {
    public static Mat mat, touchedMat, etalonMat;
    private Rect touchedRect = new Rect();
    private static final String TAG = "MainActivity";
    private CameraBridgeViewBase base;
    private static final int CAMERA_PERMISSION_REQUEST = 1;
    private int x,y;
    private boolean hasRect = false;
    // Used to load the 'native-lib' library on application startup.



    static {
        System.loadLibrary("native-lib");
    }

    private BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            if (status == LoaderCallbackInterface.SUCCESS){
                Log.e(TAG,"Opencv Load Successesfull");
                System.loadLibrary("native-lib");
                base.enableView();
            }else{
                super.onManagerConnected(status);
            }
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityCompat.requestPermissions(this,new String[]{Manifest.permission.CAMERA},
                CAMERA_PERMISSION_REQUEST);

        setContentView(R.layout.activity_main);
        base = findViewById(R.id.main_surface);
        base.setOnTouchListener(MainActivity.this);
        base.setVisibility(SurfaceView.VISIBLE);
        base.setCvCameraViewListener(this);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NotNull String[] permissions, @NotNull int[] grantResults) {
        if (requestCode == CAMERA_PERMISSION_REQUEST) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                base.setCameraPermissionGranted();
            } else {
                String message = "Camera permission was not granted";
                Log.e(TAG, message);
                Toast.makeText(this, message, Toast.LENGTH_LONG).show();
            }
        } else {
            Log.e(TAG, "Unexpected permission request");
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (base != null)
            base.disableView();
    }

    @Override
    public void onResume() {
        super.onResume();
        if (!OpenCVLoader.initDebug()) {
            Log.d(TAG, "Internal OpenCV library not found. Using OpenCV Manager for initialization");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION, this, loaderCallback);
        } else {
            Log.d(TAG, "OpenCV library found inside package. Using it!");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (base != null)
            base.disableView();
    }

    /**
     * A native method that is implemented by the 'native-lib' native library,
     * which is packaged with this application.
     */

    @Override
    public void onCameraViewStarted(int width, int height) {
        mat = new Mat(height,width, CvType.CV_8UC4);
        etalonMat = new Mat();
    }

    @Override
    public void onCameraViewStopped() {
        mat.release();
        etalonMat.release();
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {
        mat = inputFrame.rgba();
        if(hasRect){
            adaptiveThresholdFromJNI(mat.getNativeObjAddr(), touchedMat.getNativeObjAddr());
        }
        Imgproc.rectangle(mat,touchedRect, new Scalar(255));
        return mat;
    }

    public native void adaptiveThresholdFromJNI(long mat, long etalon);

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        if(touchedMat!=null) {
            touchedMat.release();

        }
        int cols = mat.cols();
        int rows = mat.rows();
        int size = 40;
        int xOffset = (base.getWidth() - cols)/2;
        int yOffset = (base.getHeight() - rows)/2;

        int x = (int)event.getX() - xOffset;
        int y = (int)event.getY() - yOffset;

        Log.e("COORDINATE IMAGE",":( x: "+x+"; y: "+y);

        if ((x < 0) || (y < 0) || (x > cols) || (y > rows)) return false;

        touchedRect.x = (x>size) ? x-size : 0;
        touchedRect.y = (y>size) ? y-size : 0;

        touchedRect.width = (x+size < cols) ? x + size - touchedRect.x : cols - touchedRect.x;
        touchedRect.height = (y+size < rows) ? y + size - touchedRect.y : rows - touchedRect.y;

        touchedMat = mat.submat(touchedRect);
        hasRect=true;
        return false;
    }
}

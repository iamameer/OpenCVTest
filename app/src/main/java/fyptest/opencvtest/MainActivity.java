package fyptest.opencvtest;
//https://www.youtube.com/watch?v=Z2vrioEr9OI

import android.Manifest;
import android.app.TaskStackBuilder;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.hardware.Camera;
import android.icu.text.LocaleDisplayNames;
import android.os.Environment;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Toast;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfPoint;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener{

    private Camera camera;
    private int cameraId = 0;

    private static String TAG = "test";
    JavaCameraView javaCameraView;
    Mat mRGBA, imgGray, imgTres, imgCanny, imgfindContour, imgDrawContour;

    BaseLoaderCallback loaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            Log.i(TAG,"onManagerConnected");
            switch (status){
                case BaseLoaderCallback.SUCCESS:
                    javaCameraView.enableView();
                    break;
                default:
                    super.onManagerConnected(status);
                    break;
            }
        }
    };

    static{
        if (OpenCVLoader.initDebug()){
            Log.i(TAG,"succ");
        }else{
            Log.i(TAG,"fail");
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG,"onCreate");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        javaCameraView = (JavaCameraView) findViewById(R.id.javacamview);
        javaCameraView.setVisibility(SurfaceView.VISIBLE);
        javaCameraView.setCvCameraViewListener(this);

        // do we have a camera?
       /* if (!getPackageManager()
                .hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
            Toast.makeText(this, "No camera on this device", Toast.LENGTH_LONG)
                    .show();
        } else {
            cameraId = findBackCamera();
            camera = Camera.open(cameraId);
        }

        checkcamera_permission();*/
    }

    private void checkcamera_permission() {
        if (ContextCompat.checkSelfPermission(MainActivity.this,
                Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {

            // Give first an explanation, if needed.
            if (ActivityCompat.shouldShowRequestPermissionRationale(MainActivity.this,
                    Manifest.permission.CAMERA)) {

                // Show an explanation to the user *asynchronously* -- don't block
                // this thread waiting for the user's response! After the user
                // sees the explanation, try again to request the permission.

            } else {

                // No explanation needed, we can request the permission.

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.CAMERA},
                        1);
            }
        }
    }

    private int findBackCamera() {
        int cameraId = -1;
        // Search for the front facing camera
        int numberOfCameras = Camera.getNumberOfCameras();
        for (int i = 0; i < numberOfCameras; i++) {
            Camera.CameraInfo info = new Camera.CameraInfo();
            Camera.getCameraInfo(i, info);
            if (info.facing == Camera.CameraInfo.CAMERA_FACING_BACK) {
                Log.d(TAG, "Camera found");
                cameraId = i;
                break;
            }
        }
        return cameraId;
    }

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
      /*  Camera.Parameters p = camera.getParameters();
        try{
            p.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
            camera.setParameters(p);
            camera.stopPreview();
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }*/
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
      /*  if (camera != null) {
            camera.release();
            camera = null;
        }*/
    }

    @Override
    protected void onResume() {
        Log.i(TAG,"onResume");
        super.onResume();
        if (OpenCVLoader.initDebug()){
            Log.i(TAG,"succ");
            loaderCallback.onManagerConnected(LoaderCallbackInterface.SUCCESS);
        }else{
            Log.i(TAG,"fail");
            OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_4_0,this,loaderCallback);
        }

     /*   Camera.Parameters p = camera.getParameters();
        try{
            p.setFlashMode(Camera.Parameters.FLASH_MODE_TORCH);
            camera.setParameters(p);
            camera.startPreview();
        }catch (Exception e){
            Log.d(TAG,e.toString());
        }*/
    }


    @Override
    public void onCameraViewStarted(int width, int height) {
        Log.i(TAG,"onCameraViewStarted");
        mRGBA = new Mat(height, width, CvType.CV_8UC4);

    }

    @Override
    public void onCameraViewStopped() {
        Log.i(TAG,"onCameraViewStopped");
        mRGBA.release();
    }

    @Override
    public Mat onCameraFrame(Mat inputFrame) {

        try {
            imgGray = new Mat(inputFrame.rows(), inputFrame.cols(), inputFrame.type());
            Imgproc.cvtColor(inputFrame, imgGray, Imgproc.COLOR_RGB2GRAY);

            imgTres = new Mat(inputFrame.rows(), inputFrame.cols(), inputFrame.type());
            Imgproc.threshold(imgGray, imgTres, 0, 255, Imgproc.THRESH_BINARY);

            imgCanny = new Mat();
            Imgproc.Canny(imgTres, imgCanny, 10, 100);

            imgfindContour = new Mat();
            List<MatOfPoint> contours = new ArrayList<>();
            Imgproc.findContours(imgCanny, contours, imgfindContour, Imgproc.RETR_EXTERNAL, Imgproc.CHAIN_APPROX_SIMPLE);
            Imgproc.drawContours(inputFrame,contours,-1,new Scalar(0,0,255),5);

            for(int i = 0 ; i < contours.size() ; i++){
            Double d = Imgproc.contourArea(contours.get(i));
            Log.d(TAG,"ContourArea: "+d.toString());
        }

        } catch (Exception e) {
            Log.d(TAG, e.toString());
        }
        return inputFrame;
    }
    /*
    private void takeshot(){
        // image naming and path  to include sd card  appending name you choose for file
        String mPath = Environment.getExternalStorageDirectory().toString() + "/" + ACCUWX.IMAGE_APPEND;

        // create bitmap screen capture
        Bitmap bitmap;
        View v1 = mCurrentUrlMask.getRootView();
        v1.setDrawingCacheEnabled(true);
        bitmap = Bitmap.createBitmap(v1.getDrawingCache());
        v1.setDrawingCacheEnabled(false);

        OutputStream fout = null;
        imageFile = new File(mPath);

        try {
            fout = new FileOutputStream(imageFile);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, fout);
            fout.flush();
            fout.close();

        } catch (FileNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        } catch (IOException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
    }*/


}

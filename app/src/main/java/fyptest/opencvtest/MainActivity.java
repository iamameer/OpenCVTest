package fyptest.opencvtest;
//https://www.youtube.com/watch?v=Z2vrioEr9OI

import android.app.TaskStackBuilder;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceView;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.CvType;
import org.opencv.core.Mat;

public class MainActivity extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener{

    private static String TAG = "test";
    JavaCameraView javaCameraView;
    Mat mRGBA;

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

    }

    @Override
    protected void onPause() {
        Log.i(TAG,"onPause");
        super.onPause();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG,"onDestroy");
        super.onDestroy();
        if(javaCameraView!=null){
            javaCameraView.disableView();
        }
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
        Log.i(TAG,"onCameraFrame");
        /*mRGBA = ((CameraBridgeViewBase.CvCameraViewFrame) inputFrame).rgba();
        return mRGBA;*/
        mRGBA = inputFrame.clone();
        return mRGBA;
    }


}

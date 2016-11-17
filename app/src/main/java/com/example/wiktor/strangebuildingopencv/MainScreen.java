package com.example.wiktor.strangebuildingopencv;

import android.os.Bundle;
import android.os.Environment;

import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.SurfaceView;
import android.view.Menu;
import android.view.MenuItem;

import org.opencv.android.BaseLoaderCallback;
import org.opencv.android.CameraBridgeViewBase;
import org.opencv.android.JavaCameraView;
import org.opencv.android.LoaderCallbackInterface;
import org.opencv.android.OpenCVLoader;
import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Core.MinMaxLocResult;


import org.opencv.imgproc.Imgproc;

import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.util.regex.Matcher;

import org.opencv.features2d.Features2d.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgcodecs.Imgcodecs;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.util.Log;
import org.opencv.android.Utils;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.Scalar;
import org.opencv.core.MatOfByte;



public class MainScreen extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String    TAG                 = "OCVSample::Activity";


    public static final int        JAVA_DETECTOR       = 0;
    public static final int        NATIVE_DETECTOR     = 1;


    private Mat                    mRgba;
    private Mat                    mGray;
//    private File                   mCascadeFile;

    private int                    mDetectorType       = JAVA_DETECTOR;
    private String[]               mDetectorName;

    private float                  mRelativeFaceSize   = 0.2f;
    private int                    mAbsoluteFaceSize   = 0;

    // images from memory
    public File root = Environment.getExternalStorageDirectory();
    Mat imageFromRootToPrint;
    Mat imageFromRootToCompare;


    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG,"OpenCV LoadedSucced");
                    mOpenCvCameraView.enableView();
                }
                default: {
                    super.onManagerConnected(status);
                }
            }
        }
    };

    private JavaCameraView mOpenCvCameraView;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        Log.i(TAG, "called onCreate");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_screen);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        mOpenCvCameraView = (JavaCameraView) findViewById(R.id.MainActivityCameraView);
        mOpenCvCameraView.setVisibility(SurfaceView.VISIBLE);
        mOpenCvCameraView.setCvCameraViewListener(this);

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {


        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main_screen, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    public void onResume() {
        super.onResume();
        OpenCVLoader.initAsync(OpenCVLoader.OPENCV_VERSION_3_0_0, this, mLoaderCallback);
    }

    public void onDestroy() {
         imageFromRootToPrint.release();
         imageFromRootToCompare.release();

        Log.w("myApp", "deloaded images");
        super.onDestroy();
        if (mOpenCvCameraView != null) {
            mOpenCvCameraView.disableView();
        }
    }

    @Override
    public void onCameraViewStarted(int width, int height) {
        LoadImagesFromFile();

    }

    @Override
    public void onCameraViewStopped() {
    }

    @Override
    public Mat onCameraFrame(CameraBridgeViewBase.CvCameraViewFrame inputFrame) {

        mRgba = inputFrame.rgba();

        Mat resizedImageToPrint =  new Mat();
        Size newSizeOfImageToPrint = new Size(200,200);

     //   Log.w("myApp", sz.toString()) ;
//
        MatOfKeyPoint points = new MatOfKeyPoint();
        MatOfKeyPoint imagePoints = new MatOfKeyPoint();

        Mat imageToCompareWithPoints =mRgba.clone();
        Mat inputFramePoints = mRgba.clone();
//        Mat mRgba1 = mRgba.clone();

//        Mat result = new Mat(mRgba.rows(),mRgba.cols(),mRgba.type());

//        FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);

//        FeatureDetector ORB = FeatureDetector.create(FeatureDetector.ORB);

        int match_method = Imgproc.TM_SQDIFF;

  //      fast.detect(mRgba, points);

//        fast.detect(imageFromRootToCompare, imagePoints);

//        Imgproc.cvtColor(mRgba, mRgba1, Imgproc.COLOR_RGBA2RGB, 4);

        Imgproc.cvtColor(imageFromRootToCompare, inputFramePoints, Imgproc.COLOR_RGBA2RGB, 4);

//        Features2d.drawKeypoints(mRgba1, points, mRgba);      //mRgba1
//
//        Features2d.drawKeypoints(inputFramePoints, imagePoints, imageToCompareWithPoints); //inputFramePoints

//        Imgproc.matchTemplate(mRgba, imageFromRootToCompare,result,1);


//        Core.absdiff(image, image, resizedImageToPrint);// tu jest błąd

//        Log.w("myApp", mask_image.toString()) ;
       // Imgproc.putText(mRgba, "=====TEST=====2013.09.15", new Point(100, 500), 3, 1, new Scalar(255, 0, 0, 255), 2);

        Imgproc.resize(imageFromRootToPrint, resizedImageToPrint, newSizeOfImageToPrint);
        Imgproc.cvtColor(resizedImageToPrint, resizedImageToPrint, Imgproc.COLOR_RGB2RGBA);
   //     Mat submatOfmRgbaFrame = mRgba.submat(200, 400, 200, 400);
   //     resizedImageToPrint.copyTo(submatOfmRgbaFrame);

        Imgproc.cvtColor(imageFromRootToCompare, imageFromRootToCompare, Imgproc.COLOR_RGB2RGBA);
        Mat result = new Mat(mRgba.rows(), mRgba.cols(), CvType.CV_32F);


        Imgproc.matchTemplate(mRgba, imageFromRootToCompare, result, match_method);



        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());



        MinMaxLocResult mmr = Core.minMaxLoc(result);

        Point matchLoc;




        if(mmr.minVal==0) {
            if (match_method == Imgproc.TM_SQDIFF || match_method == Imgproc.TM_SQDIFF_NORMED) {
                matchLoc = mmr.minLoc;

            } else {
                matchLoc = mmr.maxLoc;
            }

            Log.w("myAppmaxVal", Double.toString(mmr.maxVal)) ;
            Log.w("myAppminVal", Double.toString(mmr.minVal)) ;
            Log.w("myAppMaxLoc", Double.toString(mmr.maxLoc.x)+Double.toString(mmr.maxLoc.y)) ;
            Log.w("myAppMinLoc", Double.toString(mmr.minLoc.x)+Double.toString(mmr.minLoc.y)) ;

            Mat submatOfmRgbaFrame = mRgba.submat((int) matchLoc.x, (int) matchLoc.x + 200, (int) matchLoc.y, (int) matchLoc.y + 200);
           // Mat submatOfmRgbaFrame = mRgba.submat(200,400,200,400);
            resizedImageToPrint.copyTo(submatOfmRgbaFrame);
        }



      //  Core.flip(mRgba, mRgba, 1);

       // imageFromRootToPrint.release();
       // imageFromRootToCompare.release();
        return mRgba;
    }

    private void LoadImagesFromFile() {

        imageFromRootToPrint = Imgcodecs.imread(root + "/Images/3.jpg");
        imageFromRootToCompare = Imgcodecs.imread(root+"/images/1.jpg");

        Log.w("myApp", "loaded images");
    }
}
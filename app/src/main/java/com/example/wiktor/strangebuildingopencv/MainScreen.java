package com.example.wiktor.strangebuildingopencv;

import android.os.Bundle;
import android.os.Environment;

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

////
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

///


import org.opencv.imgproc.Imgproc;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;


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

    int match_method = Imgproc.TM_CCOEFF;

    int xValImageToPrint =200;
    int yValImageToPrint =200;
    Point matchLoc;

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
/*
  //      MatchTemplateDrawing();

//      KeyPoint1();
        MatOfKeyPoint points = new MatOfKeyPoint();
        MatOfKeyPoint imagePoints = new MatOfKeyPoint();

        Mat imageToCompareWithPoints =mRgba.clone();

        Mat inputFramePoints = mRgba.clone();

 //       KeyPoints2();
        Mat mRgba1 = mRgba.clone();

        Mat result = new Mat(mRgba.rows(),mRgba.cols(),mRgba.type());

        FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);

        FeatureDetector ORB = FeatureDetector.create(FeatureDetector.ORB);

        fast.detect(mRgba, points);

        fast.detect(imageFromRootToCompare, imagePoints);

        Imgproc.cvtColor(mRgba, mRgba1, Imgproc.COLOR_RGBA2RGB, 4);

       // KeyPoint3();

        Features2d.drawKeypoints(mRgba1, points, mRgba);      //mRgba1

 //       Features2d.drawKeypoints(inputFramePoints, imagePoints, imageToCompareWithPoints); //inputFramePoints

*/
        MatchTemplateDrawing();

        return mRgba;
    }

    private void MatchTemplateDrawing() {
        Mat resizedImageToPrint =  new Mat();
        //  KeyPoint1();

        Mat inputFramePoints = mRgba.clone();
        //  KeyPoints2();

        Imgproc.cvtColor(imageFromRootToCompare, inputFramePoints, Imgproc.COLOR_RGBA2RGB, 4);
        //  KeyPoint3();

        Size newSizeOfImageToPrint = new Size(xValImageToPrint, yValImageToPrint);
        Imgproc.resize(imageFromRootToPrint, resizedImageToPrint, newSizeOfImageToPrint);
        Imgproc.cvtColor(resizedImageToPrint, resizedImageToPrint, Imgproc.COLOR_RGB2RGBA);

        Imgproc.cvtColor(imageFromRootToCompare, imageFromRootToCompare, Imgproc.COLOR_RGB2RGBA);
        Mat result = new Mat(mRgba.rows(), mRgba.cols(), CvType.CV_32F);


        Imgproc.matchTemplate(mRgba, imageFromRootToCompare, result, match_method);

        Core.normalize(result, result, 0, 1, Core.NORM_MINMAX, -1, new Mat());

        MinMaxLocResult mmr = Core.minMaxLoc(result);


        if(mmr.maxVal>0) {
            matchLoc = mmr.maxLoc;

            Mat submatOfmRgbaFrame = mRgba.submat(0, xValImageToPrint,0, yValImageToPrint);

            DrawImageOnMatchingArea(resizedImageToPrint, matchLoc, submatOfmRgbaFrame);
        }

        Log.w("myAppmaxVal", Double.toString(mmr.maxVal)) ;
    }

    private void DrawImageOnMatchingArea(Mat resizedImageToPrint, Point matchLoc, Mat submatOfmRgbaFrame) {
        if(mRgba.width()>(matchLoc.x+ xValImageToPrint) && mRgba.height()>(matchLoc.y+ yValImageToPrint)){

             submatOfmRgbaFrame = mRgba.submat((int) matchLoc.y, (int) matchLoc.y + yValImageToPrint, (int) matchLoc.x, (int) matchLoc.x + xValImageToPrint);
        }

        Log.w("myAppmatchLocx", Double.toString(matchLoc.x) +"   " +Double.toString(matchLoc.x+200));
        Log.w("myAppmatchLocy", Double.toString(matchLoc.y)+ "   "+ Double.toString(matchLoc.y+200));

//        Imgproc.rectangle(mRgba, matchLoc, new Point(matchLoc.x + 200,matchLoc.y + 200), new Scalar(0, 255, 0));

        resizedImageToPrint.copyTo(submatOfmRgbaFrame);
    }

    private void LoadImagesFromFile() {

        imageFromRootToPrint = Imgcodecs.imread(root + "/Images/2.jpg");
        imageFromRootToCompare = Imgcodecs.imread(root+"/images/1.jpg");

        Log.w("myApp", "loaded images");
    }

    private void KeyPoint3() {
     /*           Features2d.drawKeypoints(mRgba1, points, mRgba);      //mRgba1

        Features2d.drawKeypoints(inputFramePoints, imagePoints, imageToCompareWithPoints); //inputFramePoints

        Imgproc.matchTemplate(mRgba, imageFromRootToCompare,result,1);
    */
    }


    private void KeyPoints2() {
      /*          Mat mRgba1 = mRgba.clone();

        Mat result = new Mat(mRgba.rows(),mRgba.cols(),mRgba.type());

        FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);

        FeatureDetector ORB = FeatureDetector.create(FeatureDetector.ORB);

              fast.detect(mRgba, points);

        fast.detect(imageFromRootToCompare, imagePoints);

        Imgproc.cvtColor(mRgba, mRgba1, Imgproc.COLOR_RGBA2RGB, 4);
    */
    }

    private void KeyPoint1() {
      /*  MatOfKeyPoint points = new MatOfKeyPoint();
        MatOfKeyPoint imagePoints = new MatOfKeyPoint();

        Mat imageToCompareWithPoints =mRgba.clone();
    */
    }

}
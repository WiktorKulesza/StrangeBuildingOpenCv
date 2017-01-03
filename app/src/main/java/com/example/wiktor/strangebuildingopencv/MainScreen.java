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

import org.opencv.imgproc.Imgproc;

import org.opencv.core.MatOfKeyPoint;

import org.opencv.features2d.Features2d;



import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.regex.Matcher;

import org.opencv.features2d.Features2d.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.core.DMatch;
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
     //   Mat matInBacgroundToFind =inputFrame.rgba();

        //

       // mRgba =matInBacgroundToFind.clone();

        Mat matInBacgroundToFind =imageFromRootToCompare;
        // = imageFromRootToCompare.clone();


         Size newSizeOfImageToPrint = new Size(mRgba.cols(), mRgba.rows());

        Size newSizeOfImageToPrint1 = new Size(mRgba.cols()*2, mRgba.rows()*2);

        //Imgproc.resize(matInBacgroundToFind, mRgba, newSizeOfImageToPrint);


    //    Imgproc.cvtColor(matInBacgroundToFind, mRgba, Imgproc.COLOR_BGR2RGBA, 4);

        Log.w("myAppmax_dist", "workin!!!");

        FeatureDetector Orbdetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor OrbExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        Mat descriptorsMRGBA = new Mat();
        Mat descriptorsBackGround = new Mat();

        MatOfKeyPoint keyPointMRGBA = new MatOfKeyPoint();
        MatOfKeyPoint keyPointBackGround= new MatOfKeyPoint();

        Orbdetector.detect(mRgba, keyPointMRGBA);
        Orbdetector.detect(matInBacgroundToFind, keyPointBackGround);

        OrbExtractor.compute(mRgba, keyPointMRGBA, descriptorsMRGBA);
        OrbExtractor.compute(matInBacgroundToFind, keyPointBackGround, descriptorsBackGround);


        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptorsMRGBA,descriptorsBackGround,matches);

        double max_dist = 0;
        double min_dist = 100;

        List<DMatch> matchesList = matches.toList();


        for( int i = 0; i < descriptorsMRGBA.rows(); i++ )
        { double dist = matchesList.get(i).distance;
            if( dist < min_dist ) min_dist = dist;
            if( dist > max_dist ) max_dist = dist;
        }

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();

        for( int i = 0; i < descriptorsMRGBA.rows(); i++ ) {
            if (matchesList.get(i).distance <= 1.4 * min_dist) {
                good_matches.addLast(matchesList.get(i));
                //Log.w("myAppSizzeee", "added"+Integer.toString(i));


            }
        }

//        matchesList.get(1);

//        Log.w("myAppPoint ", matchesList.get(1).toString());


        //matches.fromList(good_matches);

        //matches.get(1,1);
    //    Log.w("myAppPoint ", matches.get(1,1).toString());

//        Log.w("myApp", good_matches.toString());

        Log.w("myAppSizzeee", Integer.toString(good_matches.size()));

        Log.w("myAppx", Integer.toString(matches.rows()));
        Log.w("myAppy", Integer.toString(matches.cols()));

        //    Log.w("myApp", good_matches.toString());
        Mat clonedMrgba =mRgba.clone();
        Imgproc.resize(clonedMrgba, clonedMrgba, newSizeOfImageToPrint1);
       // try {

  /*      Size newSizeOfImageToPrint = new Size(960, 720);

        Imgproc.resize(matInBacgroundToFind, matInBacgroundToFind, newSizeOfImageToPrint);
        Imgproc.resize(mRgba, mRgba, newSizeOfImageToPrint);
        Imgproc.resize(clonedMrgba, clonedMrgba, newSizeOfImageToPrint);
*/
        Scalar RED = new Scalar(255,0,0);
        Scalar GREEN = new Scalar(0,255,0);
        MatOfByte drawnMatches = new MatOfByte();

        //tu jest problem ://
       Features2d.drawMatches(mRgba, keyPointMRGBA,  matInBacgroundToFind, keyPointBackGround,matches,clonedMrgba,GREEN, RED,  drawnMatches, 1 );

        //Features2d.drawMatches(mRgba,keyPointMRGBA,mRgba,keyPointMRGBA,matches,mRgba,GREEN, RED,  drawnMatches, Features2d.NOT_DRAW_SINGLE_POINTS);

        Imgproc.resize(clonedMrgba, mRgba, newSizeOfImageToPrint);

 //       Bitmap imageMatched = Bitmap.createBitmap(clonedMrgba.cols(), clonedMrgba.rows(), Bitmap.Config.RGB_565);//need to save bitmap
    //    Utils.matToBitmap(clonedMrgba, imageMatched);
    //    imageView.setImageBitmap(imageMatched);

//        Features2d.drawMatches(mGray, keypoints, mObject, objectkeypoints, matches, mView);

//        imageView.setImageBitmap(imageMatched);

//        }catch (Exception e){

        //}
        //Imgproc.cvtColor(clonedMrgba, clonedMrgba, Imgproc.COLOR_RGB2RGBA, 4);

        Log.w("myAppmin_dist", Double.toString(min_dist*3));
        Log.w("myAppmax_dist", Double.toString(max_dist));

       // Size newSizeOfImageToPrint = new Size(mRgba.cols(), mRgba.rows());
//        Imgproc.resize(clonedMrgba, matInBacgroundToFind, newSizeOfImageToPrint);

        //return mRgba;
        //return clonedMrgba;
        return mRgba;


/*
        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();
        MatOfDMatch gm = new MatOfDMatch();

        List<DMatch> matchesList = new ArrayList<DMatch>();
        matchesList = matches.toList();

        if(matchesList.isEmpty()==true){
            Log.w("myApp", "EMPTYYYYYYYYYYYYYYYYYY!!!");
        }else {

            Log.w("myApp", "FoundSomething!!!!!!!!");

            for (int i = 0; i < descriptorsBackGround.rows()-1; i++) {

                    if(matchesList.get(i).distance<80) // 3*min_dist is my threshold here

                        good_matches.addLast(matchesList.get(i));

                Log.w("myApp", matchesList.get(i).toString());
            }
        }

    gm.fromList(good_matches);

        Features2d.drawMatches();
*/






/*
//      KeyPoint1();
        MatOfKeyPoint points = new MatOfKeyPoint();
        MatOfKeyPoint imageFromRootToCompareKeyPoints = new MatOfKeyPoint();

        //Mat imageToCompareWithPoints =mRgba.clone();

        //Mat inputFramePoints = mRgba.clone();

 //       KeyPoints2();
        Mat mRgba1 = mRgba.clone();
        Mat imageFromRootToCompareKeyPointsMat = imageFromRootToCompare.clone();

//        Mat result = new Mat(mRgba.rows(),mRgba.cols(),mRgba.type());

        FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);

//        FeatureDetector ORB = FeatureDetector.create(FeatureDetector.ORB);

        fast.detect(mRgba, points);

//        fast.detect(imageFromRootToCompare, imageFromRootToCompareKeyPoints);

        Imgproc.cvtColor(mRgba, mRgba1, Imgproc.COLOR_RGBA2RGB, 4);

//        Imgproc.cvtColor(imageFromRootToCompare, imageFromRootToCompareKeyPointsMat, Imgproc.COLOR_RGBA2RGB, 4);

       // KeyPoint3();

        Features2d.drawKeypoints(mRgba1, points, mRgba);      //mRgba1
//        Features2d.drawKeypoints(imageFromRootToCompareKeyPointsMat, imageFromRootToCompareKeyPoints, imageFromRootToCompare);

 //       Features2d.drawKeypoints(inputFramePoints, imageFromRootToCompareKeyPoints, imageToCompareWithPoints); //inputFramePoints

        //////////////////////////////////////////////////////////////////////////////
        Mat resizedImageToPrint =  new Mat();
        //  KeyPoint1();

        Mat inputFramePoints = mRgba.clone();
        //  KeyPoints2();

        Imgproc.cvtColor(imageFromRootToCompare, inputFramePoints, Imgproc.COLOR_RGBA2RGB, 4);
        //  KeyPoint3();

        Size newSizeOfImageToPrint = new Size(mRgba.cols(), mRgba.rows());
        Imgproc.resize(imageFromRootToCompare, resizedImageToPrint, newSizeOfImageToPrint);
        Imgproc.cvtColor(resizedImageToPrint, resizedImageToPrint, Imgproc.COLOR_RGB2RGBA);
///////////////////////////////////////////////////////////////////////////////////////////////////////////////
//        MatchTemplateDrawing();
                fast.detect(resizedImageToPrint, imageFromRootToCompareKeyPoints);
                Imgproc.cvtColor(resizedImageToPrint, imageFromRootToCompareKeyPointsMat, Imgproc.COLOR_RGBA2RGB, 4);
        Features2d.drawKeypoints(imageFromRootToCompareKeyPointsMat, imageFromRootToCompareKeyPoints, imageFromRootToCompareKeyPointsMat);


        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);


        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptors1,descriptors2,matches);


      //  return imageFromRootToCompareKeyPointsMat;
*/



    }

    private void MatchTemplateDrawing() {
        Mat resizedImageToPrint =   new Mat();
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
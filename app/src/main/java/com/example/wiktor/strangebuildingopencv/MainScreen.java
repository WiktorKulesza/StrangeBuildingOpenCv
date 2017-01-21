package com.example.wiktor.strangebuildingopencv;

import android.content.Context;
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

import org.opencv.calib3d.Calib3d;
import org.opencv.core.Point;
import org.opencv.core.Size;
import org.opencv.core.CvType;
import org.opencv.core.Core.MinMaxLocResult;


import org.opencv.imgproc.Imgproc;

import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.KeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.features2d.Features2d;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;
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

import static java.lang.Math.abs;


public class MainScreen extends AppCompatActivity implements CameraBridgeViewBase.CvCameraViewListener2 {

    private static final String TAG = "OCVSample::Activity";


    public static final int JAVA_DETECTOR = 0;
    public static final int NATIVE_DETECTOR = 1;

    BufferedWriter wr;

    private Mat mRgba;
    private Mat mGray;
    private Mat blackImage;
//    private File                   mCascadeFile;

    private int mDetectorType = JAVA_DETECTOR;
    private String[] mDetectorName;

    private float mRelativeFaceSize = 0.2f;
    private int mAbsoluteFaceSize = 0;

    // images from memory
    public File root = Environment.getExternalStorageDirectory();
    Mat imageFromRootToPrint;
    Mat imageFromRootToCompare;

    int match_method = Imgproc.TM_CCOEFF;

    int xValImageToPrint = 200;
    int yValImageToPrint = 200;
    Point matchLoc;

    private BaseLoaderCallback mLoaderCallback = new BaseLoaderCallback(this) {
        @Override
        public void onManagerConnected(int status) {
            switch (status) {
                case LoaderCallbackInterface.SUCCESS: {
                    Log.i(TAG, "OpenCV LoadedSucced");
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

        try {
            wr.close();
        } catch (IOException e) {

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

        Mat clonedMrgba = blackImage.clone();

        Imgproc.cvtColor(clonedMrgba, clonedMrgba, Imgproc.COLOR_RGB2RGBA, 4);

        Mat matInBacgroundToFind = imageFromRootToCompare;

        Size newSizeOfImageToPrint = new Size(mRgba.cols() + matInBacgroundToFind.cols(), mRgba.rows() + matInBacgroundToFind.rows());

        Size newSizeOfImageToPrint1 = new Size(mRgba.cols(), mRgba.rows());

        Imgproc.resize(clonedMrgba, clonedMrgba, newSizeOfImageToPrint);

        // Log.w("myAppheigh", Double.toString(newSizeOfImageToPrint.height));
        // Log.w("myAppwidth", Double.toString(newSizeOfImageToPrint.width));

        Mat submatOfmRgbaFrame = clonedMrgba.submat(0, mRgba.rows(), 0, mRgba.cols());
        DrawImageOnMatchingArea(mRgba, clonedMrgba, submatOfmRgbaFrame);

        //Log.w("myAppmax_dist", "workin!!!");

        FeatureDetector Orbdetector = FeatureDetector.create(FeatureDetector.ORB);
        DescriptorExtractor OrbExtractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);

        Mat descriptorsMRGBA = new Mat();
        Mat descriptorsBackGround = new Mat();

        MatOfKeyPoint keyPointMRGBA = new MatOfKeyPoint();
        MatOfKeyPoint keyPointBackGround = new MatOfKeyPoint();

        Orbdetector.detect(mRgba, keyPointMRGBA);
        Orbdetector.detect(matInBacgroundToFind, keyPointBackGround);

        OrbExtractor.compute(mRgba, keyPointMRGBA, descriptorsMRGBA);
        OrbExtractor.compute(matInBacgroundToFind, keyPointBackGround, descriptorsBackGround);


        MatOfDMatch matches = new MatOfDMatch();
        matcher.match(descriptorsMRGBA, descriptorsBackGround, matches);

        double max_dist = 0;
        double min_dist = 100;

        List<DMatch> matchesList = matches.toList();


        for (int i = 0; i < descriptorsMRGBA.rows(); i++) {
            double dist = matchesList.get(i).distance;
            if (dist < min_dist) min_dist = dist;
            if (dist > max_dist) max_dist = dist;
        }

        LinkedList<DMatch> good_matches = new LinkedList<DMatch>();

        for (int i = 0; i < descriptorsMRGBA.rows(); i++) {
            if (matchesList.get(i).distance <= 1.4 * min_dist) {
                good_matches.addLast(matchesList.get(i));
            }
        }

        matches.fromList(good_matches);
        Log.w("myAppgoodmatches", Integer.toString(good_matches.size()));

        Log.w("myAppx", Integer.toString(matches.rows()));
        Log.w("myAppy", Integer.toString(matches.cols()));

        Scalar RED = new Scalar(255, 0, 0);
        Scalar GREEN = new Scalar(0, 255, 0);
        MatOfByte drawnMatches = new MatOfByte();

        Features2d.drawMatches(mRgba, keyPointMRGBA, matInBacgroundToFind, keyPointBackGround, matches, clonedMrgba, GREEN, RED, drawnMatches, 1);

        LinkedList<Point> objList = new LinkedList<Point>();
        LinkedList<Point> sceneList = new LinkedList<Point>();

        List<KeyPoint> keypoints_objectList = keyPointBackGround.toList();
        List<KeyPoint> keypoints_sceneList = keyPointMRGBA.toList();
        try {
            for (int i = 0; i < good_matches.size(); i++) {
                objList.addLast(keypoints_objectList.get(good_matches.get(i).trainIdx).pt);
                sceneList.addLast(keypoints_sceneList.get(good_matches.get(i).queryIdx).pt);
            }

            MatOfPoint2f obj = new MatOfPoint2f();
            obj.fromList(objList);

            MatOfPoint2f scene = new MatOfPoint2f();
            scene.fromList(sceneList);

            Log.w("myAppobjcols", Integer.toString(obj.cols()));
            Log.w("myAppobjrows", Integer.toString(obj.rows()));

            Log.w("myAppscenecols", Integer.toString(scene.cols()));
            Log.w("myAppscenerows", Integer.toString(scene.rows()));

            Mat H = new Mat();

            H = Calib3d.findHomography(obj, scene, Calib3d.RANSAC, 1);


            Mat obj_corners = new Mat(4, 1, CvType.CV_32FC2);
            Mat scene_corners = new Mat(4, 1, CvType.CV_32FC2);

            for (int i = 0; i < H.cols(); i++) {
                for (int j = 0; j < H.rows(); j++) {
                    System.out.println("[" + i + "," + j + "] = " + Arrays.toString(H.get(j, i)));
                }
            }


            obj_corners.put(0, 0, new double[]{0, 0});
            obj_corners.put(1, 0, new double[]{matInBacgroundToFind.cols(), 0});
            obj_corners.put(2, 0, new double[]{matInBacgroundToFind.cols(), matInBacgroundToFind.rows()});
            obj_corners.put(3, 0, new double[]{0, matInBacgroundToFind.rows()});

            if (obj_corners != null && scene_corners != null && H != null) {
                try {
                    Core.perspectiveTransform(obj_corners, scene_corners, H);
                } catch (Exception w) {
                    Log.w("myAppscenecols", "wyjatek ");
                }
            }

            Mat resizedImageToPrint = new Mat();

            Point pu1 = new Point(scene_corners.get(0, 0));
            Point pu2 = new Point(scene_corners.get(2, 0));
            Point pd1 = new Point(scene_corners.get(1, 0));
            Point pd2 = new Point(scene_corners.get(3, 0));

            Point up = FindMinVal(pu1, pu2, pd1, pd2);
            Point down = FindMaxVal(pu1, pu2, pd1, pd2);


            Log.w("myAppSizeY", Double.toString(down.y - up.y));
            Log.w("myAppSizeX", Double.toString(down.x - up.x));

            Log.w("myAppSizeY1", Double.toString(up.y));
            Log.w("myAppSizeX1", Double.toString(up.x));

            Log.w("myAppSizeY2", Double.toString(down.y));
            Log.w("myAppSizeX2", Double.toString(down.x));

            Size newSizeOfImageToPrintOnStream; //= new Size();

            if ((int) down.y - up.y >= 1 && (int) down.x - up.x >= 1)
                //newSizeOfImageToPrintOnStream = new Size(abs((int) down.y -(int) up.y), abs((int) down.x - (int)up.x));
                newSizeOfImageToPrintOnStream = new Size(down.x - up.x, down.y - up.y);


            else {
                newSizeOfImageToPrintOnStream = new Size(100.0, 200.0);

                up.x = 0;
                up.y = 0;
                down.x = 200;
                down.y = 100;
            }

            try {
                Imgproc.resize(imageFromRootToPrint, resizedImageToPrint, newSizeOfImageToPrintOnStream);


                Imgproc.cvtColor(resizedImageToPrint, resizedImageToPrint, Imgproc.COLOR_RGB2RGBA);

                ///////////////////////////////////////////////////x1   x2    y1    y2

                Mat submatOfmRgbaFrameToPrint = clonedMrgba.submat((int) up.y, (int) down.y, (int) up.x, (int) down.x);
                resizedImageToPrint.copyTo(submatOfmRgbaFrameToPrint);

            } catch (Exception e) {

            }

/////////////////////////////////////////////////////////////////
/*

    Mat submatOfmClonedMrgba = clonedMrgba.submat(0,500,0,500);

    Mat m1 = new Mat();
    Size s1 = new Size(submatOfmClonedMrgba.rows()+1,submatOfmClonedMrgba.cols()+1);

    Imgproc.resize(imageFromRootToPrint, m1, s1);

    DrawImageOnMatchingArea(m1, clonedMrgba, submatOfmClonedMrgba);
*/
//////////////////////////////////////////////////////////////////

//    Imgproc.line(clonedMrgba, p1, p2, new Scalar(0,0,255), 4);

            Imgproc.line(clonedMrgba, new Point(scene_corners.get(0, 0)), new Point(scene_corners.get(1, 0)), new Scalar(0, 255, 0), 4);
            Imgproc.line(clonedMrgba, new Point(scene_corners.get(1, 0)), new Point(scene_corners.get(2, 0)), new Scalar(0, 255, 0), 4);
            Imgproc.line(clonedMrgba, new Point(scene_corners.get(2, 0)), new Point(scene_corners.get(3, 0)), new Scalar(0, 255, 0), 4);
            Imgproc.line(clonedMrgba, new Point(scene_corners.get(3, 0)), new Point(scene_corners.get(0, 0)), new Scalar(0, 255, 0), 4);
        } catch (ArrayIndexOutOfBoundsException e) {

            Log.w("myAppClonedImage", Integer.toString(clonedMrgba.width()));
        }


        isExternalStorageWritable();



        Imgproc.resize(clonedMrgba, clonedMrgba, newSizeOfImageToPrint1);

        return clonedMrgba;




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
        Mat resizedImageToPrint = new Mat();
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


        if (mmr.maxVal > 0) {
            matchLoc = mmr.maxLoc;

            Mat submatOfmRgbaFrame = mRgba.submat(0, xValImageToPrint, 0, yValImageToPrint);

            DrawImageOnMatchingArea(resizedImageToPrint, matchLoc, submatOfmRgbaFrame);
        }

        //  Log.w("myAppmaxVal", Double.toString(mmr.maxVal)) ;
    }

    private void DrawImageOnMatchingArea(Mat resizedImageToPrint, Point matchLoc, Mat submatOfmRgbaFrame) {
        if (mRgba.width() > (matchLoc.x + xValImageToPrint) && mRgba.height() > (matchLoc.y + yValImageToPrint)) {

            submatOfmRgbaFrame = mRgba.submat((int) matchLoc.y, (int) matchLoc.y + yValImageToPrint, (int) matchLoc.x, (int) matchLoc.x + xValImageToPrint);
        }

        //  Log.w("myAppmatchLocx", Double.toString(matchLoc.x) +"   " +Double.toString(matchLoc.x+200));
        //  Log.w("myAppmatchLocy", Double.toString(matchLoc.y)+ "   "+ Double.toString(matchLoc.y+200));

//        Imgproc.rectangle(mRgba, matchLoc, new Point(matchLoc.x + 200,matchLoc.y + 200), new Scalar(0, 255, 0));

        resizedImageToPrint.copyTo(submatOfmRgbaFrame);
    }


    private void DrawImageOnMatchingArea(Mat resizedImageToPrint, Mat outputImage, Mat submatOfmRgbaFrame) {
        //      if(mRgba.width()>(matchLoc.x+ xValImageToPrint) && mRgba.height()>(matchLoc.y+ yValImageToPrint)){

        //   submatOfmRgbaFrame = outputImage.submat((int) matchLoc.y, (int) matchLoc.y + 720, (int) matchLoc.x, (int) matchLoc.x + 1280);
//        }

//        Log.w("myAppmatchLocx", Double.toString(matchLoc.x) +"   " +Double.toString(matchLoc.x+720));
//        Log.w("myAppmatchLocy", Double.toString(matchLoc.y)+ "   "+ Double.toString(matchLoc.y+960));

//        Imgproc.rectangle(mRgba, matchLoc, new Point(matchLoc.x + 200,matchLoc.y + 200), new Scalar(0, 255, 0));

        resizedImageToPrint.copyTo(submatOfmRgbaFrame);
    }

    private void LoadImagesFromFile() {

        imageFromRootToPrint = Imgcodecs.imread(root + "/Images/2.jpg");
        imageFromRootToCompare = Imgcodecs.imread(root + "/images/1.jpg");

        blackImage = Imgcodecs.imread(root + "/Images/4.jpg");
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

    private Point FindMinVal(Point p1, Point p2, Point p3, Point p4) {
        Point point = new Point();
        if (p1.x < 0) {
            p1.x = 10000;
        }
        if (p2.x < 0) {
            p2.x = 10000;
        }
        if (p3.x < 0) {
            p3.x = 10000;

        }
        if (p4.x < 0) {
            p4.x = 100000;

        }

        if (p1.y < 0) {
            p1.y = 10000;
        }
        if (p2.y < 0) {
            p2.y = 10000;
        }
        if (p3.y < 0) {
            p3.y = 10000;

        }
        if (p4.y < 0) {
            p4.y = 10000;

        }

        if (p1.x < p2.x && p1.x < p3.x && p1.x < p4.x) {
            point.x = (int) p1.x;
        } else if (p2.x < p1.x && p2.x < p3.x && p2.x < p4.x) {
            point.x = (int) p2.x;
        } else if (p3.x < p1.x && p3.x < p2.x && p3.x < p4.x) {
            point.x = (int) p3.x;
        } else if (p4.x < p1.x && p4.x < p3.x && p4.x < p2.x) {
            point.x = (int) p4.x;
        }

        if (p1.y < p2.y && p1.y < p3.y && p1.y < p4.y) {
            point.y = (int) p1.y;
        } else if (p2.y < p1.y && p2.y < p3.y && p2.y < p4.y) {
            point.y = (int) p2.y;
        } else if (p3.y < p1.y && p3.y < p2.y && p3.y < p4.y) {
            point.y = (int) p3.y;
        } else if (p4.y < p1.y && p4.y < p3.y && p4.y < p2.y) {
            point.y = (int) p4.y;
        }

        return point;
    }

    private Point FindMaxVal(Point p1, Point p2, Point p3, Point p4) {
        Point point = new Point();

        if (p1.x > p2.x && p1.x > p3.x && p1.x > p4.x) {
            point.x = (int) p1.x;
        } else if (p2.x > p1.x && p2.x > p3.x && p2.x > p4.x) {
            point.x = (int) p2.x;
        } else if (p3.x > p1.x && p3.x > p2.x && p3.x > p4.x) {
            point.x = (int) p3.x;
        } else if (p4.x > p1.x && p4.x > p3.x && p4.x > p2.x) {
            point.x = (int) p4.x;
        }

        if (p1.y > p2.y && p1.y > p3.y && p1.y > p4.y) {
            point.y = (int) p1.y;
        } else if (p2.y > p1.y && p2.y > p3.y && p2.y > p4.y) {
            point.y = (int) p2.y;
        } else if (p3.y > p1.y && p3.y > p2.y && p3.y > p4.y) {
            point.y = (int) p3.y;
        } else if (p4.y > p1.y && p4.y > p3.y && p4.y > p2.y) {
            point.y = (int) p4.y;
        }

        return point;

    }


    public boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            Log.w("myAppClonedImage", "writable");
            return true;

        }
        Log.w("myAppClonedImage", "unwritable");
        return false;
    }




}
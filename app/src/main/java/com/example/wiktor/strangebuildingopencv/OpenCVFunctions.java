package com.example.wiktor.strangebuildingopencv;

import android.os.Environment;
import android.util.Log;

import org.opencv.core.Mat;


import org.opencv.core.MatOfKeyPoint;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

import org.opencv.imgcodecs.Imgcodecs;

import java.io.File;

import org.opencv.features2d.Features2d.*;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.imgcodecs.Imgcodecs;

/**
 * Created by Wiktor on 2015-12-08.
 */
public class OpenCVFunctions {

    public Mat image = Imgcodecs.imread("/Images/1.jpg");

    public Mat imgToCompare =null;

    public FeatureDetector fast = FeatureDetector.create(FeatureDetector.FAST);
    MatOfKeyPoint matOfKeyPoint;


    MatOfKeyPoint matOfkeyPointsFromStream;
    Features2d f2d = new Features2d();


    MatOfKeyPoint points = new MatOfKeyPoint();

    public void loadImageFromFile() {


        //imgToCompare = Imgcodecs.imread("/Images/1.jpg");

       // fd.detect(imgToCompare,matOfKeyPoint);

        //matOfkeyPoints = new MatOfKeyPoint(imgToCompare);

    }
}

package org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;
import org.tensorflow.lite.examples.detection.env.Logger;

import java.text.DecimalFormat;
// class for Redline detection
public class RedlineDetection {
    private static final Logger LOGGER = new Logger();
    private final double THRESHOLD  = 2.0;
    private final int REDLINELEFT = 0;
    private final int REDLINERIGHT = 1;
    private final int REDLINENOTFOUND = 2;
    DecimalFormat df = new DecimalFormat("#.##");
    Scalar scalarLow, scalarHigh;
    Mat mat1, mat2;
    RobotNavigator robotNavigator;
    public RedlineDetection(RobotNavigator navigator) {
        this.robotNavigator = navigator;
        OpenCVLoader.initDebug();
        // set red range
        this.scalarLow = new Scalar(0, 200, 100);
        this.scalarHigh = new Scalar(5, 255, 255);
        this.mat1 = new Mat(640,480, CvType.CV_16UC4);
        this.mat2 = new Mat(640,480, CvType.CV_16UC4);
    }
    public Bitmap processImage(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        // Convert bitmap to mat
        Utils.bitmapToMat(bmp32, mat);
        // convert rgb to hsv color space for openCV inRange function
        Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2HSV);
        // every pixel which matches the defined range will be white the rest is black
        Core.inRange(mat1, scalarLow, scalarHigh, mat2);
        //rotate by 90 degrees
        Core.rotate(mat2, mat2, 0);
        // convert back to bitmap for ImageView
        Bitmap result = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, result);
        // check for white pixels
        checkRegions(mat2);
        return result;
    }
    private void checkRegions(Mat mat) {
        // create two small regions
        Rect regionLeft = new Rect(0,440,240,200);
        Rect regionRight = new Rect(240,440,240,200);
        // create two mat of the regions
        Mat matLeft = new Mat(mat, regionLeft);
        Mat matRight = new Mat(mat, regionRight);
        // countNonZero returns all pixels which are not black
        int pixelsLeft = Core.countNonZero(matLeft);
        int pixelsRight = Core.countNonZero(matRight);
        // calc percentages
        double leftPercentage = ((double) pixelsLeft) / 48000 * 100;
        double rightPercentage = ((double) pixelsRight) / 48000 * 100;
        // navigate
        if(leftPercentage > THRESHOLD && leftPercentage > rightPercentage) {
            LOGGER.i("Redline found RIGHT.");
            robotNavigator.rotateRight();
            robotNavigator.forward();
        }
        if(rightPercentage > THRESHOLD && rightPercentage > leftPercentage) {
            LOGGER.i("Redline found LEFT.");
            robotNavigator.rotateLeft();
            robotNavigator.forward();
        }
    }
}

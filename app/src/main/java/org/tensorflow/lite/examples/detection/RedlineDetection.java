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
    private DetectReader reader;
    DecimalFormat df = new DecimalFormat("#.##");
    Scalar scalarLow, scalarHigh;
    Mat mat1, mat2;
    RobotNavigator robotNavigator;
    public RedlineDetection(RobotNavigator navigator, DetectReader reader) {
        this.reader = reader;
        this.robotNavigator = navigator;
        OpenCVLoader.initDebug();
        // set red range
        this.scalarLow = new Scalar(0, 70, 100);
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
        // convert back to bnitmap for ImageView
        Bitmap result = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, result);
        // check for white pixels
        checkRegions(mat2);
        return result;
    }
    // return true when the camera detected red a the bottom of the screen.
    public boolean checkRed(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2HSV);
        Core.inRange(mat1, scalarLow, scalarHigh, mat2);
        Core.rotate(mat2, mat2, 0);
        Bitmap result = Bitmap.createBitmap(mat2.width(), mat2.height(), Bitmap.Config.ARGB_8888);
        return checkFullwidth(mat2);
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

            this.reader.STRAT = this.reader.AVOID;
            this.robotNavigator.rotateRightEndless();
            LOGGER.i("Redline found RIGHT.");
            this.reader.stopScanRoutine();
        }
        if(rightPercentage > THRESHOLD && rightPercentage > leftPercentage) {
            this.reader.STRAT = this.reader.AVOID;
            this.robotNavigator.rotateLeftEndless();
            LOGGER.i("Redline found LEFT.");
            this.reader.stopScanRoutine();

        }
    }
    private boolean checkFullwidth(Mat mat) {
        // create two small regions
        Rect region = new Rect(0,440,480,200);
        // create two mat of the regions
        Mat matm = new Mat(mat, region);
        // countNonZero returns all pixels which are not black
        int pixels = Core.countNonZero(matm);
        // calc percentages
        double Percentage = ((double) pixels) / 96000 * 100;
        return Percentage > THRESHOLD ? true : false;
    }
}

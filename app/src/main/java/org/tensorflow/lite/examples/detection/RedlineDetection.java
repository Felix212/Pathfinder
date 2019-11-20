package org.tensorflow.lite.examples.detection;

import android.graphics.Bitmap;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.Scalar;
import org.opencv.imgproc.Imgproc;

public class RedlineDetection {
    Scalar scalarLow, scalarHigh;
    Mat mat1, mat2;
    public RedlineDetection() {
        OpenCVLoader.initDebug();
        this.scalarLow = new Scalar(0, 100, 100);
        this.scalarHigh = new Scalar(10, 255, 255);
        this.mat1 = new Mat(640,480, CvType.CV_16UC4);
        this.mat2 = new Mat(640,480, CvType.CV_16UC4);
    }
    public Bitmap processImage(Bitmap bitmap) {
        Mat mat = new Mat();
        Bitmap bmp32 = bitmap.copy(Bitmap.Config.ARGB_8888, true);
        Utils.bitmapToMat(bmp32, mat);
        Imgproc.cvtColor(mat, mat1, Imgproc.COLOR_RGB2HSV);
        Core.inRange(mat1, scalarLow, scalarHigh, mat2);
        Bitmap result = Bitmap.createBitmap(640, 480, Bitmap.Config.ARGB_8888);
        Utils.matToBitmap(mat2, result);
        return result;
    }
}

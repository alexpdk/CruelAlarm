package com.weiaett.cruelalarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.Scalar;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.imgproc.Imgproc;

public class ImageComparator {

    private static String TEST = "Test2";

    private Mat loadImage(Context ctx, int drawableID){

        BitmapFactory.Options o = new BitmapFactory.Options();
        // turn off "automatic screen density scaling", which turns n-channeled image
        // to one-channeled n times larger
        o.inScaled = false;

        Bitmap bmp= BitmapFactory.decodeResource(ctx.getResources(), drawableID, o);
        Log.d(TEST, String.format("w=%d h=%d",bmp.getWidth(), bmp.getHeight()));
        // Height, then width, because measure units are rows/columns
        // You can specify CV_8UC3, it will be CV_8UC4 all the same
        Mat mat = new Mat (bmp.getHeight(), bmp.getWidth(), CvType.CV_8UC4);
        Utils.bitmapToMat(bmp, mat);

        // Convert 4-channeled to 3-channeled
        Mat matC3 = new Mat();
        Imgproc.cvtColor(mat, matC3, Imgproc.COLOR_BGRA2BGR);
        return matC3;
    }

    public Bitmap matchImages(Context ctx, int id1, int id2){

        Mat s1 = loadImage(ctx, id1);
        Mat s2 = loadImage(ctx, id2);

        MatOfKeyPoint p1 = new MatOfKeyPoint(), p2 = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        detector.detect(s1, p1);
        detector.detect(s2, p2);

        // image descriptors
        Mat d1 = new Mat(), d2 = new Mat();

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.compute(s1, p1, d1);
        extractor.compute(s2, p2, d2);

        MatOfDMatch matches = new MatOfDMatch();
        // TODO find out, method should have specified barrier size
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMING);
        matcher.match(d1, d2, matches);

        //output image
        Mat mm = new Mat(); //s1.clone();
        //feature and connection colors
        Scalar RED = new Scalar(255,0,0);
        Scalar GREEN = new Scalar(0,200,0);
        // empty mask = draw all found matches
        MatOfByte drawnMatches = new MatOfByte();
        Features2d.drawMatches(s1, p1, s2, p2, matches, mm);
        //GREEN, RED, drawnMatches, Features2d.DRAW_RICH_KEYPOINTS); // ENABLE to view keypoint areas

//        Magic OpenCV multiplication)
//        Log.d(TEST, String.format("%d",CvType.CV_8UC1)); - 0
//        Log.d(TEST, String.format("%d",CvType.CV_8UC2)); - 8
//        Log.d(TEST, String.format("%d",CvType.CV_8UC3)); - 16
//        Log.d(TEST, String.format("%d",CvType.CV_8UC4)); - 24
//        Log.d(TEST, "***");

        Bitmap bm = Bitmap.createBitmap(mm.cols(), mm.rows(), Bitmap.Config.RGB_565);
        Utils.matToBitmap(mm, bm);
        return bm;
    }
}
/* Snippet:
 * Core.hconcat/vconcat (Arrays.asList(s1, s2), matchMap) - matrix concatenation
 */

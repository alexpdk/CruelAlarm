package com.weiaett.cruelalarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.text.TextUtils;
import android.util.Log;

import org.opencv.android.Utils;
import org.opencv.calib3d.Calib3d;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
import org.opencv.core.MatOfPoint2f;
import org.opencv.core.Point;
import org.opencv.core.Scalar;
import org.opencv.engine.OpenCVEngineInterface;
import org.opencv.features2d.DMatch;
import org.opencv.features2d.DescriptorExtractor;
import org.opencv.features2d.DescriptorMatcher;
import org.opencv.features2d.FeatureDetector;
import org.opencv.features2d.Features2d;
import org.opencv.features2d.KeyPoint;
import org.opencv.imgproc.Imgproc;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ImageComparator {

    private static String TEST = "Test2";
    private static String MATCH = "Match";

    private Mat img1, img2;
    private MatOfKeyPoint kPoints1, kPoints2;

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

    private MatOfDMatch selectGoodMatches(ArrayList<MatOfDMatch> mat){
        //Log.d(MATCH, String.format("Dims=%d rows=%d cols=%d total=%d", mat.dims(), mat.rows(), mat.cols(), mat.total()));

        DMatch[][] matches = new DMatch[mat.size()][];
        for(int i=0; i<matches.length; i++){
            matches[i] = mat.get(i).toArray();
        }
        int PairNum = matches.length;
        int OptionNum = matches[0].length;

        KeyPoint[] keypoints1 = kPoints1.toArray();
        KeyPoint[] keypoints2 = kPoints2.toArray();

        Log.d(MATCH, String.format("Matches options=%d pairs=%d", OptionNum, PairNum));

        // Find min/max for the row of best pairs
        float min = matches[0][0].distance, max = matches[0][0].distance;
        for (DMatch[] options : matches) {
            min = Math.min(options[0].distance, min);
            max = Math.max(options[0].distance, max);
        }
        Log.d(MATCH, String.format("For best pair row min=%f max=%f", min, max));

        ArrayList<DMatch> good = new ArrayList<>();
        final double MATCH_AREA = 100.0;

        int counter = 0;
        double sX = 0;
        double sY = 0;
        for(int i=0; i<PairNum; i++){
            if(matches[i][1].distance - matches[i][0].distance > 3){
                counter++;
                Point from = keypoints1[matches[i][0].queryIdx].pt;
                Point to = keypoints2[matches[i][0].trainIdx].pt;
                sX += from.x - to.x;
                sY += from.y - to.y;
            }
        }
        sX = sX / counter;
        sY = sY / counter;
        Log.d(MATCH, String.format("sX=%f sY=%f\n", sX, sY));


        int counter2 = 0;
        double sX2 = 0, sY2 = 0;
        for (int i = 0; i < PairNum; i++) {
            StringBuilder b = new StringBuilder();
            StringBuilder b1 = new StringBuilder();
            boolean found = false;

            for(int j = 0; j < OptionNum; j++){
                DMatch match = matches[i][j];
                //b.append(match.distance);
                //b.append(' ');
                Point from = keypoints1[match.queryIdx].pt;
                Point to = keypoints2[match.trainIdx].pt;

                double dist = Math.hypot(from.x - to.x, from.y - to.y);
                b.append(" dx=");
                b.append(from.x-to.x);
                b1.append(" dy=");
                b1.append(from.y-to.y);

                if(dist < MATCH_AREA){
                    if(j+1<OptionNum){
                        b.append(String.format(" d1=%f d2=%f\n", match.distance, matches[i][j+1].distance));
                        if(matches[i][j+1].distance - match.distance > 3){
                            counter2++;
                            sX2 += from.x - to.x;
                            sY2 += from.y - to.y;
                        }
                    }
                    good.add(match);
                    found = true;
                    break; // search best pair for the next keypoint
                }
            }
            if(found) Log.d(MATCH, String.format("%s\n%s\n", b.toString(), b1.toString()));
        }
        Log.d(MATCH, String.format("Good matches number=%d", good.size()));
        sX2 = sX2 / counter2;
        sY2 = sY2 / counter2;
        Log.d(MATCH, String.format("sX2=%f sY2=%f\n", sX2, sY2));

        MatOfDMatch res = new MatOfDMatch();
        DMatch[] oneMoreTMP = new DMatch[good.size()];
        res.fromArray(good.toArray(oneMoreTMP));
        return res;
    }

    private ArrayList<DMatch> ApplyRANSAC(ArrayList<DMatch> matches, MatOfKeyPoint k1,MatOfKeyPoint k2){
        KeyPoint[] keypoints1 = k1.toArray();
        KeyPoint[] keypoints2 = k2.toArray();

        ArrayList<Point> points1 = new ArrayList<>();
        ArrayList<Point> points2 = new ArrayList<>();

        for(DMatch match : matches) {
            Point from = keypoints1[match.queryIdx].pt;
            points1.add(from);
            Point to = keypoints2[match.trainIdx].pt;
            points2.add(to);
        }
        MatOfPoint2f mat1 = new MatOfPoint2f(), mat2 = new MatOfPoint2f();
        Point[] tmpPointArr = new Point[matches.size()];
        mat1.fromArray(points1.toArray(tmpPointArr)); mat2.fromArray(points2.toArray(tmpPointArr));

        final int RANSAC_THRESHOLD = 30;
        Mat mask = new Mat();
        Log.d(MATCH, String.format("size1=%d size2=%d",mat1.rows(),mat2.rows()));
        Calib3d.findHomography(mat1, mat2, Calib3d.RANSAC, RANSAC_THRESHOLD, mask);
        Log.d(MATCH, String.format("mask rows=%d cols=%s vals=%d val1=%f",mask.rows(),mask.cols(),
                mask.get(0,0).length, mask.get(0,0)[0]));

        ArrayList<DMatch> passed = new ArrayList<>();

        StringBuilder b = new StringBuilder();
        for(int i = 0; i < matches.size(); i++) {
            if(Math.abs(mask.get(i,0)[0]) > 0.000001) passed.add(matches.get(i));
        }
        return passed;
    }

    private MatOfDMatch selectBestMatches(MatOfDMatch mat, MatOfKeyPoint k1,MatOfKeyPoint k2){
        //Log.d(MATCH, String.format("Dims=%d rows=%d cols=%d total=%d", mat.dims(), mat.rows(), mat.cols(), mat.total()));
        DMatch[] matches = mat.toArray();

        Log.d(MATCH, String.format("Matches num=%d", matches.length));

        // Match distance is not distance between keypoints on matched images,
        // but measure of similarity. Keypoint is binary(uchar), so we can use HammingMeasure
        // (BruteForce hamming) || some Flann measure

        float min = matches[0].distance, max = matches[0].distance;
        for (DMatch match : matches) {
            min = Math.min(match.distance, min);
            max = Math.max(match.distance, max);
        }
        // minSDK 17 - streams not supported
        // DMatch[] good = Arrays.stream(matches).filter(m -> m.distance<3*min).toArray();
        ArrayList<DMatch> good = new ArrayList<>();

        StringBuilder b = new StringBuilder();
        for(DMatch match : matches) {
            b.append(match.distance);
            b.append(' ');
            if (match.distance <= 3 * min) good.add(match);
        }

        good = ApplyRANSAC(good, k1, k2);

        Log.d(MATCH, String.format("Distances=%s", b.toString()));
        Log.d(MATCH, String.format("Min=%f max=%f", min, max));
        Log.d(MATCH, String.format("Good matches number=%d", good.size()));
        MatOfDMatch res = new MatOfDMatch();
        DMatch[] oneMoreTMP = new DMatch[good.size()];
        res.fromArray(good.toArray(oneMoreTMP));
        return res;
    }

    public void detectWithMSER(Mat img, MatOfKeyPoint kpoints, FeatureDetector mser){
        Mat grayscale = img.clone();
        Imgproc.cvtColor(img, grayscale, Imgproc.COLOR_BGR2GRAY);
        mser.detect(grayscale, kpoints);
    }


    public Bitmap matchImages(Context ctx, int id1, int id2){

        img1 = loadImage(ctx, id1);
        img2 = loadImage(ctx, id2);

//        StringBuilder s = new StringBuilder();
//        for(double d : s1.get(0,0)){
//            s.append(d);
//            s.append(" ");
//        }
//        Log.d(MATCH, s.toString());

        kPoints1 = new MatOfKeyPoint(); kPoints2 = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.MSER);
        detectWithMSER(img1, kPoints1, detector);
        detectWithMSER(img2, kPoints2, detector);
        Log.d(MATCH, String.format("Keypoint num=%d %d", kPoints1.rows()*kPoints1.cols(),
                kPoints2.rows()*kPoints2.cols()));

        // image descriptors
        Mat d1 = new Mat(), d2 = new Mat();

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.compute(img1, kPoints1, d1);
        extractor.compute(img2, kPoints2, d2);

        //MatOfDMatch matches = new MatOfDMatch();
        ArrayList<MatOfDMatch> matches = new ArrayList<>();
        // _TODO find out, method should have specified barrier size
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        //matcher.match(d1, d2, matches);
        matcher.knnMatch(d1, d2, matches, 10);


        MatOfDMatch goodMatches = selectGoodMatches(matches);
        //MatOfDMatch goodMatches = selectBestMatches(matches, k1, k2);

        //output image
        Mat mm = new Mat(); //s1.clone();
        //feature and connection colors
        Scalar RED = new Scalar(255,0,0);
        Scalar GREEN = new Scalar(0,200,0);
        // empty mask = draw all found matches
        MatOfByte drawnMatches = new MatOfByte();
        Features2d.drawMatches(img1, kPoints1, img2, kPoints2, goodMatches, mm);

        //GREEN, RED, drawnMatches, Features2d.DRAW_RICH_KEYPOINTS + Features2d.NOT_DRAW_SINGLE_POINTS); // ENABLE to view keypoint areas

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

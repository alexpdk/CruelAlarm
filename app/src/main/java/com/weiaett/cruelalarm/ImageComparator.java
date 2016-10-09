package com.weiaett.cruelalarm;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.opencv.android.OpenCVLoader;
import org.opencv.android.Utils;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfByte;
import org.opencv.core.MatOfDMatch;
import org.opencv.core.MatOfKeyPoint;
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

    private MatOfDMatch selectGoodMatches(ArrayList<MatOfDMatch> mat, MatOfKeyPoint k1,MatOfKeyPoint k2){
        //Log.d(MATCH, String.format("Dims=%d rows=%d cols=%d total=%d", mat.dims(), mat.rows(), mat.cols(), mat.total()));

        DMatch[][] matches = new DMatch[mat.size()][];
        for(int i=0; i<matches.length; i++){
            matches[i] = mat.get(i).toArray();
        }
        int PairNum = matches.length;
        int OptionNum = matches[0].length;

        KeyPoint[] keypoints1 = k1.toArray();
        KeyPoint[] keypoints2 = k2.toArray();

        Log.d(MATCH, String.format("Matches options=%d pairs=%d", OptionNum, PairNum));

        // Find min/max for the row of best pairs
        float min = matches[0][0].distance, max = matches[0][0].distance;
        for (DMatch[] options : matches) {
            min = Math.min(options[0].distance, min);
            max = Math.max(options[0].distance, max);
        }
        Log.d(MATCH, String.format("For best pair row min=%f max=%f", min, max));

        ArrayList<DMatch> good = new ArrayList<>();
        final double MATCH_AREA = 30.0;

        for (int i = 0; i < PairNum; i++) {
            StringBuilder b = new StringBuilder();

            for(int j = 0; j < OptionNum; j++){
                DMatch match = matches[i][j];
                b.append(match.distance);
                b.append(' ');
                Point from = keypoints1[match.queryIdx].pt;
                Point to = keypoints2[match.trainIdx].pt;
                double dist = Math.hypot(from.x - to.x, from.y - to.y);
                if(dist < MATCH_AREA){
                    good.add(match);
                    break; // search best pair for the next keypoint
                }
            }
            Log.d(MATCH, String.format("Distances=%s", b.toString()));
        }
        Log.d(MATCH, String.format("Good matches number=%d", good.size()));

        MatOfDMatch res = new MatOfDMatch();
        DMatch[] oneMoreTMP = new DMatch[good.size()];
        res.fromArray(good.toArray(oneMoreTMP));
        return res;
    }

    private MatOfDMatch selectGoodMatches(MatOfDMatch mat){
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
            if (match.distance < 3 * min) good.add(match);
        }

        Log.d(MATCH, String.format("Distances=%s", b.toString()));
        Log.d(MATCH, String.format("Min=%f max=%f", min, max));
        Log.d(MATCH, String.format("Good matches number=%d", good.size()));
        MatOfDMatch res = new MatOfDMatch();
        DMatch[] oneMoreTMP = new DMatch[good.size()];
        res.fromArray(good.toArray(oneMoreTMP));
        return res;
    }

    public Bitmap matchImages(Context ctx, int id1, int id2){

        Mat s1 = loadImage(ctx, id1);
        Mat s2 = loadImage(ctx, id2);

        MatOfKeyPoint p1 = new MatOfKeyPoint(), p2 = new MatOfKeyPoint();
        FeatureDetector detector = FeatureDetector.create(FeatureDetector.ORB);
        detector.detect(s1, p1);
        detector.detect(s2, p2);
        Log.d(MATCH, String.format("Keypoint num=%d %d", p1.rows()*p1.cols(), p2.rows()*p2.cols()));

        // image descriptors
        Mat d1 = new Mat(), d2 = new Mat();

        DescriptorExtractor extractor = DescriptorExtractor.create(DescriptorExtractor.ORB);
        extractor.compute(s1, p1, d1);
        extractor.compute(s2, p2, d2);

        //MatOfDMatch matches = new MatOfDMatch();
        ArrayList<MatOfDMatch> matches = new ArrayList<>();
        // TODO find out, method should have specified barrier size
        DescriptorMatcher matcher = DescriptorMatcher.create(DescriptorMatcher.BRUTEFORCE_HAMMINGLUT);
        //matcher.match(d1, d2, matches);
        matcher.knnMatch(d1, d2, matches, 10);

        MatOfDMatch goodMatches = selectGoodMatches(matches, p1, p2);

        //output image
        Mat mm = new Mat(); //s1.clone();
        //feature and connection colors
        Scalar RED = new Scalar(255,0,0);
        Scalar GREEN = new Scalar(0,200,0);
        // empty mask = draw all found matches
        // MatOfByte drawnMatches = new MatOfByte();
        Features2d.drawMatches(s1, p1, s2, p2, goodMatches, mm);

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

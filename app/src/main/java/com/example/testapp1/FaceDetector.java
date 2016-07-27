package com.example.testapp1;

/**
 * Created by leozha on 7/25/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.util.SparseArray;

import com.google.android.gms.vision.face.Face;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs; // imread, imwrite, etc
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

public class FaceDetector {

    public CascadeClassifier LoadClassifier(){
        String faceCascadeName = "haarcascade_frontalface_alt.xml";
        CascadeClassifier faceDetector = new CascadeClassifier();
        faceDetector.load( faceCascadeName );

        return faceDetector;
    }

    public Mat LoadImage(String imageName){
        Mat image = Imgcodecs.imread(imageName);

        return image;
    }

    public void SaveImage(String resultImgName, Mat image){
        Imgcodecs.imwrite(resultImgName, image);
    }

    // test whether 2 rectange
    public boolean IsOverlapping(Rect rect1, Rect rect2){
        return (!rect1.contains(new Point(rect2.x, rect2.y)) // top left
        && !rect1.contains(new Point(rect2.x + rect2.width, rect2.y)) // top right
        && !rect1.contains(new Point(rect2.x, rect2.y + rect2.height)) // bottom left
        && !rect1.contains(new Point(rect2.x + rect2.width, rect2.y + rect2.height))); // bottom right
    }

    // we want to put a text box (width * height) outside the inputRect in the following positions
    // depends on where it fits
    //           \      |      /
    //            \    _|__   /
    //            --   |  |   --
    //                 ---
    //
    //
    public Rect LocateTextBox(Rect inputRect, int width, int height, int imageWidth){
        Rect newRect = new Rect();
        newRect.width = width;
        newRect.height = height / 2;

        // top left
        if (inputRect.x - width >= 0 && inputRect.y - height >=0){
            newRect.x = inputRect.x - width;
            newRect.y = inputRect.y - height;
        }
        // top right
        else if (inputRect.x + inputRect.width + width <= imageWidth
                && inputRect.y - height >= 0) {
            newRect.x = inputRect.x + inputRect.width;
            newRect.y = inputRect.y - height;
        }
        // top
        else if (inputRect.y - height >= 0){
            newRect.x = inputRect.x;
            newRect.y = inputRect.y - height;
        }
        // left
        else if (inputRect.x - width >= 0){
            newRect.x = inputRect.x - width;
            newRect.y = inputRect.y;
        }
        // right
        else if (inputRect.x + inputRect.width + width <= imageWidth){
            newRect.x = inputRect.x + inputRect.width;
            newRect.y = inputRect.y;
        }
        else
        {
            newRect.x = -1;
            newRect.y = -1;
        }

        return newRect;
    }

    public class RectComparator implements Comparator<Rect>
    {
        @Override
        public int compare(Rect lhs, Rect rhs) {
            float sizeLhs = lhs.width * lhs.height;
            float sizeRhs = rhs.width * rhs.height;
            if (sizeLhs > sizeRhs)
                return -1;
            if (sizeRhs > sizeLhs)
                return -1;
            return 0;
        }
    }

    class FaceInformation
    {
        public Rect rect;
        public Boolean Happy = false;
    }

    public FaceInformation FindBestRect(Rect[] rects, float[] probArray){

        FaceInformation fi = new FaceInformation();
        List<Rect> listRects = Arrays.asList(rects);
        Collections.sort(listRects, new RectComparator());
        if (probArray == null || probArray.length != rects.length)
        {
            fi.rect = listRects.get(0);
            return fi;
        }

        int compareSize = listRects.size();
        if (listRects.size() > 3)
            compareSize = 3;
        int best = 0;
        float minProb = 1;
        boolean happy = false;
        for (int index = 0; index < rects.length; index++){
            float fromZero = Math.abs(probArray[index] - 0);
            float fromOne = Math.abs(probArray[index] - 1);
            float minProbLocal = Math.min(fromOne, fromZero);
            if (minProbLocal <= minProb)
            {
                boolean found = false;
                for (int i = 0; i < compareSize; i++)
                {
                    if (listRects.get(i).br().x == rects[index].br().x && listRects.get(i).br().y == rects[index].br().y)
                    {
                        found = true;
                        break;
                    }
                }
                if (found)
                {
                    best = index;
                    if (fromOne < fromZero)
                        happy = true;
                    else
                        happy = false;
                }
            }
        }

        fi.rect = rects[best];
        fi.Happy = happy;
        return fi;
    }

    public Bitmap ProcessRects(Rect[] rects, Mat image, Bitmap inputPic, float[] probArray)
    {
        FaceInformation fi = FindBestRect(rects, probArray);
        Rect bestRect = fi.rect;
        String toShow = "Shit On You!!!";
        if (fi.Happy)
            toShow = "Candy on You!!!";

//            Imgproc.rectangle(image, new Point(largestRect.x, largestRect.y), new Point(largestRect.x + largestRect.width, largestRect.y + largestRect.height),
//                    new Scalar(255, 0, 0));

        Rect newRect = LocateTextBox(bestRect, bestRect.width, bestRect.height, inputPic.getWidth());
        if (newRect.x != -1 && newRect.y != -1){
            Point topLeft = new Point(newRect.x, newRect.y);
            Point bottomRight = new Point(newRect.x + newRect.width, newRect.y + newRect.height);
            Point face = new Point(bestRect.x, bestRect.y);
            DrawThoughtBubble(image, topLeft, bottomRight, face, toShow);
//                Imgproc.rectangle(image, new Point(newRect.x, newRect.y), new Point(newRect.x + newRect.width, newRect.y + newRect.height),
//                        new Scalar(0, 255, 0));
        }
//            for (Rect rect : faceDetections.toArray()) {
//                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                        new Scalar(255, 0, 0));
//
//                Rect newRect = LocateTextBox(rect, rect.width, rect.height, inputPic.getWidth());
//                if (newRect.x != -1 && newRect.y != -1){
//                    Imgproc.rectangle(image, new Point(newRect.x, newRect.y), new Point(newRect.x + newRect.width, newRect.y + newRect.height),
//                            new Scalar(0, 255, 0));
//                }
//            }

        Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
        Bitmap outputPic = Bitmap.createBitmap(inputPic.getWidth(), inputPic.getHeight(), conf);

        Utils.matToBitmap(image, outputPic);

        return outputPic;
    }

    public Bitmap DetectFace(Bitmap inputPic, Context context, SparseArray<Face> mFaces){

        try {
            Mat image = new Mat (inputPic.getWidth(), inputPic.getHeight(), CvType.CV_8UC3);
            Utils.bitmapToMat(inputPic, image);

            if (mFaces.size() != 0)
            {
                float[] probArray = new float[mFaces.size()];
                Rect[] rects = new Rect[mFaces.size()];
                for (int i = 0; i < mFaces.size(); ++i)
                {
                    int key = mFaces.keyAt(i);
                    rects[i] = new Rect();
                    rects[i].height = (int) mFaces.get(key).getHeight();
                    rects[i].width = (int) mFaces.get(key).getWidth();
                    rects[i].x = (int) mFaces.get(key).getPosition().x;
                    rects[i].y = (int) mFaces.get(key).getPosition().y;
                    probArray[i] = mFaces.get(key).getIsSmilingProbability();
                }

                return ProcessRects(rects, image, inputPic, probArray);
            }

            InputStream is = context.getResources().openRawResource(R.raw.leofacedet);
            File cascadeDir = context.getDir("cascade", Context.MODE_PRIVATE);
            File mCascadeFile = new File(cascadeDir, "leofacedet.xml");
            FileOutputStream os = new FileOutputStream(mCascadeFile);

            byte[] buffer = new byte[4096];
            int bytesRead;
            while ((bytesRead = is.read(buffer)) != -1) {
                os.write(buffer, 0, bytesRead);
            }
            is.close();
            os.close();
            CascadeClassifier mCascadeER = new CascadeClassifier(mCascadeFile.getAbsolutePath());
            if (mCascadeER.empty()) {
                String sourceImgName = "faces1.png";
                String resultImgName = "result_image.png";
            }

            Boolean isEmpty = image.empty();

            MatOfRect faceDetections = new MatOfRect();
            mCascadeER.detectMultiScale(image, faceDetections);

            // drawing a rectangle on the face
            // TODO: we need to draw bubble and text instead
            Rect[] rects = faceDetections.toArray();

            return ProcessRects(rects, image, inputPic, null);

        } catch (Exception e) {
            // do nothing
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap outputPic = Bitmap.createBitmap(1, 1, conf);
            return outputPic;
        }
    }

    /* Draws thought bubble onto target Mat
  * target is the Mat to be drawn on
  * p1 and p2 define the size and location of the thought bubble. (opposite corners)
  * targetPoint is are the coordinates the thought bubble points to
  * text is the text to be written in the thought bubble
  */
    public static void DrawThoughtBubble(Mat target, Point p1, Point p2, Point targetPoint, String text)
    {
        Scalar bubbleColor = new Scalar(255,255,153, 255);
        Scalar textColor = new Scalar(32,32,32, 255);
        double textSize = 0.3;

        int minPuffSize = 10;
        int maxPuffSize = 20;
        int overlap = 5; //pixels to overlap on each side of a puff

        int chainStartSize = 5;
        double chainSpacing = 1.5; //spacing of bubble chain relative to size of bubbles
        double chainGrowRate = 0.2;

        double top = Math.min(p1.y, p2.y);
        double left = Math.min(p1.x, p2.x);
        double width = Math.abs(p1.x - p2.x);
        double height = Math.abs(p1.y - p2.y);

        // draw rectangle
        Imgproc.rectangle(target, p1, p2, bubbleColor, -1 /*negative thickness means filled*/);
        // TODO draw bubble cloud border
        double i;
        // top
        for (i=left; i<left+width; )
        {
            int puffSize = ThreadLocalRandom.current().nextInt(minPuffSize, maxPuffSize);
            if (i+puffSize*2 > left+width)
                puffSize = (int) Math.max((top+height-i+1)/2, minPuffSize);
            Point center = new Point(i+puffSize-overlap, top);
            Imgproc.circle(target, center, puffSize, bubbleColor, -1 /*negative thickness -> filled*/);
            i += (puffSize-overlap)*2;
        }
        // bottom
        for (i=left; i<left+width; )
        {
            int puffSize = ThreadLocalRandom.current().nextInt(minPuffSize, maxPuffSize);
            if (i+puffSize*2 > left+width)
                puffSize = (int) Math.max((top+height-i+1)/2, minPuffSize);
            Point center = new Point(i+puffSize-overlap, top+height);
            Imgproc.circle(target, center, puffSize, bubbleColor, -1 /*negative thickness -> filled*/);
            i += (puffSize-overlap)*2;
        }
        // left //TODO left/right overlap factor
        for (i=top; i<top+height; )
        {
            int puffSize = ThreadLocalRandom.current().nextInt(minPuffSize, maxPuffSize);
            if (i+puffSize*2 > top+height)
                puffSize = (int) Math.max((top+height-i+1)/2, minPuffSize);
            Point center = new Point(left, i+puffSize-overlap);
            Imgproc.circle(target, center, puffSize, bubbleColor, -1 /*negative thickness -> filled*/);
            i += (puffSize-overlap)*2;
        }
        // right
        for (i=top; i<top+height; )
        {
            int puffSize = ThreadLocalRandom.current().nextInt(minPuffSize, maxPuffSize);
            if (i+puffSize*2 > top+height)
                puffSize = (int) Math.max((top+height-i+1)/2, minPuffSize);
            Point center = new Point(left+width, i+puffSize-overlap);
            Imgproc.circle(target, center, puffSize, bubbleColor, -1 /*negative thickness -> filled*/);
            i += (puffSize-overlap)*2;
        }

        // draw bubble chain to target (from target point to thought bubble)
        Point end = new Point(left + width/2, top + height/2);
        double length = Math.sqrt(Math.pow(end.x - targetPoint.x, 2) + Math.pow(end.y - targetPoint.y, 2));
        // length 1 vector pointing in direction of thought bubble
        Point unitVector = new Point((end.x-targetPoint.x)/length, (end.y-targetPoint.y)/length);

        Point cur = targetPoint;
        int size = chainStartSize;
        double distance = 0;
        while (cur.x<left || cur.y<top || cur.x>(left+width) || cur.y>(top+height) && distance<length)
        {
            //draw
            cur.x = cur.x + unitVector.x*size;
            cur.y = cur.y + unitVector.y*size;
            Imgproc.circle(target, cur, size, bubbleColor, -1 /*negative thickness -> filled*/);
            //move cur
            cur.x += unitVector.x*size*chainSpacing;
            cur.y += unitVector.y*size*chainSpacing;
            distance += size + size*chainSpacing;
            //update size
            size = (int) Math.min(size + size*2.*chainGrowRate, maxPuffSize);
        }

        // draw bubble text
        // TODO code up some word wrap?
        Size textBoxSize = Imgproc.getTextSize(text, Core.FONT_HERSHEY_SIMPLEX, textSize, 1 /* thickness */, null);
        Imgproc.putText(target, text, new Point(left, top+textBoxSize.height),
                Core.FONT_HERSHEY_SIMPLEX, textSize, textColor);
    }


    public static void main(){



    }
}

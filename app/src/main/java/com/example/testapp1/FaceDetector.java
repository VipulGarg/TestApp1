package com.example.testapp1;

/**
 * Created by leozha on 7/25/2016.
 */

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs; // imread, imwrite, etc
import org.opencv.videoio.*;   // VideoCapture
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;

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

//    public Mat DetecteFace(String imageName, CascadeClassifier faceDetector){

        //Mat image = Imgcodecs.imread(imageName);
        //Mat image = Imgcodecs.imread(getResources().getDrawable(R.drawable.faces1).toString());
//        String faceCascadeName = "leofacedet.xml";
//        CascadeClassifier faceDetector = new CascadeClassifier();
//
//        Boolean result = faceDetector.load(faceCascadeName);

//        MatOfRect faceDetections = new MatOfRect();
//        faceDetector.detectMultiScale(image, faceDetections);
//
//        // drawing a rectangle on the face
//        // TODO: we need to draw bubble and text instead
//        for (Rect rect : faceDetections.toArray()) {
//            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
//                    new Scalar(0, 255, 0));
//        }

//        return image;
//    }

    public static void main(){

        try {
            InputStream is = getContext().getResources().openRawResource(R.raw.leofacedet);
            File cascadeDir = getContext().getDir("cascade", Context.MODE_PRIVATE);
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

            FaceDetector faceDetector = new FaceDetector();

            Bitmap bmp = BitmapFactory.decodeResource(getResources(), R.drawable.faces1);
            Mat image = new Mat (bmp.getWidth(), bmp.getHeight(), CvType.CV_8UC1);
            Utils.bitmapToMat(bmp, image);

            Boolean isEmpty = image.empty();

            MatOfRect faceDetections = new MatOfRect();
            mCascadeER.detectMultiScale(image, faceDetections);

            // drawing a rectangle on the face
            // TODO: we need to draw bubble and text instead
            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
            }

            Utils.matToBitmap(image, bmp);
           // view.setImageBitmap(bmp);
        } catch (Exception e) {
            // do nothing
        }

    }
}

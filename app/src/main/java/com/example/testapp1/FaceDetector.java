package com.example.testapp1;

/**
 * Created by leozha on 7/25/2016.
 */

import org.opencv.core.Core;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.imgcodecs.Imgcodecs; // imread, imwrite, etc
import org.opencv.videoio.*;   // VideoCapture
import org.opencv.objdetect.CascadeClassifier;
import org.opencv.imgproc.Imgproc;

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

    public Mat DetecteFace(String imageName){
        MatOfRect faceDetections = new MatOfRect();

        Mat image = LoadImage(imageName);
        CascadeClassifier faceDetector = LoadClassifier();

        faceDetector.detectMultiScale(image, faceDetections);

        // drawing a rectangle on the face
        // TODO: we need to draw bubble and text instead
        for (Rect rect : faceDetections.toArray()) {
            Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                    new Scalar(0, 255, 0));
        }

        return image;
    }

    public static void main(){
        //                    FaceDetector faceDetector = new FaceDetector();
//
//                            String sourceImgName = "faces1.png";
//                            String resultImgName = "result_image.png";
//
//                            Mat resultImage = faceDetector.DetecteFace(sourceImgName);
//                            faceDetector.SaveImage(resultImgName, resultImage);
    }
}

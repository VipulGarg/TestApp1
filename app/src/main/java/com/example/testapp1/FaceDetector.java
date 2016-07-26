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

    public Bitmap DetecteFace(Bitmap inputPic, Context context){

        try {
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

            //FaceDetector faceDetector = new FaceDetector();

//            Bitmap bmp = BitmapFactory.decodeResource(context.getResources(), R.drawable.faces1);
            Mat image = new Mat (inputPic.getWidth(), inputPic.getHeight(), CvType.CV_8UC1);
            Utils.bitmapToMat(inputPic, image);

            Boolean isEmpty = image.empty();

            MatOfRect faceDetections = new MatOfRect();
            mCascadeER.detectMultiScale(image, faceDetections);

            // drawing a rectangle on the face
            // TODO: we need to draw bubble and text instead
            for (Rect rect : faceDetections.toArray()) {
                Imgproc.rectangle(image, new Point(rect.x, rect.y), new Point(rect.x + rect.width, rect.y + rect.height),
                        new Scalar(0, 255, 0));
            }

            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap outputPic = Bitmap.createBitmap(inputPic.getWidth(), inputPic.getHeight(), conf);

            Utils.matToBitmap(image, outputPic);

            return outputPic;
            // view.setImageBitmap(bmp);
        } catch (Exception e) {
            // do nothing
            Bitmap.Config conf = Bitmap.Config.ARGB_8888; // see other conf types
            Bitmap outputPic = Bitmap.createBitmap(1, 1, conf);
            return outputPic;
        }
    }

    public static void main(){



    }
}

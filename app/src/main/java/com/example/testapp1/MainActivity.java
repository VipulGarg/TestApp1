package com.example.testapp1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.content.CursorLoader;
import android.support.v4.view.PagerAdapter;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.ImageView;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import org.opencv.android.Utils;
import org.opencv.core.Core;
import org.opencv.core.CvType;
import org.opencv.core.Mat;
import org.opencv.core.MatOfRect;
import org.opencv.core.Point;
import org.opencv.core.Rect;
import org.opencv.core.Scalar;
import org.opencv.core.Size;
import org.opencv.imgcodecs.Imgcodecs;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.ThreadLocalRandom;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link FragmentStatePagerAdapter}.
     */
    private SectionsPagerAdapter mSectionsPagerAdapter;

    /**
     * The {@link ViewPager} that will host the section contents.
     */
    private ViewPager mViewPager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        // load opencv library
        System.loadLibrary("opencv_java3");

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        // Create the adapter that will return a fragment for each of the three
        // primary sections of the activity.

        if (mSectionsPagerAdapter == null)
        {
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager(), this);
        }

        // Set up the ViewPager with the sections adapter.
        mViewPager = (ViewPager) findViewById(R.id.container);
        mViewPager.setAdapter(mSectionsPagerAdapter);

        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(mViewPager);

        /* FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();
            }
        });
        */

        System.loadLibrary("opencv_java3");
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /* Draws thought bubble onto target Mat
     * target is the Mat to be drawn on
     * p1 and p2 define the size and location of the thought bubble. (opposite corners)
     * targetPoint is are the coordinates the thought bubble points to
     * text is the text to be written in the thought bubble
     */
    public static void DrawThoughtBubble(Mat target, Point p1, Point p2, Point targetPoint, String text)
    {
        Scalar bubbleColor = new Scalar(255,255,255);
        Scalar textColor = new Scalar(0,0,0);
        double textSize = 0.4;

        int minPuffSize = 20;
        int maxPuffSize = 50;
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

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if (uri == null) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = {MediaStore.Images.Media.DATA};
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if (cursor != null) {
            int column_index = cursor
                    .getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        }
        // this is our fallback here
        return uri.getPath();
    }

    public String getRealPathFromURI(Context context, Uri contentUri) {
        Cursor cursor = null;
        try {
            String[] proj = {MediaStore.Images.Media.DATA};
            cursor = context.getContentResolver().query(contentUri, proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void SetImage() {
        findViewById(R.id.section_image);
    }


    /**
     * Base Class for Image Fragments
     */
    public static class BaseImageFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        protected static final String ARG_SECTION_NUMBER = "section_number";

        protected MainActivity mActivity;
        protected ImageView mImageView;
        protected Button mBtn;
        protected LinearLayout mLinearLayout;
        protected Bitmap mBitmap;
        protected FloatingActionButton mFabNew;
        protected FloatingActionButton mFabCancel;

        public BaseImageFragment() {
        }

        protected Bitmap GetBitmap() { return mBitmap; }
        protected void SetBitmap(Bitmap bitmap) { mBitmap = bitmap; }

        protected void SetBitmapOnImageView(Bitmap bitmap)
        {
            if (bitmap == null)
                return;

            mImageView.setImageBitmap(bitmap);
            mBitmap = bitmap;
            CleanLayout();
        }

        protected void CleanLayout()
        {
            mBtn.setVisibility(View.INVISIBLE);
            mLinearLayout.setBackgroundResource(0);

            mFabCancel.setVisibility(View.VISIBLE);
            mFabNew.setVisibility(View.VISIBLE);
        }


        protected void RunFaceDetectionAndUpdate(Bitmap inputPic)
        {
            FaceDetector faceDetector = new FaceDetector();
            Bitmap outputPic = faceDetector.DetecteFace(inputPic, getContext());

            SetBitmapOnImageView(outputPic);
        }


        @Override
        public void onAttach(Activity activity)
        {
            if (activity instanceof MainActivity)
            {
                mActivity = (MainActivity) activity;
            }
            super.onAttach(activity);
        }

        // this method is only called once for this fragment
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            // retain this fragment
            setRetainInstance(true);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            final View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            LinearLayout linearLayout = (LinearLayout) rootView.findViewById(R.id.section_ll);
            mLinearLayout = linearLayout;
            Button btn = (Button) rootView.findViewById(R.id.section_button);
            mBtn = btn;

            ImageView imageView = (ImageView) rootView.findViewById(R.id.section_image);
            mImageView = imageView;

            FloatingActionButton fabNew = (FloatingActionButton) rootView.findViewById(R.id.fab_new);
            mFabNew = fabNew;

            FloatingActionButton fabCancel = (FloatingActionButton) rootView.findViewById(R.id.fab_cancel);
            mFabCancel = fabCancel;
            mFabCancel.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    mBitmap = null;
                    mImageView.setImageResource(R.drawable.section_1);
                    mBtn.setVisibility(View.VISIBLE);
                    mFabCancel.setVisibility(View.INVISIBLE);
                    mFabNew.setVisibility(View.INVISIBLE);
                }
            });

            if (mBitmap == null)
            {
                mImageView.setImageResource(R.drawable.section_1);
                mFabCancel.setVisibility(View.INVISIBLE);
                mFabNew.setVisibility(View.INVISIBLE);
            }
            else
            {
                SetBitmapOnImageView(mBitmap);
            }

            return rootView;
        }
    }

    /**
     * Fragment for loading a picture
     */
    public static class LoadImageFragment extends BaseImageFragment {

        private static final String TAG = "TAG.LoadImageFragment";
        private static final int SELECT_PICTURE = 1;

        public LoadImageFragment() {
        }

        public void SetImageUri(Uri uri) {
            if (uri == null)
                return;

            try {
                mImageView.setImageDrawable(Drawable.createFromStream(mActivity.getContentResolver().openInputStream(uri), null));
            } catch (FileNotFoundException fe) {
                return;
            } catch (Exception e) {
                return;
            }
        }


        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static LoadImageFragment newInstance(int sectionNumber) {
            LoadImageFragment fragment = new LoadImageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = super.onCreateView(inflater, container, savedInstanceState);

            mBtn.setText("Load a Picture");
            mBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), SELECT_PICTURE);
                }

            });

            mFabNew.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent();
                    intent.setType("image/*");
                    intent.setAction(Intent.ACTION_GET_CONTENT);
                    startActivityForResult(Intent.createChooser(intent,
                            "Select Picture"), SELECT_PICTURE);
                }
            });

            final Builder alt_bld = new Builder(getActivity());
            alt_bld.setMessage("apprika target achieve...");
            alt_bld.setCancelable(true);

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (resultCode == RESULT_OK) {
                if (requestCode == SELECT_PICTURE) {
                    Uri selectedImageUri = data.getData();
                    try
                    {
                        Bitmap bitmap = MediaStore.Images.Media.getBitmap(mActivity.getContentResolver(), selectedImageUri);
                        RunFaceDetectionAndUpdate(bitmap);
                    }
                    catch (Exception e)
                    {
                        return;
                    }
                }
            }
        }


    }

    /**
     * Fragment with Camera View for Pictures
     */
    public static class CameraImageFragment extends BaseImageFragment {

        private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;

        public CameraImageFragment() { }

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CameraImageFragment newInstance(int sectionNumber) {
            CameraImageFragment fragment = new CameraImageFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = super.onCreateView(inflater, container, savedInstanceState);

            mBtn.setText("Take a Picture");
            mBtn.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,
                            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }

            });

            mFabNew.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                    startActivityForResult(intent,
                            CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                }
            });


            final Builder alt_bld = new Builder(getActivity());
            alt_bld.setMessage("apprika target achieve...");
            alt_bld.setCancelable(true);

            /*
            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));*/

            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data) {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE) {
                if (resultCode == Activity.RESULT_OK) {

                    Bitmap bmp = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    // convert byte array to Bitmap
                    Bitmap inputPic= BitmapFactory.decodeByteArray(byteArray, 0,
                            byteArray.length);

                    RunFaceDetectionAndUpdate(inputPic);
                }
            }
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        private LoadImageFragment p1;
        private CameraImageFragment p2;
        private LoadImageFragment p3;

        private SparseArray<Fragment> map;

        private Context mContext;

        public SectionsPagerAdapter(FragmentManager fm, Context context) {
            super(fm);
            mContext = context;
        }

        @Override
        public Fragment getItem(int position) {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).

            switch (position) {
                case 0:
                case 2:
                {
                    Fragment fragment = LoadImageFragment.newInstance(position + 1);
                    return fragment;
                }
                case 1:
                {
                    Fragment fragment = CameraImageFragment.newInstance(position + 1);
                    return fragment;
                }
                default: // should never reach here
                    return null;
            }
        }

        public Fragment getFragment(int pos) {
            return map.get(pos);
        }

        @Override
        public int getCount()
        {
            // If camera is turned off, show the one page to select images only.
            PackageManager packageManager = mContext.getPackageManager();
            if(packageManager.hasSystemFeature(PackageManager.FEATURE_CAMERA) == false)
            {
                return 1;
            }

            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position) {
            switch (position) {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }

        // Here we can finally safely save a reference to the created
        // Fragment, no matter where it came from (either getItem() or
        // FragmentManger). Simply save the returned Fragment from
        // super.instantiateItem() into an appropriate reference depending
        // on the ViewPager position.
        @Override
        public Object instantiateItem(ViewGroup container, int position) {
            Fragment createdFragment = (Fragment) super.instantiateItem(container, position);
            // save the appropriate reference depending on position
            switch (position) {
                case 0:
                    p1 = (LoadImageFragment) createdFragment;
                    break;
                case 1:
                    p2 = (CameraImageFragment) createdFragment;
                    break;
                case 2:
                    p3 = (LoadImageFragment) createdFragment;
                    break;
            }
            return createdFragment;
        }
    }
}

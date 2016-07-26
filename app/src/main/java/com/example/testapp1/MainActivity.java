package com.example.testapp1;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.support.design.widget.TabLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.content.CursorLoader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.view.ViewPager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;

import android.widget.TextView;
import android.widget.ImageView;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;

import org.opencv.android.Utils;
import org.opencv.core.Mat;
import org.opencv.imgproc.Imgproc;
import org.opencv.objdetect.CascadeClassifier;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity {

    /**
     * The {@link android.support.v4.view.PagerAdapter} that will provide
     * fragments for each of the sections. We use a
     * {@link FragmentPagerAdapter} derivative, which will keep every
     * loaded fragment in memory. If this becomes too memory intensive, it
     * may be best to switch to a
     * {@link android.support.v4.app.FragmentStatePagerAdapter}.
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
            mSectionsPagerAdapter = new SectionsPagerAdapter(getSupportFragmentManager());
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

    /**
     * helper to retrieve the path of an image URI
     */
    public String getPath(Uri uri) {
        // just some safety built in
        if( uri == null ) {
            // TODO perform some logging or show user feedback
            return null;
        }
        // try to retrieve the image from the media store first
        // this will only work for images selected from gallery
        String[] projection = { MediaStore.Images.Media.DATA };
        Cursor cursor = managedQuery(uri, projection, null, null, null);
        if( cursor != null ){
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
            String[] proj = { MediaStore.Images.Media.DATA };
            cursor = context.getContentResolver().query(contentUri,  proj, null, null, null);
            int column_index = cursor.getColumnIndexOrThrow(MediaStore.Images.Media.DATA);
            cursor.moveToFirst();
            return cursor.getString(column_index);
        } finally {
            if (cursor != null) {
                cursor.close();
            }
        }
    }

    public void SetImage()
    {
        findViewById(R.id.section_image);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static final int SELECT_PICTURE = 1;

        private String mSelectedImagePath;
        private Uri mSelectedImageUri = null;

        private MainActivity mActivity;
        private ImageView mImageView;

        public PlaceholderFragment() {
        }

        public void SetImageUri(Uri uri)
        {
            if (uri == null)
                return;
            
            try
            {
                mImageView.setImageDrawable(Drawable.createFromStream(mActivity.getContentResolver().openInputStream(uri), null));
            }
            catch(FileNotFoundException fe)
            {
                return;
            }
            catch(Exception e)
            {
                return;
            }
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

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static PlaceholderFragment newInstance(int sectionNumber) {
            PlaceholderFragment fragment = new PlaceholderFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
            alt_bld.setMessage("apprika target achieve...");
            alt_bld.setCancelable(true);

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            ImageView imageView = (ImageView) rootView.findViewById(R.id.section_image);
            mImageView = imageView;

            try{
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
                if (mCascadeER.empty()){
                    String sourceImgName = "faces1.png";
                    String resultImgName = "result_image.png";
                }

                FaceDetector faceDetector = new FaceDetector();
                String sourceImgName = "faces1.png";
                String resultImgName = "result_image.png";

                Mat resultImage = faceDetector.DetecteFace(sourceImgName, mCascadeER);

                Imgproc.cvtColor(resultImage, resultImage, Imgproc.COLOR_GRAY2RGBA, 4);
                Bitmap bmp = Bitmap.createBitmap(resultImage.cols(), resultImage.rows(), Bitmap.Config.ARGB_8888);
                Utils.matToBitmap(resultImage, bmp);
                imageView.setImageBitmap(bmp);

                faceDetector.SaveImage(resultImgName, resultImage);
            }
            catch (Exception e) {

            }

            if (mSelectedImageUri == null)
            {
                imageView.setImageResource(R.drawable.section_1);
            }
            else
            {
                SetImageUri(mSelectedImageUri);
            }
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    alt_bld.show();
                }

            });

            //set the ontouch listener
            imageView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                        {
                            ImageView view = (ImageView) v;
                            //overlay is black with transparency of 0x77 (119)
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();



                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        {
                            //alt_bld.show();
                            Intent intent = new Intent();
                            intent.setType("image/*");
                            intent.setAction(Intent.ACTION_GET_CONTENT);
                            startActivityForResult(Intent.createChooser(intent,
                                    "Select Picture"), SELECT_PICTURE);
                        }

                        case MotionEvent.ACTION_CANCEL:
                        {
                            ImageView view = (ImageView) v;
                            //clear the overlay
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return true;
                }
            });
            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            if (resultCode == RESULT_OK)
            {
                if (requestCode == SELECT_PICTURE)
                {
                    Uri selectedImageUri = data.getData();
                    mSelectedImageUri = selectedImageUri;
                    SetImageUri(selectedImageUri);

                    mSelectedImagePath = mActivity.getPath(selectedImageUri);
                    mSelectedImagePath = mActivity.getRealPathFromURI(getContext(), selectedImageUri);
                }
            }
        }


    }

    /**
     *  Fragment with Camera View for Pictures
     */
    public static class CameraPictureFragment extends Fragment {
        /**
         * The fragment argument representing the section number for this
         * fragment.
         */
        private static final String ARG_SECTION_NUMBER = "section_number";

        private static final int CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE = 1888;

        private String mSelectedImagePath;
        private Uri mSelectedImageUri = null;

        private MainActivity mActivity;
        private ImageView mImageView;

        public CameraPictureFragment() {
        }

        public void SetImageUri(Uri uri)
        {
            if (uri == null)
                return;

            try
            {
                mImageView.setImageDrawable(Drawable.createFromStream(mActivity.getContentResolver().openInputStream(uri), null));
            }
            catch(FileNotFoundException fe)
            {
                return;
            }
            catch(Exception e)
            {
                return;
            }
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

        /**
         * Returns a new instance of this fragment for the given section
         * number.
         */
        public static CameraPictureFragment newInstance(int sectionNumber) {
            CameraPictureFragment fragment = new CameraPictureFragment();
            Bundle args = new Bundle();
            args.putInt(ARG_SECTION_NUMBER, sectionNumber);
            fragment.setArguments(args);
            return fragment;
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {

            View rootView = inflater.inflate(R.layout.fragment_main, container, false);

            final AlertDialog.Builder alt_bld = new AlertDialog.Builder(getActivity());
            alt_bld.setMessage("apprika target achieve...");
            alt_bld.setCancelable(true);

            TextView textView = (TextView) rootView.findViewById(R.id.section_label);
            textView.setText(getString(R.string.section_format, getArguments().getInt(ARG_SECTION_NUMBER)));

            ImageView imageView = (ImageView) rootView.findViewById(R.id.section_image);
            mImageView = imageView;
            if (mSelectedImageUri == null)
            {
                imageView.setImageResource(R.drawable.section_1);
            }
            else
            {
                SetImageUri(mSelectedImageUri);
            }
            imageView.setOnClickListener(new View.OnClickListener() {

                @Override
                public void onClick(View view) {
                    alt_bld.show();
                }

            });

            //set the ontouch listener
            imageView.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    switch (event.getAction())
                    {
                        case MotionEvent.ACTION_DOWN:
                        {
                            ImageView view = (ImageView) v;
                            //overlay is black with transparency of 0x77 (119)
                            view.getDrawable().setColorFilter(0x77000000, PorterDuff.Mode.SRC_ATOP);
                            view.invalidate();
                            break;
                        }
                        case MotionEvent.ACTION_UP:
                        {
                            Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
                            startActivityForResult(intent,
                                    CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE);
                        }

                        case MotionEvent.ACTION_CANCEL:
                        {
                            ImageView view = (ImageView) v;
                            //clear the overlay
                            view.getDrawable().clearColorFilter();
                            view.invalidate();
                            break;
                        }
                    }

                    return true;
                }
            });
            return rootView;
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, Intent data)
        {
            if (requestCode == CAPTURE_IMAGE_ACTIVITY_REQUEST_CODE)
            {
                if (resultCode == Activity.RESULT_OK)
                {

                    Bitmap bmp = (Bitmap) data.getExtras().get("data");
                    ByteArrayOutputStream stream = new ByteArrayOutputStream();

                    bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
                    byte[] byteArray = stream.toByteArray();

                    // convert byte array to Bitmap

                    Bitmap bitmap = BitmapFactory.decodeByteArray(byteArray, 0,
                            byteArray.length);

                    mImageView.setImageBitmap(bitmap);

                }
            }
        }


    }

    /**
     * A {@link FragmentPagerAdapter} that returns a fragment corresponding to
     * one of the sections/tabs/pages.
     */
    public class SectionsPagerAdapter extends FragmentPagerAdapter {

        public SectionsPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position)
        {
            // getItem is called to instantiate the fragment for the given page.
            // Return a PlaceholderFragment (defined as a static inner class below).
            switch (position)
            {
                case 0:
                case 2:
                    return PlaceholderFragment.newInstance(position + 1);
                case 1:
                    return CameraPictureFragment.newInstance(position + 1);
                default:
                    return PlaceholderFragment.newInstance(position + 1);
            }
        }

        @Override
        public int getCount()
        {
            // Show 3 total pages.
            return 3;
        }

        @Override
        public CharSequence getPageTitle(int position)
        {
            switch (position)
            {
                case 0:
                    return "SECTION 1";
                case 1:
                    return "SECTION 2";
                case 2:
                    return "SECTION 3";
            }
            return null;
        }
    }
}

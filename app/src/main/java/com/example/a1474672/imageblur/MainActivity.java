 package com.example.a1474672.imageblur;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.SeekBar;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

 public class MainActivity extends AppCompatActivity implements View.OnClickListener {

    /* ------------------------*/
    /*   member variables
     * */

    private final static int BREADCRUMB_OPEN_DOCUMENT = 6560;
    private final static String TAG = "MAIN_ACTIVITY_TAG";
    private MyCustomView mMyCustomView;
    BlurServiceReceiver mBlurServiceReceiver;
    BlurServiceFFTReceiver mBlurServiceFFTReceiver;
     private SeekBar mSeekBar;
    Button buttonFFT;
    Button buttonLoad;
    Button buttonBlur;
    Uri uri;
    int h, w;
    double blurQ;
    Bitmap bmp;
    /* ------------------------*/
    /*   lifecycle methods
     * */

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        buttonLoad = findViewById(R.id.button_load);
        buttonLoad.setOnClickListener(this);
        uri = null;
        blurQ = 1;
        buttonBlur = findViewById(R.id.button_blur);
        buttonBlur.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                doSomethingWithBitmap();
            }
        });


        mSeekBar = findViewById(R.id.seekBar);
        double max = 0.0;
                double min = 75.0;
                double step = 1.0;
        mSeekBar.setMax( (int) ((max - min) / step ));
        mSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            double value = 1.0;

            public void onProgressChanged(SeekBar seekBar, int value, boolean fromUser) {
                blurQ = value/100.0;
            }

            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });
        mMyCustomView = findViewById(R.id.mcv_01);
        mBlurServiceReceiver = new BlurServiceReceiver();
        mBlurServiceFFTReceiver = new BlurServiceFFTReceiver();
        bmp = null;
        buttonFFT = findViewById(R.id.FFT);
        /*buttonFFT.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /*if(bmp != null) {
                    Drawable d = new BitmapDrawable(getResources(), bmp);
                    mMyCustomView.setBackground(d);
                }
            }
        });*/
    }

    /* ------------------------*/
    /*   interface methods
     * */

    @Override
    public void onClick(View v) {
        openImageOnDevice();
    }


    /* ------------------------*/
    /*   OPEN DOCUMENT METHODS :
     * *  SOURCE: https://developer.android.com/guide/topics/providers/document-provider#java
     * */

    private void openImageOnDevice() {


        // ACTION_OPEN_DOCUMENT is the intent to choose a file via the system's file browser
        Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);

        // Filter to only show results that can be "opened", such as a file (as opposed to a list of contacts or timezones)
        intent.addCategory(Intent.CATEGORY_OPENABLE);

        // Filter to show only images, using the image MIME data type.
        // If one wanted to search for ogg vorbis files, the type would be "audio/ogg".
        // To search for all documents available via installed storage providers,
        // it would be "*/*".
        intent.setType("image/*");

        startActivityForResult(intent, BREADCRUMB_OPEN_DOCUMENT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        if (requestCode == BREADCRUMB_OPEN_DOCUMENT && resultCode == Activity.RESULT_OK) {

            // The document selected by the user won't be returned in the intent.
            // Instead, a URI to that document will be contained in the return intent
            // provided to this method as a parameter.
            Log.i("OnActivityResult", "in activityresult");
            if (data != null) {
                uri = data.getData();

                Log.i("IMAGE STUFF", "Uri: " + uri.toString());

                try {
                    Bitmap b = getBitmapFromUri(uri);
                    bmp = b;
                    h = b.getHeight();
                    w = b.getWidth();
                    Drawable d = new BitmapDrawable(getResources(), b);
                    mMyCustomView.setBackground(d);
                } catch (IOException e) {
                    Log.e("ERROR","ERROR READING URI FOR BITMAP");
                }
            }
        }
    }

    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }

    private void doSomethingWithBitmap(){
        // the intent that will start the service
        Intent intent_from_activity_to_service =  new Intent();

        // define the context and the target (the service that will be started)
        intent_from_activity_to_service.setClass(this, BlurService.class);
        intent_from_activity_to_service.setData(uri);
        intent_from_activity_to_service.putExtra("blurQ", blurQ);
        // go ahead and start the service
        this.startService(intent_from_activity_to_service);
        //Bitmap c = mMyCustomView.getCanvasBitmap();
        /*int pixelColorAsInt = c.getPixel(0,0);
        Log.i("BITMAP", pixelColorAsInt+"");*/
    }

     private void doSomethingWithBitmap1(){
         // the intent that will start the service
         Bitmap c = mMyCustomView.getCanvasBitmap();
         Intent intent_from_activity_to_service1 =  new Intent();
         final BitmapTransferEnum transferEnum = BitmapTransferEnum.INSTANCE;
         transferEnum.setData(c);
         Log.i("Mainact", "DosomethingBitmap1");
         intent_from_activity_to_service1.setClass(this, FFTBlurService.class);
         intent_from_activity_to_service1.putExtra(BitmapTransferEnum.KEY, transferEnum.INSTANCE);


         // go ahead and start the service
         this.startService(intent_from_activity_to_service1);
     }

    @Override
    public void onResume() {
        super.onResume();

        // BROADCAST RECEIVERS ARE REGISTERED IN onResume

        // local broadcasts are used when your app is the sole listener
        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);

        // create a filter for the manager
        //    => i.e. listen to messages with this a specific key from our service class
        IntentFilter filter = new IntentFilter(BlurService.ACTION_KEY);
        IntentFilter filter1 = new IntentFilter(FFTBlurService.ACTION_KEY);

        // register your receiver class with the local broadcast receiver
        manager.registerReceiver(mBlurServiceFFTReceiver,filter1);

        // register your receiver class with the local broadcast receiver
        manager.registerReceiver(
                mBlurServiceReceiver,
                filter);
    }


    @Override
    public void onPause() {
        super.onPause();

        // BROADCAST RECEIVERS ARE UN-REGISTERED IN onPause - lest bad things happen

        LocalBroadcastManager manager = LocalBroadcastManager.getInstance(this);
        manager.unregisterReceiver(mBlurServiceReceiver);
        manager.unregisterReceiver(mBlurServiceFFTReceiver);
    }


    class BlurServiceReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent_from_service) {
            // get any information embedded in the intent
            int[] im = intent_from_service.getIntArrayExtra("wrapped");
            int   width  = w;
            int   height = h;
            Bitmap bitmap = Bitmap.createBitmap(im, width, height, Bitmap.Config.ARGB_8888);
            //bitmap.setPixels(im, 0, width, 0, 0, width, height);
                Drawable d = new BitmapDrawable(getResources(), bitmap);
                mMyCustomView.setBackground(d);
                Log.i("put in view", "done");
        }
    }
     class BlurServiceFFTReceiver extends BroadcastReceiver{   @Override
     public void onReceive(Context context, Intent intent_from_service) {

         Bundle extras = intent_from_service.getExtras();
         if (extras != null) {
             if (extras.containsKey(BitmapTransferEnum.KEY)) {

                 Log.i(TAG, "Contains enum");
                 BitmapTransferEnum bitmapTransferEnum = (BitmapTransferEnum) extras.getSerializable(BitmapTransferEnum.KEY);
                 Bitmap b = bitmapTransferEnum.INSTANCE.getData();
                 Drawable d = new BitmapDrawable(getResources(), b);
                 mMyCustomView.setBackground(d);

             }
         }
     }
     }
}
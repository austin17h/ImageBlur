package com.example.a1474672.imageblur;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.util.Arrays;
import java.util.concurrent.ThreadLocalRandom;

public class FFTBlurService extends IntentService {

    final static String ACTION_KEY = "action_key1";

    private final static String TAG = "FFT_SERVICE_TAG";

    /* -------------------------------- */
    /*    CONSTRUCTOR                   */

    public FFTBlurService() {

        // SUPER IMPORTANT!!!
        //
        //  The constructor must call super IntentService(String) with a name for the thread

        super("FFTBlueService");
    }

    /* -------------------------------- */
    /*    REQUIRED CLASS METHODS        */

    @Override
    protected void onHandleIntent(Intent intent_from_activity) {

        // THIS METHOD IS CALLED WHEN onClick in the activity triggers the start of the service
        Log.i(TAG, "GOT MESSAGE!");

        Bundle extras = intent_from_activity.getExtras();
        if (extras != null) {
            if (extras.containsKey(BitmapTransferEnum.KEY)) {

                Log.i(TAG, "Contains enum");
                BitmapTransferEnum bitmapTransferEnum = (BitmapTransferEnum)extras.getSerializable(BitmapTransferEnum.KEY);
                Bitmap b = bitmapTransferEnum.INSTANCE.getData();


                transformBitmap(b);
                respondToActivity(b);
            }
        }

    }

    private void transformBitmap(Bitmap bmp) {

        // --------------------------------------------------------
        //      INITIALIZATION

        // INITIALIZE HELPER VARIABLES USED AT VARIOUS POINTS IN THIS METHOD
        int i=0;
        int j=0;
        int pixelNo;


        // DEFINE A NEW DIMENSION (TO BE USED AS HEIGHT AND WIDTH) OF THE BITMAP
        //  THEN, RESIZE THE BITMAP
        int new_dimension = 40;
        bmp.reconfigure(new_dimension, new_dimension, bmp.getConfig());

        // EXTRACT PIXEL VALUES FROM THE OLD BITMAP.
        //   -> -> THESE PIXELS ARE NOT USED IN THIS EXAMPLE. JUST FYI.
        int[] argb_in = new int[new_dimension*new_dimension];
        bmp.getPixels(argb_in, 0, new_dimension, 0, 0, new_dimension, new_dimension );

        Log.i(TAG,"Bitmap Width: "+String.valueOf( bmp.getWidth()));
        Log.i(TAG,"Bitmap Height:"+String.valueOf( bmp.getHeight()));


        // DEFINE THE NUMBER OF POINTS IN THE FFT.
        //  FIRST CREATE AN FFT OBJECT
        //  THEN CREATE COMPLEX OBJECTS (THAT WILL HOLD fft'd VALUES)
        //  THE FFT OPERATION USED IN THIS METHOD TRANSFORMS THE OBJECTS IN PLACE
        int N = 128;
        FFT fft = new FFT(N, N);
        Complex2D KERNEL = new Complex2D(N,N);
        Complex2D COLOR  = new Complex2D(N,N);

        // DEFINE THE CONVOLUTION KERNEL
        //  KERNEL IS A 7x& GAUUSIAN
        double[][] kernel = {{0.000036, 0.000363, 0.001446, 0.002291, 0.001446, 0.000363, 0.000036},
                {0.000363, 0.003676, 0.014662, 0.023226, 0.014662, 0.003676, 0.000363},
                {0.001446, 0.014662, 0.058488, 0.092651, 0.058488, 0.014662, 0.001446},
                {0.002291, 0.023226, 0.092651, 0.146768, 0.092651, 0.023226, 0.002291},
                {0.001446, 0.014662, 0.058488, 0.092651, 0.058488, 0.014662, 0.001446},
                {0.000363, 0.003676, 0.014662, 0.023226, 0.014662, 0.003676, 0.000363},
                {0.000036, 0.000363, 0.001446, 0.002291, 0.001446, 0.000363, 0.000036}};

        // ASSIGN THE KERNEL VALUES TO AN OBJECT THAT CAN BE FFT'd
        //  THEN TAKE THE FFT OF THE KERNEL
        KERNEL.assign(kernel);
        fft.fft2(KERNEL.real, KERNEL.imag);

        // THE CONVOLVED IMAGE WILL BE INSET kernel.length/2 PIXELS FROM THE EDGE
        int x_inset = 3;
        int y_inset = 3;


        // DEFINE A 40x40 CHECKER PATTERN
        //   (THIS IS THE TEST IMAGE)
        //  THEN, CREATE AN ARRAY TO HOLD THE PIXElS THAT WILL
        //   PUSHED BE THE OUTPUT BITMAP
       // double[] color = { 255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,        255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     255.,     0.,     0.,     0.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,      0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,       0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255.,        0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     0.,     255.,     255.,     255.,       255.,     255.,     255.,     255.,     255.,     255.,     255. };
        double[] color = new double[bmp.getWidth()*bmp.getHeight()];
        int on = 0;
        for(int x = 0; x < bmp.getWidth(); x++)
        {
            for(int y = 0; y < bmp.getHeight(); y++) {
                   color[on] = (double)bmp.getPixel(x, y);
                   on++;
            }
        }

        int[] argb_out = new int[new_dimension*new_dimension];

        // SET THE OUTPUT TRANSPARENCY TO ZERO
        Arrays.fill(argb_out, 255 << 24);

        // DEFINE A VALUE THAT WILL BE USED TO FILL ALL PIXELS THAT ARE NOT EXPLICITLY SET
        double all_others = 255.;


        // --------------------------------------------------------
        //      FAST CONVOLUTION


        //  -------------- RED --------------

        // FILL THE COLOR FFT OBJECT WITH THE PIXELS FROM OUR INPUT IMAGE
        COLOR.assignFrom(color, new_dimension, all_others);

        // TAKE THE 2D FFT OF THE RED LAYER OF THE IMAGE
        fft.fft2(COLOR.real, COLOR.imag);
        Log.i(TAG,"RED FFT");

        // MULTIPLY THE FFT OF THE RED LAYER BY THE FFT OF THE KERNEL
        COLOR.complexMultiply(KERNEL.real, KERNEL.imag);

        // INVERSE 2D FFT THE MULTIPLICATION RESULT
        fft.ifft2(COLOR.real, COLOR.imag);
        Log.i(TAG,"RED iFFT");

        // ASSIGN THE CONVOLVED VALUES TO THE RED CHANNEL OF THE OUTPUT BUFFER
        pixelNo = 0;
        for (i = x_inset; i < x_inset + new_dimension; i++) {
            for (j = y_inset; j < y_inset+new_dimension; j++) {

                // CAST AS AN INT AND SHIFT TO THE RED CHANNEL
                argb_out[pixelNo++] |= ((int) COLOR.real[i][j]) << 16;
            }
        }

        //  -------------- GREEN --------------

        COLOR.assignFrom(color, new_dimension, all_others);

        fft.fft2(COLOR.real, COLOR.imag);
        Log.i(TAG,"GREEN FFT");

        COLOR.complexMultiply(KERNEL.real, KERNEL.imag);
        fft.ifft2(COLOR.real, COLOR.imag);
        Log.i(TAG,"GREEN iFFT");
        pixelNo = 0;

        for ( i=x_inset; i<x_inset+new_dimension ; i++) {
            for (j = y_inset; j < y_inset + new_dimension; j++) {
                argb_out[pixelNo++] |= ((int) COLOR.real[i][j])  <<  8 ;
            }
        }

        //  -------------- BLUE --------------

        COLOR.assignFrom(color, new_dimension, all_others);

        fft.fft2(COLOR.real, COLOR.imag);
        Log.i(TAG,"BLUE FFT");

        COLOR.complexMultiply(KERNEL.real, KERNEL.imag);
        fft.ifft2(COLOR.real, COLOR.imag);
        Log.i(TAG,"BLUE iFFT");

        pixelNo = 0;
        for ( i=x_inset; i<x_inset+new_dimension ; i++) {
            for ( j = y_inset; j < y_inset + new_dimension; j++) {
                argb_out[pixelNo++] |= ((int) COLOR.real[i][j])  ;
            }
        }

        // ASSIGN THE NEW PIXELS TO THE BITMAP
        bmp.setPixels(argb_out, 0, new_dimension, 0, 0, new_dimension, new_dimension );
    }


    /* -------------------------------- */
    /*    CUSTOM HELPER METHODS         */

    private void respondToActivity(Bitmap bmp) {

        final BitmapTransferEnum transferEnum = BitmapTransferEnum.INSTANCE;
        transferEnum.setData(bmp);

        // create a new intent to send data back to the activity
        Intent intent_from_service = new Intent();
        intent_from_service.setAction(ACTION_KEY);
        intent_from_service.putExtra(BitmapTransferEnum.KEY, transferEnum.INSTANCE);


        // send the local broadcast
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent_from_service);
    }
}

package com.example.a1474672.imageblur;

import android.app.IntentService;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.net.Uri;
import android.os.ParcelFileDescriptor;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import java.io.FileDescriptor;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import static android.graphics.Color.*;

public class BlurService extends IntentService {
    final static String ACTION_KEY = "action_key";
    int[] post;
    ArrayList<Float> array;
    public BlurService()
    {
        super("BlurService");
    }
    @Override
    protected void onHandleIntent(Intent intent_from_activity) {
        double blurQ = intent_from_activity.getDoubleExtra("blurQ", 1.0);
        Uri uri = intent_from_activity.getData();
        Log.i("tag", "GOT MESSAGE!");
        array = new ArrayList<Float>();
        for(int i = 0; i < 9; i++) {
            array.add(i,(float)1.0 * (float)blurQ);
        }
        //array.addAll(Arrays.asList(.33 ,0.33 , 0.33, 0.33, 0.33, 0.33, .33, 0.33, 0.33));
        respondToActivity(uri);
    }
    public int[] convolve_the_image(Bitmap c)
    {
        Log.i("convolve", "started");
        int height = c.getHeight();
        int width = c.getWidth();
        int on = 0;
        int kernel_number;
        int[] toReturn = new int[height*width];
        float average_blue, average_red, average_green;
        for(int i = 0; i < width; i++)
        {
            toReturn[on] = c.getPixel(i, 0);
            on++;
        }
        for(int y = 1; y < height - 1 ; y++)
        {
            toReturn[on] = c.getPixel(0, y);
            on++;
            for(int x = 1; x < width - 1; x++)
            {
                average_blue = 0;
                average_red = 0;
                average_green = 0;
                kernel_number = 0;
                while(kernel_number != 9)
                {
                    switch (kernel_number) {
                        case (0):
                            average_blue += Color.blue(c.getPixel(x-1, y-1))*array.get(0);
                            average_red += Color.red(c.getPixel(x-1, y-1))*array.get(0);
                            average_green +=  Color.green(c.getPixel(x-1, y-1))*array.get(0);
                            break;
                        case (1):
                            average_blue += Color.blue(c.getPixel(x, y-1))*array.get(1);
                            average_red += Color.red(c.getPixel(x, y-1))*array.get(1);
                            average_green +=  Color.green(c.getPixel(x, y-1))*array.get(1);
                            break;
                        case(2):
                            average_blue += Color.blue(c.getPixel(x + 1, y-1))*array.get(2);
                            average_red += Color.red(c.getPixel(x + 1, y-1))*array.get(2);
                            average_green +=  Color.green(c.getPixel(x + 1, y-1))*array.get(2);
                            break;
                        case (3):
                            average_blue += Color.blue(c.getPixel(x-1, y-1))*array.get(3);
                            average_red += Color.red(c.getPixel(x-1 , y-1))*array.get(3);
                            average_green +=  Color.green(c.getPixel(x- 1, y-1))*array.get(3);
                            break;
                        case (4):
                            average_blue += Color.blue(c.getPixel(x , y))*array.get(4);
                            average_red += Color.red(c.getPixel(x, y))*array.get(4);
                            average_green +=  Color.green(c.getPixel(x, y))*array.get(4);
                            break;
                        case(5):
                            average_blue += Color.blue(c.getPixel(x+1, y))*array.get(5);
                            average_red += Color.red(c.getPixel(x+1, y))*array.get(5);
                            average_green +=  Color.green(c.getPixel(x+1, y))*array.get(5);
                            break;
                        case(6):
                            average_blue += Color.blue(c.getPixel(x-1, y+1))*array.get(6);
                            average_red += Color.red(c.getPixel(x-1, y+1))*array.get(6);
                            average_green +=  Color.green(c.getPixel(x-1, y+1))*array.get(6);
                            break;
                        case (7):
                            average_blue += Color.blue(c.getPixel(x, y+1))*array.get(7);
                            average_red += Color.red(c.getPixel(x, y+1))*array.get(7);
                            average_green +=  Color.green(c.getPixel(x, y+1))*array.get(7);
                            break;
                        case (8):
                            average_blue += Color.blue(c.getPixel(x+1, y+1))*array.get(8);
                            average_red += Color.red(c.getPixel(x+1, y+1))*array.get(8);
                            average_green +=  Color.green(c.getPixel(x+1, y+1))*array.get(8);
                            break;
                    }
                    kernel_number++;
                }
                average_blue = average_blue/(float)9.0/(float)255.0;
                average_red = average_red/(float)9.0/(float)255.0;
                average_green = average_green/(float)9.0/(float)255.0;
                Color to_get_alpha = Color.valueOf(c.getPixel(x,y));
                toReturn[on] = argb(to_get_alpha.alpha(),average_red, average_green, average_blue);
                on++;

            }
            toReturn[on] = c.getPixel(width - 1, y);
            on++;
        }
        for(int i = 0; i < width; i++)
        {
            toReturn[on] = c.getPixel(i, height - 1);
            on++;
        }
        Log.i("done convolving", "done");
        return toReturn;
    }
    private Bitmap getBitmapFromUri(Uri uri) throws IOException {
        ParcelFileDescriptor parcelFileDescriptor =
                getContentResolver().openFileDescriptor(uri, "r");
        FileDescriptor fileDescriptor = parcelFileDescriptor.getFileDescriptor();
        Bitmap image = BitmapFactory.decodeFileDescriptor(fileDescriptor);
        parcelFileDescriptor.close();
        return image;
    }
    private void respondToActivity(Uri uri) {

        // create a new intent to send data back to the activity
        Intent intent_from_service = new Intent();

        // define a key that will be paired to the intent
        intent_from_service.setAction(ACTION_KEY);
        Log.i("respond to activity", "responds");
        try {
            Bitmap pre = getBitmapFromUri(uri);
            post = convolve_the_image(pre);
            Log.i("ok","done");
        }
        catch(IOException e)
        {
            Log.i("IOException", "pre");
        }
        // optionally attach data to the intent.
        // the key implementation here is weak.
        intent_from_service.putExtra("wrapped", post);

        // send the local broadcast
        LocalBroadcastManager.getInstance(getApplicationContext()).sendBroadcast(intent_from_service);
        Log.i("broadcasted", "done");


    }
}

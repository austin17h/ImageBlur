package com.example.a1474672.imageblur;

import android.content.Context;
import android.graphics.Bitmap;
import android.util.AttributeSet;
import android.view.View;


public class MyCustomView extends View {

    public MyCustomView(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.setDrawingCacheEnabled(true);
    }

    public Bitmap getCanvasBitmap() {
        return this.getDrawingCache();
    }
}
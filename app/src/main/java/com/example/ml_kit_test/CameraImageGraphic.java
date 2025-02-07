package com.example.ml_kit_test;

import android.graphics.Bitmap;
import android.graphics.Canvas;


public class CameraImageGraphic extends GraphicOverlay.Graphic {
    private final Bitmap bitmap;

    public CameraImageGraphic(GraphicOverlay overlay, Bitmap bitmap) {
        super(overlay);
        this.bitmap = bitmap;
    }

    @Override
    public void draw(Canvas canvas) {
        canvas.drawBitmap(bitmap, getTransformationMatrix(), null);
    }
}

package com.thesis.documentscanner.util;

import android.graphics.Bitmap;
import android.graphics.Bitmap.CompressFormat;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

public class BitmapUtils {

    public static InputStream bitmapToInputStream(Bitmap bitmap, CompressFormat format, int quality) {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        bitmap.compress(format, quality, outputStream);
        byte[] bitmapData = outputStream.toByteArray();
        return new ByteArrayInputStream(bitmapData);
    }
}

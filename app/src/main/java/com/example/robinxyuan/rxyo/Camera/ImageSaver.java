package com.example.robinxyuan.rxyo.Camera;

/**
 * Created by robinxyuan on 2017/12/16.
 */

import android.media.Image;

import java.io.File;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;

/**
 * Saves a JPEG {@link Image} into the specified {@link File}.
 */
public class ImageSaver implements Runnable {

    /**
     * The JPEG image
     */
    final Image mImage;
    /**
     * The file we save the image into.
     */
    final File mFile;

    ImageSaver(Image image, File file) {
        mImage = image;
        mFile = file;
    }

    @SuppressWarnings("ResultOfMethodCallIgnored")
    @Override
    public void run() {
        ByteBuffer buffer = mImage.getPlanes()[0].getBuffer();
        byte[] bytes = new byte[buffer.remaining()];
        buffer.get(bytes);
        try {
            if (mFile.exists()) mFile.delete();
            FileOutputStream output = new FileOutputStream(mFile);
            output.write(bytes);
            try {mImage.close();} catch (Exception ignored) {}
            try {output.close();} catch (Exception ignored) {}
            /**
             * 拍照完成后返回MainActivity.
             */
//                App.mHandler.post(() -> {
//                    setResult(200, getIntent().putExtra("file", mFile.toString()));
//                    mFinishCalled = true;
//                    finish();
//                });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

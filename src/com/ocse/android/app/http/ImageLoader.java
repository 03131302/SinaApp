package com.ocse.android.app.http;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;
import android.widget.ImageView;
import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.BinaryHttpResponseHandler;
import com.ocse.android.app.R;

import java.io.*;
import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public class ImageLoader {

    MemoryCache memoryCache = new MemoryCache();
    FileCache fileCache;
    private Map<ImageView, String> imageViews = Collections.synchronizedMap(new WeakHashMap<ImageView, String>());

    public ImageLoader(Context context) {
        fileCache = new FileCache(context);
    }

    final int stub_id = R.drawable.no_image;

    public void displayImage(String url, ImageView imageView) {
        imageViews.put(imageView, url);
        Bitmap bitmap = memoryCache.get(url);
        if (bitmap != null)
            imageView.setImageBitmap(bitmap);
        else {
            queuePhoto(url, imageView);
            imageView.setImageResource(stub_id);
        }
    }

    private void queuePhoto(String url, ImageView imageView) {
        PhotoToLoad p = new PhotoToLoad(url, imageView);
        getBitmap(url, p);
    }

    private void getBitmap(final String url, final PhotoToLoad photoToLoad) {
        final File f = fileCache.getFile(url);
        Log.v("文件存储：", f.getAbsolutePath());
        //从网络
        try {
            AsyncHttpClient client = new AsyncHttpClient();
            String[] allowedContentTypes = new String[]{"image/png", "image/jpeg"};
            client.get(url, new BinaryHttpResponseHandler(allowedContentTypes) {
                @Override
                public void onSuccess(byte[] bytes) {
                    FileOutputStream fout = null;
                    try {
                        fout = new FileOutputStream(f);
                        fout.write(bytes);

                        if (imageViewReused(photoToLoad))
                            return;
                        Bitmap bmp = decodeFile(f);
                        memoryCache.put(photoToLoad.url, bmp);
                        if (imageViewReused(photoToLoad))
                            return;
                        BitmapDisplayer bd = new BitmapDisplayer(bmp, photoToLoad);
                        Activity a = (Activity) photoToLoad.imageView.getContext();
                        a.runOnUiThread(bd);
                    } catch (Exception e) {
                        e.printStackTrace();
                    } finally {
                        if (fout != null) {
                            try {
                                fout.close();
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            });
        } catch (Exception ex) {
            ex.printStackTrace();
        }
    }

    //解码图像用来减少内存消耗
    private Bitmap decodeFile(File f) {
        try {
            //解码图像大小
            BitmapFactory.Options o = new BitmapFactory.Options();
            o.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(new FileInputStream(f), null, o);

            //找到正确的刻度值，它应该是2的幂。
            final int REQUIRED_SIZE = 70;
            int width_tmp = o.outWidth, height_tmp = o.outHeight;
            int scale = 1;
            while (true) {
                if (width_tmp / 2 < REQUIRED_SIZE || height_tmp / 2 < REQUIRED_SIZE)
                    break;
                width_tmp /= 2;
                height_tmp /= 2;
                scale *= 2;
            }

            BitmapFactory.Options o2 = new BitmapFactory.Options();
            o2.inSampleSize = scale;
            return BitmapFactory.decodeStream(new FileInputStream(f), null, o2);
        } catch (FileNotFoundException e) {
        }
        return null;
    }

    private class PhotoToLoad {
        public String url;
        public ImageView imageView;

        public PhotoToLoad(String u, ImageView i) {
            url = u;
            imageView = i;
        }
    }

    boolean imageViewReused(PhotoToLoad photoToLoad) {
        String tag = imageViews.get(photoToLoad.imageView);
        if (tag == null || !tag.equals(photoToLoad.url))
            return true;
        return false;
    }

    //用于显示位图在UI线程
    class BitmapDisplayer implements Runnable {
        Bitmap bitmap;
        PhotoToLoad photoToLoad;

        public BitmapDisplayer(Bitmap b, PhotoToLoad p) {
            bitmap = b;
            photoToLoad = p;
        }

        public void run() {
            if (imageViewReused(photoToLoad))
                return;
            if (bitmap != null)
                photoToLoad.imageView.setImageBitmap(bitmap);
            else
                photoToLoad.imageView.setImageResource(stub_id);
        }
    }

    public void clearCache() {
        memoryCache.clear();
        fileCache.clear();
    }

}


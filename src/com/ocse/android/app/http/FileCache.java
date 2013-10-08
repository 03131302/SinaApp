package com.ocse.android.app.http;

import android.content.Context;
import android.util.Log;

import java.io.File;

public class FileCache {

    private File cacheDir;

    public FileCache(Context context) {
        //找一个用来缓存图片的路径
        if (android.os.Environment.getExternalStorageState().equals(android.os.Environment.MEDIA_MOUNTED)) {
            cacheDir = new File(android.os.Environment.getExternalStorageDirectory(), "LazyList");
        } else {
            cacheDir = context.getCacheDir();
        }
        if (!cacheDir.exists()) {
            cacheDir.mkdirs();
        }
        Log.v("文件存储路径：", cacheDir.getAbsolutePath());
    }

    public File getFile(String url) {
        String filename = String.valueOf(url.hashCode());
        File f = new File(cacheDir, filename);
        return f;

    }

    public void clear() {
        File[] files = cacheDir.listFiles();
        if (files == null)
            return;
        for (File f : files)
            f.delete();
    }

}
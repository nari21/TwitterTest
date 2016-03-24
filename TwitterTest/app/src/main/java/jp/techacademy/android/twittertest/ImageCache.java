package jp.techacademy.android.twittertest;

import android.graphics.Bitmap;
import android.util.Log;

import java.util.HashMap;

/**
 * Created by k-matsuo on 2016/03/10.
 */
public class ImageCache {
    private static HashMap<String, Bitmap> cache = new HashMap<String, Bitmap>();

    public static Bitmap getImage(String key) {
        if (cache.containsKey(key)) {
            Log.d("debug", "ImageCache getImage key="+key+" cache hit!");
            return cache.get(key);
        }
        return null;
    }

    public static void setImage(String key, Bitmap image) {
        Log.d("debug", "ImageCache setImage key="+key);
        cache.put(key, image);
    }
}

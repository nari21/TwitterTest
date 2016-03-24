package jp.techacademy.android.twittertest;

import android.graphics.Bitmap;
import android.util.Log;
import android.util.LruCache;

/**
 * Created by k-matsuo on 2016/03/24.
 */
public class BitmapCache {

    private static LruCache<String, Bitmap> mMemoryCache;

    public BitmapCache() {
        int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        int cacheSize = maxMemory / 8;       // 最大メモリに依存した実装
        // int cacheSize = 5 * 1024 * 1024;  // 5MB

        mMemoryCache = new LruCache<String, Bitmap>(cacheSize) {
            @Override
            protected int sizeOf(String key, Bitmap bitmap) {
                // 使用キャッシュサイズ(ここではKB単位)
                return bitmap.getRowBytes() * bitmap.getHeight() / 1024;
                // または bitmap.getByteCount() / 1024を利用
            }
        };
    }

    // Cacheのインターフェイス実装
    public static Bitmap getBitmap(String url) {
        Log.d("debug", "BitmapCache getBitmap key=" + url);
        return mMemoryCache.get(url);
    }

    public static void putBitmap(String url, Bitmap bitmap) {
        Log.d("debug", "BitmapCache putBitmap key="+url);
        Bitmap old = mMemoryCache.put(url,bitmap);
//        // オブジェクトの解放処理が必要なら以下のように実施
//        if (old != null){
//            if(!old.isRecycled()){
//                old.recycle();
//            }
//            old = null;
//        }
    }
}

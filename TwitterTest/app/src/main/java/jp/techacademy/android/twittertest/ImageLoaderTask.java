package jp.techacademy.android.twittertest;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.util.Log;
import android.widget.ImageView;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Created by k-matsuo on 2016/03/08.
 */
public class ImageLoaderTask extends AsyncTask<ImageLoaderTask.Request, Void, ImageLoaderTask.Result> {

    private static String tag;

    public static class Request {
        public final ImageView imageView;
        public final String url;

        public Request (ImageView imageView, String url) {
            this.imageView = imageView;
            this.url = url;
            tag = imageView.getTag().toString();
            Log.d("debug","ImageLoaderTask Request tag="+tag);
        }
    }

    public static class Result {
        public final ImageView imageView;
        public final Bitmap bitmap;
        public final Exception exception;

        public Result (ImageView imageView, Bitmap bitmap) {
            this.imageView = imageView;
            this.bitmap = bitmap;
            this.exception = null;
        }

        public Result (ImageView imageView, Exception exception) {
            this.imageView = imageView;
            this.bitmap = null;
            this.exception = exception;
        }
    }

    @Override
    protected Result doInBackground(Request... params) {

        Request request = params[0];
        Result result = null;

        HttpURLConnection connection = null;

        URL url = null;
        try {
            url = new URL(request.url);
            connection = (HttpURLConnection) url.openConnection();

            Bitmap bitmap = BitmapFactory.decodeStream(connection.getInputStream());

            result = new Result(request.imageView, bitmap);

        } catch (IOException e) {
            result = new Result(request.imageView, e);
        } finally {
            if (connection != null) {
                connection.disconnect();
            }
        }

        return result;
    }

    @Override
    protected void onPostExecute(Result result) {
        super.onPostExecute(result);
        Log.d("debug", "ImageLoaderTask onPostExecute tag=" + result.imageView.getTag());

        if ((result.bitmap != null) && tag.equals(result.imageView.getTag())) {
            result.imageView.setImageBitmap(result.bitmap);
        }
    }
}

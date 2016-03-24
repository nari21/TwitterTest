package jp.techacademy.android.twittertest;

import android.os.AsyncTask;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.basic.DefaultOAuthConsumer;

/**
 * Created by k-matsuo on 2016/03/03.
 */
public class AsyncTwitterRequest extends AsyncTask<Map<String,String>, Integer, List<TwitterData>> {

    private final String consumerKey = "uJqR5qQ3gQmBcKmaRQP7eh9rp";
    private final String consumerSecret = "efGTeiCtUvKz2gFtd6RzY0IVyoKtkuoE6yuNvbSnXc0nunr3j5";
//    private final String accessToken = "251482985-bucKvb2iEEvbSXjnz9hQJHOLy8gM6E8Nvh9AQjXs";
//    private final String tokenSecret = "ApvRHODV9mgtUAherbz1biobYe1i3ju2LSbaC4MZMF0Lg";

    private final String twitterHomeTimelineUrl = "https://api.twitter.com/1.1/statuses/home_timeline.json";
    private final String get_count = "?count=50";

    @Override
    protected List<TwitterData> doInBackground(Map<String,String>... params) {
        HttpURLConnection con = null;

        try {
            // これはユーザによらない
            OAuthConsumer consumer = new DefaultOAuthConsumer(consumerKey, consumerSecret);

            String accessToken = params[0].get("token");
            String tokenSecret = params[0].get("token_secret");
            // これはユーザごとに異なる
            consumer.setTokenWithSecret(accessToken, tokenSecret);

            // URLの作成（ホームタイムライン取得）
            URL url = new URL(twitterHomeTimelineUrl + get_count);
            // 接続用HttpURLConnectionオブジェクト作成
            con = (HttpURLConnection) url.openConnection();
            // リクエストメソッドの設定
            con.setRequestMethod("GET");
            // リダイレクトを自動で許可しない設定
            con.setInstanceFollowRedirects(false);
            // タイムアウト設定(5秒)
            con.setConnectTimeout(5000);
            // OAuth認証
            consumer.sign(con);

            // 接続
            con.connect();

            StringBuilder sb = new StringBuilder();
            try {
                BufferedReader br = new BufferedReader(
                        new InputStreamReader(con.getInputStream()));
                String line;
                while ((line = br.readLine()) != null) {
                    sb.append(line);
                }
                br.close();

                // JSON Arrayを作成する(文字列としてのJSONをJSON Arrayに変換)
                JSONArray jsonArray = new JSONArray(sb.toString());

                ArrayList<TwitterData> list = new ArrayList<>();

                // JSON Objectを作成する
                for (int i = 0; i < jsonArray.length(); i++) {
                    // getJSONObjectでJSON Arrayに格納された要素をJSON Objectとして取得できる
                    JSONObject jsonObject = jsonArray.getJSONObject(i);

                    // JSONデータを解析
                    String user_name = jsonObject.getJSONObject("user").getString("name");
                    String profile_image_url = jsonObject.getJSONObject("user").getString("profile_image_url");
                    String created_ad = jsonObject.getString("created_at");
                    String text = jsonObject.getString("text");

                    // ツイート情報を保持
                    TwitterData data = new TwitterData(user_name, profile_image_url, created_ad, text);

                    try {
                        // JSONデータを解析(リツイート情報)
                        JSONObject retweet = jsonObject.getJSONObject("retweeted_status");
                        String re_user_name = retweet.getJSONObject("user").getString("name");
                        String re_profile_image_url = retweet.getJSONObject("user").getString("profile_image_url");
                        String re_created_ad = retweet.getString("created_at");
                        String re_text = retweet.getString("text");

                        // リツイート情報を保持
                        data.setRetweetInfo(re_user_name, re_profile_image_url, re_created_ad, re_text);
                    } catch (JSONException e) {
                        // NOP
                    }

                    list.add(data);
                }

                return list;

            } finally {
                con.disconnect();
            }

        } catch (MalformedURLException e) {

        } catch (Exception e) {

        }

        return null;
    }

}

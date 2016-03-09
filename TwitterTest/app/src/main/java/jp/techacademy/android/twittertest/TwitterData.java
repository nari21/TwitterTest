package jp.techacademy.android.twittertest;

import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by k-matsuo on 2016/03/03.
 */
public class TwitterData {

    // ユーザー名
    public String user_name;
    // プロフィール画像
    public String profile_image_url;
    // 投稿日
    public String created_ad;
    // ツイート内容
    public String text;

    public TwitterData(JSONObject jsonObject) throws JSONException {

        user_name = jsonObject.getJSONObject("user").getString("name");
        profile_image_url = jsonObject.getJSONObject("user").getString("profile_image_url");
        created_ad = jsonObject.getString("created_at");
        text = jsonObject.getString("text");
    }
}

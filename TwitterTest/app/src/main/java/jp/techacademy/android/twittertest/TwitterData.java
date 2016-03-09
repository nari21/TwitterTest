package jp.techacademy.android.twittertest;

import org.json.JSONException;

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

    public TwitterData(String user_name,String profile_image_url, String created_ad, String text) throws JSONException {
        this.user_name = user_name;
        this.profile_image_url = profile_image_url;
        this.created_ad = created_ad;
        this.text = text;
    }
}

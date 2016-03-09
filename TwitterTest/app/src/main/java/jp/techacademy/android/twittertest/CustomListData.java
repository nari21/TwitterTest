package jp.techacademy.android.twittertest;

/**
 * Created by k-matsuo on 2016/03/04.
 */
public class CustomListData {

    // ユーザー名
    private String user_name;
    // 更新日
    private String created_ad;
    // ツイート内容
    private String text;
    // プロフィール画像
    private String profileImage;

    public CustomListData(String user_name, String created_ad, String text,String profileImage) {
        this.user_name = user_name;
        this.created_ad = created_ad;
        this.text = text;
        this.profileImage = profileImage;
    }

    public String getUserName() { return this.user_name; }

    public String getCreatedAd() {return this.created_ad; }

    public String getText() {
        return this.text;
    }

    public String getProfileImage() { return this.profileImage; }
}

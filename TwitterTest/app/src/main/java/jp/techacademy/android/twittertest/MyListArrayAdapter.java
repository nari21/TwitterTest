package jp.techacademy.android.twittertest;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.List;

/**
 * Created by k-matsuo on 2016/03/04.
 */
public class MyListArrayAdapter extends ArrayAdapter<CustomListData> {

    private Context context;
    private LayoutInflater inflater;
    private int listItemLayoutId, user_name, created_ad, text, profile_image;

    public MyListArrayAdapter(Context context, int resouurce,
                              List<CustomListData> objects, int listItemLayoutid,
                              int user_name,int created_ad, int text, int profile_image) {
        super(context, resouurce, objects);

        this.context = context;
        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.listItemLayoutId = listItemLayoutid;
        this.user_name = user_name;
        this.created_ad = created_ad;
        this.text = text;
        this.profile_image = profile_image;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(listItemLayoutId, null);
        }

        //リストのアイテムデータの取得
        CustomListData item = this.getItem(position);

        TextView user_name = (TextView) convertView.findViewById(this.user_name);
        if(user_name != null) {
            //アイテムデータにユーザー名を設定
            user_name.setText(item.getUserName());
        }

        TextView created_ad = (TextView) convertView.findViewById(this.created_ad);
        if (created_ad != null) {
            //アイテムデータに更新日を設定
            created_ad.setText(item.getCreatedAd());
        }

        TextView text = (TextView) convertView.findViewById(this.text);
        if (text != null) {
            //アイテムデータにツイート内容を設定
            text.setText(item.getText());
        }

        ImageView profile_image = (ImageView) convertView.findViewById(this.profile_image);
        if (profile_image != null) {
            //タグを設定
            profile_image.setTag(item.getProfileImage());
            //画像取得スレッド起動
            ImageLoaderTask imageTask = new ImageLoaderTask();
            imageTask.execute(new ImageLoaderTask.Request(profile_image, item.getProfileImage()));
        }

        return convertView;
    }

}

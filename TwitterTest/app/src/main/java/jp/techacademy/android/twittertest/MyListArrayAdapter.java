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

    private LayoutInflater inflater;
    private ViewHolder holder;

    private class ViewHolder {
        TextView user_name;
        TextView created_ad;
        TextView text;
        ImageView profile_image;

        ViewHolder (View view) {
            profile_image = (ImageView) view.findViewById(R.id.custom_list_profile_image);
            user_name = (TextView) view.findViewById(R.id.custom_list_user_name);
            created_ad = (TextView) view.findViewById(R.id.custom_list_created_ad);
            text = (TextView) view.findViewById(R.id.custom_list_text);
        }
    }

    public MyListArrayAdapter(Context context, int resource,
                              List<CustomListData> objects){
        super(context, resource, objects);

        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = inflater.inflate(R.layout.custom_list, null);

            holder = new ViewHolder(convertView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder)convertView.getTag();
        }

        //リストのアイテムデータの取得
        CustomListData item = this.getItem(position);

        if(holder.user_name != null) {
            //アイテムデータにユーザー名を設定
            holder.user_name.setText(item.getUserName());
        }

        if (holder.created_ad != null) {
            //アイテムデータに更新日を設定
            holder.created_ad.setText(item.getCreatedAd());
        }

        if (holder.text != null) {
            //アイテムデータにツイート内容を設定
            holder.text.setText(item.getText());
        }

        if (holder.profile_image != null) {
            //タグを設定
            holder.profile_image.setTag(item.getProfileImage());
            //画像取得スレッド起動
            ImageLoaderTask imageTask = new ImageLoaderTask();
            imageTask.execute(imageTask.new Request(holder.profile_image, item.getProfileImage()));
        }
        return convertView;
    }

}

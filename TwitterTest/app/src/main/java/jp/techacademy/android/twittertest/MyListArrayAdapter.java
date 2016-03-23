package jp.techacademy.android.twittertest;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by k-matsuo on 2016/03/04.
 */
public class MyListArrayAdapter extends ArrayAdapter<CustomListData> {

    private LayoutInflater inflater;
    private ViewHolder holder;
    private Context context;

    private class ViewHolder {
        TextView user_name;
        TextView created_ad;
        TextView text;
        ImageView profile_image;
        TextView retweet_flg;

        ViewHolder (View view) {
            profile_image = (ImageView) view.findViewById(R.id.custom_list_profile_image);
            user_name = (TextView) view.findViewById(R.id.custom_list_user_name);
            created_ad = (TextView) view.findViewById(R.id.custom_list_created_ad);
            text = (TextView) view.findViewById(R.id.custom_list_text);
            retweet_flg = (TextView) view.findViewById(R.id.custom_list_retweet_flg);
        }
    }

    public MyListArrayAdapter(Context context, int resource,
                              List<CustomListData> objects){
        super(context, resource, objects);

        inflater = (LayoutInflater) context.getSystemService(context.LAYOUT_INFLATER_SERVICE);
        this.context = context;
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
            setSpannableString(holder.text, item.getText());
        }

        if (holder.profile_image != null) {
            ImageCache cache = new ImageCache();

            // キャッシュからBitmapを取得
            Bitmap image = cache.getImage(item.getProfileImage());

            // キャッシュに画像が存在しない場合はサーバーから取得
            if (image == null) {
                //タグを設定
                holder.profile_image.setTag(item.getProfileImage());
                //画像取得スレッド起動
                ImageLoaderTask imageTask = new ImageLoaderTask();
                imageTask.execute(imageTask.new Request(holder.profile_image, item.getProfileImage()));
            } else {
                holder.profile_image.setImageBitmap(image);
            }
        }

        // リツイートフラグが設定されている
        if (!item.getReTweetFlg().toString().isEmpty()) {
            //アイテムデータにリツイートフラグを設定
            holder.retweet_flg.setText(item.getReTweetFlg());
            holder.retweet_flg.setVisibility(View.VISIBLE);
        }
        else {
            holder.retweet_flg.setVisibility(View.GONE);
        }

        return convertView;
    }

    private void setSpannableString(View view,String message) {

        // リンク化対象の文字列、リンク先 URL を指定する
        Map<String, String> map = new HashMap<String, String>();
        map.put("(http://|https://){1}[\\w\\.\\-/:\\#\\?\\=\\&\\;\\%\\~\\+]+", "http://www.yahoo.co.jp");

        // SpannableString の取得
        SpannableString ss = createSpannableString(message, map);

        // SpannableString をセットし、リンクを有効化する
        TextView textView = (TextView) view.findViewById(R.id.custom_list_text);
        textView.setText(ss);
        textView.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private SpannableString createSpannableString(String message, Map<String, String> map) {

        SpannableString ss = new SpannableString(message);

        for (final Map.Entry<String, String> entry : map.entrySet()) {
            int start = 0;
            int end = 0;

            // リンク化対象の文字列の start, end を算出する
            Pattern pattern = Pattern.compile(entry.getKey());
            Matcher matcher = pattern.matcher(message);

            while (matcher.find()) {
                start = matcher.start();
                end = matcher.end();
                entry.setValue(matcher.group());
                break;
            }

            // SpannableString にクリックイベント、パラメータをセットする
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    String url = entry.getValue();
                    try {
                        Uri uri = Uri.parse(url);
                        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                    } catch (Exception e) {
                        Log.d("debug", "createSpannableString onClick: url error!");
                    }
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return ss;
    }
}

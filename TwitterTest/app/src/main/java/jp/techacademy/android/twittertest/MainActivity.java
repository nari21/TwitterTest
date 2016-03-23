package jp.techacademy.android.twittertest;

import android.app.Activity;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ClickableSpan;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MainActivity extends Activity {

//    private ImageCache imageCache;

    private ListView listView;
    private Button update;
    private MyListArrayAdapter listAdapter = null;
    private ProgressBar progress;

    private final String TABLE_NAME = "sample_table";
    private final String FIELD = "_id";
    private final String FIELD_TEXT = "text";
    private final String FIELD_CREATED_AD = "created_ad";
    private final String FIELD_USER_NAME = "user_name";
    private final String FIELD_PROFILE_IMAGE = "profile_image_url";

    private final String FIELD_RE_TEXT = "re_text";
    private final String FIELD_RE_CREATED_AD = "re_created_ad";
    private final String FIELD_RE_USER_NAME = "re_user_name";
    private final String FIELD_RE_PROFILE_IMAGE = "re_profile_image_url";

    private final String CREATE_TABLE_SQL = "CREATE TABLE " +TABLE_NAME+" ( "+FIELD+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +FIELD_USER_NAME+" TEXT NOT NULL ,"+FIELD_PROFILE_IMAGE+" TEXT," +FIELD_CREATED_AD+" TEXT NOT NULL ," + FIELD_TEXT +" TEXT NOT NULL ,"
            +FIELD_RE_USER_NAME+" TEXT,"+FIELD_RE_PROFILE_IMAGE+" TEXT," +FIELD_RE_CREATED_AD+" TEXT," + FIELD_RE_TEXT +" TEXT"+ ");";

    @Override
    protected void onPostResume() {
        super.onPostResume();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // ビューの設定
        listView = (ListView) findViewById(R.id.ListView);
        update = (Button) findViewById(R.id.Button1);
        progress = (ProgressBar) findViewById(R.id.progress);

        progress.setVisibility(View.GONE);

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Twitterタイムラインを再取得する
                StartTwitterRequest();
            }
        });

//        // キャッシュの設定
//        final int memClass = ((ActivityManager)getSystemService(Context.ACTIVITY_SERVICE)).getMemoryClass();
//
//        // Use 1/8th of the available memory for this memory cache.
//        final int cacheSize = 1024 * 1024 * memClass / 8;
//
//        LruCache memoryCache = new LruCache<String, Bitmap>(cacheSize) {
//            @Override
//            protected int sizeOf(String key, Bitmap bitmap) {
//                // The cache size will be measured in bytes rather than number
//                // of items.
//                return bitmap.getByteCount();
//            }
//        };
//
//        ImageProcessor processor = new ImageProcessor(this, memoryCache);


        // DBからデータを取得
        boolean result = readDb();
        // DBからデータを取得できなかった場合はHTTP通信で取得する
        if(!result) {
            //Twitterからタイムライン取得
            StartTwitterRequest();
        }

    }

    private void StartTwitterRequest() {
        AsyncTwitterRequest task = new AsyncTwitterRequest() {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
                // プログレスバー表示
                progress.setVisibility(View.VISIBLE);
                // 更新ボタンをトーンダウン
                update.setEnabled(false);
            }

            @Override
            protected void onPostExecute(List<TwitterData> result) {
                // サーバーからデータを取得できた場合
                if (result != null) {
                    // DBにデータを登録
                    createDb(result);

                    // DBからデータ読み込み
                    if (!readDb()) {
                        Toast.makeText(getApplicationContext(), "DBからデータの取得に失敗しました。", Toast.LENGTH_SHORT).show();
                    }

                    Toast.makeText(getApplicationContext(), "タイムラインを取得しました。", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(getApplicationContext(), "タイムラインの取得に失敗しました。", Toast.LENGTH_SHORT).show();
                }
                // プログレスバー非表示
                progress.setVisibility(View.GONE);
                // 更新ボタンをトーンアップ
                update.setEnabled(true);
            }
        };

        task.execute();
    }

    // DBからデータを取得しリストビューに設定
    private boolean readDb() {
        boolean result = false;

        //DBの作成
        MySQLiteOpenHelper hlpr = new MySQLiteOpenHelper(getApplicationContext(), CREATE_TABLE_SQL);
        SQLiteDatabase mydb = hlpr.getReadableDatabase();

        try {
            // 行の検索
            Cursor cursor = mydb.query(TABLE_NAME, new String[]{FIELD, FIELD_USER_NAME, FIELD_PROFILE_IMAGE, FIELD_CREATED_AD, FIELD_TEXT,
                    FIELD_RE_USER_NAME, FIELD_RE_PROFILE_IMAGE, FIELD_RE_CREATED_AD, FIELD_RE_TEXT}, null, null, null, null, "_id DESC");

            int num = cursor.getCount();
            // データが１件以上ある場合
            if (num != 0) {
                // DBからデータを取得し画面を更新する

                // リストのアイテムを作成
                List<CustomListData> listItem = new ArrayList<CustomListData>();

                // カーソルを最後に移動
                // 最新のデータからリストの先頭にセットしていく
                cursor.moveToLast();
                do {
                    // ツイート内容を取得
                    String user_name = cursor.getString(cursor.getColumnIndex(FIELD_USER_NAME));
                    String profile_image_url = cursor.getString(cursor.getColumnIndex(FIELD_PROFILE_IMAGE));
                    String created_ad = cursor.getString(cursor.getColumnIndex(FIELD_CREATED_AD));
                    String text = cursor.getString(cursor.getColumnIndex(FIELD_TEXT));

                    String re_user_name = cursor.getString(cursor.getColumnIndex(FIELD_RE_USER_NAME));
                    String retweet_flg = "";

                    // リツイート情報が存在する場合
                    if(re_user_name != null) {
                        retweet_flg = user_name + "さんがリツイートしました。";
                        // リツイート情報を表示する
                        user_name = re_user_name;
                        profile_image_url = cursor.getString(cursor.getColumnIndex(FIELD_RE_PROFILE_IMAGE));
                        created_ad = cursor.getString(cursor.getColumnIndex(FIELD_RE_CREATED_AD));
                        text = cursor.getString(cursor.getColumnIndex(FIELD_RE_TEXT));
                    }

                    // アイテムの追加
                    CustomListData itemData = new CustomListData(user_name, created_ad, text, profile_image_url, retweet_flg);
                    listItem.add(itemData);
                } while (cursor.moveToPrevious());
                listAdapter = new MyListArrayAdapter(getApplicationContext(), 0, listItem);
                listView.setAdapter(listAdapter);
                listAdapter.notifyDataSetChanged();

                result = true;
            }
            cursor.close();
        } finally {
            mydb.close();
        }

        return result;
    }

    // DBを作成しデータを登録
    private boolean createDb(List<TwitterData> data) {
        boolean result = false;

        // DBにデータを登録
        // DBの作成
        MySQLiteOpenHelper hlpr = new MySQLiteOpenHelper(getApplicationContext(), CREATE_TABLE_SQL);
        SQLiteDatabase mydb = hlpr.getWritableDatabase();

        // データを全て削除
        mydb.delete(TABLE_NAME, FIELD + " like '%'", null);

        for (TwitterData setData : data) {
            // データの追加
            ContentValues values = new ContentValues();
            values.put(FIELD_USER_NAME, setData.user_name);
            values.put(FIELD_PROFILE_IMAGE, setData.profile_image_url);
            values.put(FIELD_TEXT, setData.text);
            values.put(FIELD_CREATED_AD, setData.created_ad);

            // リツイート情報が存在する場合
            if(setData.retweet != null) {
                values.put(FIELD_RE_USER_NAME, setData.retweet.user_name);
                values.put(FIELD_RE_PROFILE_IMAGE, setData.retweet.profile_image_url);
                values.put(FIELD_RE_TEXT, setData.retweet.text);
                values.put(FIELD_RE_CREATED_AD, setData.retweet.created_ad);
            }

            mydb.insert(TABLE_NAME, null, values);
        }

        mydb.close();

        result = true;
        return result;
    }


//    public class ImageProcessor {
//        public ImageProcessor(Context context, LruCache<String, Bitmap> memoryCache) {
//            // Memory Cache
//            mMemoryCache = memoryCache;
//        }
//
//        private LruCache<String, Bitmap> mMemoryCache;
//
//        public void addBitmapToMemoryCache(String key, Bitmap bitmap) {
//            if (getBitmapFromMemCache(key) == null) {
//                mMemoryCache.put(key, bitmap);
//            }
//        }
//
//        public Bitmap getBitmapFromMemCache(String key) {
//            return mMemoryCache.get(key);
//        }
//
//        public void loadBitmap(Context context, String filePath, ImageView imageView, Bitmap loadingBitmap) {
//
//            // キャッシュにあるかチェック
//            final Bitmap bitmap = getBitmapFromMemCache(filePath);
//
//            if (bitmap != null) {
//                imageView.setImageBitmap(bitmap);
//
//            } else {
//                //画像取得スレッド起動
//                ImageLoaderTask imageTask = new ImageLoaderTask();
//                imageTask.execute(imageTask.new Request(holder.profile_image, item.getProfileImage()));
//                }
//            }
//        }
//
//
//    }

    public SpannableString createSpannableString(String message, Map<String, String> map) {

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
                break;
            }

            // SpannableString にクリックイベント、パラメータをセットする
            ss.setSpan(new ClickableSpan() {
                @Override
                public void onClick(View textView) {
                    String url = entry.getValue();
                    Uri uri = Uri.parse(url);
                    Intent intent = new Intent(Intent.ACTION_VIEW, uri);
                    startActivity(intent);
                }
            }, start, end, Spanned.SPAN_INCLUSIVE_INCLUSIVE);
        }

        return ss;
    }

}

package jp.techacademy.android.twittertest;

import android.app.Activity;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {

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
    private final String CREATE_TABLE_SQL = "CREATE TABLE " +TABLE_NAME+" ( "+FIELD+" INTEGER PRIMARY KEY AUTOINCREMENT, "
            +FIELD_USER_NAME+" TEXT NOT NULL ,"+FIELD_PROFILE_IMAGE+" TEXT," +FIELD_CREATED_AD+" TEXT NOT NULL ," + FIELD_TEXT +" TEXT NOT NULL);";

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
            Cursor cursor = mydb.query(TABLE_NAME, new String[]{FIELD, FIELD_USER_NAME, FIELD_PROFILE_IMAGE, FIELD_CREATED_AD, FIELD_TEXT}, null, null, null, null, "_id DESC");

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

                    // アイテムの追加
                    CustomListData itemData = new CustomListData(user_name, created_ad, text, profile_image_url);
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
            mydb.insert(TABLE_NAME, null, values);
        }

        mydb.close();

        result = true;
        return result;
    }

}

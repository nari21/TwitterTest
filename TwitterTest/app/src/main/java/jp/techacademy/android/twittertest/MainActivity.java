package jp.techacademy.android.twittertest;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.OAuthProvider;
import oauth.signpost.basic.DefaultOAuthConsumer;
import oauth.signpost.basic.DefaultOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;

public class MainActivity extends Activity {

    private ListView listView;
    private Button update;
    private MyListArrayAdapter listAdapter = null;
    private ProgressBar progress;
    private ProgressDialog progressDialog;

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

    private final String CONSUMER_KEY = "uJqR5qQ3gQmBcKmaRQP7eh9rp";
    private final String CONSUMER_SECRET = "efGTeiCtUvKz2gFtd6RzY0IVyoKtkuoE6yuNvbSnXc0nunr3j5";
    private final String REQUEST_TOKEN_URL = "https://twitter.com/oauth/request_token";
    private final String ACCESS_TOKEN_URL = "https://twitter.com/oauth/access_token";
    private final String AUTHORIZE_URL = "https://twitter.com/oauth/authorize";
    private final String CALLBACK = "myapp://callback/";
    private OAuthConsumer mConsumer;
    private OAuthProvider mProvider;

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

        // キャッシュの初期化
        new BitmapCache();

        update.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RequestTwitter();
            }
        });

        // DBからデータを取得
        boolean result = readDb();
        // DBからデータを取得できなかった場合はHTTP通信で取得する
        if(!result) {
            RequestTwitter();
        }
    }

    private void RequestTwitter() {
        // トークン読み込み
        SharedPreferences data = getSharedPreferences("Token", Context.MODE_PRIVATE);
        String token = data.getString("token", "");
        String token_secret = data.getString("token_secret", "");

        // トークン未取得の場合
        if(token.isEmpty() || token_secret.isEmpty()) {
            // OAuth認証
            OAuthRequestAsyncTask oAuthRequestAsyncTask = new OAuthRequestAsyncTask();
            oAuthRequestAsyncTask.execute();
        }
        // トークン取得済みの場合
        else {
            // Twitterタイムラインを再取得する
            StartTwitterRequest(token, token_secret);
        }
    }

    private void StartTwitterRequest(String token, String token_secret) {
        AsyncTwitterRequest task = new AsyncTwitterRequest() {
            @Override
            protected void onPreExecute() {
                dispProgress();
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
                hideProgress();
            }
        };

        Map<String,String> map = new HashMap<>();
        map.put("token", token);
        map.put("token_secret", token_secret);
        task.execute(map);
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

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);

        // プログレスバー非表示
        hideProgress();

        Uri uri = intent.getData();
        // ブラウザ認証からのコールバック処理
        if (uri != null && uri.toString().startsWith(CALLBACK)) {
            // アクセストークン取得およびリクエスト処理
            OAuthAccessAsyncTask oAuthAccessAsyncTask = new OAuthAccessAsyncTask();
            oAuthAccessAsyncTask.execute(uri);
        }
    }

    public class OAuthRequestAsyncTask extends AsyncTask<Void, Void, Void> {
        @Override
        protected void onPreExecute() {
//            dispProgress();
        }

        @Override
        protected Void doInBackground(Void...arg0) {
            // Oauth認証
            try {
//                mConsumer = new CommonsHttpOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
//                mProvider = new CommonsHttpOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
                mConsumer = new DefaultOAuthConsumer(CONSUMER_KEY, CONSUMER_SECRET);
                mProvider = new DefaultOAuthProvider(REQUEST_TOKEN_URL, ACCESS_TOKEN_URL, AUTHORIZE_URL);
                String authUrl = mProvider.retrieveRequestToken(mConsumer, CALLBACK);
                // ブラウザに認証ページを開かせる
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(authUrl)));
            } catch (OAuthMessageSignerException e) {
                e.printStackTrace();
            } catch (OAuthNotAuthorizedException e) {
                e.printStackTrace();
            } catch (OAuthExpectationFailedException e) {
                e.printStackTrace();
            } catch (OAuthCommunicationException e) {
                e.printStackTrace();
            }
            return null;
        }
    }

    private class OAuthAccessAsyncTask extends AsyncTask<Uri, Void, Void> {
        @Override
        protected Void doInBackground(Uri...uris) {
            Uri uri = uris[0];
            final String oauthVerifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
            try {
                // AccessToken取得
                mProvider.retrieveAccessToken(mConsumer, oauthVerifier);
                Log.d("debug", "ACCESS_TOKEN : " + mConsumer.getToken());
                Log.d("debug", "ACCESS_TOKEN_SECRET : " + mConsumer.getTokenSecret());

                // 取得したトークンを保存
                SharedPreferences data = getSharedPreferences("Token", Context.MODE_PRIVATE);
                SharedPreferences.Editor editor = data.edit();
                editor.putString("token", mConsumer.getToken());
                editor.putString("token_secret", mConsumer.getTokenSecret());
                editor.apply();
            } catch (Exception e) {
                e.printStackTrace();
            }
            return null;
        }

        @Override
        protected void onPostExecute(Void aVoid) {
            // トークン読み込み
            SharedPreferences data = getSharedPreferences("Token", Context.MODE_PRIVATE);
            String token = data.getString("token", "");
            String token_secret = data.getString("token_secret", "");

            // Twitterタイムラインを取得する
            StartTwitterRequest(token, token_secret);
        }
    }

    private void dispProgress() {
//        // プログレスバー表示
//        progress.setVisibility(View.VISIBLE);
//        // 更新ボタンをトーンダウン
//        update.setEnabled(false);

        progressDialog = ProgressDialog.show( this, "Please wait", "Loading data...");
    }

    private void hideProgress() {
//        // プログレスバー非表示
//        progress.setVisibility(View.GONE);
//        // 更新ボタンをトーンアップ
//        update.setEnabled(true);
        if(progressDialog != null) {
            progressDialog.dismiss();
            progressDialog = null;
        }
    }
}

package com.example.fripl.cut_in_app;

import android.app.Activity;
import android.app.DialogFragment;
import android.content.pm.ApplicationInfo;
import android.os.AsyncTask;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import org.w3c.dom.Text;

import java.security.acl.LastOwnerException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {
    //Log用
    private static final String TAG = "MainActivity";

    //選択された通知アプリの情報
    public static AppData selAppData;

    /* 選択されたイベントの情報
     * 0 = 電池残量
     * 1 = ロック解除
     * 2 = アプリ通知
     */
    public static int selEvent = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //イベント選択スピナーのオブジェクト取得・アイテム選択イベント追加
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(eventSelected);

        //アプリ選択ボタンのオブジェクト取得・イベント追加
        Button selAppButton = (Button)findViewById(R.id.selAppButton);
        selAppButton.setOnClickListener(selAppButtonClick);


    }


    //イベントスピナー選択時処理
    private AdapterView.OnItemSelectedListener eventSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            //リソースからスピナーアイテム取得
            String[] str = getResources().getStringArray(R.array.event_entries);

            //必要オブジェクト取得
            TextView eventName = (TextView) findViewById(R.id.eventName);
            TextView appNameText = (TextView) findViewById(R.id.appNameText);
            TextView appName = (TextView)findViewById(R.id.appName);
            Button selAppButton = (Button)findViewById(R.id.selAppButton);

            //選択イベント格納
            selEvent = position;
            if(position != 2) {  //アプリ通知でない場合はアプリ名は無し
                ImageView appIcon = (ImageView)findViewById(R.id.appIcon);
                appIcon.setImageResource(android.R.drawable.sym_def_app_icon);

                //アプリ選択しないので非表示
                selAppButton.setVisibility(View.INVISIBLE);
                appName.setVisibility(View.INVISIBLE);
                appNameText.setVisibility(View.INVISIBLE);

            }else{  //アプリ通知の場合
                // アプリ選択するので表示
                selAppButton.setVisibility(View.VISIBLE);
                appName.setVisibility(View.VISIBLE);
                appNameText.setVisibility(View.VISIBLE);
            }

            //選択されたpositionの文字列を表示
            eventName.setText(str[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {  //アイテム非選択時
            //とりあえず何もしない
        }
    };

    //アプリ読み込みタスク作成
    final LoadAppInfoTask loadAppInfoTask = new LoadAppInfoTask(this);
    //アプリ選択ボタン処理
    private View.OnClickListener selAppButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //アプリ選択ダイアログ表示
            if (AppDialog.apploaded){   //アプリが読み込み済みなら表示
                DialogFragment appDialog = new AppDialog();
                appDialog.show(getFragmentManager(), "appDialog");
            }else {                     //アプリがまだ読み込めてないなら読み込み・表示
                loadAppInfoTask.execute(0);
            }
        }
    };


}

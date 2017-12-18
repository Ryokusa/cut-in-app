package com.example.fripl.cut_in_app;

import android.app.DialogFragment;
import android.content.pm.ApplicationInfo;
import android.media.Image;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;

public class MainActivity extends AppCompatActivity {
    //選択された通知アプリの情報
    public static ApplicationInfo selAppInfo;
    public static AppData selAppData;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //イベント選択スピナーのオブジェクト取得・アイテム選択イベント追加
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(eventSelected);


    }

    public void onReturnDialog()
    {
        TextView appName = (TextView)findViewById(R.id.appName);
        ImageView appIcon = (ImageView)findViewById(R.id.appIcon);
        appName.setText("アプリ名:" + selAppData.getAppName());
        appIcon.setImageDrawable(selAppData.getIconDrawable());
    }

    //イベントスピナー選択時処理
    private AdapterView.OnItemSelectedListener eventSelected = new AdapterView.OnItemSelectedListener() {
        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

            //リソースからスピナーアイテム取得
            String[] str = getResources().getStringArray(R.array.event_entries);

            TextView eventName = (TextView) findViewById(R.id.eventName);

            if(position == 2) {   //アプリ通知の場合はアプリ選択画面へ
                //アプリ選択ダイアログ生成
                DialogFragment appDialogF = new AppDialog();
                appDialogF.show(getFragmentManager(), "app");
            }else{
                TextView appName = (TextView)findViewById(R.id.appName);
                ImageView appIcon = (ImageView)findViewById(R.id.appIcon);
                appName.setText("アプリ名:");
                appIcon.setImageResource(android.R.drawable.sym_def_app_icon);
            }
            //選択されたpositionの文字列を表示
            eventName.setText("イベント名:" + str[position]);
        }

        @Override
        public void onNothingSelected(AdapterView<?> parent) {  //アイテム非選択時
            //とりあえずなし
        }
    };
}

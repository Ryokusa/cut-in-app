package com.example.fripl.appinfoget;

import android.app.AppOpsManager;
import android.app.NotificationManager;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //ListViewのオブジェクト取得
        ListView listView = (ListView)findViewById(R.id.ListView1);

        //このコンテキストのpackageManagerを取得
        PackageManager pm = this.getPackageManager();

        //アプリデータを格納するリスト生成
        List<AppData> appDataList = new ArrayList<AppData>();

        //applicationInfo型の配列にアプリデータを格納
        List<ApplicationInfo> list = pm.getInstalledApplications(0);

        Context context = getApplicationContext();  //初期値わからないのでとりあえず
        //取得したApplicationInfoをadapterに追加していく
        for (ApplicationInfo appInfo : list) {


            //APIレベル19以上は通知の許可権限有りのアプリが判定出来るので別処理
            if(Build.VERSION.SDK_INT >= 19) {
                try {   //存在しないpackageがあるとエラーを出すので例外処理
                    context.createPackageContext(appInfo.packageName, CONTEXT_IGNORE_SECURITY | CONTEXT_INCLUDE_CODE);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }
                //通知マネージャー取得
                NotificationManagerCompat nm = NotificationManagerCompat.from(getApplicationContext());

                //通知がONのアプリのみadapterに追加
                if (nm.areNotificationsEnabled() && (appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ){
                    appDataList.add(new AppData(appInfo.loadLabel(pm).toString(), pm.getApplicationIcon(appInfo)));
                }
            }else {
                if ((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0) {   //プリインストールを排除
                    appDataList.add(new AppData(appInfo.loadLabel(pm).toString(), pm.getApplicationIcon(appInfo)));
                }
            }
        }
        //ArrayAdapterを作成
        AppDataAdapter adapter = new AppDataAdapter(this, R.id.ListView1, appDataList);
        //ListView1にセット
        listView.setAdapter(adapter);

    }
}
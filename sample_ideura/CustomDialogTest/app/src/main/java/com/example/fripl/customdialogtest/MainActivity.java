package com.example.fripl.customdialogtest;

import android.app.AlarmManager;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Adapter;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    //アプリデータリスト
    private List<AppData> appDataList = new ArrayList<AppData>();

    private AppData selAppData;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //packagemanager作成
        PackageManager pm = this.getPackageManager();

        //AppInfo取得
        final List<ApplicationInfo> appInfoList = pm.getInstalledApplications(0);

        for (ApplicationInfo appInfo : appInfoList){
            //プリインストール排除
            if((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0){
                appDataList.add(new AppData(pm.getApplicationLabel(appInfo).toString(), pm.getApplicationIcon(appInfo)));
            }
        }

        Button button = (Button) findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                AppDataAdapter adapter = new AppDataAdapter(MainActivity.this, R.layout.list_view_layout, appDataList);
                AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                builder.setTitle("だいあろうぐ");
                builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selAppData = appDataList.get(which);
                        ImageView appIcon = (ImageView)findViewById(R.id.imageView2);
                        TextView appName = (TextView)findViewById(R.id.textView3);
                        appIcon.setImageDrawable(selAppData.getIconDrawable());
                        appName.setText(selAppData.getAppName());
                    }
                });
                AlertDialog dialog = builder.create();
                dialog.show();
            }
        });


    }

}

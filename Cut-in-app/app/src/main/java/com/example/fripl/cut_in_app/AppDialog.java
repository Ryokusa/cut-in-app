package com.example.fripl.cut_in_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2017/12/18.
 */

public class AppDialog extends DialogFragment{
    List<ApplicationInfo> appInfoList;
    List<AppData> appDataList = new ArrayList<AppData>();

    @Override
    public synchronized Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("テストダイアログ");

        //アプリ情報取得
        PackageManager pm = getActivity().getPackageManager();
        appInfoList = pm.getInstalledApplications(0);

        for(ApplicationInfo appInfo : appInfoList){
            if((appInfo.flags & ApplicationInfo.FLAG_SYSTEM) == 0 ) {
                appDataList.add(new AppData(pm.getApplicationLabel(appInfo).toString(), pm.getApplicationIcon(appInfo)));
            }
        }

        //アダプター作成
        AppDataAdapter adapter = new AppDataAdapter(builder.getContext(), 0, appDataList);

        //オンクリックイベント
        builder.setAdapter(adapter, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                MainActivity.selAppInfo = appInfoList.get(which);
                MainActivity.selAppData = appDataList.get(which);
                MainActivity activity = (MainActivity)getActivity();
                activity.onReturnDialog();  //戻り処理
            }
        });


        return builder.create();
    }

}

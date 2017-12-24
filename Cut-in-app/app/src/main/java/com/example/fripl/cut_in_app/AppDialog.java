package com.example.fripl.cut_in_app;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;


import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2017/12/18.
 * アプリ選択ダイアログDialogFragment
 */

public class AppDialog extends DialogFragment{
    List<ApplicationInfo> appInfoList;
    List<AppData> appDataList = new ArrayList<AppData>();

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle("アプリ選択ダイアログ");

        //progressDialog
        DialogFragment progressDialog = new ProgressDialog();
        progressDialog.show(getFragmentManager(), "progress");

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
                //選択されたアプリの情報をセット
                MainActivity.selAppData = appDataList.get(which);

                //アプリ名とアイコンをそれぞれのコントロールにセット
                MainActivity activity = (MainActivity)getActivity();
                TextView appName = (TextView)activity.findViewById(R.id.appName);
                ImageView appIcon = (ImageView)activity.findViewById(R.id.appIcon);
                appName.setText(appDataList.get(which).getAppName());
                appIcon.setImageDrawable(appDataList.get(which).getIconDrawable());
            }
        });


        progressDialog.dismiss();
        return builder.create();
    }

}

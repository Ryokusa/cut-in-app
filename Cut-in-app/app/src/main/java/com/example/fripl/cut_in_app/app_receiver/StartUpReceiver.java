package com.example.fripl.cut_in_app.app_receiver;

import android.app.AlertDialog;
import android.app.FragmentManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.fripl.cut_in_app.CutInService;
import com.example.fripl.cut_in_app.Dialog.SimpleAlertDialog;

/**
 * Created by fripl on 2018/01/20.
 * システム起動時のレシーバー
 */

public class StartUpReceiver extends BroadcastReceiver {
    private static final String TAG = "StartUpReceiver";

    //起動時
    @Override
    public void onReceive(Context context, Intent intent) {
        //サービス開始
        Log.i(TAG, "onReceive");
        if(Intent.ACTION_BOOT_COMPLETED.equals(intent.getAction()) && !CutInService.onService){
            Intent serviceIntent = new Intent(context, CutInService.class);
            context.startService(new Intent(serviceIntent));
        }
    }

}
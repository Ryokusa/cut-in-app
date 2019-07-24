package com.example.fripl.cut_in_app.app_receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.example.fripl.cut_in_app.CutInService;

/**
 * Created by fripl on 2018/01/20.
 * 画面ONレシーバー
 */

public class ScreenOnReceiver extends BroadcastReceiver {
    private static final String TAG = "ScreenOnReceiver";

    @Override
    public void onReceive(Context context, Intent intent){
        if(Intent.ACTION_SCREEN_ON.equals(intent.getAction())) {
            Log.i(TAG, "onReceive");
            CutInService.play("screen_on");
        }
    }
}

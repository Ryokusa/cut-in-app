package com.example.fripl.serviceanimation;

import android.app.Activity;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.Settings;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {
    public static int OVERLAY_PERMISSION_REQUEST_CODE = 2746;
    private static boolean overlayPermission;
    private boolean isConnection;
    private FilterService mService;

    //コネクション作成
    private ServiceConnection serviceConnection = new ServiceConnection() {
        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            //Binderからservice取得
            mService = ((FilterService.ServiceBinder)service).getService();
            Log.i("MainActivity", "onConnected");
            isConnection = true;
        }

        @Override
        public void onServiceDisconnected(ComponentName name) {
            mService = null;
            Log.i("MainActivity", "Disnnected");
            isConnection = false;
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button startButton = (Button)findViewById(R.id.button);
        Button endButton = (Button)findViewById(R.id.button2);

        startButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overlayPermission && !FilterService.onView) {

                    Intent intent = new Intent(MainActivity.this, FilterService.class);
                    stopService(intent);
                    startService(intent);

                    //bindService(new Intent(MainActivity.this, FilterService.class), serviceConnection, Context.BIND_AUTO_CREATE);

                }
            }
        });

        endButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (overlayPermission && FilterService.onView) {

                    Intent intent = new Intent(MainActivity.this, FilterService.class);
                    stopService(intent);

                    //unbindService(serviceConnection);

                }
            }
        });

    }





    //再表示されたとき
    @Override
    protected void onResume()
    {
        super.onResume();
        if (!(overlayPermission = checkOverlayPermission(this))) requestOverlayPermission();

    }

    //オーバーレイ権限チェック
    public Boolean checkOverlayPermission(Context context){
        if(Build.VERSION.SDK_INT < 23){
            //APILevel23未満は常時ON
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    public void requestOverlayPermission()
    {
        Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
        this.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE){
            if(checkOverlayPermission(this)){
                overlayPermission = true;
                return;
            }
        }
        //オーバレイの権限がない場合
        overlayPermission = false;
        Toast.makeText(this, "オーバーレイの権限がないと実行できません", Toast.LENGTH_SHORT).show();

    }

    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        //unbindService(serviceConnection);
    }


}

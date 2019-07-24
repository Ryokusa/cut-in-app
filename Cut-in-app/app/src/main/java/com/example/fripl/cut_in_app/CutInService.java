package com.example.fripl.cut_in_app;

import android.app.Notification;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.graphics.PixelFormat;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.WindowManager;

import com.example.fripl.cut_in_app.data_manage.CutInDataManager;
import com.example.fripl.cut_in_app.app_receiver.ScreenOnReceiver;
import com.example.fripl.cut_in_app.app_receiver.UnlockReceiver;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2018/01/16.
 * バックグラウンドサービス
 */

public class CutInService extends NotificationListenerService {
    private static final String TAG = "CutInService";
    public static boolean onService = false;
    private WindowManager wm;
    public static List<CutIn> cutInList = new ArrayList<CutIn>();   //カットイン
    private static Context context;

    //アプリにセットされたカットインリスト
    public static List<CutInSet> cutInSetList = new ArrayList<CutInSet>();

    //再生するカットイン
    public static CutIn cutIn;

    //ディスプレイ情報定義
    int height, width, dp;

    //カットインデータマネージャー
    private CutInDataManager cidm;

    //別スレッドから実行するためのHandler
    //実際は通知受け取り時は別スレッドなため、再生処理をメインスレッドに渡すため
    Handler handler;

    public CutInService(){
        //何もなし
    }

    @Override
    public void onCreate(){
        super.onCreate();
        Log.i(TAG, "onCreate");

        context = getApplicationContext();

        //カットイン取得
        cutInList = MainActivity.cutInList;

        //ディスプレイ情報格納
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager)getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        height = displayMetrics.heightPixels;
        width = displayMetrics.widthPixels;
        dp = (int)displayMetrics.density;

        //カットインデータマネージャー作成
        cidm = new CutInDataManager(getApplicationContext());

        //メインスレッドのHandler取得
        handler = new Handler();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId){
        Log.i(TAG, "onStartCommand");

        onService = true;

        //
        PackageManager pm = getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(this, CutInService.class),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);

        pm.setComponentEnabledSetting(new ComponentName(this, CutInService.class),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        //重ね合わせするViewの設定
        WindowManager.LayoutParams layoutParams = new WindowManager.LayoutParams(
                WindowManager.LayoutParams.MATCH_PARENT,
                WindowManager.LayoutParams.MATCH_PARENT,
                (Build.VERSION.SDK_INT >= 26) ? WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY : WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY,
                WindowManager.LayoutParams.FLAG_NOT_TOUCH_MODAL,
                PixelFormat.TRANSLUCENT
        );

        //WindowManager取得
        wm = (WindowManager)getSystemService(Context.WINDOW_SERVICE);

        //メインカットイン作成
        cutIn = new CutIn(getApplicationContext());

        //メインカットインをレイヤーに追加
        wm.addView(cutIn, layoutParams);

        //カットインデータ読み込み

        if((cutInList = cidm.cutInListLoad()) != null) {
            //読み込み成功
        }else{
            //カットインデータがない場合
            //サンプルもないので作成
            makeSampleCutIn();

            //次回以降の起動のためにカットイン保存
            cidm.cutInListSave(new ArrayList<CutIn>(cutInList));
        }

        //カットインセット読み込み
        if((cutInSetList = cidm.cutInSetListLoad()) == null){
            //カットインセットが無い場合リストを初期化
            cutInSetList = new ArrayList<>();
        }

        //レシーバー登録
        IntentFilter filter = new IntentFilter(Intent.ACTION_SCREEN_ON);    //画面ON
        getApplicationContext().registerReceiver(new ScreenOnReceiver(), filter);
        filter = new IntentFilter(Intent.ACTION_USER_PRESENT);  //ロック解除
        getApplicationContext().registerReceiver(new UnlockReceiver(), filter);


        //保存読み込みテスト (成功)
        /*
        CutInDataManager cidm = new CutInDataManager(getApplicationContext());
        ArrayList<String> cutInNameList = new ArrayList<>();
        for(CutIn cutIn : cutInList){
            cutInNameList.add(cutIn.getTitle());
        }
        cidm.cutInNameSave(cutInNameList);
        Log.i(TAG, "dataSave");
        ArrayList<String> loadData = new ArrayList<>();
        loadData = cidm.cutInNameLoad();
        Log.i(TAG, "dataLoad");
        */

        //常駐化
        Intent mainIntent = new Intent(this, MainActivity.class);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, mainIntent, 0);
        Notification notification = new Notification.Builder(this)
                .setContentIntent(pendingIntent)
                .setContentTitle("カットインアプリ")
                .setContentText("起動中")
                .setSmallIcon(R.mipmap.ic_launcher)
                .build();
        notification.flags = Notification.FLAG_ONGOING_EVENT;
        startForeground(startId, notification);

        return START_STICKY;
    }


    //通知が来たとき
    @Override
    public void onNotificationPosted(StatusBarNotification sbn){
        super.onNotificationPosted(sbn);

        //アプリ名を取得
        PackageManager pm = getPackageManager();
        String appName;
        try {
            appName = pm.getApplicationLabel(pm.getApplicationInfo(sbn.getPackageName(), 0)).toString();
        }catch(PackageManager.NameNotFoundException e){
            //名前が見つからなかった場合
            appName = "";
        }

        //通知内容連絡
        /*
        01-23 22:14:02.525 23162-23182/com.example.fripl.cut_in_app I/CutInService: onNotificationPosted:LINE
        01-23 22:14:02.525 23162-23182/com.example.fripl.cut_in_app I/CutInService: チャケ : 新着メッセージがあります。
        01-23 22:14:03.630 23162-23183/com.example.fripl.cut_in_app I/CutInService: onNotificationPosted:LINE
        01-23 22:14:03.630 23162-23183/com.example.fripl.cut_in_app I/CutInService: チャケ : いでうらひろゆきかっこいい
         */
        Log.i(TAG, "onNotificationPosted:" + appName);
        if(sbn.getNotification().tickerText != null){
            Log.i(TAG, "" + sbn.getNotification().tickerText.toString());
        }


        final String appNameFinal = appName;

        //別スレッドからUIを操作するのでメインスレッドにまかせる
        //アプリ名より対応したカットイン再生
        handler.post(new Runnable() {
            @Override
            public void run() {
                play(appNameFinal);
            }
        });
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        super.onNotificationRemoved(sbn);
        Log.i(TAG, "onNotificationRemoved");
    }

    //再生(CutInListの要素番号より)
    public static void play(int CutInIndex){
        if(MainActivity.cutInVisible) {
            //カットインをセット
            setCutIn(cutInList.get(CutInIndex));

            //カットイン再生
            cutIn.play();

        }
    }

    //再生(イベント名より)
    public static void play(String action){
        int index;
        if((index = getCutInSetIndex(action)) != -1){
            play(cutInSetList.get(index).getCutInId());
        }
    }

    //再生(カットイン指定)
    public static void play(CutIn sourceCutIn){
        if(MainActivity.cutInVisible) {
            setCutIn(sourceCutIn);
            cutIn.play();
        }
    }

    //メインカットインに指定したカットインを代入
    public static void setCutIn(CutIn sourceCutIn){
        cutIn.removeAllViews();   //前のオブジェクト削除
        cutIn.setThumbnail(sourceCutIn.getThumbnail());
        cutIn.setTitle(sourceCutIn.getTitle());
        cutIn.setAnimObjList(sourceCutIn.getAnimObjList());
        cutIn.addObject();      //オブジェクトをViewに追加
    }

    @Override
    public void onDestroy() {
        super.onDestroy();

        // サービスが破棄されるときには重ね合わせしていたViewを削除する
        if(cutIn != null) wm.removeView(cutIn);

        //フラグ管理
        onService = false;

        System.exit(0);
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.i(TAG, "onBind");
        //ここでサービスをスタートするとかなり早い
        Intent i = new Intent(this, CutInService.class);
        startService(i);
        return super.onBind(intent);
    }

    //サンプルカットイン作成
    private void makeSampleCutIn()
    {
        //cutInList初期化
        cutInList = new ArrayList<>();

        //サンプルカットインリスト作成(仮)
        CutIn sample1 = new CutIn(getApplicationContext(), "sample1", R.drawable.nico);
        CutIn sample2 = new CutIn(getApplicationContext(), "sample2", R.drawable.nico);
        CutIn sample3 = new CutIn(getApplicationContext(), "sample3", R.drawable.nico);
        sample1.addImageView(R.drawable.nico, 80*dp, -80*dp, 80*dp, 80*dp);
        sample1.animObjList.get(0).addAlphaHolder(1.0f);
        sample1.animObjList.get(0).addAnimator( 0);

        sample1.animObjList.get(0).addTranslationHolder(-80*dp, 80*dp);
        sample1.animObjList.get(0).addAnimator( 500);

        sample1.animObjList.get(0).addTranslationHolder(width - 2*80*dp, height);
        sample1.animObjList.get(0).addAnimator( 0);
        sample1.animObjList.get(0).addTranslationHolder(width, height - 2*80*dp);
        sample1.animObjList.get(0).addAnimator( 500);

        sample1.animObjList.get(0).addTranslationHolder(width/2 - 40*dp, -80*dp);
        sample1.animObjList.get(0).addAnimator( 0);
        sample1.animObjList.get(0).addTranslationHolder(width/2 - 40*dp, height/2 - 40*dp);
        sample1.animObjList.get(0).addAnimator( 500);

        sample1.animObjList.get(0).addAlphaHolder(.0f);
        sample1.animObjList.get(0).addAnimator( 500);

        //sample2
        sample2.addImageView(R.drawable.nico, 40*dp, 40*dp);
        sample2.animObjList.get(0).addAlphaHolder(1.0f);
        sample2.animObjList.get(0).addScaleHolder(1.0f, 1.0f);
        sample2.animObjList.get(0).addTranslationHolder(-100*dp, 40*dp);
        sample2.animObjList.get(0).addAnimator( 0);

        sample2.animObjList.get(0).addTranslationHolder(width + 80*dp, 40*dp);
        sample2.animObjList.get(0).addScaleHolder(2.0f, 2.0f);
        sample2.animObjList.get(0).addAnimator( 1000);

        sample2.animObjList.get(0).addTranslationHolder(width + 80*dp, height - 120*dp);
        sample2.animObjList.get(0).addScaleHolder(3.0f, 3.0f);
        sample2.animObjList.get(0).addAnimator( 0);

        sample2.animObjList.get(0).addTranslationHolder(-100*dp, height - 120*dp);
        sample2.animObjList.get(0).addAnimator( 500);

        //sample3
        sample3.addImageView(R.drawable.nico, 60*dp, 60*dp);
        sample3.animObjList.get(0).addAlphaHolder(1.0f);
        sample3.animObjList.get(0).addTranslationHolder(-100*dp, 0);
        sample3.animObjList.get(0).addRotateHolder(0);
        sample3.animObjList.get(0).addAnimator( 0);

        sample3.animObjList.get(0).addRotateHolder(1080);
        sample3.animObjList.get(0).addTranslationHolder(500* dp, height/2);
        sample3.animObjList.get(0).addAnimator( 3000);

        cutInList.add(sample1);
        cutInList.add(sample2);
        cutInList.add(sample3);
        CutInService.cutInList = cutInList; //カットインをサービスに伝達
    }

    //指定されたアクション名のCutIn番号を取得
    public static int getCutInSetIndex(String action){
        int i = 0;
        for (CutInSet cutInSet : cutInSetList){
            //同じアクションを探す
            if(cutInSet.getAction().equals(action)){
                return i;
            }
            i++;
        }

        return -1;  //見つからなかった場合
    }

    //カットイン削除処理
    public static void removeCutIn(int index){
        cutInList.remove(index);
        int i = 0;
        //カットインセットに削除対象のカットインを使用している場合削除
        for (i = 0; i < cutInSetList.size(); i++){
            if(cutInSetList.get(i).getCutInId() == index){
                cutInSetList.remove(i);
            }
        }
    }

    //コンテキスト取得
    public static Context getContext(){
        return context;
    }
}


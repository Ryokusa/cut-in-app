package com.example.fripl.cut_in_app;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.example.fripl.cut_in_app.data_manage.CutInDataManager;
import com.example.fripl.cut_in_app.Dialog.SimpleAlertDialog;

import java.util.ArrayList;
import java.util.List;
/* 今後の課題
 * メモリ消費削減(CutInのデータ形式を変えてcanvasを使用したほうが得？)
 * 通知内容受取(LINEの相手など)
 * TODO レイアウト改良
 */
//TODO カットイン非表示設定
//TODO カットイン選択画面に戻ったときにプレビューが違う
//TODO 回転が起こらない

public class MainActivity extends AppCompatActivity {
    //リクエストコード
    public static int OVERLAY_PERMISSION_REQUEST_CODE = 2746;

    //カットイン表示フラグ
    public static boolean cutInVisible = true;

    Menu menu;

    //Log用
    private static final String TAG = "MainActivity";

    private boolean overlayPermission = false;  //オーバーレイ権限フラグ
    private boolean dialogShowed = false;       //ダイアログ表示フラグ

    //選択された通知アプリの情報
    public static AppData selAppData = new AppData("", "", null);


    public static int selEvent = 0;/* 選択されたイベント番号 */
    public static String[] eventName;

    //カットインリスト
    public static List<CutIn> cutInList = new ArrayList<CutIn>();

    //アラートダイアログ
    SimpleAlertDialog alertDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Log.i(TAG, "onCreate");

        eventName = getResources().getStringArray(R.array.event_names);

        //イベント選択スピナーのオブジェクト取得・アイテム選択イベント追加
        Spinner spinner = (Spinner) findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(eventSelected);

        //アプリ選択ボタンのオブジェクト取得・イベント追加
        Button selAppButton = (Button)findViewById(R.id.selAppButton);
        selAppButton.setOnClickListener(selAppButtonClick);

        //イベント選択完了ボタンのオブジェクト取得・イベント追加
        Button eventOkButton = (Button)findViewById(R.id.eventOkButton);
        eventOkButton.setOnClickListener(eventOkButtonClick);

        //カットインサムネイルボタンのオブジェクト取得イベント追加
        ImageView cutInThumbnail = (ImageView)findViewById(R.id.cutInThumbnail);
        cutInThumbnail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //カットインプレビュー
                String action = MainActivity.eventName[selEvent];
                if (action.equals("app_notification")){
                    CutInService.play(selAppData.getAppName());
                }else{
                    CutInService.play(action);
                }
            }
        });

        //設定解除ボタンのオブジェクト取得。イベント追加
        Button resetButton = (Button)findViewById(R.id.resetButton);
        resetButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String action = eventName[selEvent];  //イベント名
                //現在のイベントに設定されているカットイン解除
                if(action.equals("app_notification")) {
                    //通知の場合、アプリ名を格納
                    action = selAppData.getAppName();
                }

                //イベントに対応したカットイン番号を取得
                final int index = CutInService.getCutInSetIndex(action);

                if (index != -1) {
                    //設定カットインがある場合確認ダイアログ表示
                    AlertDialog.Builder builder = new AlertDialog.Builder(MainActivity.this);
                    builder.setTitle("注意");
                    builder.setMessage("設定されたカットインを解除しますがよろしいですか？");
                    builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //キャンセル時何もせず閉じる
                        }
                    });
                    builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //OK時カットインセット削除
                            CutInService.cutInSetList.remove(index);
                            new CutInDataManager(MainActivity.this).cutInSetListSave(new ArrayList<CutInSet>(CutInService.cutInSetList));
                            reLoadScreen(); //画面更新
                        }
                    });
                    builder.create().show();
                }
            }
        });


        //通知アクセス権限がない場合は設定画面を促す
        if(!checkNotificationPermission()){
            alertDialog = SimpleAlertDialog.getInstance("注意", "通知アクセス権限がオフです。左上の設定から通知アクセス権限設定を選び、カットインアプリをONにしてください\n" +
                    "OFFの場合、アプリ通知ではカットインは作動しません。");
        }

        //サービス開始
        if(checkOverlayPermission(this) && !CutInService.onService) {
            Intent intent = new Intent(MainActivity.this, CutInService.class);
            startService(intent);
        }

    }


    //イベントスピナー選択時処理
    private AdapterView.OnItemSelectedListener eventSelected = new AdapterView.OnItemSelectedListener() {

        @Override
        public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
            //選択イベント番号格納
            selEvent = position;

            //画面更新
            reLoadScreen();

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

    //イベント選択完了ボタン
    private View.OnClickListener eventOkButtonClick = new View.OnClickListener() {
        @Override
        public void onClick(View v) {
            //選ばれたイベントがアプリかつなにもアプリが選ばれてない場合
            if (eventName[selEvent].equals("app_notification") && selAppData.getAppName().equals("")){
                //トースト表示
                Toast.makeText(getApplicationContext(), "アプリを選択してください", Toast.LENGTH_SHORT).show();
            }else{  //そうでない場合
                //画面遷移
                Intent intent = new Intent(MainActivity.this, SelCutInActivity.class);
                startActivity(intent);
            }
        }
    };

    //オーバーレイ権限チェック
    public Boolean checkOverlayPermission(Context context){
        if(Build.VERSION.SDK_INT < 23){
            //APILevel23未満は常時ON
            return true;
        }
        return Settings.canDrawOverlays(context);
    }

    //オーバーレイ権限設定画面表示
    public void requestOverlayPermission()
    {
        if(Build.VERSION.SDK_INT >= 23) {
            //23以上のみ設定画面あり
            Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + getPackageName()));
            this.startActivityForResult(intent, OVERLAY_PERMISSION_REQUEST_CODE);
        }

    }

    //オーバーレイ設定から帰ってきたときの判定用
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == OVERLAY_PERMISSION_REQUEST_CODE){
            if(checkOverlayPermission(this)){
                overlayPermission = true;

                //サービス開始
                if(!CutInService.onService) {
                    Intent intent = new Intent(MainActivity.this, CutInService.class);
                    startService(intent);
                }
                return;
            }
        }
        //オーバレイの権限がない場合
        overlayPermission = false;

    }

    //再表示されたとき
    @Override
    protected void onResume()
    {
        super.onResume();


        //オーバーレイ権限がない場合は設定画面へ
        if (!(overlayPermission = checkOverlayPermission(this))) {
            alertDialog = SimpleAlertDialog.getInstance("注意", "他のアプリの上に表示を許可をONにしてください。\nOFFの場合、カットインが表示されません");
            alertDialog.addOnClickListener(new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    //オーバーレイ権限設定画面へ
                    requestOverlayPermission();
                }
            });
            alertDialog.show(getFragmentManager(), "alertDialog");
        }

        //画面再読込
        reLoadScreen();

    }

    //オプションメニュー作成時
    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        //メニューレイアウト適用
        getMenuInflater().inflate(R.menu.main_menu, menu);

        this.menu = menu;

        //オーバーレイ権限設定できない場合は設定無効
        if(Build.VERSION.SDK_INT < 23){
            MenuItem overlaySetting = menu.findItem(R.id.overlaySettingItem);
            overlaySetting.setEnabled(false);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        int id = item.getItemId();
        switch (id){
            case R.id.overlaySettingItem:    //オーバレイ権限設定
                requestOverlayPermission();
                break;
            case R.id.notificationSettingItem:  //通知アクセス権限設定
                showNotificationPermissionSetting();
                break;
            case R.id.cutInVisible: //カットイン表示・非表示
                if(cutInVisible){
                    cutInVisible = false;
                    MenuItem cutInVisibleItem = menu.findItem(R.id.cutInVisible);
                    cutInVisibleItem.setTitle("カットイン表示");
                }else{
                    cutInVisible = true;
                    MenuItem cutInVisibleItem = menu.findItem(R.id.cutInVisible);
                    cutInVisibleItem.setTitle("カットイン非表示");
                }
        }
        return true;
    }

    //シンプルなダイアログ表示関数
    private void showDialog(String title, String msg)
    {
        alertDialog = SimpleAlertDialog.getInstance(title, msg);
        alertDialog.show(getFragmentManager(), "alertDialog");
    }

    //画面のコントロールに再読込
    public void reLoadScreen(){
        //設定されたカットインなどから各コントロールへ設定
        //リソースからスピナーアイテム取得
        String[] str = getResources().getStringArray(R.array.event_entries);

        //必要オブジェクト取得
        TextView eventName = (TextView) findViewById(R.id.eventName);
        TextView appNameText = (TextView) findViewById(R.id.appNameText);
        TextView appName = (TextView)findViewById(R.id.appName);
        Button selAppButton = (Button)findViewById(R.id.selAppButton);
        TextView cutInName = (TextView)findViewById(R.id.cutInName);
        ImageView cutInThumbnail = (ImageView)findViewById(R.id.cutInThumbnail);

        if(!MainActivity.eventName[selEvent].equals("app_notification")) {  //アプリ通知でない場合はアプリ名は無し
            //アプリ選択しないので非表示
            selAppButton.setVisibility(View.INVISIBLE);
            appName.setVisibility(View.INVISIBLE);
            appNameText.setVisibility(View.INVISIBLE);

        }else{  //アプリ通知の場合
            // アプリ選択するので表示
            selAppButton.setVisibility(View.VISIBLE);
            appName.setVisibility(View.VISIBLE);
            appName.setText(selAppData.getAppName());
            appNameText.setVisibility(View.VISIBLE);
        }

        int index;
        if(!MainActivity.eventName[selEvent].equals("app_notification")) {
            //アプリ通知でない場合
            if ((index = CutInService.getCutInSetIndex(MainActivity.eventName[selEvent])) != -1) {
                //設定されたカットインがある場合
                CutIn cutIn = CutInService.cutInList.get(CutInService.cutInSetList.get(index).getCutInId());
                cutInThumbnail.setImageDrawable(cutIn.getThumbnail());
                cutInName.setTextColor(Color.BLACK);
                cutInName.setText(cutIn.getTitle());
            } else {
                //設定されたカットインがない場合
                cutInThumbnail.setImageResource(R.mipmap.ic_launcher);
                cutInName.setTextColor(Color.argb(50, 200, 0, 0));
                cutInName.setText("none");
                //TODO サムネイルが無いことを示す画像を追加
            }
        }else{
            //アプリ通知の場合　パッケージ名から探索、挿入
            if ((index = CutInService.getCutInSetIndex(selAppData.getAppName())) != -1) {
                //設定されたカットインがある場合
                CutIn cutIn = CutInService.cutInList.get(CutInService.cutInSetList.get(index).getCutInId());
                cutInThumbnail.setImageDrawable(cutIn.getThumbnail());
                cutInName.setTextColor(Color.BLACK);
                cutInName.setText(cutIn.getTitle());
            } else {
                //設定されたカットインがない場合
                cutInThumbnail.setImageResource(R.mipmap.ic_launcher);
                cutInName.setTextColor(Color.argb(50, 200, 0, 0));
                cutInName.setText("none");
                //TODO サムネイルが無いことを示す画像を追加
            }
        }


        //選択されたイベントの文字列を表示
        eventName.setText(str[selEvent]);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    private boolean checkNotificationPermission(){
        if (Build.VERSION.SDK_INT >= 18) {
            for (String service : NotificationManagerCompat.getEnabledListenerPackages(this)) {
                if (service.equals(getPackageName()))
                    return true;
            }
            return false;
        }
        return false;
    }

    private void showNotificationPermissionSetting(){
        Intent intent = new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS");
        startActivity(intent);
    }
}

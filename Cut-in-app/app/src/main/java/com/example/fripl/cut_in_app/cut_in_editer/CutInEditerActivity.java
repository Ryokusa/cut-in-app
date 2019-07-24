package com.example.fripl.cut_in_app.cut_in_editer;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.ObjectAnimator;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.media.Image;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TabHost;
import android.widget.TextView;

import com.example.fripl.cut_in_app.CutIn;
import com.example.fripl.cut_in_app.CutInService;
import com.example.fripl.cut_in_app.R;
import com.example.fripl.cut_in_app.data_manage.AnimObj;
import com.example.fripl.cut_in_app.data_manage.CutInDataManager;

import org.w3c.dom.Text;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Created by fripl on 2018/01/22.
 *
 * //カットインエディターアクティビティ
 * //TODO 初期値処理はいじれないことを知らせる処理
 * //TODO レイヤー名保存
 * //TODO カットインサムネ指定
 * //TODO 初期値設定画面もワンタッチで移動・シークバーで拡大
 * //TODO ホームバーを含めないように
 */

public class CutInEditerActivity extends AppCompatActivity {
    private static final String TAG = "CutInEditerActivity";
    private static final int REQUEST_ADD_CHOOSER = 3149;
    private static final int REQUEST_IMAGE_CHOOSER = 1242;

    EditerView editerView;  //エディタービュー

    //場面フラグ
    public static int sceneFlag = 0;
    public static final int CUTIN_INIT = 0;   //初期値設定画面
    public static final int LAYER_EDIT = 1;   //レイヤー編集画面
    public static final int OBJ_EDIT = 2;     //オブジェクト編集画面
    public static final int ANIM_EDIT = 3;    //アニメーション編集画面

    //レイヤーウィンドウ関連
    private ObjectAnimator enterAnimator;
    private ObjectAnimator exitAnimator;
    private LayerAdapter adapter;

    //オブジェクトウィンドウ関連
    private AnimAdapter animAdapter;
    private int makeId = -1;    //前回参照したオブジェクト番号

    private CutIn cutIn;    //編集用カットイン
    private int selCutInId; //編集しているカットイン番号(-1は新規?)

    //タイマー関連
    Handler handler;
    Timer timer;
    EditerTimerTask timerTask;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.cut_in_editer_layout);

        //ハンドラー取得
        handler = new Handler();

        //カットイン取得
        Bundle bundle = getIntent().getExtras();
        if((selCutInId = bundle.getInt("selCutInId")) != -1) {
            //カットインを
            cutIn = CutInService.cutInList.get(selCutInId).clone();
        }else{
            //新規作成
            cutIn = new CutIn(CutInService.getContext());
        }

        //エディタービュー作成
        editerView = new EditerView(this, cutIn.getAnimObjList());
        final ConstraintLayout layout = (ConstraintLayout)findViewById(R.id.editerLayout);
        layout.addView(editerView, 0);  //エディタービューを最下層に追加
        editerView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //画面遷移
                changeScene(CUTIN_INIT);
            }
        });

        //レイヤーウィンドウ初期位置へ
        RelativeLayout layerWindow = (RelativeLayout)findViewById(R.id.layerMenu);
        layerWindow.setTranslationY(50);
        layerWindow.setTranslationX(editerView.screenWidth);

        //レイヤーウィンドウボタン
        ImageView layerEditButton = (ImageView)findViewById(R.id.layerEditButton);
        layerEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //遷移
                changeScene(LAYER_EDIT);
            }
        });

        //レイヤーウィンドウ閉じボタン
        final Button closeButton = (Button)findViewById(R.id.closeButton);
        closeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                changeScene(CUTIN_INIT);
            }
        });

        //レイヤーリストにImageObject追加
        ListView layerListView = (ListView)findViewById(R.id.layerListView);
        adapter = new LayerAdapter(this, 0, editerView.getAnimObjList(), editerView.getLayerName(), editerView.getSelObjId());
        layerListView.setAdapter(adapter);
        layerListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                //オブジェクト選択
                if(position == editerView.getSelObjId()) {
                    //選択オブジェクトが再度選択された場合は選択解除
                    editerView.setSelObjId(-1);
                }else{
                    editerView.setSelObjId(position);
                }

                //リスト更新
                adapter.setSelId(editerView.getSelObjId());
                adapter.notifyDataSetChanged();
            }
        });

        //レイヤー追加ボタン
        ImageView addButton = (ImageView)findViewById(R.id.addButton);
        addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //追加ウィンドウ表示
                showAddWindow();
            }
        });

        //レイヤー削除ボタン
        ImageView deleteButton = (ImageView)findViewById(R.id.deleteButton);
        deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //削除処理
                if(editerView.getSelObjId() != -1) {
                    editerView.deleteAnimObject(editerView.getSelObjId());
                    editerView.setSelObjId(-1);  //未選択
                    adapter.setSelId(-1);
                    adapter.notifyDataSetChanged(); //リスト更新
                }
            }
        });

        //再生ボタン
        ImageView playButton = (ImageView)findViewById(R.id.playButton);
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //再生処理
                playCutIn();
            }
        });


        //オブジェクトウィンドウ作成
        LayoutInflater layoutInflater = LayoutInflater.from(CutInEditerActivity.this);
        View v = layoutInflater.inflate(R.layout.object_edit_layout, null);
        layout.addView(v);  //追加

        //タブ作成
        TabHost tabHost = (TabHost)v.findViewById(R.id.tabHost);
        tabHost.setup();    //必ず呼ぶ
        TabHost.TabSpec tab1 = tabHost.newTabSpec("property");
        TabHost.TabSpec tab2 = tabHost.newTabSpec("anim");
        tab1.setIndicator(getResources().getString(R.string.object_property));
        tab1.setContent(R.id.propertyTab);
        tab2.setIndicator(getResources().getString(R.string.object_animations));
        tab2.setContent(R.id.animTab);
        tabHost.addTab(tab1);
        tabHost.addTab(tab2);
        v.setTranslationX(-editerView.screenWidth); //隠し

        //オブジェクトウィンドウボタン
        ImageView objEditButton = (ImageView)findViewById(R.id.objEditButton);
        objEditButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //遷移
                changeScene(OBJ_EDIT);
            }
        });

        //シークバー
        //シークバー値取得
        ((SeekBar)findViewById(R.id.seekBar1)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                //シークバーの値が変わったとき
                editerView.seekBarValue1 = progress;
                switch (editerView.animEdit){
                    case EditerView.TRANSLATION:
                        break;
                    case EditerView.ALPHA:
                        ((TextView)findViewById(R.id.seekBarText1)).setText("透明度:" + progress);
                        break;
                    case EditerView.ROTATE:
                        ((TextView)findViewById(R.id.seekBarText1)).setText("回転角:" + progress);
                        break;
                    case EditerView.SCALE:
                        ((TextView)findViewById(R.id.seekBarText1)).setText("倍率:" + (float)progress /100);
                        break;
                    case EditerView.TEXTSIZE:
                        ((TextView)findViewById(R.id.seekBarText1)).setText("テキストサイズ:" + progress);
                        break;
                }
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
        ((SeekBar)findViewById(R.id.seekBar2)).setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                editerView.seekBarValue2 = progress;
                ((TextView)findViewById(R.id.seelBarText2)).setText("時間(ms):" + progress);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });

        //TODO ウィンドウアニメーション作成


        //タイマー設定
        timer = new Timer();
        timerTask = new EditerTimerTask();
        timer.schedule(timerTask, 0, 17);
    }

    //レイヤーウィンドウ表示
    private void showLayerWindow(){
        //アニメーション開始
        RelativeLayout layerWindow = (RelativeLayout) findViewById(R.id.layerMenu);
        Animator animator = AnimatorInflater.loadAnimator(this, R.anim.setting_enter_animation);
        animator.setTarget(layerWindow);
        animator.start();

        //リスト更新
        adapter.setSelId(editerView.getSelObjId());
        adapter.notifyDataSetChanged();
    }

    //レイヤーウィンドウ非表示
    private void closeLayerWindow(){
        //アニメーション開始
        RelativeLayout layerWindow = (RelativeLayout)findViewById(R.id.layerMenu);;
        Animator animator = AnimatorInflater.loadAnimator(this, R.anim.setting_exit_animation);
        animator.setTarget(layerWindow);
        animator.start();
    }

    //追加ウィンドウ表示
    private void showAddWindow(){
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("オブジェクト選択");
        builder.setItems(R.array.object_type, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                switch (which){
                    case 0:
                        Intent intentGallery;
                        if (Build.VERSION.SDK_INT < 19) {
                            intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
                            intentGallery.setType("image/*");
                        } else {
                            intentGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
                            intentGallery.setType("image/*");
                        }

                        startActivityForResult(intentGallery, REQUEST_ADD_CHOOSER);
                        break;
                    case 1:
                        //テキストオブジェクト追加
                        editerView.addAnimObject(new AnimObj(CutInEditerActivity.this, "テキスト", 18));
                        adapter.notifyDataSetChanged();
                        changeScene(CUTIN_INIT);
                        break;
                }
            }
        });

        builder.create().show();
    }

    //オブジェクトウィンドウ表示
    private void showObjWindow(){
        LinearLayout layout = (LinearLayout) findViewById(R.id.objectEditLayout);
        if(editerView.getSelObjId() != makeId) {
            //再作成
            makeId = editerView.getSelObjId();
            //プロパティタブ取得
            LinearLayout propertyTab = (LinearLayout) findViewById(R.id.propertyTab);
            propertyTab.removeAllViews();   //前回のビューを削除
            AnimObj animObj = editerView.getAnimObjList().get(editerView.getSelObjId());
            AnimObj.Type type = animObj.getType();
            View v;
            if (type == AnimObj.Type.Image) {
                //画像の場合
                v = LayoutInflater.from(CutInEditerActivity.this).inflate(R.layout.image_object_edit_layout, propertyTab);
                ((TextView) v.findViewById(R.id.layerValue)).setText(editerView.getLayerName().get(editerView.getSelObjId()));
                ImageView layerImage = (ImageView) v.findViewById(R.id.layerMenuImage);
                layerImage.setImageDrawable(animObj.imageView.getDrawable());

                //画像選択
                layerImage.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        Intent intentGallery;
                        if (Build.VERSION.SDK_INT < 19) {
                            intentGallery = new Intent(Intent.ACTION_GET_CONTENT);
                            intentGallery.setType("image/*");
                        } else {
                            intentGallery = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                            intentGallery.addCategory(Intent.CATEGORY_OPENABLE);
                            intentGallery.setType("image/*");
                        }

                        startActivityForResult(intentGallery, REQUEST_IMAGE_CHOOSER);
                    }
                });
            } else if (type == AnimObj.Type.Text) {
                //テキストの場合
                v = LayoutInflater.from(CutInEditerActivity.this).inflate(R.layout.text_object_edit_layout, propertyTab);
                ((TextView) v.findViewById(R.id.layerValue)).setText(editerView.getLayerName().get(editerView.getSelObjId()));
                ((TextView) v.findViewById(R.id.textValue)).setText(animObj.textView.getText());
            }

            //アニメーションリストビュー作成
            animAdapter = new AnimAdapter(this, 0, editerView.getAnimObjList().get(editerView.getSelObjId()).getSaveHolders(), 0);
            ((ListView) findViewById(R.id.animListView)).setAdapter(animAdapter);

            //クリックリスナー追加
            animAdapter.setOnItemClickListener(new AnimAdapter.OnItemClickListener() {
                @Override
                public void onItemClick(int parentPos, int pos, String itemName) {
                    if(parentPos == 0) return;  //初期値の場合は編集不可
                    Log.i(TAG, "parentPos:" + parentPos + "  pos:" + pos);
                    //編集モード格納(editerView.animEdit等)
                    editerView.selAnimId = parentPos;   //アニメーション番号格納
                    changeScene(ANIM_EDIT);
                    editerView.getAnimStart(parentPos);
                    View v = editerView.getAnimObjList().get(editerView.getSelObjId()).getObjView();
                    switch (itemName) {
                        case "移動":
                            editerView.animEdit = EditerView.TRANSLATION;
                            break;
                        case "透明度":
                            editerView.animEdit = EditerView.ALPHA;
                            Log.i(TAG, "" + v.getAlpha());
                            seekBarSet(0, (int)(v.getAlpha() * 255), 255);
                            showSeekBar(0);
                            break;
                        case "回転":
                            editerView.animEdit = EditerView.ROTATE;
                            seekBarSet(0,  (int)v.getRotation(), 1080);
                            showSeekBar(0);
                            break;
                        case "拡大・縮小":
                            editerView.animEdit = EditerView.SCALE;
                            seekBarSet(0,  (int)(v.getScaleX()*100), 500);
                            showSeekBar(0);
                            break;
                        case "テキストサイズ":
                            editerView.animEdit = EditerView.TEXTSIZE;
                            seekBarSet(0,
                                    (int)(editerView.getAnimObjList().get(editerView.getSelObjId()).textView.getTextSize() /(editerView.getAnimObjList().get(editerView.getSelObjId()).textView.getText().length()-1)),
                                    50);
                            showSeekBar(0);
                            break;
                    }
                    //時間シークバー
                    seekBarSet(1,
                            (int)editerView.getAnimObjList().get(editerView.getSelObjId()).getAnimatorList().get(editerView.selAnimId).getDuration(), 5000);
                    showSeekBar(1);
                }
            });

            //アニメーション追加処理
            animAdapter.setAddAnimOnClickListener(new AnimAdapter.OnClickListener() {
                @Override
                public void onClick(final int position) {
                    if(position == 0) return;   //初期化処理は追加不可能
                    //アニメーション追加処理
                    String[] strings;
                    if (editerView.getAnimObjList().get(editerView.getSelObjId()).getType() == AnimObj.Type.Image){
                         strings = getResources().getStringArray(R.array.image_anim_type);
                    }else{
                         strings = getResources().getStringArray(R.array.text_anim_type);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(CutInEditerActivity.this);
                    builder.setTitle("アニメーション選択");
                    builder.setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0: //移動
                                    editerView.getAnimObjList().get(editerView.getSelObjId()).setTranslation(position,0,0);
                                    break;
                                case 1: //透明度
                                    editerView.getAnimObjList().get(editerView.getSelObjId()).setAlpha(position, 1.0f);
                                    break;
                                case 2: //回転
                                    editerView.getAnimObjList().get(editerView.getSelObjId()).setRotate(position, 0);
                                    break;
                                case 3: //拡大縮小
                                    if(editerView.getAnimObjList().get(editerView.getSelObjId()).getType() == AnimObj.Type.Image) {
                                        editerView.getAnimObjList().get(editerView.getSelObjId()).setScale(position, 1.0f, 1.0f);
                                        break;
                                    }else{
                                        editerView.getAnimObjList().get(editerView.getSelObjId()).setTextSize(position, 18);
                                        break;
                                    }
                            }
                            animAdapter.notifyDataSetChanged(); //更新
                        }
                    });
                    builder.create().show();
                }
            });

            //アニメーションホルダー削除処理
            animAdapter.setDeleteAnimOnClickListener(new AnimAdapter.OnClickListener() {
                @Override
                public void onClick(final int position) {
                    if(position == 0) return; //初期化処理は削除不可能
                    AlertDialog.Builder builder = new AlertDialog.Builder(CutInEditerActivity.this);
                    builder.setTitle("注意");
                    builder.setMessage("このアニメーションホルダーを削除しますがよろしいですか？");
                    builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //削除処理
                            editerView.getAnimObjList().get(editerView.getSelObjId()).removeAnimator(position);
                            animAdapter.notifyDataSetChanged();
                        }
                    });
                    builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            //何もしない
                        }
                    });
                    builder.create().show();
                }
            });

            //アニメーションホルダー追加
            animAdapter.setAddHolderOnClickListener(new AnimAdapter.OnClickListener() {
                @Override
                public void onClick(final int position) {
                    //ホルダー追加処理
                    //アニメーション追加処理
                    String[] strings;
                    if (editerView.getAnimObjList().get(editerView.getSelObjId()).getType() == AnimObj.Type.Image){
                        strings = getResources().getStringArray(R.array.image_anim_type);
                    }else{
                        strings = getResources().getStringArray(R.array.text_anim_type);
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(CutInEditerActivity.this);
                    builder.setTitle("アニメーション選択");
                    builder.setItems(strings, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            switch (which){
                                case 0: //移動
                                    editerView.getAnimObjList().get(editerView.getSelObjId()).addTranslationHolder(0,0);
                                    break;
                                case 1: //透明度
                                    editerView.getAnimObjList().get(editerView.getSelObjId()).addAlphaHolder(1.0f);
                                    break;
                                case 2: //回転
                                    editerView.getAnimObjList().get(editerView.getSelObjId()).addRotateHolder(0);
                                    break;
                                case 3: //拡大縮小
                                    if(editerView.getAnimObjList().get(editerView.getSelObjId()).getType() == AnimObj.Type.Image) {
                                        editerView.getAnimObjList().get(editerView.getSelObjId()).addScaleHolder(1.0f, 1.0f);
                                        break;
                                    }else{
                                        editerView.getAnimObjList().get(editerView.getSelObjId()).addTextSizeHolder( 18);
                                        break;
                                    }
                            }
                            editerView.getAnimObjList().get(editerView.getSelObjId()).addAnimator(1000, position + 1);
                            animAdapter.notifyDataSetChanged(); //更新
                        }
                    });
                    builder.create().show();
                }
            });
        }

        final Animator animator = AnimatorInflater.loadAnimator(this, R.anim.setting_enter_animation);
        animator.setTarget(layout);
        animator.start();
    }

    //オブジェクトウィンドウ非表示
    private void closeObjWindow(){
        //プロパティタブ取得
        LinearLayout layout = (LinearLayout)findViewById(R.id.objectEditLayout) ;
        Animator animator = AnimatorInflater.loadAnimator(this, R.anim.setting_exit_animation);
        animator.setTarget(layout);
        animator.start();

        //プロパティ保存
        EditText layerValue = (EditText)findViewById(R.id.layerValue);
        editerView.getLayerName().set(editerView.getSelObjId(), layerValue.getText().toString());   //レイヤー名取得
        if(editerView.getAnimObjList().get(editerView.getSelObjId()).getType() == AnimObj.Type.Image){
            //画像プロパティ保存
            ImageView layerImage = (ImageView)findViewById(R.id.layerMenuImage);
            Bitmap bitmap;
            if((bitmap = (Bitmap)layerImage.getTag()) != null){
                //もし変更されていたら適用
                editerView.getBitmapList().set(editerView.getSelObjId(), bitmap);
                editerView.getAnimObjList().get(editerView.getSelObjId()).setImage(bitmap);
                layerImage.setTag(null);    //空にする
            }
        }else{
            //テキストプロパティ保存
            EditText textValue = (EditText)findViewById(R.id.textValue);
            editerView.getAnimObjList().get(editerView.getSelObjId()).textView.setText(textValue.getText());
        }

    }

    //画像追加処理
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if(requestCode == REQUEST_ADD_CHOOSER && resultCode == RESULT_OK){
            //オブジェクト追加処理
            Uri resultUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
            }catch(IOException e){
                e.printStackTrace();
                return;
            }
            editerView.addAnimObject(new AnimObj(this, new BitmapDrawable(bitmap), -1, -1));
            editerView.setSelObjId(cutIn.animObjList.size()); //オブジェクト選択
            adapter.notifyDataSetChanged(); //リスト更新

            //設定ウィンドウ閉じ
            changeScene(0);
        }else if(requestCode == REQUEST_IMAGE_CHOOSER && resultCode == RESULT_OK){
            ImageView layerImage = (ImageView)findViewById(R.id.layerMenuImage);

            //ビットマップ生成
            Uri resultUri = data.getData();
            Bitmap bitmap;
            try {
                bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), resultUri);
            }catch(IOException e){
                e.printStackTrace();
                return;
            }

            //ビットマップはタグ付け
            layerImage.setTag(bitmap);

            //イメージ適用
            layerImage.setImageDrawable(new BitmapDrawable(bitmap));
            layerImage.invalidate();
        }

    }

    //カットイン生成
    private void makeCutIn(){
        //TODO タイトルサムネイル等格納
        cutIn.setAnimObjList(new ArrayList<AnimObj>(editerView.getAnimObjList()));
    }

    //カットイン再生
    private void playCutIn(){
        makeCutIn();
        CutInService.play(cutIn);
    }

    //タイマークラス
    private class EditerTimerTask extends TimerTask{

        @Override
        public void run() {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    //タイマー
                    //エディタービューを再描画
                    editerView.invalidate();

                    //選択オブジェクトがある場合はオブジェクト編集ボタン表示
                    if(editerView.getSelObjId() == -1){
                        ((ImageView)findViewById(R.id.objEditButton)).setAlpha(0.5f);
                    }else{
                        ((ImageView)findViewById(R.id.objEditButton)).setAlpha(1.0f);
                    }
                }
            });
        }
    }

    //場面変更処理
    private void changeScene(int flag){
        if(flag == CUTIN_INIT){ //初期値設定画面へ
            switch (sceneFlag){ //現在のフラグで動作わけ
                case OBJ_EDIT:
                    closeObjWindow();
                    break;
                case LAYER_EDIT:
                    closeLayerWindow();
                    break;
                default:
                    return;
            }
        }else if(flag == LAYER_EDIT){   //レイヤー編集画面へ
            switch (sceneFlag){
                case CUTIN_INIT:
                    showLayerWindow();
                    break;
                case OBJ_EDIT:
                    closeObjWindow();
                    showLayerWindow();
                    break;
                default:
                    return;
            }
        }else if(flag == OBJ_EDIT){     //オブジェクト編集画面へ
            if(editerView.getSelObjId() == -1) return;  //オブジェクト選択されてない場合は無し
                switch (sceneFlag) {
                    case CUTIN_INIT:
                        showObjWindow();
                        break;
                    case LAYER_EDIT:
                        closeLayerWindow();
                        showObjWindow();
                        break;
                    case ANIM_EDIT:
                        closeSeekBar(0);
                        closeSeekBar(1);
                        ((ImageView)findViewById(R.id.objEditButton)).setVisibility(View.VISIBLE);
                        ((ImageView)findViewById(R.id.playButton)).setVisibility(View.VISIBLE);
                        ((ImageView)findViewById(R.id.layerEditButton)).setVisibility(View.VISIBLE);
                        showObjWindow();
                        break;
                    default:
                        return;
                }
        }else if(flag == ANIM_EDIT){    //アニメーション編集画面へ
            switch (sceneFlag){
                case OBJ_EDIT:
                    ((ImageView)findViewById(R.id.objEditButton)).setVisibility(View.INVISIBLE);
                    ((ImageView)findViewById(R.id.playButton)).setVisibility(View.INVISIBLE);
                    ((ImageView)findViewById(R.id.layerEditButton)).setVisibility(View.INVISIBLE);
                    closeObjWindow();
                    break;
                default:
                    return;
            }
        }
        sceneFlag = flag;
    };

    //キーダウン
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode != KeyEvent.KEYCODE_BACK) {
            return super.onKeyDown(keyCode, event);
        }else{
            //たとえばアニメ編集から戻るところとか
            if(sceneFlag == CUTIN_INIT){
                //保存確認
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle("注意");
                builder.setMessage("保存しますか？");
                builder.setPositiveButton("はい", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //保存処理
                        makeCutIn();
                        if (selCutInId != -1) {
                            //既成のカットインを編集している場合代入
                            CutInService.cutInList.set(selCutInId, cutIn.clone());  //カットイン追加
                        }else{
                            //新規カットインの場合追加
                            CutInService.cutInList.add(cutIn.clone());
                        }
                        new CutInDataManager(CutInEditerActivity.this).cutInListSave(new ArrayList<CutIn>(CutInService.cutInList));
                        finish();
                    }
                });
                builder.setNegativeButton("いいえ", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        //何もせず終了
                        finish();
                    }
                });
                builder.create().show();
            }else if(sceneFlag == LAYER_EDIT){
                changeScene(CUTIN_INIT);
            }else if(sceneFlag == OBJ_EDIT){
                changeScene(CUTIN_INIT);
            }else if(sceneFlag == ANIM_EDIT){
                changeScene(OBJ_EDIT);
                //移動パラメータ保存
                if(editerView.animEdit == EditerView.TRANSLATION){
                    editerView.getAnimObjList().get(editerView.getSelObjId()).setTranslation(editerView.selAnimId,
                            editerView.getEditAnimObjTranslationX(), editerView.getEditAnimObjTranslationY());
                }
            }
            return false;
        }
    }

    //シークバーセット
    public void seekBarSet(int index, int progress, int max){
        SeekBar seekBar;
        if(index == 0){
            seekBar = (SeekBar)findViewById(R.id.seekBar1);
        }else{
            seekBar = (SeekBar)findViewById(R.id.seekBar2);
        }
        seekBar.setMax(max);
        seekBar.setProgress(progress);
    }

    //シークバー表示
    public void showSeekBar(int index){
        SeekBar seekBar;
        TextView seekBarText;
        if(index == 0){
            seekBar = (SeekBar)findViewById(R.id.seekBar1);
            seekBarText = (TextView)findViewById(R.id.seekBarText1);
        }else{
            seekBar = (SeekBar)findViewById(R.id.seekBar2);
            seekBarText = (TextView) findViewById(R.id.seelBarText2);
        }
        seekBar.setVisibility(View.VISIBLE);
        seekBarText.setVisibility(View.VISIBLE);
    }

    //シークバー非表示
    public void closeSeekBar(int index){
        SeekBar seekBar;
        TextView seekBarText;
        if(index == 0){
            seekBar = (SeekBar)findViewById(R.id.seekBar1);
            seekBarText = (TextView)findViewById(R.id.seekBarText1);
        }else{
            seekBar = (SeekBar)findViewById(R.id.seekBar2);
            seekBarText = (TextView) findViewById(R.id.seelBarText2);
        }
        seekBar.setVisibility(View.INVISIBLE);
        seekBarText.setVisibility(View.INVISIBLE);
    }

}

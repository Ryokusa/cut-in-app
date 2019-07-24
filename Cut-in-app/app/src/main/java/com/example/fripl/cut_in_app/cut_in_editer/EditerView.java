package com.example.fripl.cut_in_app.cut_in_editer;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Interpolator;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.ScaleGestureDetector;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.fripl.cut_in_app.CutIn;
import com.example.fripl.cut_in_app.R;
import com.example.fripl.cut_in_app.data_manage.AnimObj;
import com.example.fripl.cut_in_app.data_manage.CustomPropertyValuesHolder;
import com.example.fripl.cut_in_app.data_manage.CustomPropertyValuesHolderList;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2018/01/22.
 * エディター画面のビュー
 */

public class EditerView extends View {
    private static final String TAG = "EditerView";

    private List<AnimObj> animObjList = new ArrayList<>();  //オブジェクト
    private List<Bitmap> bitmapList = new ArrayList<>();
    private List<Matrix> matrixList = new ArrayList<>();
    private List<String> layerName = new ArrayList<>(); //レイヤー名
    private Paint mPaint = new Paint();

    //アニメ編集関連
    private AnimObj editAnimObj;    //編集時の一時的AnimObj
    private Bitmap editAnimObjBitmap;
    private boolean editAnimObjMove = false;    //移動フラグ
    private AnimObj preAnimObj; //一個前のAnimObj
    private Bitmap preAnimObjBitmap;
    private AnimObj previewAnimObj; //プレビュー時のAnimObj
    private Bitmap previewAnimObjBitmap;
    private long previewMaxFrame = 0, previewFrame = -1;
    private long preAnimId = -1;  //以前使ったアニメーションID

    private ScaleGestureDetector scaleGestureDetector;

    //復数タッチフラグ
    boolean multiTouch = false;

    //描画用矩形範囲
    Rect srcRect = new Rect(), destRect = new Rect();
    Matrix matrix = new Matrix();

    //選択オブジェクト番号
    private int selObjId = -1;
    //操作しているアニメーションの番号
    public int selAnimId = -1;

    //アニメ編集モードフラグ
    public int animEdit = -1;
    public static final int TRANSLATION = 0;
    public static final int SCALE = 1;
    public static final int ALPHA = 2;
    public static final int ROTATE = 3;
    public static final int TEXTSIZE = 4;
    public static boolean animPreview = true;

    //スクリーン情報
    private Matrix screenMatrix = new Matrix();
    public int screenWidth, screenHeight;

    //移動用
    private int preFocusX, preFocusY;

    //シークバーデータ
    public int seekBarValue1;
    public int seekBarValue2;

    public EditerView(Context context, List<AnimObj> animObjList){
        super(context);

        //AnimObjがある場合は代入
        if(animObjList != null) {
            for (AnimObj ao : animObjList) {
                addAnimObject(ao);
            }
        }


        //スケールジェスチャーリスナー登録
        scaleGestureDetector = new ScaleGestureDetector(context, onScaleGestureListener);
        //画面サイズ取得
        DisplayMetrics displayMetrics = new DisplayMetrics();
        ((WindowManager)context.getSystemService(Context.WINDOW_SERVICE)).getDefaultDisplay().getMetrics(displayMetrics);
        screenWidth = displayMetrics.widthPixels;
        screenHeight = displayMetrics.heightPixels;

    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);

        //タイマーイベントで一定周期で呼び出される

        //各種描画
        canvas.concat(screenMatrix);

        //スクリーン枠描画
        mPaint.setStyle(Paint.Style.STROKE);    //線
        mPaint.setARGB(255, 0,0,0);
        canvas.drawRect(0, 0, screenWidth, screenHeight, mPaint);

        if(CutInEditerActivity.sceneFlag != CutInEditerActivity.ANIM_EDIT) {
            //編集モードでない場合
            //全オブジェクト描画
            int i = 0;
            for(AnimObj ao : animObjList){
                //非選択オブジェクトは半透明
                if(i != selObjId){
                    mPaint.setAlpha(127);
                }else{
                    mPaint.setAlpha(255);
                }

                //TODO 回転描画 マトリックスで行えそう
                if(ao.getType() == AnimObj.Type.Image){
                    //画像オブジェクトの場合
                    matrix = new Matrix();
                    matrix.postScale((float)ao.getInitWidth() / bitmapList.get(i).getWidth()
                            , (float)ao.getInitHeight() / bitmapList.get(i).getHeight());
                    matrix.postTranslate(ao.getInitX(), ao.getInitY());
                    canvas.drawBitmap(bitmapList.get(i), matrix, mPaint);
                }else if (ao.getType() == AnimObj.Type.Text) {
                    //テキストオブジェクトの場合
                    mPaint.setTextSize(ao.getInitTextSize() * getContext().getResources().getDisplayMetrics().density);  //サイズ取得
                    canvas.drawText(ao.textView.getText().toString(), ao.getInitX(), ao.getInitY(), mPaint);
                }
                i++;
            }
        }else{
            //編集モードの場合の描画
            //選択オブジェクト以外描画
            int i = 0;
            for(AnimObj ao : animObjList){
                if(i != selObjId) {
                    //非選択オブジェクトは半透明
                    drawAnimObj(canvas, ao, bitmapList.get(i), (int)(100 * preAnimObj.getObjView().getAlpha()));
                }
                i++;
            }

            //編集用AnimObjセット

            if(animPreview) {
                //プレビュー描画
                drawPreview(canvas);
            }
            //編集AnimObj描画
            setSeekBarValues2EditAnimObj(); //シークバーの値を取得
            //移動後
            if (!(animEdit == ALPHA && editAnimObj.getAnimatorList().get(selAnimId).getDuration() != 0))
                drawAnimObj(canvas, editAnimObj, editAnimObjBitmap, (int)(editAnimObj.getObjView().getAlpha() * 255));
            //移動前
            //drawAnimObj(canvas, preAnimObj, preAnimObjBitmap, (int)(100 * preAnimObj.getObjView().getAlpha()));
        }

    }

    //スケールジェスチャーリスナー
    private ScaleGestureDetector.SimpleOnScaleGestureListener onScaleGestureListener = new ScaleGestureDetector.SimpleOnScaleGestureListener() {
        @Override
        public boolean onScale(ScaleGestureDetector detector) {

            if(selObjId == -1 || CutInEditerActivity.sceneFlag == CutInEditerActivity.ANIM_EDIT) {    //なにも選択されていないときもしくはアニメーション編集時
                //スクリーンスケール処理
                screenMatrix.postScale(detector.getScaleFactor(), detector.getScaleFactor(), detector.getFocusX(), detector.getFocusY());
            }else if (selObjId != -1){
                //オブジェクトスケール処理(拡大基準座標は中心)
                //初期値設定画面のみ操作可能
                if(CutInEditerActivity.sceneFlag == CutInEditerActivity.CUTIN_INIT) {
                    AnimObj ao;
                    if ((ao = animObjList.get(selObjId)).getType() == AnimObj.Type.Image) {
                        //画像オブジェクトの場合
                        matrixList.get(selObjId).postScale(detector.getScaleFactor(), detector.getScaleFactor(), ao.getInitX() + ao.getInitWidth() / 2, ao.getInitY() + ao.getInitHeight() / 2);
                        RectF rectF = new RectF();
                        rectF.set(0, 0, ao.getInitWidth(), ao.getInitHeight());
                        rectF.offset(ao.getInitX(), ao.getInitY());
                        matrixList.get(selObjId).mapRect(rectF);
                        ao.setInitX((int) rectF.left);
                        ao.setInitY((int) rectF.top);
                        ao.setInitHeight((int) rectF.height());
                        ao.setInitWidth((int) (ao.getImageRatio() * ao.getInitHeight()));
                        matrixList.set(selObjId, new Matrix());
                    }
                }
            }
            return true;
        }

        @Override
        public boolean onScaleBegin(ScaleGestureDetector detector) {

            //falseの場合Endするらしい
            return super.onScaleBegin(detector);
        }

        @Override
        public void onScaleEnd(ScaleGestureDetector detector) {

            super.onScaleEnd(detector);
        }
    };

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        //スケールジェスチャー検知
        scaleGestureDetector.onTouchEvent(event);

        //単体タッチ
        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                performClick();
                if(!multiTouch) {
                    if (animEdit == TRANSLATION) {
                        RectF rectF = new RectF();
                        if(editAnimObj.getType() == AnimObj.Type.Image) {
                            //画像オブジェクト
                            rectF.set(0, 0, editAnimObj.getInitWidth() * editAnimObj.imageView.getScaleX(),  editAnimObj.getInitHeight() * editAnimObj.imageView.getScaleY());
                            rectF.offset(editAnimObj.imageView.getTranslationX(), editAnimObj.imageView.getTranslationY());
                            screenMatrix.mapRect(rectF);
                        }else if (editAnimObj.getType() == AnimObj.Type.Text){
                            //テキストオブジェクト
                            Rect rect = new Rect();
                            Paint paint = new Paint();
                            paint.setTextSize(editAnimObj.getInitTextSize() * getResources().getDisplayMetrics().scaledDensity);
                            paint.getTextBounds(editAnimObj.textView.getText().toString(), 0, editAnimObj.textView.getText().length(), rect);
                            rect.offset(editAnimObj.getInitX(), editAnimObj.getInitY());
                            rectF = new RectF(rect);
                            screenMatrix.mapRect(rectF);
                        }
                        if(rectF.contains((int)event.getX(), (int)event.getY())) {   //タッチした場所がオブジェクトなら
                            //移動編集時タッチで移動処理
                            preFocusX = (int) (event.getX());
                            preFocusY = (int) (event.getY());
                            editAnimObjMove = true; //移動フラグ
                        }else {
                            editAnimObjMove = false;
                        }
                    }
                }
                break;

            case MotionEvent.ACTION_UP:
                Log.i(TAG, "ACTION_UP");

                if(!multiTouch){
                    //オブジェクト切り替え処理
                    //初期値設定画面以外は無効
                    if(CutInEditerActivity.sceneFlag == CutInEditerActivity.CUTIN_INIT) {
                        AnimObj animObj;
                        RectF rectF = new RectF();
                        int i;
                        for (i = animObjList.size() - 1; i >= 0; i--) {
                            animObj = animObjList.get(i);
                            if (animObj.getType() == AnimObj.Type.Image) {
                                //画像オブジェクトの場合
                                rectF.set(0, 0, animObj.getInitWidth(), animObj.getInitHeight());
                                rectF.offset(animObj.getInitX(), animObj.getInitY());
                                screenMatrix.mapRect(rectF);    //クリック範囲マトリックス変換
                                if (rectF.contains((int) event.getX(), (int) event.getY())) {
                                    //範囲内なら
                                    selObjId = i;
                                    break;
                                }
                            } else if (animObj.getType() == AnimObj.Type.Text) {
                                //テキストオブジェクトの場合
                                Rect rect = new Rect();
                                Paint paint = new Paint();
                                paint.setTextSize(animObj.getInitTextSize() * getResources().getDisplayMetrics().scaledDensity);
                                paint.getTextBounds(animObj.textView.getText().toString(), 0, animObj.textView.getText().length(), rect);
                                rect.offset(animObj.getInitX(), animObj.getInitY());
                                rectF = new RectF(rect);
                                screenMatrix.mapRect(rectF);
                                if (rectF.contains((int) event.getX(), (int) event.getY())) {
                                    //範囲内なら
                                    selObjId = i;
                                    break;
                                }
                            }
                        }
                        if (i < 0) selObjId = -1;  //見つからなかった場合未選択
                    }
                }else{
                    //初期化ホルダーセット
                    if(selObjId != -1) {
                        animObjList.get(selObjId).setInitHolder();
                    }
                    multiTouch = false;
                }
                break;
            case MotionEvent.ACTION_MOVE:{
                if(!multiTouch){
                    if(animEdit == TRANSLATION && editAnimObjMove){
                        //移動編集時タッチで移動処理
                        int focusX = (int)event.getX();
                        int focusY = (int)event.getY();
                        int dx = focusX - preFocusX;
                        int dy = focusY - preFocusY;
                        AnimObj animObj = editAnimObj;
                        Matrix matrix = new Matrix(screenMatrix);
                        float[] vecs = {dx, dy};
                        //逆変換
                        matrix.invert(matrix);
                        matrix.mapVectors(vecs);
                        dx = (int) vecs[0];
                        dy = (int) vecs[1];
                        View v = animObj.getObjView();
                        v.setTranslationX(v.getTranslationX() + dx);
                        v.setTranslationY(v.getTranslationY() + dy);
                        //現在の座標格納
                        preFocusX = focusX;
                        preFocusY = focusY;

                        Log.i(TAG, "" + v.getTranslationX());
                    }
                }
            }
        }

        //復数タッチ
        switch (event.getActionMasked()){
            case MotionEvent.ACTION_POINTER_DOWN:
                //タッチされたとき
                Log.i(TAG, "POINTER_DOWN");


                //タッチ移動開始座標格納(マルチタッチ時)
                if(event.getPointerCount() == 2) {
                    preFocusX = (int) (event.getX(1) + (event.getX(0) - event.getX(1)) / 2);
                    preFocusY = (int) (event.getY(1) + (event.getY(0) - event.getY(1)) / 2);
                }

                multiTouch = true;  //復数タッチフラグ

                break;
            case MotionEvent.ACTION_MOVE:
                //タッチ場所が移動したとき

                //タッチ移動処理
                if(event.getPointerCount() == 2){
                    int focusX = (int) (event.getX(1) + (event.getX(0) - event.getX(1)) / 2);
                    int focusY = (int) (event.getY(1) + (event.getY(0) - event.getY(1)) / 2);
                    int dx = focusX - preFocusX;
                    int dy = focusY - preFocusY;
                    if(selObjId == -1 || CutInEditerActivity.sceneFlag == CutInEditerActivity.ANIM_EDIT) {
                        screenMatrix.postTranslate(dx, dy);
                    }else if (selObjId != -1){
                        AnimObj animObj = animObjList.get(selObjId);
                        Matrix matrix = new Matrix(screenMatrix);
                        float[] vecs = {dx, dy};
                        //逆変換
                        matrix.invert(matrix);
                        matrix.mapVectors(vecs);
                        dx = (int) vecs[0];
                        dy = (int) vecs[1];

                        animObjList.get(selObjId).setInitX(animObj.getInitX() + dx);
                        animObjList.get(selObjId).setInitY(animObj.getInitY() + dy);
                    }

                    //現在の座標格納
                    preFocusX = focusX;
                    preFocusY = focusY;
                }
                break;
            default:
                break;

        }

        return true;
    }

    public int getSelObjId() {
        return selObjId;
    }

    public void setSelObjId(int selObjId) {
        this.selObjId = selObjId;
    }

    //オブジェクト追加
    public void addAnimObject(AnimObj animObj){
        animObjList.add(animObj);
        if(animObj.getType() == AnimObj.Type.Image) {
            //画像オブジェクト
            bitmapList.add(drawableToBitmap(animObj.imageView.getDrawable()));
            matrixList.add(new Matrix());
            layerName.add("" + layerName.size());
        }else if (animObj.getType() == AnimObj.Type.Text){
            //TODO テキストオブジェクトの場合のbitmap,matrix追加処理
            //TODO テキストオブジェクトのサムネイル
            bitmapList.add(drawableToBitmap(getResources().getDrawable(R.drawable.thumnail)));
            matrixList.add(new Matrix());   //ダミー
            layerName.add("" + layerName.size());
        }
    }

    //オブジェクト削除
    public void deleteAnimObject(int index){
        animObjList.remove(index);
        bitmapList.remove(index);
        matrixList.remove(index);
        layerName.remove(index);
    }

    public List<AnimObj> getAnimObjList() {
        return animObjList;
    }

    public List<String> getLayerName() {
        return layerName;
    }

    public List<Bitmap> getBitmapList() {
        return bitmapList;
    }

    //Drawable→Bitmap
    private Bitmap drawableToBitmap (Drawable drawable) {

        //BitmapDrawableならBitmapを取得可能
        if (drawable instanceof BitmapDrawable) {
            return ((BitmapDrawable)drawable).getBitmap();
        }

        //画像サイズのビットマップ作成
        Bitmap bitmap = Bitmap.createBitmap(drawable.getIntrinsicWidth(), drawable.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);

        //bitmapのキャンバス取得して画像描画
        Canvas canvas = new Canvas(bitmap);
        drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
        drawable.draw(canvas);

        return bitmap;
    }

    //選択されたオブジェクトの指定アニメーションの終了時の状態を取得
    //editAnimObjをその状態にする
    public void getAnimStart(int animId){
        editAnimObj = animObjList.get(selObjId);

        //初期位置へ
        View v = editAnimObj.getObjView();
        if(editAnimObj.getType() == AnimObj.Type.Image){
            v.setTranslationX(editAnimObj.getInitX());
            v.setTranslationY(editAnimObj.getInitY());
            v.setScaleX(1.0f);
            v.setScaleY(1.0f);
            editAnimObjBitmap = drawableToBitmap(((ImageView)v).getDrawable());  //ビットマップ生成
            preAnimObjBitmap = editAnimObjBitmap;
        }else if (editAnimObj.getType() == AnimObj.Type.Text){
            v.setTranslationX(editAnimObj.getInitX());
            v.setTranslationY(editAnimObj.getInitY());
            ((TextView)v).setTextSize(editAnimObj.getInitTextSize());
        }



        //計算開始
        for (int i = 0; i <= animId; i++){
            for(CustomPropertyValuesHolder holder : editAnimObj.getSaveHolders().get(i)){
                switch (holder.getPropertyName()){
                    case "translationX":
                        v.setTranslationX(holder.getValues()[0]);
                        break;
                    case "translationY":
                        v.setTranslationY(holder.getValues()[0]);
                        break;
                    case "alpha":
                        v.setAlpha(holder.getValues()[0]);
                        break;
                    case "rotation":
                        v.setRotation(holder.getValues()[0]);
                        break;
                    case "scaleX":
                        v.setScaleX(holder.getValues()[0]);
                        break;
                    case "scaleY":
                        v.setScaleY(holder.getValues()[0]);
                        break;
                    case "textSize":
                        editAnimObj.textView.setTextSize(holder.getValues()[0]);
                        break;
                }
            }
            if(i == animId - 1 && animId > 0){
                //一個前のアニメーション保持
                preAnimObj = editAnimObj.clone();
            }
        }

    }

    public AnimObj getEditAnimObj() {
        return editAnimObj;
    }

    public void drawAnimObj(Canvas canvas, AnimObj ao, Bitmap bitmap, int alpha){
        mPaint.setAlpha(alpha);
        //TODO 回転描画 マトリックスで行えそう
        if(ao.getType() == AnimObj.Type.Image){
            //画像オブジェクトの場合
            srcRect.set(0, 0, bitmap.getWidth(), bitmap.getHeight());
            destRect.set(0,0, (int)(ao.getInitWidth()*ao.imageView.getScaleX()), (int)(ao.getInitHeight()*ao.imageView.getScaleY()));
            destRect.offset((int)ao.imageView.getTranslationX(), (int)ao.imageView.getTranslationY());
            matrix = new Matrix();
            matrix.postRotate(ao.imageView.getRotation(), bitmap.getWidth() / 2.0f, bitmap.getHeight() / 2.0f);
            matrix.postScale(ao.imageView.getScaleX(), ao.imageView.getScaleY(), bitmap.getWidth()/2.0f, bitmap.getHeight()/2.0f);
            matrix.postScale((float)ao.getInitWidth() / bitmap.getWidth()
                    , (float)ao.getInitHeight() / bitmap.getHeight());
            matrix.postTranslate(ao.imageView.getTranslationX(), ao.imageView.getTranslationY());
            canvas.drawBitmap(bitmap, matrix, mPaint);
        }else if (ao.getType() == AnimObj.Type.Text){
            //テキストオブジェクトの場合
            mPaint.setTextSize(ao.textView.getTextSize() / (ao.textView.getText().length()-1) *  getContext().getResources().getDisplayMetrics().density);  //サイズ取得
            canvas.drawText(ao.textView.getText().toString(), ao.textView.getTranslationX(), ao.textView.getTranslationY(), mPaint);
            Log.i(TAG, "" + ao.textView.getTranslationY());
        }
    }

    //編集AnimObjのパラメータ入手
    public int getEditAnimObjTranslationX(){
        if(animObjList.get(selObjId).getType() == AnimObj.Type.Image){
            return (int)editAnimObj.imageView.getTranslationX();
        }else{
            return (int)editAnimObj.textView.getTranslationX();
        }
    }

    public int getEditAnimObjTranslationY(){
        if(animObjList.get(selObjId).getType() == AnimObj.Type.Image){
            return (int)editAnimObj.imageView.getTranslationY();
        }else{
            return (int)editAnimObj.textView.getTranslationY();
        }
    }

    //プレビュー描画
    public void drawPreview(Canvas canvas){
        if (preAnimId != selAnimId) previewFrame = -1;  //前回のオブジェクトと違う場合は初期化
        AccelerateDecelerateInterpolator interpolator = new AccelerateDecelerateInterpolator();//インターぽレーター
        if(previewFrame == -1){
            //プレビューアニメーションが終了していたら
            previewMaxFrame = editAnimObj.getAnimatorList().get(selAnimId).getDuration(); //フレーム数取得
            if(previewMaxFrame == 0) return;    //0フレームのときは終了
            previewMaxFrame /= 17;  //onDraw用に変換
            previewFrame = 0;
            previewAnimObj = editAnimObj.clone();   //コピー
            preAnimId = selAnimId;
        }else if (previewFrame <= previewMaxFrame) {
            previewMaxFrame = editAnimObj.getAnimatorList().get(selAnimId).getDuration() / 17;  //再取得
            if(previewMaxFrame == 0) {  //0の場合描画しない
                previewFrame = -1;
            }
            View v, prev, previewv;
            if (animObjList.get(selObjId).getType() == AnimObj.Type.Image) {
                v = editAnimObj.imageView;
                prev = preAnimObj.imageView;
                previewv = previewAnimObj.imageView;
            } else {
                v = editAnimObj.textView;
                prev = preAnimObj.textView;
                previewv = previewAnimObj.textView;
            }

            //移動
            float distancex = v.getTranslationX() - prev.getTranslationX();
            float distancey = v.getTranslationY() - prev.getTranslationY();
            float input = (float) previewFrame / previewMaxFrame;
            float output = interpolator.getInterpolation(input);
            int x = (int) (prev.getTranslationX() + ((distancex) * output));
            int y = (int) (prev.getTranslationY() + ((distancey) * output));
            previewv.setTranslationX(x);
            previewv.setTranslationY(y);

            //透明度
            //TODO プレビュー時不具合
            float startAlpha = prev.getAlpha();
            float endAlpha = v.getAlpha();
            float alpha = startAlpha + (endAlpha - startAlpha) * input;
            previewv.setAlpha(alpha);

            //スケール
            float startScale = prev.getScaleX();
            float endScale = v.getScaleX();
            float scale = startScale + (endScale - startScale) * input;
            previewv.setScaleX(scale);
            previewv.setScaleY(scale);

            //回転
            float rotate = prev.getRotation() + (v.getRotation() - prev.getRotation()) * output;
            previewv.setRotation(rotate);

            //テキストサイズ
            if(animObjList.get(selObjId).getType() == AnimObj.Type.Text) {
                float startTextSize  = ((TextView) prev).getTextSize() / (((TextView) prev).getText().length()-1);
                float endTextSize = ((TextView) v).getTextSize() / (((TextView) v).getText().length()-1);
                float textSize = startTextSize + (endTextSize - startTextSize) * output;
                ((TextView)previewv).setTextSize(textSize);
            }
            drawAnimObj(canvas, previewAnimObj, editAnimObjBitmap, (int) (previewv.getAlpha() * 255));

            previewFrame++;
            if (previewFrame > previewMaxFrame) previewFrame = 0;//終了次第繰り返す
        }
    }

    //TODO 他アニメーションパラメータ
    /*
    public int getEditAnimObjAlpha(){
        if(animObjList.get(selObjId).getType() == AnimObj.Type.Image){
            return (int)editAnimObj.imageView.getTranslationX();
        }else{
            return (int)editAnimObj.textView.getTranslationX();
        }
    }
    */

    //シークバーの値をeditAnimObjへ
    public void setSeekBarValues2EditAnimObj(){
        //TODO 編集モードによってセット
        View v = editAnimObj.getObjView();
        switch (animEdit){
            case ALPHA:
                v.setAlpha((float)seekBarValue1 / 255);
                editAnimObj.setAlpha(selAnimId, (float)seekBarValue1 / 255);
                break;
            case ROTATE:
                v.setRotation(seekBarValue1);
                editAnimObj.setRotate(selAnimId, seekBarValue1);
                break;
            case SCALE:
                v.setScaleX((float)seekBarValue1 / 100);
                v.setScaleY((float)seekBarValue1 / 100);
                editAnimObj.setScale(selAnimId, (float)seekBarValue1 / 100, (float)seekBarValue1 / 100);
                break;
            case TEXTSIZE:
                ((TextView)v).setTextSize(seekBarValue1);
                editAnimObj.setTextSize(selAnimId, seekBarValue1);
        }
        editAnimObj.getAnimatorList().get(selAnimId).setDuration(seekBarValue2);
    }
}

package com.example.fripl.propertyanimtest;

import android.app.Activity;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Created by fripl on 2017/12/27.
 * 移動ポイント用カスタムビュー
 */

public class MovePoint extends View {
    private Paint mPaint = new Paint();
    private int preX, preY;   //移動用
    private int radius;
    private Rect moveRect;

    public MovePoint(Context context, int radius){
        super(context);
        this.radius = radius;
    }

    //再描画時に呼び出される関数Canvas
    @Override
    protected void onDraw(Canvas canvas){
        super.onDraw(canvas);

        mPaint.setAntiAlias(true);  //アンチエイリアス
        mPaint.setStyle(Paint.Style.FILL);    //ペンスタイルセット
        canvas.drawCircle(radius,radius, radius, mPaint); //半径10の円を描画
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        super.onTouchEvent(event);

        switch (event.getAction()){
            case MotionEvent.ACTION_DOWN:
                preX = (int)event.getRawX();
                preY = (int)event.getRawY();
                break;
            case MotionEvent.ACTION_UP:
                performClick();

                //親ViewGroupでは変更後の座標を知らないので教える
                ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
                lp.setMargins(getLeft(), getTop(), 0, 0);   //マージン設定
                //マージンの矢印つなげる
                lp.leftToLeft = R.id.parentLayout;  //左壁
                lp.topToTop= R.id.parentLayout;     //上壁
                setLayoutParams(lp);
                break;
            case MotionEvent.ACTION_MOVE:   //操作処理
                //差分だけ移動する
                int x = (int)event.getRawX();
                int y = (int)event.getRawY();
                int dx = x - preX;
                int dy = y - preY;
                int d;  //はみ出し差分

                //補正
                if ((d = moveRect.left - getLeft() - dx) > 0){    //左
                    dx += d;
                    preX = x + d;
                }else if ((d = getRight() + dx - moveRect.right) > 0){
                    dx -= d;
                    preX = x - d;
                }else{
                    preX = x;
                }
                if ((d = moveRect.top - getTop() - dy) > 0){ //上
                    dy += d;
                    preY = y + d;
                }else if ((d = getBottom() + dy - moveRect.bottom) > 0) { //下
                    dy -= d;
                    preY = y - d;
                }else{
                    preY = y;
                }
                layout(getLeft() + dx, getTop() + dy, getRight() + dx, getBottom() + dy);
                invalidate();   //再描画(必須)
                break;
        }
        return true;
    }

    //意図的なタッチイベントだとこれも必要らしい
    @Override
    public boolean performClick(){
        super.performClick();
        return true;
    }

    //円の中心座標返す系メソッド
    public float getCenterX(){
        return getLeft() + radius;
    }

    public float getCenterY(){
        return getTop() + radius;
    }

    //ViewGroupに大きさを伝える処理
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec){
        //親より小さいのでモードによる分岐はめんどいなし
        int width = MeasureSpec.makeMeasureSpec(radius*2, MeasureSpec.getMode(widthMeasureSpec));
        setMeasuredDimension(width, width);
        super.onMeasure(width, width);
    }

    public void setMoveRect(Rect rect){
        moveRect = rect;
    }

    //指定位置に移動関数
    public void move(int x, int y)
    {
        ConstraintLayout.LayoutParams lp = new ConstraintLayout.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        lp.setMargins(x, y, 0, 0);   //マージン設定
        //マージンの矢印つなげる
        lp.leftToLeft = R.id.parentLayout;  //左壁
        lp.topToTop= R.id.parentLayout;     //上壁
        setLayoutParams(lp);
        layout(x, y, x + getWidth(), y + getHeight());
    }
}

package com.example.fripl.propertyanimtest;

import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.animation.TimeInterpolator;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.constraint.ConstraintLayout;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.MotionEvent;
import android.view.View;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnticipateInterpolator;
import android.view.animation.AnticipateOvershootInterpolator;
import android.view.animation.BounceInterpolator;
import android.view.animation.CycleInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.LinearInterpolator;
import android.view.animation.OvershootInterpolator;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.Spinner;
import android.widget.TextView;

import org.w3c.dom.Text;

import java.util.List;

/*
* かなり構造が雑になってしまった…
* */

public class MainActivity extends AppCompatActivity {

    //移動ポイント作成
    MovePoint startPoint;
    MovePoint endPoint;

    //移動範囲
    Rect moveRect = new Rect();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //移動ポイントをレイアウトに追加
        startPoint = new MovePoint(this, 20);
        endPoint = new MovePoint(this, 30);
        ConstraintLayout controlLayout = (ConstraintLayout)findViewById(R.id.parentLayout);
        controlLayout.addView(startPoint);
        controlLayout.addView(endPoint);

        //アイテム選択イベント
        Spinner spinner = (Spinner)findViewById(R.id.spinner);
        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    //0,4,7はseekBarは未使用なので無効にする
                    case 0:case 4:case 7:
                        ((SeekBar)findViewById(R.id.seekBar)).setEnabled(false);
                        break;
                    default:
                        ((SeekBar)findViewById(R.id.seekBar)).setEnabled(true);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });

        //再生ボタンクリックイベント
        Button button = (Button)findViewById(R.id.button);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //スピナーポジション取得
                Spinner spinner = (Spinner)findViewById(R.id.spinner);
                int index = spinner.getSelectedItemPosition();

                //シークバーの位置取得
                SeekBar seekBar = (SeekBar)findViewById(R.id.seekBar);
                float f = seekBar.getProgress() / 10.0f;


                ImageView imageView = (ImageView)findViewById(R.id.imageView);

                //アニメーターセット
                float startX = startPoint.getCenterX() - imageView.getLeft() - imageView.getWidth()/2;
                float startY = startPoint.getCenterY() - imageView.getTop() - imageView.getHeight()/2;
                float endX = endPoint.getCenterX() - imageView.getLeft() - imageView.getWidth()/2;
                float endY = endPoint.getCenterY() - imageView.getTop() - imageView.getHeight()/2;
                PropertyValuesHolder translateX = PropertyValuesHolder.ofFloat("translationX", startX, endX);
                PropertyValuesHolder translateY = PropertyValuesHolder.ofFloat("translationY", startY, endY);
                ObjectAnimator objectAnimator = ObjectAnimator.ofPropertyValuesHolder(imageView, translateX, translateY);
                objectAnimator.setDuration(1000);
                objectAnimator.setInterpolator(getInterpolator(index, f));
                objectAnimator.start();

            }
        });

        controlLayout.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()){
                    case MotionEvent.ACTION_DOWN:
                        v.performClick();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        TextView textView = (TextView)findViewById(R.id.textView3);
                        textView.setText("" + (int)event.getX() + "," + (int)event.getY());
                        break;
                }

                return true;
            }
        });
    }

    //このときでないとコントロールの座標(getLeftなど)が取得できない
    @Override
    public void onWindowFocusChanged(boolean hasFocus)
    {
        super.onWindowFocusChanged(hasFocus);
        ConstraintLayout controlLayout = (ConstraintLayout)findViewById(R.id.parentLayout);

        //移動範囲作成
        moveRect.top = ((TextView)findViewById(R.id.textView2)).getBottom();
        moveRect.left = controlLayout.getLeft();
        moveRect.right = controlLayout.getRight();
        moveRect.bottom = controlLayout.getBottom();
        startPoint.setMoveRect(moveRect);
        endPoint.setMoveRect(moveRect);

        //移動ポイントを最前面に
        startPoint.bringToFront();
        endPoint.bringToFront();

        //位置補正
        startPoint.move(moveRect.left, moveRect.top);
        endPoint.move(moveRect.left, moveRect.top);
    }

    //スピナーアイテム選択ポジションによってinterpolatorを返す関数
    private TimeInterpolator getInterpolator(int type, float f){
        switch (type){
            case 0:
                return new AccelerateDecelerateInterpolator();
            case 1:
                return new AccelerateInterpolator(f);
            case 2:
                return new AnticipateInterpolator(f);
            case 3:
                return new AnticipateOvershootInterpolator(f);
            case 4:
                return new BounceInterpolator();
            case 5:
                return new CycleInterpolator(f);
            case 6:
                return new DecelerateInterpolator(f);
            case 7:
                return new LinearInterpolator();
            case 8:
                return new OvershootInterpolator(f);
            default:
                return null;
        }
    }
}

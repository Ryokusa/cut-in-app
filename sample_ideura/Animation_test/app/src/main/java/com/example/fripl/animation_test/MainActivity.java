package com.example.fripl.animation_test;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.Spinner;

public class MainActivity extends AppCompatActivity {

    //animationリソース配列を定義
    private int[] ids = {R.anim.rotate, R.anim.move_fast, R.anim.move_slow, R.anim.move, R.anim.curve_move, R.anim.fade_in, R.anim.fade_out};
    private int index;  //スピーナー選択インデックス

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //Buttonオブジェクトを取得
        Button animButton = (Button)findViewById(R.id.button);

        //ボタンクリックリスナー登録
        animButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){    //クリックされたああああああああああああああああ
                //spinnerの選択インデックス取得
                Spinner spinner = (Spinner)findViewById(R.id.spinner);
                index = spinner.getSelectedItemPosition();

                //アニメーションを取得・再生
                ImageView img1 = (ImageView)findViewById(R.id.imageView);
                Animation animation = AnimationUtils.loadAnimation(v.getContext(), ids[index]);
                img1.startAnimation(animation);
            }
        });
    }
}

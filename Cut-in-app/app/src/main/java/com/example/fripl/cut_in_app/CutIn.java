package com.example.fripl.cut_in_app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;

import com.example.fripl.cut_in_app.data_manage.AnimObj;

import java.util.ArrayList;

/**
 * Created by fripl on 2017/12/25.
 * カットインデータクラス
 * 保存するのでシリアライズしておく
 */

public class CutIn extends ConstraintLayout implements Cloneable {
    private final String TAG = "CutIn";

    private Context context;
    private Drawable thumbnail;
    public ArrayList<AnimObj> animObjList = new ArrayList<>();
    public AnimatorSet animatorSet = new AnimatorSet();
    private String title;
    public boolean animated = false;


    //コンストラクタ
    public CutIn(Context context, String title, int resource){
        super(context);
        //コンテキスト取得
        this.context = context;

        //サムネ設定
        this.thumbnail = getResources().getDrawable(resource);

        //タイトル設定
        this.title = title;

        //初期化
        setLayoutParams(new ConstraintLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
        setBackgroundColor(Color.argb(0,0,0,0));
        setVisibility(View.INVISIBLE);
    }

    public CutIn(Context context, String title){
        this(context, title, R.mipmap.ic_launcher);
    }

    public CutIn(Context context){
        this(context, "non title", R.mipmap.ic_launcher);
    }

    public CutIn(Context context, int resource){
        this(context, "non title", resource);
    }

    public void addImageView(int resource, int x, int y, int width, int height){
        animObjList.add(new AnimObj(context, getResources().getDrawable(resource), x, y,  width, height));
    }

    public void addImageView(int resource, int width, int height){
        addImageView(resource, 0, 0, width, height);
    }

    //再生
    public void play() {
        animatorSet.end();
        setVisibility(View.VISIBLE);    //可視化
        removeListener();   //旧animatorリスナー削除

        //新しくアニメーターセット作成
        animatorSet = new AnimatorSet();
        addListener();  //リスナー登録

        //アニメーションセット作成
        ArrayList<Animator> animatorSets = new ArrayList<>();
        for(AnimObj animObj : animObjList){
            //アニメーションオブジェクトからアニメーター引き抜き
            //アニメーションするとアニメーターセットが書き換えられるためディープコピー
            animatorSets.add(animObj.getAnimatorSet().clone());
        }
        animatorSet.playTogether(animatorSets);
        animatorSet.start();    //再生
    }

    //Viewにオブジェクト追加
    public void addObject(){
        for(AnimObj animObj : animObjList){
            if(animObj.type == AnimObj.Type.Image){ //画像オブジェクト
                addView(animObj.imageView);
            }else if (animObj.type == AnimObj.Type.Text){   //テキストオブジェクト
                addView(animObj.textView);
            }
        }
    }

    //リスナー追加処理
    public void addListener(){
        animatorSet.addListener(animatorListener);
    }

    //リスナー削除
    public void removeListener(){
        animatorSet.removeListener(animatorListener);
    }

    //getset
    public Drawable getThumbnail(){
        return thumbnail;
    }

    public void setThumbnail(Drawable thumbnail){
        this.thumbnail = thumbnail;
    }

    public String getTitle(){
        return this.title;
    }

    public void setTitle(String title){
        this.title = title;
    }

    public AnimatorSet getAnimatorSet(){
        return animatorSet;
    }

    public void setAnimatorSet(AnimatorSet animatorSet){
        this.animatorSet = animatorSet;
    }

    public ArrayList<AnimObj> getAnimObjList(){
        return this.animObjList;
    }

    public void setAnimObjList(ArrayList<AnimObj> animObjList){
        this.animObjList = animObjList;
    }

    private Animator.AnimatorListener animatorListener = new Animator.AnimatorListener() {
        @Override
        public void onAnimationStart(Animator animation) {
            animated = true;
        }

        @Override
        public void onAnimationEnd(Animator animation) {
            //終了時非表示
            animated = false;
            setVisibility(View.INVISIBLE);
        }

        @Override
        public void onAnimationCancel(Animator animation) {

        }

        @Override
        public void onAnimationRepeat(Animator animation) {

        }
    };

    public CutIn clone(){
        try{
            //ディープコピー
            CutIn rCutIn;
            rCutIn = (CutIn) super.clone();
            rCutIn.setAnimObjList(new ArrayList<AnimObj>());
            for(AnimObj ao : animObjList){
                rCutIn.getAnimObjList().add(ao.clone());
            }
            rCutIn.setAnimatorSet(animatorSet.clone());
            return rCutIn;
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
            return null;
        }
    }
}

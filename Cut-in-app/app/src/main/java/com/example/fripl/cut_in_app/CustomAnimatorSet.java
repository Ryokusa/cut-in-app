package com.example.fripl.cut_in_app;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.graphics.Color;
import android.view.View;
import android.widget.ImageView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2018/01/18.
 * アニメーションオブジェクト
 */

public class CustomAnimatorSet{
    public AnimatorSet animatorSet = new AnimatorSet();
    private List<Animator> animatorList = new ArrayList<Animator>();

    //オブジェクトタイプ
    enum Type{
        Image,
        Text
    }
    private int type;

    //

    //ホルダー
    //プロパティホルダー
    private PropertyValuesHolder alphaHolder;
    private PropertyValuesHolder translationXHolder, translationYHolder;
    private PropertyValuesHolder rotateHolder;
    private PropertyValuesHolder scaleXHolder, scaleYHolder;

    public CustomAnimatorSet() {
        initHolder();
    }

    /* アニメーション作成関連 */

    //移動プロパティ追加
    public void addTranslationHolder(int toX, int toY){
        translationXHolder = PropertyValuesHolder.ofFloat("translationX", toX);
        translationYHolder = PropertyValuesHolder.ofFloat("translationY", toY);
    }

    //透明プロパティ追加
    public void addAlphaHolder(float alpha) {
        alphaHolder = PropertyValuesHolder.ofFloat("alpha", alpha);
    }

    //回転プロパティ追加
    public void addRotateHolder(float degree){
        rotateHolder = PropertyValuesHolder.ofFloat("rotation", degree);
    }

    //拡大縮小プロパティ追加
    public void addScaleHolder(float toScaleX, float toScaleY){
        scaleXHolder = PropertyValuesHolder.ofFloat("scaleX", toScaleX);
        scaleYHolder = PropertyValuesHolder.ofFloat("scaleY", toScaleY);
    }

    //設定されたプロパティホルダーを元にanimator追加・セット作成
    public void addAnimator(Object target, int duration){
        //アニメーター作成追加
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, translationXHolder, translationYHolder, alphaHolder, rotateHolder, scaleXHolder, scaleYHolder);
        animator.setDuration(duration);
        animatorList.add(animator);

        //次のアニメーションのためにホルダー初期化
        initHolder();

        //現時点でのanimatorSet作成
        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animatorList);
    }

    private void initHolder(){
        //意味がないbackGroundColorを入れて初期化(これで任意のホルダーを無効に出来る)
        alphaHolder = PropertyValuesHolder.ofInt("backGroundColor", Color.argb(0,0,0,0));
        translationXHolder = alphaHolder;
        translationYHolder = alphaHolder;
        rotateHolder = alphaHolder;
        scaleXHolder = alphaHolder;
        scaleYHolder = alphaHolder;
    }

    public void play() {
        //アニメーター再生
        animatorSet.start();
    }

    public void end() {
        animatorSet.end();
    }

    //リスナー設定関連
    public void addListener(Animator.AnimatorListener animatorListener){
        animatorSet.addListener(animatorListener);
    }

    public void removeListener(Animator.AnimatorListener animatorListener){
        animatorSet.removeListener(animatorListener);
    }

    //アニメーション関連
    public static List<Animator> addTranslation(List<Animator> animatorList, Object target, int fromX, int fromY, int toX, int toY, int duration){
        PropertyValuesHolder transX = PropertyValuesHolder.ofFloat("translationX", fromX, toX);
        PropertyValuesHolder transY = PropertyValuesHolder.ofFloat("translationY", fromY, toY);
        ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(target, transX, transY);
        animator.setDuration(duration);
        animatorList.add(animator);
        return animatorList;
    }

}

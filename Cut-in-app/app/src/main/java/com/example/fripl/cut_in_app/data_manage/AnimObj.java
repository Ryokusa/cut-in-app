package com.example.fripl.cut_in_app.data_manage;

import android.animation.Animator;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.drawable.Drawable;
import android.support.constraint.ConstraintLayout;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2017/12/27.
 * 未使用
 */

public class AnimObj implements Cloneable {

    private AnimatorSet animatorSet = new AnimatorSet();
    private ArrayList<Animator> animatorList = new ArrayList<Animator>();

    Context context;

    //オブジェクトタイプ
    public enum Type{
        Image,
        Text
    }
    public Type type;

    //オブジェクト
    public ImageView imageView;
    public TextView textView;
    public float imageRatio;

    //保存用ホルダー
    private ArrayList<CustomPropertyValuesHolderList> saveHolders = new ArrayList<>();
    private CustomPropertyValuesHolderList holders = new CustomPropertyValuesHolderList();

    //初期設定
    private int initWidth, initHeight, initX, initY;
    private float initTextSize;


    //画像オブジェクト作成
    public AnimObj(Context context, Drawable drawable, int x, int y, int width, int height){
        this.context = context;

        //ImageView作成
        ImageView imageView  = new ImageView(context);
        imageView.setImageDrawable(drawable);

        //widthまたはheightが-1の場合画像のサイズ
        if (width != -1) {
            imageView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            initWidth = width;
            initHeight = height;
        }else{
            initWidth = drawable.getIntrinsicWidth();
            initHeight = drawable.getIntrinsicHeight();
        }
        initX = x;
        initY = y;

        this.imageView = imageView;
        this.type = Type.Image;

        //画像比率保存
        imageRatio = (float)initWidth / initHeight;

        //初期化ホルダーセット
        setInitHolder();
    }

    public AnimObj(Context context, Drawable drawable, int width, int height){
        this(context, drawable, 0, 0, width, height);
    }

    //テキストオブジェクト作成
    public AnimObj(Context context, String text, float size){
        this(context, text, 0, 0, size);
    }

    public AnimObj(Context context, String text, int x, int y, float size){
        this.context = context;

        //TextView作成
        TextView textView = new TextView(context);
        if(size != -1) textView.setTextSize(size);
        textView.setText(text);

        //初期値
        initX = x;
        initY = y;
        initTextSize = size;

        this.textView = textView;
        this.type = Type.Text;

        //初期化ホルダーセット
        setInitHolder();
    }

    /* アニメーション作成関連 */

    //移動プロパティ追加
    public void addTranslationHolder(int toX, int toY){
        holders.add(new CustomPropertyValuesHolder("translationX", toX));
        holders.add(new CustomPropertyValuesHolder("translationY", toY));
    }

    //透明プロパティ追加
    public void addAlphaHolder(float alpha) {
        holders.add(new CustomPropertyValuesHolder("alpha", alpha));
    }

    //回転プロパティ追加
    public void addRotateHolder(float degree){
        holders.add(new CustomPropertyValuesHolder("rotation", degree));
    }

    //拡大縮小プロパティ追加
    public void addScaleHolder(float toScaleX, float toScaleY){
        holders.add(new CustomPropertyValuesHolder("scaleX", toScaleX));
        holders.add(new CustomPropertyValuesHolder("scaleY", toScaleY));
    }

    //文字サイズプロパティ追加
    public void addTextSizeHolder(float textSize){
        holders.add(new CustomPropertyValuesHolder("textSize", textSize));
    }

    //設定されたプロパティホルダーを元にanimator追加・セット作成
    public void addAnimator(int duration){
        //アニメーター作成追加
        ObjectAnimator animator = new ObjectAnimator();
        if(type == Type.Image) {
            animator = ObjectAnimator.ofPropertyValuesHolder(imageView, getPropertyValuesHolderList(holders).toArray(new PropertyValuesHolder[holders.size()]));
        }else if (type == Type.Text) {
            animator = ObjectAnimator.ofPropertyValuesHolder(textView, getPropertyValuesHolderList(holders).toArray(new PropertyValuesHolder[holders.size()]));
        }
        animator.setDuration(duration);
        animatorList.add(animator);

        //保存用ホルダーに追加
        saveHolders.add(holders);

        //次のアニメーションのためにホルダー初期化
        holders = new CustomPropertyValuesHolderList();


    }

    //指定されたインデックスに追加
    public void addAnimator(int duration, int index){
        //アニメーター作成追加
        if(index > animatorList.size()) addAnimator(duration);
        ObjectAnimator animator = new ObjectAnimator();
        if(type == Type.Image) {
            animator = ObjectAnimator.ofPropertyValuesHolder(imageView, getPropertyValuesHolderList(holders).toArray(new PropertyValuesHolder[holders.size()]));
        }else if (type == Type.Text) {
            animator = ObjectAnimator.ofPropertyValuesHolder(textView, getPropertyValuesHolderList(holders).toArray(new PropertyValuesHolder[holders.size()]));
        }
        animator.setDuration(duration);
        animatorList.add(index, animator);

        //保存用ホルダーに追加
        saveHolders.add(index, holders);

        //次のアニメーションのためにホルダー初期化
        holders = new CustomPropertyValuesHolderList();
    }

    //指定されたインデックスのアニメーション削除
    public void removeAnimator(int index){
        animatorList.remove(index);
        saveHolders.remove(index);
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

    //getter
    public AnimatorSet getAnimatorSet() {
        //現時点のAnimatorSet作成して返す
        animatorSet = new AnimatorSet();
        animatorSet.playSequentially(animatorList);

        return animatorSet;
    }

    public List<Animator> getAnimatorList() {
        return animatorList;
    }

    public Type getType() {
        return type;
    }

    public ArrayList<CustomPropertyValuesHolderList> getSaveHolders() {
        return saveHolders;
    }

    //setter
    public void setHolders(CustomPropertyValuesHolderList holders) {
        this.holders = holders;
    }

    public void setAnimatorList(ArrayList<Animator> animatorList) {
        this.animatorList = animatorList;
    }

    //保存用ホルダーリストからホルダーリストへ変換・取得
    public static ArrayList<PropertyValuesHolder> getPropertyValuesHolderList(CustomPropertyValuesHolderList customHolders){
        ArrayList<PropertyValuesHolder> holders = new ArrayList<>();
        for(CustomPropertyValuesHolder customHolder : customHolders){
            holders.add(customHolder.getPropertyValuesHolder());
        }
        return holders;
    }

    //初期化ホルダーセット処理
    public void setInitHolder(){
        //ホルダーを一時避難
        CustomPropertyValuesHolderList holders = this.holders;
        this.holders = new CustomPropertyValuesHolderList();

        if(type == Type.Image) {
            //初期位置・サイズへ
            if (animatorList.size() != 0) {
                removeAnimator(0);  //初期化ホルダー削除
            }
            addTranslationHolder(initX, initY);
            addScaleHolder(1.0f, 1.0f);
            addAnimator(0, 0);  //初期化ホルダー追加
        }else if (type == Type.Text){
            //初期テキストサイズへ
            if (animatorList.size() != 0) {
                removeAnimator(0);  //初期化ホルダー削除
            }
            addTranslationHolder(initX, initY);
            addTextSizeHolder(initTextSize);
            addAnimator(0, 0);
        }
        this.holders = holders;
    }

    public int getInitX() {
        return initX;
    }

    public int getInitY() {
        return initY;
    }

    public int getInitHeight() {
        return initHeight;
    }

    public int getInitWidth() {
        return initWidth;
    }

    public float getInitTextSize() {
        return initTextSize;
    }

    public void setInitX(int initX) {
        this.initX = initX;
    }

    public void setInitY(int initY) {
        this.initY = initY;
        setInitHolder();
    }

    public void setInitHeight(int initHeight) {
        this.initHeight = initHeight;
        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(getInitWidth(), getInitHeight()));
    }

    public void setInitWidth(int initWidth) {
        this.initWidth = initWidth;
        imageView.setLayoutParams(new ConstraintLayout.LayoutParams(getInitWidth(), getInitHeight()));
    }

    public void setInitTextSize(float initTextSize) {
        this.initTextSize = initTextSize;
    }

    //画像セット
    public void setImage(Bitmap bitmap){
        imageView.setImageBitmap(bitmap);
        //比率測定
        imageRatio = (float)bitmap.getWidth() / bitmap.getHeight();
        setInitWidth((int)(imageRatio * getInitHeight()));
    }

    public void setImageRatio(float imageRatio) {
        this.imageRatio = imageRatio;
    }

    public float getImageRatio() {
        return imageRatio;
    }

    public void setSaveHolders(ArrayList<CustomPropertyValuesHolderList> saveHolders) {
        this.saveHolders = saveHolders;
    }

    public CustomPropertyValuesHolderList getHolders() {
        return holders;
    }

    public AnimObj clone(){
        try {
            AnimObj animObj = (AnimObj) super.clone();

            if(type == Type.Image) {
                animObj.imageView = new ImageView(context);
                animObj.imageView.setLayoutParams(imageView.getLayoutParams());
                animObj.imageView.setImageDrawable(imageView.getDrawable());
                animObj.imageView.setTranslationX(imageView.getTranslationX());
                animObj.imageView.setTranslationY(imageView.getTranslationY());
                animObj.imageView.setAlpha(imageView.getAlpha());
                animObj.imageView.setScaleX(imageView.getScaleX());
                animObj.imageView.setScaleY(imageView.getScaleY());
                animObj.imageView.setRotation(imageView.getRotation());
            }else if(type == Type.Text) {
                animObj.textView = new TextView(context);
                animObj.textView.setText(textView.getText());
                animObj.textView.setTextSize(textView.getTextSize() / (textView.getText().length()-1));
                animObj.textView.setTranslationX(textView.getTranslationX());
                animObj.textView.setTranslationY(textView.getTranslationY());
                animObj.textView.setAlpha(textView.getAlpha());
                animObj.textView.setScaleX(textView.getScaleX());
                animObj.textView.setScaleY(textView.getScaleY());
                animObj.textView.setRotation(textView.getRotation());
            }


            animObj.setAnimatorList(new ArrayList<Animator>());
            for(Animator animator : animatorList){
                Animator animator1 = animator.clone();
                //ターゲット指定
                if(type == Type.Image) {
                    animator1.setTarget(animObj.imageView);
                }else if(type == Type.Text){
                    animator1.setTarget(animObj.textView);
                }
                animObj.getAnimatorList().add(animator1);
            }

            animObj.setHolders(new CustomPropertyValuesHolderList());
            for(CustomPropertyValuesHolder cpvh : holders){
                animObj.getHolders().add(cpvh.clone());
            }
            animObj.setSaveHolders(new ArrayList<CustomPropertyValuesHolderList>());
            for(CustomPropertyValuesHolderList cpvhl : saveHolders){
                animObj.getSaveHolders().add(cpvhl.clone());
            }
            return animObj;
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
            return null;
        }
    }

    //アニメーション編集関連の関数
    public void setTranslation(int index, float x, float y){
        CustomPropertyValuesHolderList holderList = saveHolders.get(index).clone();
        boolean f = false;
        for(CustomPropertyValuesHolder holder : holderList) {
            //移動プロパティがある場合変更
            switch (holder.getPropertyName()) {
                case "translationX":
                    holder.setValues(x);
                    f = true;
                    break;
                case "translationY":
                    holder.setValues(y);
                    f = true;
                    break;
            }
        }
        if (!f) holderList.add(new CustomPropertyValuesHolder("translationX", x));
        if (!f) holderList.add(new CustomPropertyValuesHolder("translationY", y));

        //アニメーターリスト入れ替え
        this.holders = holderList.clone();
        long duration = animatorList.get(index).getDuration();
        removeAnimator(index);  //削除
        addAnimator((int)duration, index);  //また追加//intキャストよくない

    }

    public void setAlpha(int index, float alpha){
        CustomPropertyValuesHolderList holderList = saveHolders.get(index).clone();
        boolean f = false;
        for(CustomPropertyValuesHolder holder : holderList) {
            //移動プロパティがある場合変更
            switch (holder.getPropertyName()) {
                case "alpha":
                    holder.setValues(alpha);
                    f = true;
                    break;
            }
        }
        if(!f) holderList.add(new CustomPropertyValuesHolder("alpha", alpha));

        //アニメーターリスト入れ替え
        this.holders = holderList.clone();
        long duration = animatorList.get(index).getDuration();
        removeAnimator(index);  //削除
        addAnimator((int)duration, index);  //また追加//intキャストよくない

    }

    public void setRotate(int index, float deg){
        CustomPropertyValuesHolderList holderList = saveHolders.get(index).clone();
        boolean f = false;
        for(CustomPropertyValuesHolder holder : holderList) {
            //移動プロパティがある場合変更
            switch (holder.getPropertyName()) {
                case "rotation":
                    holder.setValues(deg);
                    f = true;
                    break;
            }
        }
        if(!f) holderList.add(new CustomPropertyValuesHolder("rotation", deg));

        //アニメーターリスト入れ替え
        this.holders = holderList.clone();
        long duration = animatorList.get(index).getDuration();
        removeAnimator(index);  //削除
        addAnimator((int)duration, index);  //また追加//intキャストよくない
    }

    public void setScale(int index, float scaleX, float scaleY){
        CustomPropertyValuesHolderList holderList = saveHolders.get(index).clone();
        boolean f = false;
        for(CustomPropertyValuesHolder holder : holderList) {
            //移動プロパティがある場合変更
            switch (holder.getPropertyName()) {
                case "scaleX":
                    holder.setValues(scaleX);
                    f = true;
                    break;
                case "scaleY":
                    holder.setValues(scaleY);
                    f = true;
                    break;
            }
        }
        if (!f) holderList.add(new CustomPropertyValuesHolder("scaleX", scaleX));
        if (!f) holderList.add(new CustomPropertyValuesHolder("scaleY", scaleY));

        //アニメーターリスト入れ替え
        this.holders = holderList.clone();
        long duration = animatorList.get(index).getDuration();
        removeAnimator(index);  //削除
        addAnimator((int)duration, index);  //また追加//intキャストよくない
    }

    public void setTextSize(int index, float textSize){
        CustomPropertyValuesHolderList holderList = saveHolders.get(index).clone();
        boolean f = false;
        for(CustomPropertyValuesHolder holder : holderList) {
            //移動プロパティがある場合変更
            switch (holder.getPropertyName()) {
                case "textSize":
                    holder.setValues(textSize);
                    f = true;
                    break;
            }
        }
        if(!f) holderList.add(new CustomPropertyValuesHolder("textSize", textSize));

        //アニメーターリスト入れ替え
        this.holders = holderList.clone();
        long duration = animatorList.get(index).getDuration();
        removeAnimator(index);  //削除
        addAnimator((int)duration, index);  //また追加//intキャストよくない
    }

    public View getObjView(){
        if(type == Type.Image){
            return imageView;
        }else{
            return textView;
        }
    }

}

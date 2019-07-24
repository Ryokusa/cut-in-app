package com.example.fripl.cut_in_app.data_manage;

import android.animation.Animator;
import android.animation.ObjectAnimator;
import android.animation.PropertyValuesHolder;
import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.fripl.cut_in_app.CutIn;

import java.io.ByteArrayOutputStream;
import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by fripl on 2018/01/21.
 * 読み書き用のCutInクラス
 * Context系はシリアライズできないため
 */

public class CutInData implements Serializable {

    private byte[] thumbnailByte;
    private String title;
    private ArrayList<AnimObjDataModel> animObjDataModelList = new ArrayList<>();

    //コンストラクタ
    public CutInData(CutIn cutIn){
        //画像データ格納
        Bitmap bmp = ((BitmapDrawable)cutIn.getThumbnail()).getBitmap();
        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
        thumbnailByte = stream.toByteArray();

        title = cutIn.getTitle();
        setAnimObjDataModelList(cutIn.getAnimObjList());    //AnimObjDataModelListを変換・格納
    }

    //以下シリアライズ用

    //imageView用
    public class ImageViewModel implements Serializable {
        private byte[] imageByte;
        private int width, height;

        public ImageViewModel(ImageView imageView){
            //画像データ格納
            Bitmap bmp = ((BitmapDrawable)imageView.getDrawable()).getBitmap();
            ByteArrayOutputStream stream = new ByteArrayOutputStream();
            bmp.compress(Bitmap.CompressFormat.PNG, 100, stream);
            imageByte = stream.toByteArray();

            //サイズ(imageViewはまだ中身が生成されていないからかサイズは0)
            ViewGroup.LayoutParams layoutParams = imageView.getLayoutParams();
            width = layoutParams.width;
            height = layoutParams.height;
        }

        //ImageViewを作成して返す
        public ImageView getImageView(Context context){
            ImageView imageView = new ImageView(context);
            imageView.setImageBitmap(BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length));
            imageView.setLayoutParams(new ViewGroup.LayoutParams(width, height));
            return imageView;
        }

        public int getWidth() {
            return width;
        }

        public int getHeight() {
            return height;
        }

        public Drawable getDrawable(){
            //何故かwarningが出るが対象APIレベルは18からなので無視
            return new BitmapDrawable(BitmapFactory.decodeByteArray(imageByte, 0, imageByte.length));
        }
    }

    //TextView用
    public class TextViewModel implements Serializable {
        private String text;
        private float size;

        public TextViewModel(TextView textView) {
            this.text = textView.getText().toString();
            this.size = textView.getTextSize();
        }

        //TextViewを作成して返す
        public TextView getTextView(Context context){
            TextView textView = new TextView(context);
            textView.setTextSize(size);
            textView.setText(text);

            return textView;
        }

        public float getSize() {
            return size;
        }

        public void setSize(float size) {
            this.size = size;
        }

        public void setText(String text) {
            this.text = text;
        }

        public String getText() {
            return text;
        }
    }

    //animator用
    public class AnimatorDataModel implements Serializable {
        private CustomPropertyValuesHolderList holders;
        private long duration;

        public AnimatorDataModel(ObjectAnimator animator, CustomPropertyValuesHolderList customHolders){
            holders = customHolders;
            duration = animator.getDuration();
        }

        //ObjectAnimatorを作成して返す
        public ObjectAnimator getObjectAnimator(Object object){
            ObjectAnimator animator = ObjectAnimator.ofPropertyValuesHolder(object, AnimObj.getPropertyValuesHolderList(holders).toArray(new PropertyValuesHolder[holders.size()]));
            animator.setDuration(duration);

            return animator;
        }

        public long getDuration() {
            return duration;
        }

        public CustomPropertyValuesHolderList getHolders() {
            return holders;
        }

        public void setDuration(long duration) {
            this.duration = duration;
        }

        public void setHolders(CustomPropertyValuesHolderList holders) {
            this.holders = holders;
        }
    }

    //animObj用
    public class AnimObjDataModel implements Serializable {
        //animObj作成に必要な変数定義
        ArrayList<AnimatorDataModel> animatorDataModelList = new ArrayList<>();
        ArrayList<CustomPropertyValuesHolderList> holders = new ArrayList<>();

        ImageViewModel imageViewModel;
        TextViewModel textViewModel;
        AnimObj.Type type;
        public float imageRatio;
        private int initWidth, initHeight, initX, initY;
        private float initTextSize;

        public AnimObjDataModel(AnimObj animObj){
            //各変数をModel型に変換して格納
            type = animObj.getType();
            int i = 0;
            for(Animator animator : animObj.getAnimatorList()){
                animatorDataModelList.add(new AnimatorDataModel((ObjectAnimator)animator, animObj.getSaveHolders().get(i)));
                i++;
            }
            if(type == AnimObj.Type.Image){
                imageViewModel = new ImageViewModel(animObj.imageView);
            }else if (type == AnimObj.Type.Text){
                textViewModel = new TextViewModel(animObj.textView);
            }
            holders = animObj.getSaveHolders();
            imageRatio = animObj.getImageRatio();
            initWidth = animObj.getInitWidth();
            initHeight = animObj.getInitHeight();
            initX = animObj.getInitX();
            initY = animObj.getInitY();
            initTextSize = animObj.getInitTextSize();
        }

        //AnimObjを作成して返す
        public AnimObj getAnimObj(Context context){
            //AnimObjのコンストラクター
            AnimObj animObj = new AnimObj(context, "", -1); //とりあえず初期化
            if(type == AnimObj.Type.Image) {
                animObj = new AnimObj(context, imageViewModel.getDrawable(), imageViewModel.getWidth(), imageViewModel.getHeight());
            }else if (type == AnimObj.Type.Text){
                animObj = new AnimObj(context, textViewModel.getText(), textViewModel.getSize());
            }

            //animatorList作成・適用
            ArrayList<Animator> animatorList = new ArrayList<>();
            //ObjectAnimator作成(Holderも適用済み)
            for(AnimatorDataModel animatorDataModel : animatorDataModelList){
                if(type == AnimObj.Type.Image) {
                    animatorList.add(animatorDataModel.getObjectAnimator(animObj.imageView));
                }else if (type == AnimObj.Type.Text){
                    animatorList.add(animatorDataModel.getObjectAnimator(animObj.textView));
                }
            }
            animObj.setAnimatorList(animatorList);

            animObj.setSaveHolders(holders);

            animObj.setInitWidth(initWidth);
            animObj.setInitHeight(initHeight);
            animObj.setInitX(initX);
            animObj.setInitY(initY);
            animObj.setInitTextSize(initTextSize);

            return animObj;
        }

    }

    //カットインを作成して返す
    public CutIn getCutIn(Context context){
        CutIn cutIn = new CutIn(context);   //基本カットイン作成
        cutIn.setAnimObjList(getAnimObjList(context));  //アニメーションオブジェクトセット
        cutIn.setTitle(title);
        cutIn.setThumbnail(new BitmapDrawable(BitmapFactory.decodeByteArray(thumbnailByte, 0, thumbnailByte.length)));

        return cutIn;
    }

    //AnimObj形式のリストを取得
    private ArrayList<AnimObj> getAnimObjList(Context context){
        ArrayList<AnimObj> animObjList = new ArrayList<>();
        for(AnimObjDataModel animObjDataModel : animObjDataModelList){
            animObjList.add(animObjDataModel.getAnimObj(context));
        }
        return animObjList;
    }

    //AnimObjListよりAnimObjDataModelListへ格納
    private void setAnimObjDataModelList(ArrayList<AnimObj> animObjList){
        animObjDataModelList = new ArrayList<>();   //初期化
        //変換して格納
        for(AnimObj animObj : animObjList){
            animObjDataModelList.add(new AnimObjDataModel(animObj));
        }
    }

}
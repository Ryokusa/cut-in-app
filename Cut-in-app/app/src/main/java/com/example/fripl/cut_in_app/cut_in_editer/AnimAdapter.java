package com.example.fripl.cut_in_app.cut_in_editer;

import android.content.Context;
import android.graphics.Color;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.example.fripl.cut_in_app.R;
import com.example.fripl.cut_in_app.data_manage.AnimObj;
import com.example.fripl.cut_in_app.data_manage.CustomPropertyValuesHolder;
import com.example.fripl.cut_in_app.data_manage.CustomPropertyValuesHolderList;

import org.w3c.dom.Text;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by fripl on 2018/01/29.
 */

public class AnimAdapter extends ArrayAdapter {
    private static final String TAG = "AnimAdapter";

    private LayoutInflater layoutInflater;  //レイアウト設定用
    private int selId;  //選択オブジェクトId
    private List<String> animNames = new ArrayList<>();

    List<CustomPropertyValuesHolderList> holders = new ArrayList<>();

    private OnItemClickListener onItemClickListener;
    private OnClickListener deleteListener;
    private OnClickListener addListener;
    private OnClickListener addHolderListener;

    //タグ用ホルダー
    private class ViewHolder{
        TextView animName;
        ListView animListView;
        TextView addHolderButton;
        ImageView addButton;
        ImageView deleteButton;
        public ViewHolder(View v, int position){
            animName = (TextView)v.findViewById(R.id.animName);
            animListView = (ListView)v.findViewById(R.id.animListView);
            addButton = (ImageView)v.findViewById(R.id.addAnimButton);
            deleteButton = (ImageView)v.findViewById(R.id.deleteAnimButton);
            addHolderButton = (TextView)v.findViewById(R.id.addHolderButton);
        }
    }

    public AnimAdapter(Context context, int resource, List<CustomPropertyValuesHolderList> holders, int selId){
        super(context, resource, holders);
        layoutInflater = LayoutInflater.from(context);
        this.holders = holders;

        this.selId = selId;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        final AnimAdapter.ViewHolder viewHolder;

        if(convertView == null){
            //新しく作成
            convertView = layoutInflater.inflate(R.layout.anim_adapter_layout, null);
            viewHolder = new AnimAdapter.ViewHolder(convertView, position);
            convertView.setTag(viewHolder); //タグつけ

        }else{
            //再利用
            viewHolder = (AnimAdapter.ViewHolder) convertView.getTag(); //ゲット
        }

        viewHolder.animName.setText("" + position);
        animNames = new ArrayList<>();
        for(int i = 0; i < holders.get(position).size(); i++){
            CustomPropertyValuesHolder cpvh = holders.get(position).get(i);
            if(cpvh.getPropertyName().equals("translationX")){
                //移動リスト追加
                animNames.add("移動");
                i++;    //transYをスキップ
            }else if (cpvh.getPropertyName().equals("alpha")){
                //透明リスト追加
                animNames.add("透明度");
            }else if (cpvh.getPropertyName().equals("rotation")){
                //回転リスト追加
                animNames.add("回転");
            }else if (cpvh.getPropertyName().equals("scaleX")){
                //拡大縮小リスト追加
                animNames.add("拡大・縮小");
                i++;
            }else if (cpvh.getPropertyName().equals("textSize")){
                animNames.add("テキストサイズ");
            }
        }

        ArrayAdapter<String> adapter = new ArrayAdapter<String>(convertView.getContext(), android.R.layout.simple_list_item_1, animNames);
        viewHolder.animListView.setAdapter(adapter);

        //一個分の縦幅測って全体の高さ設定
        View lv = adapter.getView(0, null, viewHolder.animListView);
        lv.measure(View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED),
                View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED));
        RelativeLayout.LayoutParams layoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, lv.getMeasuredHeight()*viewHolder.animListView.getCount());
        layoutParams.addRule(RelativeLayout.ALIGN_START, R.id.animName);
        layoutParams.addRule(RelativeLayout.BELOW, R.id.animName);
        viewHolder.animListView.setLayoutParams(layoutParams);

        //親のリストビュー位置保存
        final int parentPos = position;

        //アニメーションリストのリスナー
        viewHolder.animListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Log.i(TAG, "onItemClick:" + parentPos + ":" + position);

                //リスナー呼び
                if(onItemClickListener != null) {
                    onItemClickListener.onItemClick(parentPos, position, (String)parent.getItemAtPosition(position));
                }
            }
        });

        //削除ボタンリスナー
        viewHolder.deleteButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(deleteListener != null) deleteListener.onClick(parentPos);
            }
        });

        //追加ボタンリスナー
        viewHolder.addButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addListener != null)addListener.onClick(parentPos);
            }
        });

        //ホルダー追加ボタンリスナー
        viewHolder.addHolderButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(addHolderListener != null)addHolderListener.onClick(parentPos);
            }
        });


        return convertView;
    }

    public void setOnItemClickListener(OnItemClickListener listener){
        onItemClickListener = listener;
    }

    public void setDeleteAnimOnClickListener(OnClickListener listener){
        deleteListener = listener;
    }

    public void setAddAnimOnClickListener(OnClickListener listener) {addListener = listener;}

    public void setAddHolderOnClickListener(OnClickListener listener){addHolderListener = listener;}

    public interface OnItemClickListener{
        void onItemClick(int parentPos, int pos, String itemName);
    }

    public interface OnClickListener{
        void onClick(int position);
    }
}

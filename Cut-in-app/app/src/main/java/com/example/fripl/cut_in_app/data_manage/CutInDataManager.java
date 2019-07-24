package com.example.fripl.cut_in_app.data_manage;
import android.content.Context;
import android.util.Log;

import com.example.fripl.cut_in_app.CutIn;
import com.example.fripl.cut_in_app.CutInSet;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;

/**
 * Created by fripl on 2018/01/21.
 * カットインデータ読み書きクラス
 */

public class CutInDataManager{
    private static final String TAG = "CutInDataManager";
    private Context context;

    public CutInDataManager(Context context){
        this.context = context;
    }

    //CutIn読み込み(念のためコンテキストは各自設定)
    public ArrayList<CutIn> cutInListLoad()
    {
        try {
            //読み込み処理
            FileInputStream fis = context.openFileInput("CutInListData.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<CutInData> cutInDataList = (ArrayList<CutInData>) ois.readObject();
            ois.close();

            //読み込んだCutInDataをCutInに変換
            ArrayList<CutIn> cutInList = new ArrayList<>();
            for(CutInData cutInData : cutInDataList){
                //リストに追加
                cutInList.add(cutInData.getCutIn(context));
            }

            Log.i(TAG, "cutInListLoaded");
            return cutInList;
        }catch (Exception e){
            Log.e(TAG, "cutInListLoadFailed:" + e.getMessage());
            //見つからない場合null
            return null;
        }
    }

    public void cutInListSave(ArrayList<CutIn> cutInList) {
        try{
            FileOutputStream fos = context.openFileOutput("CutInListData.dat", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);

            //貰ったCutInListをCutInDataListに変換
            ArrayList<CutInData> cutInDataList = new ArrayList<>();
            for(CutIn cutIn : cutInList){
                //リストに追加
                cutInDataList.add(new CutInData(cutIn));
            }

            //書き込み
            oos.writeObject(cutInDataList);
            oos.close();

            Log.i(TAG, "cutInListSaved");
        }catch(Exception e){
            //保存できなかった場合
            //TODO 例外処理
            Log.e(TAG, "cutInListSaveFailed:" + e.getMessage());
        }
    }

    //カットインセット読み込み
    public ArrayList<CutInSet> cutInSetListLoad(){
        try {
            //読み込み処理
            FileInputStream fis = context.openFileInput("CutInSetList.dat");
            ObjectInputStream ois = new ObjectInputStream(fis);
            ArrayList<CutInSet> cutInSetList = (ArrayList<CutInSet>) ois.readObject();
            ois.close();

            Log.i(TAG, "cutInSetListLoaded");
            return cutInSetList;
        }catch (Exception e){
            Log.e(TAG, "cutInSetListLoadFailed:" + e.getMessage());
            //見つからない場合null
            return null;
        }
    }

    //カットインセット保存
    public void cutInSetListSave(ArrayList<CutInSet> cutInSetList){
        try {
            //保存処理
            FileOutputStream fos = context.openFileOutput("CutInSetList.dat", Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(cutInSetList);
            oos.close();

            Log.i(TAG, "cutInSetListSaved");
        }catch (Exception e){
            Log.e(TAG, "cutInSetListSaveFailed:" + e.getMessage());
            //TODO 保存できない場合
        }
    }
}

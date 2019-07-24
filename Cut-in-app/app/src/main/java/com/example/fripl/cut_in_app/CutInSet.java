package com.example.fripl.cut_in_app;

import java.io.Serializable;

/**
 * Created by fripl on 2018/01/20.
 * カットイン番号とアクション名が入ったクラス
 * 保存するのでシリアライズしておく
 */


public class CutInSet implements Serializable{
    private int cutInId = -1;
    private String action = "";

    public CutInSet(int cutInId, String action){
        this.cutInId = cutInId;
        this.action = action;
    }

    public int getCutInId(){ return this.cutInId; }
    public void setCutInId(int cutInId){ this.cutInId = cutInId; }
    public String getAction(){ return this.action; }
    public void setAction(String action){this.action = action;}

}
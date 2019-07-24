package com.example.fripl.cut_in_app.data_manage;

import android.content.ContentUris;

import java.util.ArrayList;

/**
 * Created by fripl on 2018/01/21.
 * 保存用ホルダーリスト
 */

public class CustomPropertyValuesHolderList extends ArrayList<CustomPropertyValuesHolder> implements Cloneable {
    public CustomPropertyValuesHolderList(){
        super();
    }

    public CustomPropertyValuesHolderList clone(){
        CustomPropertyValuesHolderList holderList = new CustomPropertyValuesHolderList();
        for(CustomPropertyValuesHolder cpvh : this){
            holderList.add(cpvh.clone());
        }
        return holderList;
    }
}
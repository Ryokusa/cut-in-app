package com.example.fripl.cut_in_app.data_manage;

import android.animation.PropertyValuesHolder;

import java.io.Serializable;

/**
 * Created by fripl on 2018/01/21.
 * 保存用ホルダー (ホルダーからはValuesがとれないため)
 */

public class CustomPropertyValuesHolder implements Serializable, Cloneable {
    private String propertyName;    //プロパティ名
    private float[] values; //値

    public CustomPropertyValuesHolder(String propertyName, float... values){
        this.propertyName = propertyName;
        this.values = values;
    }

    public float[] getValues() {
        return values;
    }

    //PropertyHolderに変換して返す
    public PropertyValuesHolder getPropertyValuesHolder() {
        return PropertyValuesHolder.ofFloat(propertyName, values);
    }

    public void setPropertyName(String propertyName) {
        this.propertyName = propertyName;
    }

    public String getPropertyName() {
        return propertyName;
    }

    public void setValues(float... values) {
        this.values = values;
    }

    public CustomPropertyValuesHolder clone(){
        try{
            return (CustomPropertyValuesHolder)super.clone();
        }catch (CloneNotSupportedException e){
            e.printStackTrace();
            return null;
        }
    }
}

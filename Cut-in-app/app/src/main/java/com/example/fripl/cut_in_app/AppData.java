package com.example.fripl.cut_in_app;

import android.graphics.drawable.Drawable;

/**
 * Created by fripl on 2017/12/17.
 * アプリの各種データ
 */

public class AppData {
    private String appName;
    private Drawable iconDrawable;
    private String packageName;

    public AppData(String packageName, String appName, Drawable iconDrawable){
        this.packageName = packageName;
        this.appName = appName;
        this.iconDrawable = iconDrawable;
    }

    public void setAppName(String appName)
    {
        this.appName = appName;
    }

    public void setIconId(Drawable iconDrawable)
    {
        this.iconDrawable = iconDrawable;
    }

    public String getAppName()
    {
        return this.appName;
    }

    public Drawable getIconDrawable()
    {
        return this.iconDrawable;
    }

    public void setPackageName(String packageName){ this.packageName = packageName; }
    public String getPackageName(){return this.packageName; }


}

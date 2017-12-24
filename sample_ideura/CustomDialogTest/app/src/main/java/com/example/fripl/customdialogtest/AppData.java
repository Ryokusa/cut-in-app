package com.example.fripl.customdialogtest;

import android.graphics.drawable.Drawable;

/**
 * Created by fripl on 2017/12/17.
 */

public class AppData {
    private String appName;
    private Drawable iconDrawable;

    public AppData(String appName, Drawable iconDrawable){
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

}

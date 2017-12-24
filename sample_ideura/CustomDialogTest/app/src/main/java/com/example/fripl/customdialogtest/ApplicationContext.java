package com.example.fripl.customdialogtest;

import android.app.Application;
import android.content.Context;

/**
 * Created by fripl on 2017/12/17.
 * Contextをどのファイルからも参照出来るように
 */

public class ApplicationContext extends Application {
    private static Context instance = null;

    @Override
    public void onCreate(){
        super.onCreate();

        instance = this;
    }

    public static Context getInstance()
    {
        return instance;
    }
}

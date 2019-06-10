package com.example.dong.odometer;

import android.content.Context;
import android.content.SharedPreferences;

import java.util.Set;

public class SPUtils {

    private String TAG = "SPUtils";

    private SharedPreferences sharedPreferences;
    private SharedPreferences.Editor editor;
    private static SPUtils spUtils;

    private SPUtils(Context context) {
        sharedPreferences = context.getSharedPreferences("StepCount", context.MODE_PRIVATE);
        editor = sharedPreferences.edit();
    }

    public static SPUtils getInstance(Context context) {
        if (spUtils == null) {
            spUtils = new SPUtils(context);
        }
        return spUtils;
    }

    public String getString(String key, String defaultValue) {
        return sharedPreferences.getString(key, defaultValue);
    }

    public void putString(String key, String value) {
        editor.putString(key, value);
        editor.commit();
    }

    public int getInt(String key, int defaultValue) {
        return sharedPreferences.getInt(key, defaultValue);
    }

    public void putInt(String key, int value) {
        editor.putInt(key, value);
        editor.commit();
    }

    public void putBoolean(String key,boolean value){
        editor.putBoolean(key,value);
        editor.commit();
    }

    public boolean getBoolean(String key,boolean defaultValue){
        return sharedPreferences.getBoolean(key,defaultValue);
    }

    public void remove(String key) {
        editor.remove(key);
    }

}

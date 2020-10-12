package com.example.smarttts;

import android.app.Application;

import com.example.ttslib.xfyun.XSpeechConfig;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechUtility;

/**
 * 创建时间：2020/9/18
 * 创建人：singleCode
 * 功能描述：
 **/
public class MyApplication extends Application {
    @Override
    public void onCreate() {
        super.onCreate();
//        SpeechUtility.createUtility(this, SpeechConstant.APPID +"="+ XSpeechConfig.mAPPID);
    }
}

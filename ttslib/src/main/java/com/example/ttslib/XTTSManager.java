package com.example.ttslib;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;

import com.iflytek.cloud.ErrorCode;
import com.iflytek.cloud.InitListener;
import com.iflytek.cloud.SpeechConstant;
import com.iflytek.cloud.SpeechError;
import com.iflytek.cloud.SpeechEvent;
import com.iflytek.cloud.SpeechSynthesizer;
import com.iflytek.cloud.SpeechUtility;
import com.iflytek.cloud.SynthesizerListener;
import com.iflytek.cloud.util.ResourceUtil;

/**
 * 创建时间：2020/9/18
 * 创建人：singleCode
 * 功能描述：科大讯飞TTS
 **/
public class XTTSManager implements SynthesizerListener {
    private String TAG = XTTSManager.class.getSimpleName();
    private static XTTSManager manager;
    // 语音合成对象
    private SpeechSynthesizer mTts;
    // 默认本地发音人
    public static String voicerLocal="xiaoyan";
    public static String voicerXtts="xiaoyan";
    // 默认云端发音人
    public static String voicerCloud="xiaoyan";
    private String mEngineType = SpeechConstant.TYPE_LOCAL;
    private XTTSManager(){

    }
    public static XTTSManager getInstance(){
        if(manager == null){
            synchronized (XTTSManager.class){
                if(manager == null){
                    manager = new XTTSManager();
                }
            }
        }
        return manager;
    }
    public void register(Context context,String app_id){
        if(TextUtils.isEmpty(app_id)){
            app_id = "5f6418e8";//科大讯飞自己demo的appId
        }
        StringBuffer param = new StringBuffer();
        param.append(SpeechConstant.APPID+"="+app_id);
        param.append(",");
        // 设置使用v5+
        param.append(SpeechConstant.ENGINE_MODE+"="+SpeechConstant.MODE_MSC);
        SpeechUtility.createUtility(context, param.toString());
        init(context);
    }
    private void init(Context context){
       mTts = SpeechSynthesizer.createSynthesizer(context, new InitListener() {
            @Override
            public void onInit(int code) {
                if (code != ErrorCode.SUCCESS){
                    Log.d(TAG, "onInit: error");
                }else {
                    Log.d(TAG, "onInit: success");
                }
            }
        });
    }

    /**
     * 取消合成
     */
    public void  stopSpeaking(){
       if(mTts!=null){
           mTts.stopSpeaking();
       }
    }

    /**
     * 暂停播报
     */
    public void  pauseSpeaking(){
        if(mTts != null){
            mTts.pauseSpeaking();
        }
    }

    /**
     * 继续播放
     */
    public void resumeSpeaking(){
        if(mTts != null){
            mTts.resumeSpeaking();
        }
    }

    public int startSpeaking(Context context,String text){
        if(mTts != null){
            setParameter(context);
            int code= mTts.startSpeaking(text,this);
            Log.d(TAG, "startSpeaking: "+code);
            return code;
        }
        return -1;
    }

    private void setParameter(Context context){
        // 清空参数
        mTts.setParameter(SpeechConstant.PARAMS, null);
        //设置合成
        if(mEngineType.equals(SpeechConstant.TYPE_CLOUD))
        {
            //设置使用云端引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_CLOUD);
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME,voicerCloud);

        }else if(mEngineType.equals(SpeechConstant.TYPE_LOCAL)){
            //设置使用本地引擎
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_LOCAL);
            //设置发音人资源路径
            mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath(context));
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME,voicerLocal);
        }else{
            mTts.setParameter(SpeechConstant.ENGINE_TYPE, SpeechConstant.TYPE_XTTS);
            //设置发音人资源路径
            mTts.setParameter(ResourceUtil.TTS_RES_PATH,getResourcePath(context));
            //设置发音人
            mTts.setParameter(SpeechConstant.VOICE_NAME,voicerXtts);
        }
        //mTts.setParameter(SpeechConstant.TTS_DATA_NOTIFY,"1");//支持实时音频流抛出，仅在synthesizeToUri条件下支持
        //设置合成语速
        mTts.setParameter(SpeechConstant.SPEED, "50");
        //设置合成音调
        mTts.setParameter(SpeechConstant.PITCH, "50");
        //设置合成音量
        mTts.setParameter(SpeechConstant.VOLUME, "50");
        //设置播放器音频流类型
        mTts.setParameter(SpeechConstant.STREAM_TYPE, "3");//0：通话、1：系统 2：铃声 3：音乐 4：闹铃 5：通知
//        mTts.setParameter(SpeechConstant.STREAM_TYPE, AudioManager.STREAM_MUSIC+"");

        // 设置播放合成音频打断音乐播放，默认为true
        mTts.setParameter(SpeechConstant.KEY_REQUEST_FOCUS, "true");

        // 设置音频保存路径，保存音频格式支持pcm、wav，设置路径为sd卡请注意WRITE_EXTERNAL_STORAGE权限
        mTts.setParameter(SpeechConstant.AUDIO_FORMAT, "wav");

        mTts.setParameter(SpeechConstant.TTS_AUDIO_PATH, Environment.getExternalStorageDirectory()+"/msc/tts.wav");

    }
    //获取发音人资源路径
    private String getResourcePath(Context context){
        StringBuffer tempBuffer = new StringBuffer();
        String type= "tts";
        if(mEngineType.equals(SpeechConstant.TYPE_XTTS)){
            type="xtts";
        }
        //合成通用资源
        tempBuffer.append(ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, type+"/common.jet"));
        tempBuffer.append(";");
        //发音人资源
        if(mEngineType.equals(SpeechConstant.TYPE_XTTS)){
            tempBuffer.append(ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, type+"/"+voicerXtts+".jet"));
        }else {
            tempBuffer.append(ResourceUtil.generateResourcePath(context, ResourceUtil.RESOURCE_TYPE.assets, type + "/" + voicerLocal + ".jet"));
        }

        return tempBuffer.toString();
    }

    @Override
    public void onSpeakBegin() {
        Log.d(TAG, "onSpeakBegin: 播报开始");
    }

    @Override
    public void onBufferProgress(int percent, int beginPos, int endPos,
                                 String info) {
        Log.d(TAG, "onBufferProgress: 合成进度="+percent);
    }

    @Override
    public void onSpeakPaused() {
        Log.d(TAG, "onSpeakPaused: 暂停播报");
    }

    @Override
    public void onSpeakResumed() {
        Log.d(TAG, "onSpeakResumed: 继续播报");
    }

    @Override
    public void onSpeakProgress(int percent, int beginPos, int endPos) {
        Log.d(TAG, "onSpeakProgress: 播报进度="+percent);
    }

    @Override
    public void onCompleted(SpeechError speechError) {
        Log.d(TAG, "onCompleted: 播报完成");
    }

    @Override
    public void onEvent(int eventType, int arg1, int arg2, Bundle obj) {
        // 以下代码用于获取与云端的会话id，当业务出错时将会话id提供给技术支持人员，可用于查询会话日志，定位出错原因
        // 若使用本地能力，会话id为null
        if (SpeechEvent.EVENT_SESSION_ID == eventType) {
            String sid = obj.getString(SpeechEvent.KEY_EVENT_AUDIO_URL);
            Log.d(TAG, "session id =" + sid);
        }

        //实时音频流输出参考
			/*if (SpeechEvent.EVENT_TTS_BUFFER == eventType) {
				byte[] buf = obj.getByteArray(SpeechEvent.KEY_EVENT_TTS_BUFFER);
				Log.e("MscSpeechLog", "buf is =" + buf);
			}*/
    }
}

package com.example.smarttts.binderlink;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.IBinder;
import android.os.RemoteException;
import android.util.Log;

import com.xier.keep.alive.RemoteKeepAidl;

import java.util.concurrent.CountDownLatch;

/**
 * 创建时间：2020/6/11
 * 创建人：singleCode
 * 功能描述：
 **/
public class AppBinderHelper {
    private static String TAG = AppBinderHelper.class.getSimpleName();
    private static AppBinderHelper helper;
    private Context mContext;
    private RemoteKeepAidl mBinderPool;
    private CountDownLatch mConnectBinderPoolCountDownLatch;
    private String remotePkgName;
    private String serviceAction;
    private AppBinderHelper(){
    }
    public static AppBinderHelper getHelper(){
        if(helper == null){
            synchronized (AppBinderHelper.class){
                if(helper == null){
                    helper = new AppBinderHelper();
                }
            }
        }
        return helper;
    }
    public void sendCRunBrodCast(Context context){
        Intent intent = new Intent();
        intent.setAction("com.xier.keep.link");
        intent.addFlags(0x01000000);
        context.sendBroadcast(intent);
    }
    /**
     *
     * @param context
     * @param remotePkgName  远程服务包名
     * @param serviceAction 远程服务action
     */
    public void bindService(Context context, String remotePkgName,String serviceAction){
        Log.d(TAG, "bindService: remotePkgName="+remotePkgName+" serviceAction="+serviceAction);
        this.mContext = context;
        this.remotePkgName = remotePkgName;
        this.serviceAction = serviceAction;
        connectBinderPoolService();
    }
    public void unBindSipService(Context context){
        if(mBinderPoolConnection != null){
            try {
                if(mBinderPool != null) {
                    mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
                    context.unbindService(mBinderPoolConnection);
                    mBinderPool = null;
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    private synchronized void connectBinderPoolService() {
        Intent service = new Intent();
        service.setAction(serviceAction);
        service.setPackage(remotePkgName);
        mContext.bindService(service, mBinderPoolConnection, Context.BIND_AUTO_CREATE);
    }

    private ServiceConnection mBinderPoolConnection = new ServiceConnection() {   // 5

        @Override
        public void onServiceDisconnected(ComponentName name) {
            Log.d(TAG,"onServiceDisconnected");
        }

        @Override
        public void onServiceConnected(ComponentName name, IBinder service) {
            Log.d(TAG,"onServiceConnected");
            mBinderPool =  RemoteKeepAidl.Stub.asInterface(service);
            try {
                mBinderPool.asBinder().linkToDeath(mBinderPoolDeathRecipient, 0);
            } catch (RemoteException e) {
                e.printStackTrace();
            }
        }
    };

    private IBinder.DeathRecipient mBinderPoolDeathRecipient = new IBinder.DeathRecipient() {    // 6
        @Override
        public void binderDied() {
            Log.d(TAG,"binderDied");
            mBinderPool.asBinder().unlinkToDeath(mBinderPoolDeathRecipient, 0);
            mBinderPool = null;
            connectBinderPoolService();
        }
    };
}

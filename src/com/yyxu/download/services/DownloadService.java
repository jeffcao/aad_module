
package com.yyxu.download.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.os.RemoteException;
import android.text.TextUtils;

import com.g.sc.AdLogger;
import com.yyxu.download.utils.ConfigUtils;
import com.yyxu.download.utils.MyIntents;

public class DownloadService extends Service {

    private DownloadManager mDownloadManager;

    @Override
    public IBinder onBind(Intent intent) {
    	AdLogger.i("DownloadService","DownloadService onBind");
        return new DownloadServiceImpl();
    }

    @Override
    public void onCreate() {
    	AdLogger.i("DownloadService","DownloadService onCreate");
        super.onCreate();
        mDownloadManager = new DownloadManager(this);
    }

    @Override
    public void onStart(Intent intent, int startId) {
    	AdLogger.i("DownloadService","DownloadService onStart");
        super.onStart(intent, startId);

         if (mDownloadManager == null) {
        	 mDownloadManager = new DownloadManager(this);
         }

        //if (null != intent
        //		&& intent.getAction().equals("com.yyxu.download.services.IDownloadService")) {
         if (null == intent) return;
            int type = intent.getIntExtra(MyIntents.TYPE, -1);
            String url;
            AdLogger.i("DownloadService","type is " + type);
            switch (type) {
                case MyIntents.Types.START:
                    if (!mDownloadManager.isRunning()) {
                        mDownloadManager.startManage();
                    } else {
                        mDownloadManager.reBroadcastAddAllTask();
                    }
                    break;
                case MyIntents.Types.ADD:
                    url = intent.getStringExtra(MyIntents.URL);
                    String name = intent.getStringExtra(MyIntents.SAVE_PATH);
                    if (!TextUtils.isEmpty(url) && !mDownloadManager.hasTask(url)) {
                        mDownloadManager.addTask(url, name);
                    }
                    break;
                case MyIntents.Types.CONTINUE:
                    url = intent.getStringExtra(MyIntents.URL);
                    if (!TextUtils.isEmpty(url)) {
                        mDownloadManager.continueTask(url);
                    }
                    break;
                case MyIntents.Types.DELETE:
                    url = intent.getStringExtra(MyIntents.URL);
                    if (!TextUtils.isEmpty(url)) {
                        mDownloadManager.deleteTask(url);
                    }
                    break;
                case MyIntents.Types.PAUSE:
                    url = intent.getStringExtra(MyIntents.URL);
                    if (!TextUtils.isEmpty(url)) {
                        mDownloadManager.pauseTask(url);
                    }
                    break;
                case MyIntents.Types.STOP:
                	if (null != mDownloadManager) {
                		mDownloadManager.close();
                		mDownloadManager = null;
                		ConfigUtils.clearAllUrls(getApplicationContext());
                	}
                    break;

                default:
                    break;
            }
        //}

    }

    private class DownloadServiceImpl extends IDownloadService.Stub {

        @Override
        public void startManage() throws RemoteException {
        	AdLogger.i("DownloadService","DownloadServiceImpl startManage");
            mDownloadManager.startManage();
        }

        @Override
        public void addTask(String url, String name) throws RemoteException {
        	AdLogger.i("DownloadService","DownloadServiceImpl startManage");
            mDownloadManager.addTask(url, name);
        }

        @Override
        public void pauseTask(String url) throws RemoteException {

        }

        @Override
        public void deleteTask(String url) throws RemoteException {

        }

        @Override
        public void continueTask(String url) throws RemoteException {

        }

    }

}

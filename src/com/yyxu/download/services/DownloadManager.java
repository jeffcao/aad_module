
package com.yyxu.download.services;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;

import android.content.Context;
import android.content.Intent;
import android.os.Parcelable;
import android.widget.Toast;

import com.g.sc.AdLogger;
import com.yyxu.download.utils.ConfigUtils;
import com.yyxu.download.utils.MyIntents;
import com.yyxu.download.utils.NetworkUtils;
import com.yyxu.download.utils.StorageUtils;

public class DownloadManager extends Thread {

    private static final int MAX_TASK_COUNT = 100;
    private static final int MAX_DOWNLOAD_THREAD_COUNT = 3;

    private Context mContext;

    private TaskQueue mTaskQueue;
    private List<DownloadTask> mDownloadingTasks;
    private List<DownloadTask> mPausingTasks;

    private Boolean isRunning = false;

    public DownloadManager(Context context) {
    	try {
			StorageUtils.mkdir();
		} catch (IOException e) {
			e.printStackTrace();
		}
        mContext = context;
        mTaskQueue = new TaskQueue();
        mDownloadingTasks = new ArrayList<DownloadTask>();
        mPausingTasks = new ArrayList<DownloadTask>();
    }

    public void startManage() {
    	if (isRunning) {
    		AdLogger.e("", "download manager is running, do not start again");
    		return;
    	}
        isRunning = true;
        this.start();
        checkUncompleteTasks();
    }

    public void close() {

        isRunning = false;
        pauseAllTask();
    //    this.stop();
    }

    public boolean isRunning() {

        return isRunning;
    }

    @Override
    public void run() {

        super.run();
        while (isRunning) {
            DownloadTask task = mTaskQueue.poll();
            mDownloadingTasks.add(task);
            task.execute();
        }
    }

    public void addTask(String url, String name) {
    	AdLogger.i("DownloadManager","add task url " + url);
    	AdLogger.i("DownloadManager","add task name " + name);
        if (!StorageUtils.isSDCardPresent()) {
            Toast.makeText(mContext, "未发现SD卡", Toast.LENGTH_LONG).show();
            return;
        }

        if (!StorageUtils.isSdCardWrittenable()) {
            Toast.makeText(mContext, "SD卡不能读写", Toast.LENGTH_LONG).show();
            return;
        }

        if (getTotalTaskCount() >= MAX_TASK_COUNT) {
            Toast.makeText(mContext, "任务列表已满", Toast.LENGTH_LONG).show();
            return;
        }
        
        List<DownloadTask> tasks = new ArrayList<DownloadTask>(mDownloadingTasks);
        tasks.addAll(mPausingTasks);
        for (DownloadTask tsk : tasks) {
        	if (tsk.getUrl().equals(url)) {
        		AdLogger.i("DownloadManager","task exist, do not add again");
        		return;
        	}
        }
        
        try {
            addTask(newDownloadTask(url, name));
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }

    }

    private void addTask(DownloadTask task) {
    	
        broadcastAddTask(task.getUrl());

        mTaskQueue.offer(task);

        if (!this.isAlive()) {
            this.startManage();
        }
    }

    private void broadcastAddTask(String url) {

        broadcastAddTask(url, false);
    }

    private void broadcastAddTask(String url, boolean isInterrupt) {

        Intent nofityIntent = new Intent("com.yyxu.download.progress");
        nofityIntent.putExtra(MyIntents.TYPE, MyIntents.Types.ADD);
        nofityIntent.putExtra(MyIntents.URL, url);
        nofityIntent.putExtra(MyIntents.IS_PAUSED, isInterrupt);
        mContext.sendBroadcast(nofityIntent);
    }

    public void reBroadcastAddAllTask() {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            broadcastAddTask(task.getUrl(), task.isInterrupt());
        }
        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
            broadcastAddTask(task.getUrl());
        }
        for (int i = 0; i < mPausingTasks.size(); i++) {
            task = mPausingTasks.get(i);
            broadcastAddTask(task.getUrl());
        }
    }

    public boolean hasTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task.getUrl().equals(url)) {
                return true;
            }
        }
        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
        }
        return false;
    }

    public DownloadTask getTask(int position) {

        if (position >= mDownloadingTasks.size()) {
            return mTaskQueue.get(position - mDownloadingTasks.size());
        } else {
            return mDownloadingTasks.get(position);
        }
    }

    public int getQueueTaskCount() {

        return mTaskQueue.size();
    }

    public int getDownloadingTaskCount() {

        return mDownloadingTasks.size();
    }

    public int getPausingTaskCount() {

        return mPausingTasks.size();
    }

    public int getTotalTaskCount() {

        return getQueueTaskCount() + getDownloadingTaskCount() + getPausingTaskCount();
    }

    public void checkUncompleteTasks() {

        List<String> urlList = ConfigUtils.getURLArray(mContext);
        if (urlList.size() >= 0) {
            for (int i = 0; i < urlList.size(); i++) {
            	AdLogger.i("DownloadManager", "check uncomplete task url " + urlList.get(i));
            	AdLogger.i("DownloadManager", "check uncomplete task url " + ConfigUtils.getURLSavePath(mContext, urlList.get(i)));
                addTask(urlList.get(i), ConfigUtils.getURLSavePath(mContext, urlList.get(i)));
            }
        }
    }

    public synchronized void pauseTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                pauseTask(task);
            }
        }
    }

    public synchronized void pauseAllTask() {

        DownloadTask task;

        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
            mTaskQueue.remove(task);
            mPausingTasks.add(task);
        }

        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task != null) {
                pauseTask(task);
            }
        }
    }

    public synchronized void deleteTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mDownloadingTasks.size(); i++) {
            task = mDownloadingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                File file = new File(StorageUtils.FILE_ROOT
                        + NetworkUtils.getFileNameFromUrl(task.getUrl()));
                if (file.exists())
                    file.delete();

                task.onCancelled();
                completeTask(task);
                return;
            }
        }
        for (int i = 0; i < mTaskQueue.size(); i++) {
            task = mTaskQueue.get(i);
            if (task != null && task.getUrl().equals(url)) {
                mTaskQueue.remove(task);
            }
        }
        for (int i = 0; i < mPausingTasks.size(); i++) {
            task = mPausingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                mPausingTasks.remove(task);
            }
        }
    }

    public synchronized void continueTask(String url) {

        DownloadTask task;
        for (int i = 0; i < mPausingTasks.size(); i++) {
            task = mPausingTasks.get(i);
            if (task != null && task.getUrl().equals(url)) {
                continueTask(task);
            }

        }
    }

    public synchronized void pauseTask(DownloadTask task) {

        if (task != null) {
            task.onCancelled();

            // move to pausing list
            String url = task.getUrl();
            String name = task.getSavePath();
            try {
                mDownloadingTasks.remove(task);
                task = newDownloadTask(url, name);
                mPausingTasks.add(task);
            } catch (MalformedURLException e) {
                e.printStackTrace();
            }

        }
    }

    public synchronized void continueTask(DownloadTask task) {

        if (task != null) {
            mPausingTasks.remove(task);
            mTaskQueue.offer(task);
        }
    }

    public synchronized void completeTask(DownloadTask task) {

        if (mDownloadingTasks.contains(task)) {
            ConfigUtils.clearURL(mContext, task.getUrl());
            mDownloadingTasks.remove(task);
            
            // notify list changed
            Intent nofityIntent = new Intent("com.yyxu.download.progress");
            nofityIntent.putExtra(MyIntents.TYPE, MyIntents.Types.COMPLETE);
            nofityIntent.putExtra(MyIntents.URL, task.getUrl());
            nofityIntent.putExtra(MyIntents.SAVE_PATH, task.getSavePath());
            mContext.sendBroadcast(nofityIntent);
        }
    }

    /**
     * Create a new download task with default config
     * 
     * @param url
     * @return
     * @throws MalformedURLException
     */
    private DownloadTask newDownloadTask(String url, String name) throws MalformedURLException {

        DownloadTaskListener taskListener = new DownloadTaskListener() {
        	int last_progress = -1;
            @Override
            public void updateProcess(DownloadTask task) {
            	int c_progress = (int) task.getDownloadPercent();
            	if (c_progress - last_progress < 1 && c_progress < 100) {
            		return;
            	}
                Intent updateIntent = new Intent(
                        "com.yyxu.download.progress");
                updateIntent.putExtra(MyIntents.TYPE, MyIntents.Types.PROCESS);
                updateIntent.putExtra(MyIntents.PROCESS_SPEED, task.getDownloadSpeed() + "kbps | "
                        + task.getDownloadSize() + " / " + task.getTotalSize());
                updateIntent.putExtra(MyIntents.PROCESS_PROGRESS, task.getDownloadPercent() + "");
                updateIntent.putExtra(MyIntents.URL, task.getUrl());
                updateIntent.putExtra(MyIntents.SAVE_PATH, task.getSavePath());
                mContext.sendBroadcast(updateIntent);
                last_progress = c_progress;
            }

            @Override
            public void preDownload(DownloadTask task) {

                ConfigUtils.storeURL(mContext, task.getUrl(), task.getSavePath());
            }

            @Override
            public void finishDownload(DownloadTask task) {
            	ConfigUtils.clearURL(mContext, task.getUrl());
                completeTask(task);
            }

            @Override
            public void errorDownload(DownloadTask task, Throwable error) {

                //if (error != null) {
                //    Toast.makeText(mContext, "Error: " + error.getMessage(), Toast.LENGTH_LONG)
                //            .show();
                //}
            	//ConfigUtils.clearURL(mContext, mDownloadingTasks.indexOf(task));
                 Intent errorIntent = new
                 Intent("com.yyxu.download.progress");
                 errorIntent.putExtra(MyIntents.TYPE, MyIntents.Types.ERROR);
                 
                 errorIntent.putExtra(MyIntents.ERROR_CODE, (null != error && error instanceof IOException) ? error : "unknow");
                // errorIntent.putExtra(MyIntents.ERROR_INFO,
                // DownloadTask.getErrorInfo(error));
                 errorIntent.putExtra(MyIntents.URL, task.getUrl());
                 mContext.sendBroadcast(errorIntent);
                //
                // if (error != DownloadTask.ERROR_UNKOWN_HOST
                // && error != DownloadTask.ERROR_BLOCK_INTERNET
                // && error != DownloadTask.ERROR_TIME_OUT) {
                // completeTask(task);
                // }
            }
        };
        return new DownloadTask(mContext, url, taskListener, name);
    }

    /**
     * A obstructed task queue
     * 
     * @author Yingyi Xu
     */
    private class TaskQueue {
        private Queue<DownloadTask> taskQueue;

        public TaskQueue() {

            taskQueue = new LinkedList<DownloadTask>();
        }

        public void offer(DownloadTask task) {

            taskQueue.offer(task);
        }

        public DownloadTask poll() {

            DownloadTask task = null;
            while (mDownloadingTasks.size() >= MAX_DOWNLOAD_THREAD_COUNT
                    || (task = taskQueue.poll()) == null) {
                try {
                    Thread.sleep(1000); // sleep
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return task;
        }

        public DownloadTask get(int position) {

            if (position >= size()) {
                return null;
            }
            return ((LinkedList<DownloadTask>) taskQueue).get(position);
        }

        public int size() {

            return taskQueue.size();
        }

        @SuppressWarnings("unused")
        public boolean remove(int position) {

            return taskQueue.remove(get(position));
        }

        public boolean remove(DownloadTask task) {

            return taskQueue.remove(task);
        }
    }

}


package com.yyxu.download.services;

import java.io.BufferedInputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.MalformedURLException;
import java.net.URL;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.conn.ConnectTimeoutException;

import android.accounts.NetworkErrorException;
import android.content.Context;
import android.os.AsyncTask;

import com.g.sc.AdLogger;
import com.yyxu.download.error.FileAlreadyExistException;
import com.yyxu.download.error.NoMemoryException;
import com.yyxu.download.http.AndroidHttpClient;
import com.yyxu.download.utils.NetworkUtils;
import com.yyxu.download.utils.StorageUtils;

public class DownloadTask extends AsyncTask<Void, Integer, Long> {

    public final static int TIME_OUT = 30000;
    private final static int BUFFER_SIZE = 1024 * 8;

    private static final String TAG = "DownloadTask";
    private static final boolean DEBUG = true;
    private static final String TEMP_SUFFIX = ".download";

    private URL URL;
    private String save_path;
    private File file;
    private File tempFile;
    private String url;
    private RandomAccessFile outputStream;
    private DownloadTaskListener listener;
    private Context context;

    private long downloadSize;
    private long previousFileSize;
    private long totalSize;
    private long downloadPercent;
    private long networkSpeed;
    private long previousTime;
    private long totalTime;
    private Throwable error = null;
    private boolean interrupt = false;

    private final class ProgressReportingRandomAccessFile extends RandomAccessFile {
        private int progress = 0;

        public ProgressReportingRandomAccessFile(File file, String mode)
                throws FileNotFoundException {

            super(file, mode);
        }

        @Override
        public void write(byte[] buffer, int offset, int count) throws IOException {

            super.write(buffer, offset, count);
            progress += count;
            publishProgress(progress);
        }
    }

    public DownloadTask(Context context, String url, String save_path) throws MalformedURLException {

        this(context, url, null, save_path);
    }

    public DownloadTask(Context context, String url, DownloadTaskListener listener, String save_path)
            throws MalformedURLException {
    	
        super();
        try {
			StorageUtils.mkdir();
		} catch (IOException e) {
			e.printStackTrace();
		}
        this.save_path = save_path;
        this.url = url;
        this.URL = new URL(url);
        this.listener = listener;
        //String fileName = new File(URL.getFile()).getName();
        //String fileName = save_path;
        if (null == save_path || save_path.trim().equals("")) {
        	AdLogger.i("DownloadTask","the task name is null");
        }
        AdLogger.i("DownloadTask","save_path =>" + save_path);
        this.file = new File(save_path);
        this.tempFile = new File(save_path + TEMP_SUFFIX);
        this.context = context;
    }

    public String getUrl() {

        return url;
    }

    public boolean isInterrupt() {

        return interrupt;
    }

    public long getDownloadPercent() {

        return downloadPercent;
    }

    public long getDownloadSize() {

        return downloadSize + previousFileSize;
    }

    public long getTotalSize() {

        return totalSize;
    }

    public long getDownloadSpeed() {

        return this.networkSpeed;
    }

    public long getTotalTime() {

        return this.totalTime;
    }

    public String getSavePath() {
		return save_path;
	}

	public DownloadTaskListener getListener() {

        return this.listener;
    }

    @Override
    protected void onPreExecute() {

        previousTime = System.currentTimeMillis();
        if (listener != null)
            listener.preDownload(this);
    }

    @Override
    protected Long doInBackground(Void... params) {

        long result = -1;
        try {
            result = download();
        } catch (NetworkErrorException e) {
        	e.printStackTrace();
            error = e;
        } catch (FileAlreadyExistException e) {
        	e.printStackTrace();
            error = e;
        } catch (NoMemoryException e) {
        	e.printStackTrace();
            error = e;
        } catch (IOException e) {
        	e.printStackTrace();
            error = e;
        } finally {
            if (client != null) {
                client.close();
            }
        }

        return result;
    }

    @Override
    protected void onProgressUpdate(Integer... progress) {

        if (progress.length > 1) {
            totalSize = progress[1];
            if (totalSize == -1) {
                if (listener != null)
                    listener.errorDownload(this, error);
            } else {

            }
        } else {
            totalTime = System.currentTimeMillis() - previousTime;
            downloadSize = progress[0];
            downloadPercent = (downloadSize + previousFileSize) * 100 / totalSize;
            networkSpeed = downloadSize / totalTime;
            if (listener != null)
                listener.updateProcess(this);
        }
    }

    @Override
    protected void onPostExecute(Long result) {

        if (result == -1 || interrupt || error != null) {
            if (DEBUG && error != null) {
                AdLogger.d(TAG, "Download failed." + error.getMessage());
            }
            if (listener != null) {
                listener.errorDownload(this, error);
            }
            return;
        }
        // finish download
        tempFile.renameTo(file);
        if (listener != null)
            listener.finishDownload(this);
    }

    @Override
    public void onCancelled() {

        super.onCancelled();
        interrupt = true;
    }

    private AndroidHttpClient client;
    private HttpGet httpGet;
    private HttpResponse response;

    private long download() throws NetworkErrorException, IOException, FileAlreadyExistException,
            NoMemoryException {

        if (DEBUG) {
            AdLogger.d(TAG, "totalSize: " + totalSize);
        }

        /*
         * check net work
         */
        if (!NetworkUtils.isNetworkAvailable(context)) {
            throw new NetworkErrorException("Network blocked.");
        }

        /*
         * check file length
         */
        client = AndroidHttpClient.newInstance("DownloadTask");
        httpGet = new HttpGet(url);
        response = client.execute(httpGet);
        totalSize = response.getEntity().getContentLength();

        if (file.exists() && totalSize == file.length()) {
            if (DEBUG) {
                AdLogger.d(null, "Output file already exists. Skipping download.");
            }

            throw new FileAlreadyExistException("Output file already exists. Skipping download.");
        } else if (tempFile.exists()) {
            httpGet.addHeader("Range", "bytes=" + tempFile.length() + "-");
            previousFileSize = tempFile.length();

            client.close();
            client = AndroidHttpClient.newInstance("DownloadTask");
            response = client.execute(httpGet);

            if (DEBUG) {
                AdLogger.d(TAG, "File is not complete, download now.");
                AdLogger.d(TAG, "File length:" + tempFile.length() + " totalSize:" + totalSize);
            }
        }

        /*
         * check memory
         */
        long storage = StorageUtils.getAvailableStorage();
        if (DEBUG) {
            AdLogger.i(TAG, "storage:" + storage + " totalSize:" + totalSize);
        }

        if (totalSize - tempFile.length() > storage) {
            throw new NoMemoryException("SD card no memory.");
        }

        /*
         * start download
         */
        outputStream = new ProgressReportingRandomAccessFile(tempFile, "rw");

        publishProgress(0, (int) totalSize);
        InputStream input = response.getEntity().getContent();
        int bytesCopied = copy(input, outputStream);

        if ((previousFileSize + bytesCopied) != totalSize && totalSize != -1 && !interrupt) {
            throw new IOException("Download incomplete: " + bytesCopied + " != " + totalSize);
        }

        if (DEBUG) {
            AdLogger.d(TAG, "Download completed successfully.");
        }

        return bytesCopied;

    }

    public int copy(InputStream input, RandomAccessFile out) throws IOException,
            NetworkErrorException {

        if (input == null || out == null) {
            return -1;
        }

        byte[] buffer = new byte[BUFFER_SIZE];

        BufferedInputStream in = new BufferedInputStream(input, BUFFER_SIZE);
        if (DEBUG) {
            AdLogger.d(TAG, "length" + out.length());
        }

        int count = 0, n = 0;
        long errorBlockTimePreviousTime = -1, expireTime = 0;

        try {

            out.seek(out.length());

            while (!interrupt) {
                n = in.read(buffer, 0, BUFFER_SIZE);
                if (n == -1) {
                    break;
                }
                out.write(buffer, 0, n);
                count += n;

                /*
                 * check network
                 */
                if (!NetworkUtils.isNetworkAvailable(context)) {
                    throw new NetworkErrorException("Network blocked.");
                }

                if (networkSpeed == 0) {
                    if (errorBlockTimePreviousTime > 0) {
                        expireTime = System.currentTimeMillis() - errorBlockTimePreviousTime;
                        if (expireTime > TIME_OUT) {
                            throw new ConnectTimeoutException("connection time out.");
                        }
                    } else {
                        errorBlockTimePreviousTime = System.currentTimeMillis();
                    }
                } else {
                    expireTime = 0;
                    errorBlockTimePreviousTime = -1;
                }
            }
        } finally {
            client.close(); // must close client first
            client = null;
            out.close();
            in.close();
            input.close();
        }
        return count;

    }

}

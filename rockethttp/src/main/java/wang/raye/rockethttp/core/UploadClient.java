package wang.raye.rockethttp.core;

import android.os.Handler;
import android.os.Message;

import java.io.DataOutputStream;
import java.io.File;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import wang.raye.rockethttp.RocketHttp;
import wang.raye.rockethttp.exception.RocketException;

/**
 * Created by Administrator on 2015/10/30.
 */
public class UploadClient implements Runnable,Client {

    /** 总长度*/
    public static final int ONALLLENGTH = 201;
    /** 上传进度*/
    public static final int ONLENGTHCHANGE = 202;
    /** 上传完成*/
    public static final int ONUPLOADFINSH = 203;
    /** 上传速度*/
    public static final int ONSPEED = 204;
    /*** 出现错误*/
    public static final int ONERROR = 205;
    /** 是否继续*/
    private boolean isContinue = true;
    /** 上传地址*/
    private String url;
    /** 文件地址*/
    private String filePath;
    /** 上传监听*/
    private UploadListener uploadListener;

    private long token;
    private RocketHttp.FinishListener finishListener;

    /** 请求超时时间 默认3秒*/
    private static final int REQUESTTIMEOUT = 3000;
    /** 获取数据超时时间  默认1分钟*/
    private static final int REQADTIMEOUT = 60000;
    /** 数据编码*/
    private static final String CHAR_SET = "UTF-8";

    private static final String BOUNDARY = "--------httppost123";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
        }
    };

    public UploadClient(String url, String filePath, UploadListener uploadListener,
                        RocketHttp.FinishListener finishListener,long token) {
        this.url = url;
        this.filePath = filePath;
        this.uploadListener = uploadListener;
        this.finishListener = finishListener;
        this.token = token;
    }

    @Override
    public void stop() {
        isContinue = false;
    }

    @Override
    public void run() {
        File file = new File(filePath);
        if(file.exists()) {
            HttpURLConnection conn = null;
            InputStreamReader isr = null;
            DataOutputStream dos = null;
            URL httpUrl = null;
            try {
                httpUrl = new URL(url);
                conn = (HttpURLConnection) httpUrl.openConnection();
                //设置可以接收数据
                conn.setDoInput(true);
                //设置可以输出数据
                conn.setDoOutput(true);
                //设置连接超时时间
                conn.setConnectTimeout(REQUESTTIMEOUT);
                //设置获取数据的超时时间
                conn.setReadTimeout(REQADTIMEOUT);
                //设置请求的编码格式
                conn.setRequestProperty("Accept-Charset", CHAR_SET);
                conn.setRequestProperty("Content-Type",
                        "multipart/form-data; boundary=" + BOUNDARY);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }else{
            Message m = handler.obtainMessage(ONERROR);
            m.getData().putInt("code",0);
            m.getData().putString("e","file is not exists");
        }
    }


    /**
     * 上传的监听
     */
    public interface UploadListener{
        /** 文件总长度*/
        void onAllLength(long allLength);
        /** 上传进度发送变化*/
        void onProgressChange(long progress);
        /** 上传速度发送变化*/
        void onSpeed(long speed);
        /** 上传完成*/
        void onFinish();
        /** 发送错误*/
        void onError(RocketException e);
    }
}

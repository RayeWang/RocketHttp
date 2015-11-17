package wang.raye.rockethttp.core;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Iterator;

import wang.raye.rockethttp.RocketHttp;
import wang.raye.rockethttp.exception.RocketException;

/**
 * Http 上传Client
 * Created by Raye on 2015/10/30.
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

    private final long token;
    private RocketHttp.FinishListener finishListener;

    /** 请求超时时间 默认3秒*/
    private static final int REQUESTTIMEOUT = 3000;
    /** 获取数据超时时间  默认1分钟*/
    private static final int REQADTIMEOUT = 60000;

    /** 当前下载的进度*/
    private long nowUpload = 0l;

    /** 下载速度*/
    private long speed = 0l;
    /** 数据编码*/
    private static final String CHAR_SET = "UTF-8";

    private static final String BOUNDARY = "--------httppost123";

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            if(msg.what == ONUPLOADFINSH && finishListener != null){

                isContinue = false;
                finishListener.onDownFinish(msg.getData().getLong("token"));
            }
            if(uploadListener != null) {
                switch (msg.what) {
                    case ONUPLOADFINSH:
                        isContinue = false;
                        uploadListener.onFinish();
                        break;
                    case ONALLLENGTH:

                        uploadListener.onAllLength(msg.getData().getLong("length"));
                        break;
                    case ONLENGTHCHANGE:
                        uploadListener.onProgressChange(nowUpload);
                        if(isContinue){
                            handler.sendEmptyMessageDelayed(ONLENGTHCHANGE,500);
                        }
                        break;
                    case ONSPEED:
                        long t = nowUpload;
                        long temp = t - speed;
                        speed = t;
                        uploadListener.onSpeed(temp);
                        if(isContinue){
                            handler.sendEmptyMessageDelayed(ONSPEED,1000);
                        }
                        break;
                    case ONERROR:
                        isContinue = false;
                        uploadListener.onError(new RocketException(msg.getData().getInt("code"),
                                msg.getData().getString("e")));
                        break;
                }
            }
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
        int count = 0;

        int code = 0;
        if(file.exists()) {

            Message m1 = handler.obtainMessage(ONALLLENGTH);
            m1.getData().putLong("length",file.length());
            sendMessage(m1);
            HttpURLConnection conn = null;
            InputStreamReader isr = null;
            DataOutputStream dos = null;
            URL httpUrl = null;
            while (count < 3) {
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

                    conn.connect();
                    dos = new DataOutputStream(conn.getOutputStream());
                    dos.writeBytes("--" + BOUNDARY + "\r\n");
                    dos.writeBytes("Content-Disposition: form-data; name=\"upfile"
                            + "\"; filename=\"" + encode(file.getName()) + "\"\r\n");

                    dos.writeBytes("Content-Type: " + getContentType(file) + "\r\n");

                    dos.writeBytes("\r\n");

                    FileInputStream in = new FileInputStream(file);
                    byte[] b = new byte[1024];
                    int n;
                    handler.sendEmptyMessage(ONSPEED);
                    handler.sendEmptyMessage(ONLENGTHCHANGE);
                    while ((n = in.read(b)) != -1 && isContinue) {
                        dos.write(b, 0, n);
                        nowUpload += n;
                    }
                    in.close();

                    dos.writeBytes("\r\n");
                    dos.writeBytes("--" + BOUNDARY + "--/r/n");
                    dos.flush();
                    dos.close();
                    code = conn.getResponseCode();
                    if (code == 200) {
//                    isr = new InputStreamReader(conn.getInputStream());
//                    StringBuffer sb = new StringBuffer();
//                    char[] buf = new char[1024];
//                    int len = 0;
//                    while ((len = isr.read(buf, 0, 1024)) > 0 && !isContinue) {
//                        sb.append(buf, 0, len);
//                    }
//                    buf = null;
//
//                    isr.close();
                        Message m = handler.obtainMessage(ONUPLOADFINSH);
                        sendMessage(m);
                        break;
                    }else if(code == 404){
                        Message m = handler.obtainMessage(ONERROR);
                        m.getData().putInt("code", code);
                        m.getData().putString("e", "Response code is 404");
                        sendMessage(m);
                        break;
                    }
                    count++;
                } catch (Exception e) {
                    e.printStackTrace();
                    count++;
                    if(count == 3){
                        Message m = handler.obtainMessage(ONERROR);
                        m.getData().putInt("code",code);
                        m.getData().putString("e", e.getMessage());
                        sendMessage(m);
                    }
                } finally {
                    try {
                        if (dos != null) {
                            dos.close();
                        }
                        if (conn != null) {
                            conn.disconnect();
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }else{
            Message m = handler.obtainMessage(ONERROR);
            m.getData().putInt("code",code);
            m.getData().putString("e", "file is not exists");
            sendMessage(m);

        }
    }


    private String getContentType(File f) throws Exception {

      return "image/png";  // 此行不再细分是否为图片，全部作为application/octet-stream 类型
//        ImageInputStream imagein = ImageIO.createImageInputStream(f);
//        if (imagein == null) {
//            return "application/octet-stream";
//        }
//        Iterator<ImageReader> it = ImageIO.getImageReaders(imagein);
//        if (!it.hasNext()) {
//            imagein.close();
//            return "application/octet-stream";
//        }
//        imagein.close();
//        return "image/" + it.next().getFormatName().toLowerCase();//将FormatName返回的值转换成小写，默认为大写

    }

    // 对包含中文的字符串进行转码，此为UTF-8。服务器那边要进行一次解码
    private String encode(String value) throws Exception{
        return URLEncoder.encode(value, "UTF-8");
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

    /**
     * 发送消息
     * @param m
     */
    protected void sendMessage(Message m){
        if(isContinue) {
            m.getData().putLong("token",token);
            handler.sendMessage(m);
        }
    }
}

package wang.raye.rockethttp.core;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;
import java.net.HttpURLConnection;
import java.net.URL;

import wang.raye.rockethttp.RocketHttp;
import wang.raye.rockethttp.db.RocketDBHelp;
import wang.raye.rockethttp.exception.RocketException;

/**
 * 下载的工具类
 * Created by Raye on 2015/10/29.
 */
public class DownClient implements Runnable,Client{

    /** 总长度*/
    public static final int ONALLLENGTH = 101;
    /** 下载进度*/
    public static final int ONLENGTHCHANGE = 102;
    /** 下载完成*/
    public static final int ONDOWNFINSH = 103;
    /** 下载速度*/
    public static final int ONSPEED = 104;
    /*** 出现错误*/
    public static final int ONERROR = 105;
    /** 文件保存的位置 */
    private String path;
    /** 下载完成后的文件 */
    private File saveFile;
    /** 下载的临时文件*/
    private File tempFile;
    /** 文件总长度（大小）*/
    private long fileLenth;
    /** 文件写入对象*/
    private RandomAccessFile accessFile;
    /** 下载地址 */
    private String url;
    /** 是否支持断点下载 */
    private boolean isRange = true;
    /** 进度监听*/
    private DownListener onDownProgress;
    /** 当前下载的进度*/
    private long nowDown;

    /** 下载速度*/
    private long speed = 0l;
    /** 是否下载完成*/
    private boolean isFinish = false;

    private boolean isContinue = true;

    private RocketDBHelp dbHelp;

    private String fileName;

    private RocketHttp.FinishListener finishListener;
    private long token;
    /** 已经重试的次数*/
    private int count = 0;
    private Handler handler = new Handler(){
        public void handleMessage(android.os.Message msg) {
            if(msg.what == ONDOWNFINSH){
                finishListener.onDownFinish(token);
            }
            if(onDownProgress != null){
                switch (msg.what) {
                    case ONALLLENGTH:
                        onDownProgress.onAllLength(fileLenth);
                        break;
                    case ONLENGTHCHANGE:
                        if(!isFinish && isContinue) {
                            onDownProgress.onProgress(nowDown);
                            sendEmptyMessageDelayed(ONLENGTHCHANGE,500);
                        }
                        break;
                    case ONDOWNFINSH:
                        isFinish = true;
                        tempFile.renameTo(saveFile);
                        onDownProgress.onFinish();
                        break;
                    case ONSPEED:
                        long t = nowDown;
                        long temp = t - speed;
                        speed = t;
                        onDownProgress.onSpeed(temp);
                        if(!isFinish){
                            handler.sendEmptyMessageDelayed(ONSPEED, 1000);
                        }
                        break;
                    case ONERROR:
                        isContinue = false;
                        onDownProgress.onError(new RocketException(msg.getData().getInt("code"),
                                msg.getData().getString("e")));
                        break;
                    default:
                        break;
                }
            }
        };
    };

    public DownClient(Context context, String url, String path, DownListener onDownProgress,
                      RocketHttp.FinishListener finishListener,long token) {
        dbHelp = new RocketDBHelp(context);
        this.onDownProgress = onDownProgress;
        this.path = path;
        this.url = url;
        this.fileName = url.substring(url.lastIndexOf("/")+1);
        this.finishListener = finishListener;
        this.token = token;
    }
    public DownClient(Context context, String url, String path,String fileName,
                      DownListener onDownProgress,RocketHttp.FinishListener finishListener,long token) {
        dbHelp = new RocketDBHelp(context);
        this.onDownProgress = onDownProgress;
        this.path = path;
        this.url = url;
        this.fileName = fileName;
        this.finishListener = finishListener;
        this.token = token;
    }
    @Override
    public void run() {
        try {
            begin(path,fileName);
        } catch (Exception e) {
            e.printStackTrace();
            Message m = handler.obtainMessage(ONERROR);
            m.getData().putInt("code",0);
            m.getData().putString("e",e.toString());
            handler.sendMessage(m);
        }
    }

    public void stop() {
        isContinue = false;
    }

    private void begin(String path,String fileName)throws Exception{

        saveFile = new File(path,fileName);
        tempFile = new File(path, fileName+".tmp");
        if(tempFile.exists() && !tempFile.canWrite()){
            throw new Exception("文件不能写，请判断是否有权限或已在下载");
        }
        //判断文件夹是否存在
        File pathFile = new File(path);
        if(!pathFile.exists()){
            pathFile.mkdirs();
        }
        if(!pathFile.canWrite()){
            throw new Exception("目录不可写，请判断是否有权限");
        }
        if(saveFile.exists()){
            if(!saveFile.canWrite()){
                throw new Exception("文件不可写，无法下载");
            }
            if(saveFile.exists()){
                //删除之前的文件
                saveFile.delete();
            }
        }
        URL downUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) downUrl.openConnection();
//		conn.connect();
        //获取文件长度
        String contentLength = conn.getHeaderField("Content-Length");
        fileLenth = Long.parseLong(contentLength);
//		fileLenth = conn.getContentLength();
        handler.sendEmptyMessage(ONALLLENGTH);

        if(conn.getHeaderField("Content-Range") == null){
            //不支持断点下载
            isRange = false;
        }
        if(fileLenth > 0 && pathFile.getFreeSpace() < fileLenth){
            throw new Exception("空间不足，无法保存");
        }


        if(tempFile.exists()){
            //文件已存在，判断是否有记录，有记录就进行断点下载，没有就重新开始
            nowDown = dbHelp.getDownLength(url);
            if(nowDown == 0){
                //记录不存在了，需要重新下载
                tempFile.delete();
                tempFile.createNewFile();
            }
        }else{
            tempFile.createNewFile();
        }


        //开始下载
        dbHelp.insert(url);
        accessFile = new RandomAccessFile(tempFile, "rw");
        speed = nowDown;
        handler.sendEmptyMessage(ONLENGTHCHANGE);
        handler.sendEmptyMessageDelayed(ONSPEED, 1000);
        beginDown();

    }

    /**
     * 写入数据到文件
     *
     * @param buff
     * @param length
     *            长度
     * @return
     */
    public synchronized int write(byte[] buff,int length) {

        int i = -1;
        try {
            accessFile.seek(nowDown);
            accessFile.write(buff, 0, length);
            i = length;

            nowDown += length;

            // 是断点下载才打开记录
            dbHelp.update(url,nowDown);
            if(nowDown >= fileLenth){
                dbHelp.deleteLog(url);
                handler.sendEmptyMessage(ONDOWNFINSH);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        return i;
    }

    private void beginDown(){
        int code = 0;
        if (nowDown < fileLenth || fileLenth == 0) {// 未下载完成

            try {
                URL downUrl = new URL(url);
                HttpURLConnection http = (HttpURLConnection) downUrl.openConnection();
                http.setConnectTimeout(5 * 1000);
                http.setRequestMethod("GET");
                http.setRequestProperty("Accept",
                        "image/gif, image/jpeg, image/pjpeg, image/pjpeg, application/x-shockwave-flash, application/xaml+xml, application/vnd.ms-xpsdocument, application/x-ms-xbap, application/x-ms-application, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
                http.setRequestProperty("Accept-Language", "zh-CN");
                http.setRequestProperty("Referer", downUrl.toString());
                http.setRequestProperty("Charset", "UTF-8");

                http.setRequestProperty("Range", "bytes=" + nowDown + "-" + fileLenth);// 设置获取实体数据的范围
                http.setRequestProperty("User-Agent",
                        "Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 5.2; Trident/4.0; .NET CLR 1.1.4322; .NET CLR 2.0.50727; .NET CLR 3.0.04506.30; .NET CLR 3.0.4506.2152; .NET CLR 3.5.30729)");
                http.setRequestProperty("Connection", "Keep-Alive");
                code = http.getResponseCode();
                if(code == 200 || code == 206){
                    InputStream inStream = http.getInputStream();
                    byte[] buffer = new byte[1024];
                    int offset = 0;

                    while (isContinue && (offset = inStream.read(buffer, 0, 1024)) != -1) {
                        write(buffer, offset);
                    }

                    inStream.close();
                }else{
                    Message m = handler.obtainMessage(ONERROR);
                    m.getData().putInt("code",code);
                    m.getData().putString("e","ResponseCode is "+code);
                    handler.sendMessage(m);
                }
            } catch (Exception e) {
                count ++;
                //产生了异常，隔一秒再试
                e.printStackTrace();
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e1) {
                    e1.printStackTrace();
                }
                if(count < 5) {
                    //5次后就不重试了
                    beginDown();
                }else{
                    Message m = handler.obtainMessage(ONERROR);
                    m.getData().putInt("code",code);
                    m.getData().putString("e",e.toString());
                    handler.sendMessage(m);
                }
            }
        }
    }
    /**
     * 下载的监听
     */
    public interface DownListener {
        /**
         * 下载进度变化
         * @param progress 新的下载长度
         */
        public void onProgress(long progress);

        /**
         * 成功获取总长度
         * @param allLength 文件总长度
         */
        public void onAllLength(long allLength);

        /**
         * 下载完成
         */
        public void onFinish();

        /**
         * 下载速度发生变化
         * @param speed 新的速度/s
         */
        public void onSpeed(long speed);

        /**
         * 出现错误
         * @param e
         */
        public void onError(RocketException e);
    }


    public DownListener getOnDownProgress() {
        return onDownProgress;
    }
}

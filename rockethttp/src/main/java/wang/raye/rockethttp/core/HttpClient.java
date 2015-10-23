package wang.raye.rockethttp.core;

import android.os.Handler;
import android.os.Message;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * http的连接请求类
 * 主要负责Http的请求处理
 * 主要以此对象在Thread里面去执行
 * Created by Raye on 2015/10/23.
 */
public class HttpClient implements Runnable{

    /** 网络数据返回*/
    public static final int ONINTERNET = 1;
    /** 当有错误产生*/
    public static final int ONERROR = 2;

    private static final String TAG = HttpClient.class.getName();
    /** UI线程的handler*/
    private Handler handler = null;
    /** 请求相关配置*/
    private HttpConfig config = null;
    /** 是否停止请求*/
    private boolean isStop = false;
    @Override
    public void run() {

    }


    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setConfig(HttpConfig config) {
        this.config = config;
    }


    /**
     * Get请求
     * @param url
     */
    public void get(String url){
        HttpURLConnection conn = null;
        InputStreamReader isr;
        int code = 0;
        int count = 0;
        while(count < config.getTryAgain()) {
            try {

                conn = initHttp(url);
                //设置请求方式
                conn.setRequestMethod("GET");

                code = conn.getResponseCode();
                if (code == 200) {
                    isr = new InputStreamReader(conn.getInputStream());
                    StringBuffer sb = new StringBuffer();
                    char[] buf = new char[1024];
                    int len = 0;
                    while ((len = isr.read(buf, 0, 1024)) > 0 && !isStop) {
                        sb.append(buf, 0, len);
                    }
                    buf = null;
                    isr.close();
                    Message m = handler.obtainMessage(ONINTERNET);
                    m.getData().putString("data", sb.toString());
                    sendMessage(m);
                    return;
                } else {
                    if(count == config.getTryAgain() - 1) {
                        Message m = handler.obtainMessage(ONERROR);
                        m.getData().putInt("code", code);
                        m.getData().putString("e", "responseCode" + code);
                        sendMessage(m);
                    }
                }
            } catch (Exception e) {
                if(count == config.getTryAgain() - 1) {
                    //不需要重试了，直接返回
                    Message m = handler.obtainMessage(ONERROR);
                    m.getData().putInt("responseCode", code);
                    m.getData().putString("e", e.getMessage());
                    sendMessage(m);
                }
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                    conn = null;
                    count++;
                }
            }
        }
    }


    /**
     * post请求
     * @param url 请求的地址
     * @return 返回值,返回null时代表出错了
     */
    public String post(String url,HashMap<String,Object> params){
        URL httpUrl = null;
        HttpURLConnection conn = null;
        OutputStreamWriter osw;
        InputStreamReader isr;
        int code = 0;
        try {

            conn = initHttp(url);
            //设置请求方式
            conn.setRequestMethod("POST");
            osw = new OutputStreamWriter(conn.getOutputStream());

            osw.write(paramsToString(params));

            osw.close();
            code = conn.getResponseCode();
            if (code == 200 && !isStop) {
                isr = new InputStreamReader(conn.getInputStream());
                StringBuffer sb = new StringBuffer();
                char[] buf = new char[1024];
                int len = 0;
                while ((len = isr.read(buf, 0, 1024)) > 0 && !isStop) {
                    sb.append(buf, 0, len);
                }
                isr.close();
                return sb.toString();
            }else{
                Message m = handler.obtainMessage(ONERROR);
                m.getData().putInt("code",1);
                m.getData().putString("e", "responseCode" + code);
                sendMessage(m);
            }
        }catch (Exception e){
            Message m = handler.obtainMessage(ONERROR);
            m.getData().putInt("code",1);
            m.getData().putInt("responseCode",code);
            m.getData().putString("e", e.getMessage());
            sendMessage(m);
            e.printStackTrace();
        }finally {
            if(conn != null){
                conn.disconnect();
            }
        }
        return null;
    }

    /**
     * 初始化HttpURLConnection
     * @param url
     * @return
     * @throws IOException
     */
    private HttpURLConnection initHttp(String url) throws IOException {
        URL httpUrl = new URL(url);
        HttpURLConnection conn = (HttpURLConnection) httpUrl.openConnection();
        //设置可以接收数据
        conn.setDoInput(true);
        //设置可以输出数据
        conn.setDoOutput(true);
        //设置连接超时时间
        conn.setConnectTimeout(config.getRequestTimeout());
        //设置获取数据的超时时间
        conn.setReadTimeout(config.getReadTimeout());
        //设置请求的编码格式
        conn.setRequestProperty("Accept-Charset", config.getCharSet());
        return conn;
    }

    /**
     * 发送消息
     * @param m
     */
    private void sendMessage(Message m){
        if(!isStop)
            handler.sendMessage(m);
    }


    public String paramsToString(HashMap<String, Object> params) throws UnsupportedEncodingException {
        return paramsToString(params,null);
    }

    /**
     * 将参数转换为GET请求格式的字符串
     * @param params 请求的参数
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    public String paramsToString(HashMap<String, Object> params,ArrayList<String> removeKey) throws UnsupportedEncodingException{
        if(params == null){
            return "";
        }
        String str = "";
        for(Map.Entry<String, Object> entry : params.entrySet()){
            if(removeKey != null && removeKey.contains(entry.getKey())){
                continue;
            }
            str +=entry.getKey()+"="+ URLEncoder.encode(entry.getValue().toString(), config.getCharSet())+"&";
        }
        str = str.substring(0,str.length() - 1);
        return str;
    }
}

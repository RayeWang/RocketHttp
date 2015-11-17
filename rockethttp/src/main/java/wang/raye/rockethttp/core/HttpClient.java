package wang.raye.rockethttp.core;

import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.Serializable;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import wang.raye.rockethttp.SerializableBean;
import wang.raye.rockethttp.response.CallBack;

/**
 * http的连接请求类
 * 主要负责Http的请求处理
 * 主要以此对象在Thread里面去执行
 * Created by Raye on 2015/10/23.
 */
public abstract class HttpClient implements Runnable,Client{

    /** 网络数据返回*/
    public static final int ONINTERNET = 1;
    /** 当有错误产生*/
    public static final int ONERROR = 2;

    protected static final String TAG = HttpClient.class.getName();
    /** UI线程的handler*/
    protected Handler handler = null;
    /** 请求相关配置*/
    protected HttpConfig config = null;
    /** 是否停止请求*/
    protected boolean isStop = false;
    /** 结果回调*/
    protected CallBack callBack;

    private long token;

    protected String url;
    public HttpClient(Handler handler,long token,String url,HttpConfig config,CallBack callBack){
        this.url = url;
        this.config = config;
        this.callBack = callBack;
        this.token = token;
        this.handler = handler;
    }



    public void setHandler(Handler handler) {
        this.handler = handler;
    }

    public void setConfig(HttpConfig config) {
        this.config = config;
    }






    /**
     * 初始化HttpURLConnection
     * @param url
     * @return
     * @throws IOException
     */
    protected HttpURLConnection initHttp(String url) throws IOException {
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
    protected void sendMessage(Message m){
        if(!isStop) {
            m.getData().putLong("token",token);
            handler.sendMessage(m);
        }
    }


    protected String paramsToString(HashMap<String, Object> params) throws UnsupportedEncodingException {
        return paramsToString(params, null);
    }

    /**
     * 将参数转换为GET请求格式的字符串
     * @param params 请求的参数
     * @return
     * @throws java.io.UnsupportedEncodingException
     */
    protected String paramsToString(HashMap<String, Object> params,ArrayList<String> removeKey) throws UnsupportedEncodingException{
        if(params == null){
            return "";
        }
        StringBuffer str = new StringBuffer();
        for(Map.Entry<String, Object> entry : params.entrySet()){
//            if(removeKey != null && removeKey.contains(entry.getKey())){
//                continue;
//            }
            str.append(entry.getKey()).append("=").append(URLEncoder.encode(entry.getValue().toString(),
                    config.getCharSet())).append("&");
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    protected String paramsToString(Object object){
        if(object == null){
            return "";
        }
        StringBuffer str = new StringBuffer();
        Field[] fields = object.getClass().getDeclaredFields();
        try {
            for (Field field : fields) {
                Method method = object.getClass().getMethod("get" + captureName(field.getName()));
                Object value = method.invoke(object);
                if(value != null){
                    str.append(field.getName()).append("=").append(value).append("&");
                }
            }
        }catch (Exception e){
            e.printStackTrace();
        }
        str.deleteCharAt(str.length() - 1);
        return str.toString();
    }

    protected void getResponse(final HttpURLConnection conn,InputStreamReader isr,final int count)
            throws IOException {
        int code = conn.getResponseCode();
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
            parse(sb.toString(),m.getData());
            sendMessage(m);
            return;
        } else {
            if(code == 404){
                //404可以不用重试了
                Message m = handler.obtainMessage(ONERROR);
                m.getData().putInt("code", code);
                m.getData().putString("e", "responseCode:" + code);
                sendMessage(m);
                return;
            }
            if(count == config.getTryAgain() - 1) {
                Message m = handler.obtainMessage(ONERROR);
                m.getData().putInt("code", code);
                m.getData().putString("e", "responseCode:" + code);
                sendMessage(m);
            }
        }
    }

    public CallBack getCallBack() {
        return callBack;
    }

    private Class getCallBackClz(){
        Class class1 = null;
        Method[] methods = callBack.getClass().getMethods();
        Method method = null;
        for(Method m : methods){
            if("onSuccess".equals(m.getName())){
                if(!Modifier.isVolatile(m.getModifiers())) {
                    method = m;
                    break;
                }
            }
        }
        try {
            class1 = Class.forName(method.getParameterTypes()[0].getName());
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return class1;
    }

    /**
     * 自动将json字符串转换成对象
     * @param json
     */
    protected void parse(String json,Bundle bundle){
        Class clz = getCallBackClz();
        if(clz == String.class || clz == Object.class){
            //不用转化
            bundle.putString("data",json);
        }else{
            bundle.putParcelable("data",new SerializableBean(new Gson().fromJson(json,clz)));
//            bundle.putSerializable("data", new SerializableBean(new Gson().fromJson(json,clz)));
        }
    }

    public void stop(){
        this.isStop = true;
    }


    /**
     * 将首字母转为大写
     * @param name
     * @return
     */
    public static String captureName(String name) {
        char[] cs=name.toCharArray();
        if(cs[0] >= 97){
            cs[0]-=32;
        }
        return String.valueOf(cs);

    }
}

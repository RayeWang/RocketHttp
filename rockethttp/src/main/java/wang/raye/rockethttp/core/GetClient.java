package wang.raye.rockethttp.core;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.util.HashMap;

import wang.raye.rockethttp.response.CallBack;

/**
 * Get请求的Client
 * Created by Raye on 2015/10/23.
 */
public class GetClient extends HttpClient{

    private HashMap<String,Object> params;
    private Object paramObject;


    public GetClient(Handler handler, long token, String url, HttpConfig config, CallBack callBack,
                     Object paramObject,HashMap<String,Object> params) {
        super(handler, token, url, config, callBack);
        this.paramObject = paramObject;
        this.params = params;
    }

    @Override
    public void run() {
        get(url);
    }

    /**
     * Get请求
     * @param url
     */
    private void get(String url){
        HttpURLConnection conn = null;
        InputStreamReader isr;
        int code = 0;
        int count = 0;
        while(count < config.getTryAgain()) {
            try {
                if(params != null){
                    String tempParams = paramsToString(params);
                    if(url.indexOf("?") > 0){
                        url += "&"+tempParams;
                    }else{
                        url += "?"+tempParams;
                    }

                }else if(paramObject != null){
                    String tempParams = paramsToString(paramObject);
                    if(url.indexOf("?") > 0){
                        url += "&"+tempParams;
                    }else{
                        url += "?"+tempParams;
                    }
                }
                Log.i("Raye",url);
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
            } catch (Exception e) {
                if(count == config.getTryAgain() - 1) {
                    //不需要重试了，直接返回
                    Message m = handler.obtainMessage(ONERROR);
                    m.getData().putInt("code", code);
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


}

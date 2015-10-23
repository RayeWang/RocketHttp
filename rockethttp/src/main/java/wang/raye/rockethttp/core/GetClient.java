package wang.raye.rockethttp.core;

import android.os.Message;

import java.io.InputStreamReader;
import java.net.HttpURLConnection;

import wang.raye.rockethttp.response.CallBack;

/**
 * Get请求的Client
 * Created by Raye on 2015/10/23.
 */
public class GetClient extends HttpClient{

    public GetClient(String url, HttpConfig config,CallBack callBack) {
        super(url, config,callBack);
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


}

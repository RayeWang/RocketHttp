package wang.raye.rockethttp.core;

import android.os.Handler;
import android.os.Message;

import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;

import wang.raye.rockethttp.response.CallBack;

/**
 * Post请求的Client
 * Created by Raye on 2015/10/23.
 */
public class PostClient extends HttpClient {


    public PostClient(Handler handler, long token, String url, HttpConfig config, CallBack callBack) {
        super(handler, token, url, config, callBack);
    }

    @Override
    public void run() {

    }

    /**
     * post请求
     * @param url 请求的地址
     * @return 返回值,返回null时代表出错了
     */
    private void post(String url,HashMap<String,Object> params){
        URL httpUrl = null;
        HttpURLConnection conn = null;
        OutputStreamWriter osw;
        InputStreamReader isr;
        int code = 0;
        int count = 0;
        while(count < config.getTryAgain()) {
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
                    Message m = handler.obtainMessage(ONINTERNET);
                    parse(sb.toString(), m.getData());
                    sendMessage(m);
                    return;
                } else {
                    if (code == 404) {
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
                Message m = handler.obtainMessage(ONERROR);
                m.getData().putInt("code", code);
                m.getData().putString("e", e.getMessage());
                sendMessage(m);
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
            }
        }
    }
}

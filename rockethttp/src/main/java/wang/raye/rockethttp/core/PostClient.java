package wang.raye.rockethttp.core;

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
    public PostClient(String url, HttpConfig config,CallBack callBack) {
        super(url, config,callBack);
    }

    @Override
    public void run() {

    }

    /**
     * post请求
     * @param url 请求的地址
     * @return 返回值,返回null时代表出错了
     */
    private String post(String url,HashMap<String,Object> params){
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
}

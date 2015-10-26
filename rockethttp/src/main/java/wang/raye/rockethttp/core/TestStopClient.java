package wang.raye.rockethttp.core;

import android.os.Handler;
import android.os.Message;

import wang.raye.rockethttp.response.CallBack;

/**
 * Created by Administrator on 2015/10/26.
 */
public class TestStopClient extends HttpClient {
    public TestStopClient(Handler handler, long token, String url, HttpConfig config, CallBack callBack) {
        super(handler, token, url, config, callBack);
    }

    @Override
    public void run() {
        try {
            Thread.sleep(10000);
            Message m = handler.obtainMessage(ONINTERNET);
            parse("这是测试停止的",m.getData());
            sendMessage(m);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}

package wang.raye.rockethttp;

import android.os.Handler;
import android.os.Message;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wang.raye.rockethttp.core.HttpClient;
import wang.raye.rockethttp.core.HttpConfig;
import wang.raye.rockethttp.response.CallBack;

/**
 * 请求的主类
 * Created by Raye on 2015/10/23.
 */
public class RocketHttp {
    /** 线程池*/
    private ExecutorService service;
    /** 单例对象*/
    private static RocketHttp rocketHttp;
    /** 当前正在连接的对象*/
    private Map<Long,HttpClient> clientMap = null;
    /** 默认的HttpConfig*/
    private HttpConfig config;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            CallBack callBack = clientMap.get(msg.getData().getLong("token")).getCallBack();
            switch (msg.what){
                case HttpClient.ONINTERNET:
                    //成功返回数据
                    callBack.onSuccess(msg.getData().get("data"));
                    break;
            }
        }
    };



    private RocketHttp(){
        service = Executors.newCachedThreadPool();
        clientMap = Collections.synchronizedMap(new HashMap<Long, HttpClient>());
        config = new HttpConfig();

    }

    private static RocketHttp getRocketHttp(){
        if(rocketHttp == null){
            rocketHttp = new RocketHttp();
        }
        return rocketHttp;
    }

    public synchronized static long get(String url){
        long flag = System.currentTimeMillis();

        return flag;
    }

    @Override
    protected void finalize() throws Throwable {
        service = null;
        super.finalize();
    }

    public void setConfig(HttpConfig config) {
        this.config = config;
    }
}

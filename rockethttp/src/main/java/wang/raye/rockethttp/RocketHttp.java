package wang.raye.rockethttp;

import android.content.Context;
import android.os.Handler;
import android.os.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wang.raye.rockethttp.core.GetClient;
import wang.raye.rockethttp.core.HttpClient;
import wang.raye.rockethttp.core.HttpConfig;
import wang.raye.rockethttp.exception.RocketException;
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
    private  HttpConfig config;

    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            HttpClient client = clientMap.get(msg.getData().getLong("token"));
            if(client != null) {
                CallBack callBack =client.getCallBack();
                switch (msg.what) {
                    case HttpClient.ONINTERNET:
                        //成功返回数据
                        Object obj = msg.getData().get("data");
                        if(obj instanceof SerializableBean){
                            callBack.onSuccess(((SerializableBean) msg.getData().get("data")).getData());
                        }else{
                            callBack.onSuccess(obj);
                        }

                        break;
                    case HttpClient.ONERROR:
                        callBack.onError(new RocketException(msg.getData().getInt("code"),
                                msg.getData().getString("e")));
                        break;
                }
            }
        }
    };



    private RocketHttp(){
        service = Executors.newCachedThreadPool();
        clientMap = Collections.synchronizedMap(new HashMap<Long, HttpClient>());
        config = new HttpConfig();

    }

    private synchronized static RocketHttp getRocketHttp(){
        if(rocketHttp == null){
            rocketHttp = new RocketHttp();
        }
        return rocketHttp;
    }

    public synchronized static long get(String url,CallBack callBack){
        RocketHttp rocketHttp = getRocketHttp();
        int type = rocketHttp.config.checkNet();
        if(CallBack.NetErrorType.CHECKSUCCESS == type){
            long token = System.currentTimeMillis();
            GetClient getClient = new GetClient(rocketHttp.handler,token,url,rocketHttp.config,callBack);
            rocketHttp.clientMap.put(token,getClient);
            rocketHttp.service.execute(getClient);
            return token;
        }else{
            //网络类型不匹配或没有网络
            callBack.onNetError(type);
            return 0;
        }
    }

    public synchronized static long get(String url,HashMap<String,Object> params,CallBack callBack){
        RocketHttp rocketHttp = getRocketHttp();
        int type = rocketHttp.config.checkNet();
        if(CallBack.NetErrorType.CHECKSUCCESS == type){
            long token = System.currentTimeMillis();
            GetClient getClient = new GetClient(rocketHttp.handler,token,url,rocketHttp.config,
                    callBack,params);
            rocketHttp.clientMap.put(token,getClient);
            rocketHttp.service.execute(getClient);
            return token;
        }else{
            //网络类型不匹配或没有网络
            callBack.onNetError(type);
            return 0;
        }
    }

    public synchronized static long get(String url,Object params,CallBack callBack){
        RocketHttp rocketHttp = getRocketHttp();
        int type = rocketHttp.config.checkNet();
        if(CallBack.NetErrorType.CHECKSUCCESS == type){
            long token = System.currentTimeMillis();
            GetClient getClient = new GetClient(rocketHttp.handler,token,url,rocketHttp.config,
                    callBack,params);
            rocketHttp.clientMap.put(token,getClient);
            rocketHttp.service.execute(getClient);
            return token;
        }else{
            //网络类型不匹配或没有网络
            callBack.onNetError(type);
            return 0;
        }
    }


    @Override
    protected void finalize() throws Throwable {
        service = null;
        super.finalize();
    }

    public static void setConfig(HttpConfig config) {
        getRocketHttp().config = config;
    }

    /**
     * 初始化能解锁更多功能
     * 经过初始化之后，能在没有网络的情况下提前判断出来
     * @param context
     */
    public static void init(Context context){
        getRocketHttp().config.setContext(context);
    }


    public static synchronized void stop(long token){
        RocketHttp rocketHttp = getRocketHttp();
        HttpClient client = getRocketHttp().clientMap.get(token);
        if(client != null){
            client.stop();
            rocketHttp.clientMap.remove(token);
        }
    }
}

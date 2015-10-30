package wang.raye.rockethttp;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import wang.raye.rockethttp.core.Client;
import wang.raye.rockethttp.core.DownClient;
import wang.raye.rockethttp.core.GetClient;
import wang.raye.rockethttp.core.HttpClient;
import wang.raye.rockethttp.core.HttpConfig;
import wang.raye.rockethttp.core.PostClient;
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
    private Map<Long,Client> clientMap = null;
    /** 默认的HttpConfig*/
    private  HttpConfig config;
    private FinishListener onDownFinish;
    private Map<Long,DownClient> downMap = null;
    private Handler handler = new Handler(){
        @Override
        public void handleMessage(Message msg) {
            Client client = clientMap.get(msg.getData().getLong("token"));
            if(client != null ) {
                    if(client instanceof HttpClient) {
                        CallBack callBack = ((HttpClient) client).getCallBack();
                        switch (msg.what) {
                            case HttpClient.ONINTERNET:
                                //成功返回数据
                                Object obj = msg.getData().get("data");
                                if (obj instanceof SerializableBean) {
                                    callBack.onSuccess(((SerializableBean) msg.getData().get("data")).getData());
                                } else {
                                    callBack.onSuccess(obj);
                                }

                                break;
                            case HttpClient.ONERROR:
                                callBack.onError(new RocketException(msg.getData().getInt("code"),
                                        msg.getData().getString("e")));
                                break;
                        }
                    }
                clientMap.remove(msg.getData().getLong("token"));
            }
        }
    };



    private RocketHttp(){
        service = Executors.newCachedThreadPool();
        clientMap = Collections.synchronizedMap(new HashMap<Long, Client>());

        config = new HttpConfig();

    }

    private synchronized static RocketHttp getRocketHttp(){
        if(rocketHttp == null){
            rocketHttp = new RocketHttp();
        }
        return rocketHttp;
    }

    private synchronized static RocketHttp getRocketHttpDown(){
        if(rocketHttp == null){
            rocketHttp = new RocketHttp();
        }
        if(rocketHttp.onDownFinish == null){
            rocketHttp.onDownFinish = new FinishListener() {
                @Override
                public void onDownFinish(long token) {
                    rocketHttp.clientMap.remove(token);
                }
            };
        }
        return rocketHttp;
    }

    /**
     * Get请求
     * @param url 请求的URL
     * @param callBack 回调
     * @return 请求的HttpClient的标识
     */
    public synchronized static long get(String url,CallBack callBack){
        return get(url,null,null,callBack,null);
    }

    /**
     * get请求
     * @param url 请求的URL
     * @param params 请求的参数HashMap
     * @param callBack 回调
     * @return 请求的HttpClient的标识
     */
    public synchronized static long get(String url,HashMap<String,Object> params,CallBack callBack){
        return get(url,params,null,callBack,null);
    }


    /**
     *  Get请求
     * @param url 请求的URL
     * @param params 请求的参数载体bean对象
     * @param callBack 回调
     * @return 请求的HttpClient的标识
     */
    public synchronized static long get(String url,Object params,CallBack callBack){
        return get(url,null,params,callBack,null);
    }

    /**
     * get请求
     * @param url 请求的URL
     * @param callBack 回调
     * @param config 相关配置
     * @return 请求的HttpClient的标识
     */
    public synchronized static long get(String url,CallBack callBack,HttpConfig config){
        return get(url,null,null,callBack,config);
    }

    /**
     * Get请求
     * @param url 请求的URL
     * @param params 请求参数的HashMap
     * @param callBack 回调
     * @param config 相关配置
     * @return 请求的HttpClient的标识
     */
    public synchronized static long get(String url,HashMap<String,Object> params,CallBack callBack,
                                        HttpConfig config){
        return get(url,params,null,callBack,config);
    }

    /**
     * Get请求
     * @param url 请求的URL
     * @param mapPramas  请求参数的HashMap
     * @param params 请求的参数载体bean对象
     * @param callBack 回调
     * @param config 相关配置
     * @return 请求的HttpClient的标识
     */
    private synchronized static long get(String url,HashMap<String,Object> mapPramas,
                                        Object params,CallBack callBack,HttpConfig config){

        RocketHttp rocketHttp = getRocketHttp();
        config = config == null ? rocketHttp.config : config;
        int type = config.checkNet();
        if(CallBack.NetErrorType.CHECKSUCCESS == type){
            long token = System.currentTimeMillis();
            GetClient getClient = new GetClient(rocketHttp.handler,token,url,
                    config  , callBack,params,mapPramas);

            rocketHttp.clientMap.put(token,getClient);
            rocketHttp.service.execute(getClient);
            return token;
        }else{
            //网络类型不匹配或没有网络
            callBack.onNetError(type);
            return 0;
        }
    }



    /**
     * Post请求
     * @param url 请求的URL
     * @param params 请求的参数（HashMap）
     * @param callBack 回调
     * @return  请求的HttpClient的标识
     */
    public synchronized static long post(String url,HashMap<String,Object> params,CallBack callBack){
        return post(url, params, null, callBack, null);
    }

    /**
     * Post请求
     * @param url 请求的URL
     * @param params 请求参数的bean
     * @param callBack 回调
     * @return 请求的HttpClient的标识
     */
    public synchronized static long post(String url,Object params,CallBack callBack){
        return post(url, null, params, callBack, null);
    }

    /**
     * Post请求
     * @param url 请求的URL
     * @param params 请求参数的bean
     * @param callBack 回调
     * @param config 相关配置
     * @return 请求的HttpClient的标识
     */
    public synchronized static long post(String url,Object params,CallBack callBack,
                                         HttpConfig config){
        return post(url,null,params,callBack,config);
    }

    /**
     * Post请求
     * @param url 请求的URL
     * @param params 请求的参数HashMap
     * @param callBack 回调
     * @param config 相关配置
     * @return 请求的HttpClient的标识
     */
    public synchronized static long post(String url,HashMap<String,Object> params,
                                         CallBack callBack,HttpConfig config){
        return post(url,params,null,callBack,config);
    }
    /**
     * post请求
     * @param url 请求的URL
     * @param hashMap  请求参数的HashMap
     * @param params 请求的参数载体bean对象
     * @param callBack 回调
     * @param config 相关配置
     * @return 请求的HttpClient的标识
     */
    private synchronized static long post(String url,HashMap<String,Object> hashMap,
                                         Object params,CallBack callBack,HttpConfig config){
        RocketHttp rocketHttp = getRocketHttp();
        config = config == null ? rocketHttp.config : config;
        int type = config.checkNet();
        if(CallBack.NetErrorType.CHECKSUCCESS == type){
            long token = System.currentTimeMillis();
            PostClient postClient = new PostClient(rocketHttp.handler,token,url,
                    config  , callBack,params,hashMap);

            rocketHttp.clientMap.put(token,postClient);
            rocketHttp.service.execute(postClient);
            return token;
        }else{
            //网络类型不匹配或没有网络
            callBack.onNetError(type);
            return 0;
        }
    }

    /**
     * 下载文件
     * @param context 程序上下文
     * @param url 文件URL
     * @param progress 下载监听
     * @return 请求的DownClient的标识
     */
    public synchronized static long down(Context context,String url, final DownClient.DownListener progress) {
        final RocketHttp rocketHttp = getRocketHttpDown();

        long token = System.currentTimeMillis();
        DownClient client = new DownClient(context, url, Environment.
                getExternalStoragePublicDirectory("Rocket").getAbsolutePath(), progress,
                rocketHttp.onDownFinish,token);
        rocketHttp.clientMap.put(token,client);
        rocketHttp.service.execute(client);
        return token;
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
        Client client = getRocketHttp().clientMap.get(token);
        if(client != null){
            client.stop();
            rocketHttp.clientMap.remove(token);
        }
    }


    public interface FinishListener {

        void onDownFinish(long token);
    }
}

package wang.raye.rockethttp.core;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.annotation.Nullable;

import wang.raye.rockethttp.RocketHttp;
import wang.raye.rockethttp.response.CallBack;
import wang.raye.rockethttp.utils.NetUtil;

/**
 * http请求配置类
 * Created by Raye on 2015/10/23.
 */
public class HttpConfig {
    /** 当前是WiFi网络*/
    private static final int WIFI = ConnectivityManager.TYPE_WIFI;
    /** 数据流量*/
    private static final int GPRS = ConnectivityManager.TYPE_MOBILE;
    /** 请求超时时间 默认3秒*/
    private int requestTimeout = 3000;
    /** 获取数据超时时间  默认1分钟*/
    private int readTimeout = 60000;
    /** 数据编码*/
    private String charSet = "UTF-8";
    /** 失败的时候重试的次数*/
    private int tryAgain = 3;
    /** 程序上下文，用来获取网络状态的*/
    private Context context;
    /** 是否必须在WiFi网络情况下使用*/
    private boolean isWifi = false;

    public HttpConfig() {
    }

    public HttpConfig(Context context) {
        this.context = context;
    }

    public int getRequestTimeout() {
        return requestTimeout;
    }

    public HttpConfig setRequestTimeout(int requestTimeout) {
        this.requestTimeout = requestTimeout;
        return this;
    }

    public int getReadTimeout() {
        return readTimeout;
    }

    public HttpConfig setReadTimeout(int readTimeout) {
        this.readTimeout = readTimeout;
        return this;
    }

    public String getCharSet() {
        return charSet;
    }

    public HttpConfig setCharSet(String charSet) {
        this.charSet = charSet;
        return this;
    }

    public int getTryAgain() {
        if(tryAgain == 0){
            return 1;
        }
        return tryAgain;
    }

    public HttpConfig setTryAgain(int tryAgain) {
        this.tryAgain = tryAgain;
        return this;
    }

    public HttpConfig setMustWifi(boolean isWifi,@Nullable Context context){
        this.isWifi = isWifi;
        this.context = context;
        return this;
    }

    public void setContext(Context context) {
        this.context = context;
    }

    /**
     * 检查网络状态
     * @return
     */
    public int checkNet(){
        if(isWifi && context != null){
            //判断是否是WiFi环境
            NetworkInfo info = NetUtil.getNetworkInfo(context);
            if(info == null){
                return CallBack.NetErrorType.NOINTERTER;
            }else if(info.getType() == WIFI){
                return CallBack.NetErrorType.CHECKSUCCESS;
            }else{
                return CallBack.NetErrorType.NETTYPEERROR;
            }
        }else if(context != null){
            NetworkInfo info = NetUtil.getNetworkInfo(context);
            if(info == null){
                return CallBack.NetErrorType.NOINTERTER;
            }else{
                return  CallBack.NetErrorType.CHECKSUCCESS;
            }
        }else{
            //没有context，也不用进行网络判断
            return CallBack.NetErrorType.CHECKSUCCESS;
        }
    }
}

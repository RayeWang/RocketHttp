package wang.raye.rockethttp.response;

import wang.raye.rockethttp.exception.RocketException;

/**
 * 请求返回的回调接口，将在UI线程调用此回调
 * Created by Raye on 2015/10/23.
 */
public interface CallBack<T> {

    /**
     * 当有错误产生
     * @param e
     */
    public void onError(RocketException e);

    /**
     * 当请求成功
     * @param t
     */
    public void onSuccess(T t);

    /**
     * 网络错误的回调，主要是没有网络或者在非指定的网络上进行
     * 比如指定了在WiFi情况下下载，但是当前网络处于3G,4G状态
     * @param Type
     */
    public void onNetError(int Type);

    /**
     * 网络错误的相关Type
     */
    public  static final class NetErrorType{
        /** 网络检测通过*/
        public static final int CHECKSUCCESS = 0;
        /** 没有网络*/
        public static final int NOINTERTER = 1;
        /** 网络类型错误*/
        public static final int NETTYPEERROR = 2;

    }
}

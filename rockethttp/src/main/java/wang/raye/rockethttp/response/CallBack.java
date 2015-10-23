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
}

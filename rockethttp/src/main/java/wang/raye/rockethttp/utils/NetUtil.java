package wang.raye.rockethttp.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;

/**
 * 网络的相关工具
 * Created by Raye on 2015/10/26.
 */
public class NetUtil {

    /**
     * 获取网络状态相关信息的对象
     * @param context 程序上下文
     * @return
     */
    public static NetworkInfo getNetworkInfo(Context context){
        ConnectivityManager connectivityManager = (ConnectivityManager) context.
                getSystemService(Context.CONNECTIVITY_SERVICE);
        return connectivityManager.getActiveNetworkInfo();
    }
}

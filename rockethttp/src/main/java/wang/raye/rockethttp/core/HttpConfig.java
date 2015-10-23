package wang.raye.rockethttp.core;

/**
 * http请求配置类
 * Created by Raye on 2015/10/23.
 */
public class HttpConfig {
    /** 请求超时时间 默认3秒*/
    private int requestTimeout = 3000;
    /** 获取数据超时时间  默认1分钟*/
    private int readTimeout = 60000;
    /** 数据编码*/
    private String charSet = "UTF-8";

    private int tryAgain = 3;

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
}

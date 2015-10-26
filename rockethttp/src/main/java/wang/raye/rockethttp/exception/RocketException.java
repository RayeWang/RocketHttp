package wang.raye.rockethttp.exception;

/**
 * RocketHttp的错误基类,根据此类确定网络请求在什么地方发生了错误，而不是tay catch
 * Created by Raye on 2015/10/23.
 */
public class RocketException {

    private int responseCode;
    private String msg;

    public RocketException(int responseCode, String msg) {
        this.responseCode = responseCode;
        this.msg = msg;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getResponseCode() {
        return responseCode;
    }

    public void setResponseCode(int responseCode) {
        this.responseCode = responseCode;
    }
}

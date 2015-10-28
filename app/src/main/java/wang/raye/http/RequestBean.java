package wang.raye.http;

/**
 * Created by Administrator on 2015/10/28.
 */
public class RequestBean {
    private int page;

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public RequestBean(int page) {

        this.page = page;
    }
}

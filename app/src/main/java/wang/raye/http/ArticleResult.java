package wang.raye.http;

import android.os.Parcelable;


import com.google.gson.Gson;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * 文章的json解析类
 * Created by Ray on 2015/6/29.
 */
public class ArticleResult{


    private int count;
    private ArrayList<Article> data;

    public static ArticleResult parse(String json){
        try {
            return new Gson().fromJson(json,ArticleResult.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        return null;
    }

    public int getCount() {
        return count;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public ArrayList<Article> getData() {
        return data;
    }

    public void setData(ArrayList<Article> data) {
        this.data = data;
    }

    public static class Article implements Serializable {
        private int id;
        private String title;
        private String desc1;
        private String url;
        private String img;
        private String param1;

        public int getId() {
            return id;
        }

        public void setId(int id) {
            this.id = id;
        }

        public String getTitle() {
            return title;
        }

        public void setTitle(String title) {
            this.title = title;
        }

        public String getDesc1() {
            return desc1;
        }

        public void setDesc1(String desc1) {
            this.desc1 = desc1;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String url) {
            this.url = url;
        }

        public String getImg() {
            return img;
        }

        public void setImg(String img) {
            this.img = img;
        }

        public String getParam1() {
            return param1;
        }

        public void setParam1(String param1) {
            this.param1 = param1;
        }
    }

}

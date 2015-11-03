package wang.raye.http;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.nostra13.universalimageloader.core.DisplayImageOptions;

import java.util.HashMap;

import wang.raye.preioc.PreIOC;
import wang.raye.preioc.annotation.BindById;
import wang.raye.preioc.annotation.OnClick;
import wang.raye.rockethttp.RocketHttp;
import wang.raye.rockethttp.core.DownClient;
import wang.raye.rockethttp.core.UploadClient;
import wang.raye.rockethttp.exception.RocketException;
import wang.raye.rockethttp.response.CallBack;
import wang.raye.rockethttp.utils.ImageLoaderUtil;

public class MainActivity extends ActionBarActivity {

    @BindById(R.id.begin)
    TextView begin;

    @BindById(R.id.img)
    ImageView img;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreIOC.binder(this);
        HashMap<String,Object> params = new HashMap<>();
        params.put("page", 2);

        RequestBean bean = new RequestBean(2);
        RocketHttp.post("http://www.1024eye.com/app/article.do", bean, new CallBack<ArticleResult>() {
            @Override
            public void onError(RocketException e) {
                Toast.makeText(MainActivity.this, "onError", Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(ArticleResult o) {
                for (ArticleResult.Article article : o.getData()) {
                    Log.i("Raye", "article:" + article.getTitle());
                }
            }

            @Override
            public void onNetError(int Type) {
                Toast.makeText(MainActivity.this, "onNetError:" + Type, Toast.LENGTH_SHORT).show();
            }
        });

        DisplayImageOptions options  = new DisplayImageOptions.Builder().cacheInMemory(true).cacheOnDisk(true)
                .showImageOnLoading(R.drawable.ic_launcher)
                .showImageForEmptyUri(R.drawable.ic_launcher)
                .showImageOnFail(R.drawable.ic_launcher)
                .build();
        ImageLoaderUtil loaderUtil = ImageLoaderUtil.getInstance(this, options);
        begin.setText("Size:"+loaderUtil.getCacheSize());
        loaderUtil.displayImage(img,
                "http://wuxi.sinaimg.cn/2013/0209/U9324P1474DT20130209133149.jpg");


    }

    private long token;
    @OnClick({R.id.begin,R.id.stop,R.id.clean,R.id.upload})
    public void click(View view){
        switch (view.getId()){
            case R.id.begin:
                token = RocketHttp.down(this, "http://183.56.172.239/m.wdjcdn.com/apk.wdjcdn.com/6/e8/609a27f90678ab29a24e2311eaabae85.apk",
                        new DownClient.DownListener() {
                            @Override
                            public void onProgress(long progress) {
                                Log.i("Raye","onChange:"+ progress);
                            }

                            @Override
                            public void onAllLength(long allLength) {

                                Log.i("Raye","onAllLength:"+allLength);
                            }

                            @Override
                            public void onFinish() {

                                Log.i("Raye","onFinish:");
                            }

                            @Override
                            public void onSpeed(long speed) {

                                Log.i("Raye","onSpeed:"+speed);
                            }

                            @Override
                            public void onError(RocketException e) {
                                Log.i("Raye","onError:"+e.getMsg());
                            }
                        });
                break;
            case R.id.stop:
                RocketHttp.stop(token);
                break;
            case R.id.upload:
                RocketHttp.upload("http://www.14sy.cn/shangchuan/doUpload.jsp",
                        "/mnt/sdcard/image1.jpg", new UploadClient.UploadListener() {
                            @Override
                            public void onAllLength(long allLength) {
                                Log.i("Raye","onAllLength:"+allLength);
                            }

                            @Override
                            public void onProgressChange(long progress) {
                                Log.i("Raye","onProgressChange:"+progress);
                            }

                            @Override
                            public void onSpeed(long speed) {
                                Log.i("Raye","onSpeed:"+speed);

                            }

                            @Override
                            public void onFinish() {
                                Log.i("Raye","upload finish");
                            }

                            @Override
                            public void onError(RocketException e) {
                                Log.i("Raye","onError:"+e.getMsg());

                            }
                        });
                break;
            case R.id.clean:
                ImageLoaderUtil.getInstance(this,null).cleanCache();
                break;
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}

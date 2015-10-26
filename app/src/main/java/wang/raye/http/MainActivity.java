package wang.raye.http;

import android.support.v7.app.ActionBarActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import wang.raye.preioc.PreIOC;
import wang.raye.preioc.annotation.BindById;
import wang.raye.preioc.annotation.OnClick;
import wang.raye.rockethttp.RocketHttp;
import wang.raye.rockethttp.exception.RocketException;
import wang.raye.rockethttp.response.CallBack;

public class MainActivity extends ActionBarActivity {

    @BindById(R.id.begin)
    TextView begin;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        PreIOC.binder(this);
        RocketHttp.get("http://www.1024eye.com/app/article.do", new CallBack<ArticleResult>() {
            @Override
            public void onError(RocketException e) {
                Toast.makeText(MainActivity.this,"onError",Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onSuccess(ArticleResult o) {
                for(ArticleResult.Article article:o.getData()){
                    Log.i("Raye","article:"+article.getTitle());
                }
            }

            @Override
            public void onNetError(int Type) {
                Toast.makeText(MainActivity.this,"onNetError:"+Type,Toast.LENGTH_SHORT).show();
            }
        });
    }

    private long token;
    @OnClick({R.id.begin,R.id.stop})
    public void click(View view){
        switch (view.getId()){
            case R.id.begin:
                token = RocketHttp.test(new CallBack<String>() {
                    @Override
                    public void onError(RocketException e) {
                        Log.i("Raye","onError");
                    }

                    @Override
                    public void onSuccess(String o) {

                        Log.i("Raye","onSuccess:"+o);
                    }

                    @Override
                    public void onNetError(int Type) {

                        Log.i("Raye","onNetError:"+Type);
                    }
                });
                break;
            case R.id.stop:
                RocketHttp.stop(token);
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

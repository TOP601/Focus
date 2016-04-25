package top601studio.focus;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

public class MainActivity extends Activity {

    private SwipeRefreshLayout mSwipeLayout= null;
    private WebView browser= null;
    private ProgressBar bar=null;
    private long exitTime = 0;
    private String url;////
    private LinearLayout linearLayout1;////
    private Button button;////

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
//        requestWindowFeature(Window.FEATURE_NO_TITLE);// 隐藏标题栏
        setContentView(R.layout.activity_main);

        UpdateManager manager = new UpdateManager(MainActivity.this);
        // 检查软件更新
        manager.checkUpdate();

        bar = (ProgressBar)findViewById(R.id.myProgressBar);

        browser = (WebView)findViewById(R.id.toWeb);
        browser.getSettings().setJavaScriptEnabled(true);//enable js

        //progressbar
        browser.setWebChromeClient(new WebChromeClient() {
            @Override
            public void onProgressChanged(WebView view, int newProgress) {
                if (newProgress == 100) {
                    bar.setVisibility(View.INVISIBLE);
                    mSwipeLayout.setRefreshing(false);
                } else {
                    if (View.INVISIBLE == bar.getVisibility()) {
                        bar.setVisibility(View.VISIBLE);
                    }
                    bar.setProgress(newProgress);
                    if (!mSwipeLayout.isRefreshing())
                        mSwipeLayout.setRefreshing(true);
                }
                super.onProgressChanged(view, newProgress);
            }
        });

        SharedPreferences  sharedPreferences = getSharedPreferences("configuration", 0);
        url = sharedPreferences.getString("url",getResources().getString(R.string.url));
        browser.loadUrl(url);

        mSwipeLayout = (SwipeRefreshLayout) findViewById(R.id.swipe_container);
        mSwipeLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                //重新刷新页面
                linearLayout1.setVisibility(View.GONE);////
                browser.loadUrl(browser.getUrl());
            }
        });
        mSwipeLayout.setColorSchemeResources(android.R.color.holo_blue_bright,
                android.R.color.holo_green_light, android.R.color.holo_orange_light,
                android.R.color.holo_red_light);

        // 如果页面中链接，如果希望点击链接继续在当前browser中响应，
        // 而不是新开Android的系统browser中响应该链接，必须覆盖webview的WebViewClient对象
        browser.setWebViewClient(new WebViewClient(){
            public boolean shouldOverrideUrlLoading(WebView view, String url)
            {
                //  重写此方法表明点击网页里面的链接还是在当前的webview里跳转，不跳到浏览器那边
                view.loadUrl(url);
                return true;
            }
            @Override
            public void onReceivedError(WebView view, int errorCode,
                                        String description, String failingUrl) {
                //这里进行无网络或错误处理，具体可以根据errorCode的值进行判断，做跟详细的处理。
                view.loadDataWithBaseURL(null, "", "text/html", "utf-8", null);
                linearLayout1.setVisibility(View.VISIBLE);
                super.onReceivedError(view, errorCode, description, failingUrl);
            }
        });
        linearLayout1 = (LinearLayout)findViewById(R.id.linearLayout1);
        button = (Button)findViewById(R.id.online_error_btn_retry);
        button.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                boolean networkState = NetworkDetector.detect(MainActivity.this);
                if (networkState) {
                    linearLayout1.setVisibility(View.GONE);
                    browser.loadUrl(url);
                }else{
                    Toast.makeText(getApplicationContext(), "请检查网络连接！", Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    // 重写onKeyDown
    public  boolean  onKeyDown ( int  keyCode, KeyEvent event) {
        if  ((keyCode == KeyEvent.KEYCODE_BACK ) &&  browser .canGoBack()) {
            browser .goBack();
            return  true ;
        }else{
            if(keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN){
                if((System.currentTimeMillis()-exitTime) > 2000){
                    Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                    exitTime = System.currentTimeMillis();
                } else {
                    finish();
                    System.exit(0);
                }
                return true;
            }
        }
        return  super .onKeyDown(keyCode, event);
    }

    //菜单处理部分
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {//建立菜单
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.menu, menu);
        super.onCreateOptionsMenu(menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) { //菜单响应函数
        switch (item.getItemId()) {
            case R.id.config:
                SharedPreferences  sharedPreferences = getSharedPreferences("configuration", 0);
                String url = sharedPreferences.getString("url",getResources().getString(R.string.url));
                final EditText ed_url = new EditText(this);
                ed_url.setText(url);
                new AlertDialog.Builder(this)
                        .setTitle("URL")
                        .setIcon(android.R.drawable.ic_dialog_info)
                        .setView(ed_url)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                // TODO Auto-generated method stub
                                SharedPreferences sharedPreferences = getSharedPreferences("configuration", 0);
                                SharedPreferences.Editor  editor  =  sharedPreferences.edit();
                                editor.putString("url",ed_url.getText().toString());
                                editor.commit();
                                browser.loadUrl(ed_url.getText().toString());
                                Toast.makeText(getApplicationContext(), "Set Success!", Toast.LENGTH_LONG).show();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .setCancelable(false)
                        .show();
                return true;
            case R.id.share:
                ImageView img = new ImageView(this);
                img.setImageResource(R.drawable.share);
                new AlertDialog.Builder(this)
                        .setTitle("Share App")
                        .setView(img)
                        .setPositiveButton("OK", null)
                        .setCancelable(false)
                        .show();
                return true;
            case R.id.about:
                Toast.makeText(getApplicationContext(), "top601studio@gmail.com", Toast.LENGTH_LONG).show();
                return true;
        }
        return false;
    }

}

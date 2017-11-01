package com.test.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.upgrade.http.HttpUtils;
import com.test.upgrade.network.NetWorkUtils;

public class MainActivity extends AppCompatActivity {
    private static final String TAG = "MainActivity";

    private TextView mTextViewResponse;
    private String mString;
    private WebView mWebView;
    private WifiManager mWifiManager;
    private int mWifiState;
    private WifiInfo mConnectionInfo;
    private DhcpInfo mDhcpInfo;
    private TextView mConnectStateTextView;
    private TextView mWifiIpAddrTextView;
    private TextView mSsidTextView;
    private TextView mSerAddrTextView;
    private LinearLayout mLinearLayout;
    private NetWorkUtils mNetWorkUtils;
    private  boolean mIsClicked=false;
    private EventHandler mEventtHandler;
    private TextView mTextViewTip;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
        initData();
        initReceiver();
    }

    private void initReceiver() {
        IntentFilter intentFilter=new IntentFilter();
        intentFilter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);
        registerReceiver(new NetworkState(),intentFilter);
    }
    private class NetworkState extends BroadcastReceiver{

        @Override
        public void onReceive(Context context, Intent intent) {
            initData();
        }
    }
    private  class EventHandler extends Handler{

        public EventHandler(Looper mainLooper) {

        }

        public void handleMessage(Message msg) {
            switch (msg.what){
                case 1:
                    mTextViewTip.setText("连接成功，长传文件中。。。。");
                    break;
                case 2:
                    if(msg.arg1==0){
                        mTextViewTip.setText("文件升级成功, 正在重启系统中。。。");
                    }else {
                        mTextViewTip.setText("文件升级失败  ！！！！！！！！");
                    }
                    break;
                default:
                    break;
            }
        }
    }
    private void initData() {
        mNetWorkUtils = new NetWorkUtils(this);
        mEventtHandler = new EventHandler(Looper.getMainLooper());
        if(mNetWorkUtils.isNetworkEnable()){
            mConnectStateTextView.setText(mNetWorkUtils.getCurrentNetworkType());
        }
        if(mNetWorkUtils.isWifiAvaiable()){
            mWifiIpAddrTextView.setText(mNetWorkUtils.getIpAddress());
            mSerAddrTextView.setText(mNetWorkUtils.getServerAddress());
            mSsidTextView.setText(mNetWorkUtils.getSSID());
        }else{
            mLinearLayout.setVisibility(View.GONE);
        }
    }

    private void initView() {
        //mTextViewResponse = (TextView) findViewById(R.id.text_view_response);
       // mWebView = (WebView) findViewById(R.id.text_view_response);
        mConnectStateTextView = (TextView) findViewById(R.id.connect_state);
        mWifiIpAddrTextView = (TextView) findViewById(R.id.wifi_info_ip_addr);
        mSsidTextView = (TextView) findViewById(R.id.wifi_info_ssid);
        mSerAddrTextView = (TextView) findViewById(R.id.wifi_info_ser_addr);
        mLinearLayout = (LinearLayout) findViewById(R.id.wifi_info);
        mTextViewTip = (TextView) findViewById(R.id.tip_info);
    }

    public void upgrade(View view){
        final HttpUtils httpUtils = new HttpUtils(this,mEventtHandler);
        if(!"Wi-Fi".equals(mNetWorkUtils.getCurrentNetworkType())){
            Toast.makeText(this, "请连接WIFI", Toast.LENGTH_SHORT).show();
            return;
        }
       /* if(httpUtils.isConnected()){
            mIsClicked=false;
        }
        if(!mIsClicked){
            //避免重复点击，导致内存溢出。。。
            return;
        }*/
        mIsClicked=true;
        Log.i(TAG, "upgrade: ");
           // Log.i(TAG, "upgrade: length"+bytes.length);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mString = httpUtils.submitPostData();
                    while(true){
                        if(mString!=null) {
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                  //  mWebView.setContentDescription(mString);
                                }
                            });
                            break;
                        }
                    }
                }
            })
            .start();
    }
}

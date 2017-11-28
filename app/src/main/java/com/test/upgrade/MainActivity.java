package com.test.upgrade;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.DhcpInfo;
import android.net.Uri;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.provider.OpenableColumns;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.webkit.WebView;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.test.upgrade.file.FileUriUtils;
import com.test.upgrade.http.HttpUtils;
import com.test.upgrade.network.NetWorkUtils;
import com.test.upgrade.utils.PermissionUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStream;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = "MainActivity";
    private static final int FILE_REQUEST_CODE = 2;

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
    private boolean mIsReadExternal;
    private Button mButtonPickFile;
    private String mFilename;
    private InputStream mInputStream;
    private boolean requestSuccess;
    private TextView mFileNameText;


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

    @Override
    public void onClick(View v) {
        switch (v.getId()){
            case R.id.pick_file_button:
                Intent intent=new Intent(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(Intent.createChooser(intent,"Select a File to upload"),FILE_REQUEST_CODE);
                break;
        }
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
                    mTextViewTip.setText("连接成功，上传文件中。。。。");
                    break;
                case 2:
                    if(msg.arg1==0){
                        mTextViewTip.setText("文件升级成功, 正在重启系统中。。。");
                    }else {
                        mTextViewTip.setText("文件升级失败  ！！！！！！！！");
                        mIsClicked=false;
                    }
                    break;
                default:
                    break;
            }
        }
    }
    private void initData() {
        PermissionUtils.checkPermission(this,new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, Manifest.permission.READ_EXTERNAL_STORAGE,PermissionUtils.REQUEST_READ_EXTERNA);
        mNetWorkUtils = new NetWorkUtils(this);
        mEventtHandler = new EventHandler(Looper.getMainLooper());
        if(mNetWorkUtils.isNetworkEnable()){
            mConnectStateTextView.setText(mNetWorkUtils.getCurrentNetworkType());
        }else {
            mConnectStateTextView.setText("无网络连接");
        }
        if(mNetWorkUtils.isWifiAvaiable()){
            if(mLinearLayout.getVisibility()==View.GONE){
                mLinearLayout.setVisibility(View.VISIBLE);
            }
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
        mButtonPickFile = (Button) findViewById(R.id.pick_file_button);
        mButtonPickFile.setOnClickListener(this);
        mFileNameText = (TextView) findViewById(R.id.file_name_text);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode){
            case PermissionUtils.REQUEST_READ_EXTERNA:
                if(grantResults[0]== PackageManager.PERMISSION_GRANTED){
                    Log.i(TAG, "onRequestPermissionsResult: permission read");
                    mIsReadExternal = true;
                }else{
                    mIsReadExternal=false;
                }
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode!=RESULT_OK){
            Log.i(TAG, "onActivityResult: result error:"+requestCode);
            return;
        }
        switch (requestCode){
            case FILE_REQUEST_CODE:
                Uri uri = data.getData();
                String mimeType = getContentResolver().getType(uri);
                if(mimeType==null){
                    String path= FileUriUtils.getPath(this,uri);
                    if(path==null){
                        mFilename=FileUriUtils.getFileName(uri.toString());
                    }else{
                        File file=new File(path);
                        mFilename =file.getName();
                    }
                }else {
                    Uri returnUri=data.getData();
                    Cursor returnCursor=getContentResolver().query(returnUri,null,null,null,null);
                    int nameIndex=returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
                    int sizeIndex=returnCursor.getColumnIndex(OpenableColumns.SIZE);
                    returnCursor.moveToFirst();
                    mFilename=returnCursor.getString(nameIndex);
                    String size=Long.toString(returnCursor.getLong(sizeIndex));
                }
                try {
                    mInputStream = getContentResolver().openInputStream(uri);
                    if(mFilename.contains("linux")||mFilename.contains("Linux")){
                        mFileNameText.setTextColor(getResources().getColor(R.color.colorPrimary));
                        mFileNameText.setText("获取成功："+mFilename);
                        requestSuccess =true;
                        mButtonPickFile.setVisibility(View.GONE);
                    }else{
                        mFileNameText.setText("文件名不正确，需包含(linux)");
                        mButtonPickFile.setText("重新获取文件");
                    }
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                break;
            default:
                break;
        }
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
       if(requestSuccess!=true){
           Log.i(TAG, "upgrade: read error,cannot get the file");
           Toast.makeText(this, "文件状态不正确，不能升级", Toast.LENGTH_SHORT).show();
           return;
       }
        if(mIsClicked==true){
            Log.i(TAG, "upgrade: 已经出发升级，请等待");
            Toast.makeText(this, "已经出发升级，请等待", Toast.LENGTH_SHORT).show();
            return;
        }
        mIsClicked=true;
        Log.i(TAG, "upgrade: ");
           // Log.i(TAG, "upgrade: length"+bytes.length);
            new Thread(new Runnable() {
                @Override
                public void run() {
                    mString = httpUtils.submitPostData(mInputStream);
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

package com.test.upgrade.http;

import android.content.Context;
import android.os.Handler;
import android.os.Message;
import android.util.Log;

import com.test.upgrade.R;

import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * Created by dell on 2017/10/23.
 *
 */

public class HttpUtils {
    private static final String TAG = "HttpUtils";
    private Context mContext;
    private boolean mIsConnected=false;
    private static Handler mHandler;

    public HttpUtils(Context context, Handler eventHandler){
       mContext=context;
        mHandler=eventHandler;
    }

    public String submitPostData(){

        try {
            URL url = new URL("http://192.168.1.1/upgrade.asp");
            String boundary="-----------------------------7e1b3242d0f6e";
            String enter="\r\n";
            HttpURLConnection httpURLConnection= (HttpURLConnection)url.openConnection();
            httpURLConnection.setConnectTimeout(3000);
            httpURLConnection.setDoInput(true);
            httpURLConnection.setDoOutput(true);
            httpURLConnection.setRequestMethod("POST");
            httpURLConnection.setUseCaches(false);
           // httpURLConnection.setRequestProperty("Cache-Control","max-age=0");
           // httpURLConnection.setRequestProperty("Origin","http://192.168.1.1");
           // httpURLConnection.setRequestProperty("Upgrade-Insecure-Requests","1");
            httpURLConnection.setRequestProperty("Accept","image/jpeg, application/x-ms-application, image/gif, application/xaml+xml, image/pjpeg, application/x-ms-xbap, application/vnd.ms-excel, application/vnd.ms-powerpoint, application/msword, */*");
            httpURLConnection.setRequestProperty("Referer","http://192.168.1.1/upgrade.asp");
            httpURLConnection.setRequestProperty("Accept-Language","zh-CN");
            httpURLConnection.setRequestProperty("User-Agent","Mozilla/4.0 (compatible; MSIE 8.0; Windows NT 6.1; Trident/4.0; SLCC2; .NET CLR 2.0.50727; .NET CLR 3.5.30729; .NET CLR 3.0.30729; Media Center PC 6.0; InfoPath.2)");
            httpURLConnection.setRequestProperty("Content-Type","multipart/form-data;"+"boundary="+boundary);
            httpURLConnection.setRequestProperty("Accept-Encoding","gzip, deflate");
            httpURLConnection.setRequestProperty("Host","192.168.1.1");
           // httpURLConnection.setRequestProperty("Content-Length","16137894");
            httpURLConnection.setRequestProperty("Connection","Keep-Alive");
            httpURLConnection.setRequestProperty("Cache-Control","no-cache");
            OutputStream outputStream=httpURLConnection.getOutputStream();
            DataOutputStream dataOutputStream=new DataOutputStream(outputStream);

            //postflag=1
            StringBuffer postFlag=new StringBuffer();
            postFlag.append(enter+"--"+boundary+enter);
            postFlag.append("Content-Disposition: form-data; name="+"\"postflag\""+enter+enter);
            postFlag.append("1"+enter);
            dataOutputStream.writeBytes(postFlag.toString());
            //outputStream.write(postFlag.toString().getBytes());


            //HTML_HEADER_TYPE=2

            StringBuffer headerType=new StringBuffer();
            headerType.append("--"+boundary+enter);
            headerType.append("Content-Disposition: form-data; name=\"HTML_HEADER_TYPE\""+enter+enter);
            headerType.append("2"+enter);
            dataOutputStream.writeBytes(headerType.toString());
            //outputStream.write(postFlag.toString().getBytes());

            //upload_type=4
            StringBuffer uploadType=new StringBuffer();
            uploadType.append("--"+boundary+enter);
            uploadType.append("Content-Disposition: form-data; name="+"\"upload_type\""+enter+enter);
            uploadType.append("4"+enter);
            dataOutputStream.writeBytes(uploadType.toString());
          //  outputStream.write(postFlag.toString().getBytes());
            //img content

            InputStream inputStreamContent=mContext.getResources().openRawResource(R.raw.tclinux);
            byte[] bytes=new byte[1024];
           // int length=inputStreamContent.available();
         //   byte[] bytes=new byte[length];
            //byte[] bytes= FileUtils.toByteArray(inputStreamContent);
            int length=-1;

           // byte[] bytes = FileUtils.toByteArray(inputStreamContent);
            Log.i(TAG, "submitPostData: length="+bytes.length);
          //  Log.i(TAG, "submitPostData: bytes:"+new String(bytes));
            // StringBuffer postFlag=new StringBuffer();
            dataOutputStream.writeBytes(boundary+enter);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name="+"\"FW_UploadFile\";");
            dataOutputStream.writeBytes("filename="+"\"tclinux.bin\""+enter);
            dataOutputStream.writeBytes("Content-Type: application/octet-stream"+enter+enter);
            while((length=inputStreamContent.read(bytes))!=-1){
                dataOutputStream.write(bytes,0,length);
            }
            inputStreamContent.close();
            dataOutputStream.writeBytes(enter);
           // outputStream.write(postFlag.toString().getBytes());
            //..... content
           // StringBuffer postFlag=new StringBuffer();
            dataOutputStream.writeBytes("--"+boundary+enter);
            dataOutputStream.writeBytes("Content-Disposition: form-data; name="+"\"uiStatus\""+enter+enter);
            dataOutputStream.writeBytes("..........................."+enter);
            dataOutputStream.writeBytes(boundary+"--"+enter);
            //outputStream.write(postFlag.append());
            //outputStream.write(postFlag.toString().getBytes());
            dataOutputStream.flush();
            dataOutputStream.close();

            int response =httpURLConnection.getResponseCode();
            Message message=new Message();
            if(response==HttpURLConnection.HTTP_OK){
                message.what=1;
                Log.i(TAG, "submitPostData: HTTP_OK");
                mHandler.sendMessage(message);
                mIsConnected=true;
                InputStream inputStream=httpURLConnection.getInputStream();
                return dealResponseResult(inputStream);
            }
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return "";

    }
    public boolean isConnected(){
        return mIsConnected;
    }
    public static String dealResponseResult(InputStream inputStream){
        String resultData=null;
        Message message=new Message();
        ByteArrayOutputStream byteArrayOutputStream=new ByteArrayOutputStream();
        byte[] data=new byte[1024];
        int len=0;
        try {
            while((len=inputStream.read(data))!=-1){
                byteArrayOutputStream.write(data,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        resultData =new String(byteArrayOutputStream.toByteArray());
        Log.i(TAG, "dealResponseResult: "+resultData);
        if(resultData.contains("升级成功")){
            message.what=2;
            message.arg1=0;
            mHandler.sendMessage(message);
        }else {
            message.what=2;
            message.arg1=1;
            mHandler.sendMessage(message);
        }
        return resultData;
    }
}

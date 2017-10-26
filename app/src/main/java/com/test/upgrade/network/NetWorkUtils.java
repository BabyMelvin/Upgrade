package com.test.upgrade.network;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

/**
 * Created by dell on 2017/10/26.
 *
 */

public class NetWorkUtils {
    private static final int NETWORK_TYPE_UNAVAILABLE = -1;
    // private static final int NETWORK_TYPE_MOBILE = -100;
    private static final int NETWORK_TYPE_WIFI = -101;

    private static final int NETWORK_CLASS_WIFI = -101;
    private static final int NETWORK_CLASS_UNAVAILABLE = -1;
    /** Unknown network class. */
    private static final int NETWORK_CLASS_UNKNOWN = 0;
    /** Class of broadly defined "2G" networks. */
    private static final int NETWORK_CLASS_2_G = 1;
    /** Class of broadly defined "3G" networks. */
    private static final int NETWORK_CLASS_3_G = 2;
    /** Class of broadly defined "4G" networks. */
    private static final int NETWORK_CLASS_4_G = 3;

    // 适配低版本手机
    /** Network type is unknown */
    public static final int NETWORK_TYPE_UNKNOWN = 0;
    /** Current network is GPRS */
    public static final int NETWORK_TYPE_GPRS = 1;
    /** Current network is EDGE */
    public static final int NETWORK_TYPE_EDGE = 2;
    /** Current network is UMTS */
    public static final int NETWORK_TYPE_UMTS = 3;
    /** Current network is CDMA: Either IS95A or IS95B */
    public static final int NETWORK_TYPE_CDMA = 4;
    /** Current network is EVDO revision 0 */
    public static final int NETWORK_TYPE_EVDO_0 = 5;
    /** Current network is EVDO revision A */
    public static final int NETWORK_TYPE_EVDO_A = 6;
    /** Current network is 1xRTT */
    public static final int NETWORK_TYPE_1xRTT = 7;
    /** Current network is HSDPA */
    public static final int NETWORK_TYPE_HSDPA = 8;
    /** Current network is HSUPA */
    public static final int NETWORK_TYPE_HSUPA = 9;
    /** Current network is HSPA */
    public static final int NETWORK_TYPE_HSPA = 10;
    /** Current network is iDen */
    public static final int NETWORK_TYPE_IDEN = 11;
    /** Current network is EVDO revision B */
    public static final int NETWORK_TYPE_EVDO_B = 12;
    /** Current network is LTE */
    public static final int NETWORK_TYPE_LTE = 13;
    /** Current network is eHRPD */
    public static final int NETWORK_TYPE_EHRPD = 14;
    /** Current network is HSPA+ */
    public static final int NETWORK_TYPE_HSPAP = 15;
    Context mContext;
    private final ConnectivityManager mConnectivityManager;
    private final NetworkInfo mNetworkInfo;
    private WifiManager mWifiManager;

    public NetWorkUtils(Context context){
        mContext=context;
        mConnectivityManager = (ConnectivityManager) mContext.getSystemService(Context.CONNECTIVITY_SERVICE);
        mNetworkInfo = mConnectivityManager.getActiveNetworkInfo();
        mWifiManager = (WifiManager) mContext.getSystemService(Context.WIFI_SERVICE);
    }
    public boolean isWifiAvaiable(){

        return ( mNetworkInfo !=null&& mNetworkInfo .isConnected()&& mNetworkInfo .getType()==ConnectivityManager.TYPE_WIFI);
    }
    public boolean isNetworkEnable(){
        if(mConnectivityManager==null) {
            Toast.makeText(mContext, "网络未连接", Toast.LENGTH_SHORT).show();
            return false;
        }
        if(mNetworkInfo!=null&& mNetworkInfo.isConnected()){
            if(mNetworkInfo.getState()== NetworkInfo.State.CONNECTED){
                return true;
            }
        }
        Toast.makeText(mContext, "无法连接网络", Toast.LENGTH_SHORT).show();
        return false;
    }
    public String getSSID(){
        if(isWifiAvaiable()&&mWifiManager!=null){
            return mWifiManager.getConnectionInfo().getSSID();
        }
        return "";
    }
    public String getIpAddress(){
        if(isWifiAvaiable()&&mWifiManager!=null){
            //Log.i(TAG, "getIpAddress: "+mWifiManager.getDhcpInfo());
            return formatIpAddress(mWifiManager.getDhcpInfo().ipAddress);
        }
        return "";
    }
    public String getServerAddress(){
        if(isWifiAvaiable()&&mWifiManager!=null){
            return formatIpAddress(mWifiManager.getDhcpInfo().serverAddress);
        }
        return "";
    }
    public String getCurrentNetworkType(){
        int networkClass=getNetworkClass();
        String type = "未知";
        switch (networkClass) {
            case NETWORK_CLASS_UNAVAILABLE:
                type = "无";
                break;
            case NETWORK_CLASS_WIFI:
                type = "Wi-Fi";
                break;
            case NETWORK_CLASS_2_G:
                type = "2G";
                break;
            case NETWORK_CLASS_3_G:
                type = "3G";
                break;
            case NETWORK_CLASS_4_G:
                type = "4G";
                break;
            case NETWORK_CLASS_UNKNOWN:
                type = "未知";
                break;
        }
        return type;

    }

    private int getNetworkClass() {
        int networkType=NETWORK_TYPE_UNKNOWN;
        if(mNetworkInfo!=null&&mNetworkInfo.isAvailable()){
            int type=mNetworkInfo.getType();
            if(type==ConnectivityManager.TYPE_MOBILE){
                TelephonyManager telephonyManager= (TelephonyManager) mContext.getSystemService(Context.TELEPHONY_SERVICE);
                networkType=telephonyManager.getNetworkType();
            }else if(type==ConnectivityManager.TYPE_WIFI){
                networkType=NETWORK_TYPE_WIFI;
            }
        }else{
            networkType=NETWORK_TYPE_UNAVAILABLE;
        }
        return getNetworkClassByType(networkType);
    }

    private int getNetworkClassByType(int networkType) {
        switch (networkType) {
            case NETWORK_TYPE_UNAVAILABLE:
                return NETWORK_CLASS_UNAVAILABLE;
            case NETWORK_TYPE_WIFI:
                return NETWORK_CLASS_WIFI;
            case NETWORK_TYPE_GPRS:
            case NETWORK_TYPE_EDGE:
            case NETWORK_TYPE_CDMA:
            case NETWORK_TYPE_1xRTT:
            case NETWORK_TYPE_IDEN:
                return NETWORK_CLASS_2_G;
            case NETWORK_TYPE_UMTS:
            case NETWORK_TYPE_EVDO_0:
            case NETWORK_TYPE_EVDO_A:
            case NETWORK_TYPE_HSDPA:
            case NETWORK_TYPE_HSUPA:
            case NETWORK_TYPE_HSPA:
            case NETWORK_TYPE_EVDO_B:
            case NETWORK_TYPE_EHRPD:
            case NETWORK_TYPE_HSPAP:
                return NETWORK_CLASS_3_G;
            case NETWORK_TYPE_LTE:
                return NETWORK_CLASS_4_G;
            default:
                return NETWORK_CLASS_UNKNOWN;
        }
    }
    private String formatIpAddress(int ipAdress) {

        return (ipAdress & 0xFF ) + "." +
                ((ipAdress >> 8 ) & 0xFF) + "." +
                ((ipAdress >> 16 ) & 0xFF) + "." +
                ( ipAdress >> 24 & 0xFF) ;
    }
}

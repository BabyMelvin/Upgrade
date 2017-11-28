package com.test.upgrade;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Created by dell on 2017/10/26.
 *
 */

public class NetworkReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent intentS=new Intent(context,MainActivity.class);
        intentS.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intentS);
    }
}

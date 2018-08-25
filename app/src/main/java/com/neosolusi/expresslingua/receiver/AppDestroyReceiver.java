package com.neosolusi.expresslingua.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppUtils;

public class AppDestroyReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Intent serviceIntent = new Intent(context, AppServices.class);
        serviceIntent.setAction(AppServices.ACTION_SYNC);
        if (!AppUtils.isServiceRunning(AppServices.class, context)) {
            context.startService(serviceIntent);
        }
    }
}

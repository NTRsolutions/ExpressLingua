package com.neosolusi.expresslingua.receiver;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.AppUtils;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.repo.UserRepository;

public class AppBootReceiver extends BroadcastReceiver {

    @Override
    public void onReceive(Context context, Intent intent) {
        Toast.makeText(context, "Testing from boot receiver", Toast.LENGTH_LONG).show();

        if (intent.getAction() != null && intent.getAction().equalsIgnoreCase(AppConstants.BROADCAST_APP_BOOT)) {
            Intent serviceIntent = new Intent(context, AppServices.class);
            intent.setAction(AppServices.ACTION_SYNC);

            UserRepository userRepository = AppInjectors.provideUserRepository(context);
            User user = userRepository.findActiveUser();

            if (user != null) {
                if (!AppUtils.isServiceRunning(AppServices.class, context)) {
                    context.startService(serviceIntent);
                }
            }
        }
    }
}

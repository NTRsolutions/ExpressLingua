package com.neosolusi.expresslingua.data.network;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.neosolusi.expresslingua.AppConstants;
import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.data.entity.User;
import com.neosolusi.expresslingua.data.repo.UserRepository;

import io.realm.Realm;

public class MessagingService extends FirebaseMessagingService {

    private static final String TAG = MessagingService.class.getSimpleName();
    private static final String TOPIC = "notification";

    @Override public void onNewToken(String token) {
        // If you need to handle the generation of a token, initially or
        // after a refresh this is where you should do that.
        Log.d(TAG, "FCM Token: " + token);

        // Once a token is generated, we subscribe to topic.
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC);

        // Send a token to our backend API if necessary
        NetworkDataSource network = AppInjectors.provideNetworkDataSource(this);

        Realm database = Realm.getDefaultInstance();
        User user = database.where(User.class).equalTo("active", true).findFirst();
        if (user == null) return;

        Bundle bundle = new Bundle();
        bundle.putString("userid", user.getUserid());
        bundle.putString("token", token);

        network.startNetworkServiceWithExtra("upload_message_token", bundle);
        database.close();
    }

    @Override public void onMessageReceived(RemoteMessage remoteMessage) {
        // Handle data payload of FCM messages.
        Log.d(TAG, "FCM Message Id: " + remoteMessage.getMessageId());

        if (remoteMessage.getNotification() != null) {
            Log.d(TAG, "FCM Notification Title: " + remoteMessage.getNotification().getTitle());
            Log.d(TAG, "FCM Notification Body: " + remoteMessage.getNotification().getBody());

            Intent intent = new Intent(AppConstants.BROADCAST_FCM_MESSAGE);
            intent.putExtra(AppConstants.BROADCAST_MESSAGE, remoteMessage.getNotification().getBody());
            LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
        }

        Log.d(TAG, "FCM Data Message: " + remoteMessage.getData());
    }

}

package com.neosolusi.expresslingua.data.network;

import android.app.IntentService;
import android.content.Intent;
import android.support.annotation.Nullable;

import com.neosolusi.expresslingua.AppInjectors;

public class NetworkSyncIntentService extends IntentService {

    public NetworkSyncIntentService() {
        super(NetworkSyncIntentService.class.getSimpleName());
    }

    @Override protected void onHandleIntent(@Nullable Intent intent) {
        if (intent != null) {
            NetworkDataSource networkDataSource = AppInjectors.provideNetworkDataSource(this);

            switch (intent.getStringExtra("service")) {
                case "config":
                    networkDataSource.fetchConfig();
                    break;
                case "login":
                    networkDataSource.fetchUser(intent.getBundleExtra("extra"));
                    break;
                case "register":
                    networkDataSource.registerUser(intent.getBundleExtra("extra"));
                    break;
                case "dictionary":
                    networkDataSource.fetchDictionary();
                    break;
                case "episode":
                    networkDataSource.fetchEpisode();
                    break;
                case "readingInfo":
                    networkDataSource.fetchReadingInfo();
                    break;
                case "reading":
                    networkDataSource.fetchReading();
                    break;
                case "readingUser":
                    networkDataSource.fetchReadingUser(intent.getBundleExtra("extra"));
                    break;
                case "flashcard":
                    networkDataSource.fetchFlashcard(intent.getBundleExtra("extra"));
                    break;
                case "notification":
                    networkDataSource.fetchNotification();
                    break;
                case "upload_flashcard":
                    networkDataSource.uploadFlashcard(intent.getBundleExtra("extra"));
                    break;
                case "upload_reading":
                    networkDataSource.uploadReading(intent.getBundleExtra("extra"));
                    break;
                case "upload_user":
                    networkDataSource.uploadUser(intent.getBundleExtra("extra"));
                    break;
                case "upload_challenge":
                    networkDataSource.uploadChallenge(intent.getBundleExtra("extra"));
                    break;
                case "show_groups":
                    networkDataSource.fetchGroups();
                    break;
                case "upload_group":
                    networkDataSource.uploadGroup(intent.getBundleExtra("extra"));
                    break;
                case "update_group":
                    networkDataSource.updateGroup(intent.getBundleExtra("extra"));
                    break;
                case "show_members":
                    networkDataSource.fetchMembers();
                    break;
                case "progress_member":
                    networkDataSource.fetchMemberProgress(intent.getBundleExtra("extra"));
                    break;
                case "upload_member":
                    networkDataSource.uploadMember(intent.getBundleExtra("extra"));
                    break;
                case "upload_message_token":
                    networkDataSource.uploadFCMToken(intent.getBundleExtra("extra"));
                    break;
            }
        }
    }
}

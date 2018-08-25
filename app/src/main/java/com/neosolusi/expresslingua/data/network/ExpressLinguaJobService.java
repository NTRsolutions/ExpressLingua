package com.neosolusi.expresslingua.data.network;

import com.firebase.jobdispatcher.JobParameters;
import com.firebase.jobdispatcher.JobService;
import com.neosolusi.expresslingua.AppExecutors;
import com.neosolusi.expresslingua.AppInjectors;

public class ExpressLinguaJobService extends JobService {

    @Override public boolean onStartJob(JobParameters job) {
        NetworkDataSource networkDataSource = AppInjectors.provideNetworkDataSource(this.getApplicationContext());

        /* For testing only
        * SharedPreferences.Editor mPrefEdit = AppInjectors.provideSharedPreferencesEditor(this);
        * mPrefEdit.putString(AppConstants.PREFERENCE_LAST_SYNC_DICTIONARY, "0000-00-00").apply();
        */

        AppExecutors executors = AppExecutors.getInstance();
        executors.networkIO().execute(() -> networkDataSource.startNetworkService("dictionary"));
        executors.networkIO().execute(() -> networkDataSource.startNetworkService("episode"));
        executors.networkIO().execute(() -> networkDataSource.startNetworkService("readingInfo"));
        executors.networkIO().execute(() -> networkDataSource.startNetworkService("reading"));
        executors.networkIO().execute(() -> networkDataSource.startNetworkService("show_groups"));
        executors.networkIO().execute(() -> networkDataSource.startNetworkService("show_members"));

        // Because local data always be latest, we don't download again
        // executors.networkIO().execute(() -> networkDataSource.startNetworkServiceWithExtra("flashcard", job.getExtras()));

        jobFinished(job, false);

        return true;
    }

    @Override public boolean onStopJob(JobParameters job) {
        return true;
    }
}

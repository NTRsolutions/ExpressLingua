package com.neosolusi.expresslingua.data.network;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;

import com.neosolusi.expresslingua.AppServices.AudioDownloadCallback;

import static com.neosolusi.expresslingua.data.network.NetworkDownloadIntentService.DOWNLOAD_COMPLETE;
import static com.neosolusi.expresslingua.data.network.NetworkDownloadIntentService.UPDATE_PROGRESS;

public class DownloadReceiver extends ResultReceiver {

    private AudioDownloadCallback mCallback;

    public DownloadReceiver(AudioDownloadCallback callback, Handler handler) {
        super(handler);
        this.mCallback = callback;
    }

    @Override protected void onReceiveResult(int resultCode, Bundle resultData) {
        super.onReceiveResult(resultCode, resultData);
        if (resultCode == UPDATE_PROGRESS) {
            mCallback.onProgressUpdate(resultData.getInt("progress"));
        } else if (resultCode == DOWNLOAD_COMPLETE) {
            mCallback.onDownloadFinish(resultData.getBoolean("finish"));
        }
    }

}

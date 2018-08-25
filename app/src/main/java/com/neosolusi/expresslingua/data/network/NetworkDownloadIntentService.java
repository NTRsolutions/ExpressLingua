package com.neosolusi.expresslingua.data.network;

import android.app.IntentService;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.ResultReceiver;
import android.support.v4.app.NotificationCompat;

import com.neosolusi.expresslingua.AppInjectors;
import com.neosolusi.expresslingua.AppServices;
import com.neosolusi.expresslingua.R;
import com.neosolusi.expresslingua.features.lesson.LessonActivity;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import okhttp3.ResponseBody;
import retrofit2.Response;
import retrofit2.Retrofit;

import static com.neosolusi.expresslingua.AppServices.DOWNLOAD_ACTION;

public class NetworkDownloadIntentService extends IntentService {

    public static final int UPDATE_PROGRESS = 8344;
    public static final int DOWNLOAD_COMPLETE = 8345;

    private ExpressLinguaApi mNetwork;
    private ResultReceiver mReceiver;
    private NotificationManager mNotificationManager;
    private NotificationCompat.Builder mNotification;
    private String mLessonsAudio;
    private File audioPath;
    private File audioFile;
    private byte[] fileReader = new byte[1024];
    private long fileSize, fileSizeDownloaded;
    private int read, done = 0;

    public NetworkDownloadIntentService() {
        super("NetworkDownloadIntentService");
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Retrofit retrofit = NetworkUtils.getWorker(this, AppInjectors.provideSharedPreferences(this));
        Bundle bundle = intent.getBundleExtra("extra");

        mReceiver = bundle.getParcelable("receiver");
        mLessonsAudio = bundle.getString("filename");
        Intent notificationIntent = new Intent(this, LessonActivity.class);
        notificationIntent.setAction(DOWNLOAD_ACTION);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
        Bitmap icon = BitmapFactory.decodeResource(getResources(), R.mipmap.ic_launcher);
        mNotification = new NotificationCompat.Builder(this, AppServices.class.getSimpleName())
                .setContentTitle("ExpressLingua Audio Download")
                .setContentText("Download in progress")
                .setContentIntent(pendingIntent)
                .setSmallIcon(R.drawable.ic_notif_icon)
                .setLargeIcon(Bitmap.createScaledBitmap(icon, 128, 128, false))
                .setOngoing(false);
        mNotificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
        audioPath = new File(this.getFilesDir(), "audio");
        audioFile = new File(audioPath, mLessonsAudio.split("\\.")[0] + ".m4a");
        mNetwork = retrofit.create(ExpressLinguaApi.class);

        try {
            Response<ResponseBody> response = mNetwork.downloadAudioFile(mLessonsAudio.split("\\.")[0] + ".m4a").execute();
            if (!response.isSuccessful()) return;

            fileSize = response.body().contentLength();
            fileSizeDownloaded = 0;

            if (!audioPath.isDirectory() && !audioPath.mkdir()) return;
            if (!audioFile.exists() && !audioFile.createNewFile()) return;

            InputStream input = response.body().byteStream();
            OutputStream output = new FileOutputStream(audioFile);

            while ((read = input.read(fileReader)) != -1) {
                output.write(fileReader, 0, read);
                fileSizeDownloaded += read;

                Long d = fileSizeDownloaded * 100 / fileSize;
                if (d.intValue() != done) {
                    done = d.intValue();

                    Bundle resultData = new Bundle();
                    resultData.putInt("progress", d.intValue());
                    mReceiver.send(UPDATE_PROGRESS, resultData);
                }
            }

            Bundle resultData = new Bundle();
            resultData.putBoolean("finish", true);
            mReceiver.send(DOWNLOAD_COMPLETE, resultData);

            output.flush();
            output.close();
            input.close();
        } catch (IOException e) {
            e.printStackTrace();
            Bundle resultData = new Bundle();
            resultData.putBoolean("finish", false);
            mReceiver.send(DOWNLOAD_COMPLETE, resultData);
        }
    }

}
package com.neosolusi.expresslingua;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Handler;
import android.support.v4.content.FileProvider;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.concurrent.TimeUnit;

public class AppPlayer {

    private static final String URL_PATH = "public/audio/";
    private static final String USER_AGENT = "ua";
    private static final float SLOW_FACTOR = 2f;

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static AppPlayer mInstance;
    private final WeakReference<Context> mContext;

    // Player property
    private SimpleExoPlayer mPlayer;
    private PlayerListener mPlayerListener;
    private Uri mAudioFile;
    private ReadingInfo mInfo;
    private int mCurrentWindow;
    private long mSpeechDelay;
    private long mPlaybackPosition;
    private boolean mSpeechToggle = false;
    private Handler mSpeechHandler = new Handler();
    private Runnable mPauseSpeech = this::pause;

    private AppPlayer(Context context) {
        this.mContext = new WeakReference<>(context);
    }

    public synchronized static AppPlayer getInstance(Context context) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new AppPlayer(context);
            }
        }

        return mInstance;
    }

    public SimpleExoPlayer getPlayer() {
        return mPlayer;
    }

    public void setInfo(ReadingInfo info) {
        this.mInfo = info;
    }

    public void initializePlayer() {
        if (mInfo == null) return;

        MediaSource source;

        if (isAudioExists()) {
            source = buildLocalSource(mAudioFile);
        } else {
            String fileName = mInfo.getAudio_file_name().split("\\.")[0] + ".m4a";
            mAudioFile = Uri.parse(AppConstants.BASE_URL + URL_PATH + fileName);
            source = buildMediaSource(mAudioFile);
        }

        TrackSelection.Factory mAdaptiveTrackSelectionFactory = provideTrackSelectionFactory(provideBandwidthMeter());
        DefaultTrackSelector mTrackSelector = new DefaultTrackSelector(mAdaptiveTrackSelectionFactory);
        DefaultRenderersFactory mRenderersFactory = new DefaultRenderersFactory(mContext.get());
        mPlayerListener = new PlayerListener();
        mPlayer = ExoPlayerFactory.newSimpleInstance(mRenderersFactory, mTrackSelector);
        mPlayer.addListener(mPlayerListener);

        prepare(source);
    }

    public void releasePlayer() {
        if (mPlayer != null) {
            mAudioFile = null;
            mPlaybackPosition = 0;
            mCurrentWindow = 0;
            mPlayer.setPlayWhenReady(false);
            mPlayer.removeListener(mPlayerListener);
            mPlayer.release();
            mPlayer = null;
            mInfo = null;
        }
    }

    public void pause() {
        if (mPlayer != null) {
            mCurrentWindow = mPlayer.getCurrentWindowIndex();
            mPlaybackPosition = mPlayer.getCurrentPosition();
            mPlayer.setPlayWhenReady(false);
            mPlayer.setPlaybackParameters(PlaybackParameters.DEFAULT);
            sendBroadcast(AppConstants.BROADCAST_PLAYER_PAUSE, "pause audio");
        }
    }

    public void speech(Reading reading, boolean isSlow) {
        try {
            String[] start = reading.getStart_duration().split(":");
            int minute = Integer.valueOf(start[1]);
            int second = Integer.valueOf(start[2].split("\\.")[0]);
            int millis = Integer.valueOf(start[2].split("\\.")[1]);

            long startTime;
            startTime = TimeUnit.MINUTES.toMillis(minute);
            startTime += TimeUnit.SECONDS.toMillis(second);
            startTime += TimeUnit.MILLISECONDS.toMillis(millis);

            String[] stop = reading.getEnd_duration().split(":");
            int minuteStop = Integer.valueOf(stop[1]);
            int secondStop = Integer.valueOf(stop[2].split("\\.")[0]);
            int millisStop = Integer.valueOf(stop[2].split("\\.")[1]);

            long stopTime;
            stopTime = TimeUnit.MINUTES.toMillis(minuteStop);
            stopTime += TimeUnit.SECONDS.toMillis(secondStop);
            stopTime += TimeUnit.MILLISECONDS.toMillis(millisStop);

            mSpeechToggle = true;
            mSpeechDelay = stopTime - startTime;

            if (isSlow) {
                mSpeechDelay *= SLOW_FACTOR;
                mPlayer.setPlaybackParameters(new PlaybackParameters(1 / SLOW_FACTOR, 1f));
            } else {
                mPlayer.setPlaybackParameters(PlaybackParameters.DEFAULT);
            }

            seekTo(startTime);
        } catch (Exception e) {
            e.printStackTrace();
            LocalBroadcastManager.getInstance(mContext.get()).sendBroadcast(new Intent(AppConstants.BROADCAST_PLAYER_ERROR));
        }
    }

    public boolean isAudioExists() {
        if (mInfo == null || !mInfo.isDownload_complete()) {
//            mAudioFile = null;
            return false;
        }

        String fileName = mInfo.getAudio_file_name().split("\\.")[0] + ".m4a";
        File audioPath = new File(mContext.get().getFilesDir(), "audio");
        File audioFile = new File(audioPath, fileName);

        if (audioFile.exists()) {
            mAudioFile = FileProvider.getUriForFile(mContext.get(), mContext.get().getString(R.string.file_provider), audioFile);
        }

        return audioFile.exists();
    }

    private void sendBroadcast(String intentString, String message) {
        Intent intent = new Intent(intentString);
        intent.putExtra(AppConstants.BROADCAST_MESSAGE, message);
        LocalBroadcastManager.getInstance(mContext.get()).sendBroadcast(intent);
    }

    private void seekTo(long start) {
        if (mAudioFile != null) {
            mPlayer.seekTo(mCurrentWindow, start);
            mPlayer.setPlayWhenReady(false);
        }
    }

    private void prepare(MediaSource source) {
        if (mAudioFile != null) {
            mPlayer.seekTo(mCurrentWindow, mPlaybackPosition);
            mPlayer.prepare(source, false, false);
        }
    }

    private DefaultBandwidthMeter provideBandwidthMeter() {
        return new DefaultBandwidthMeter();
    }

    private TrackSelection.Factory provideTrackSelectionFactory(DefaultBandwidthMeter bandwidthMeter) {
        return new AdaptiveTrackSelection.Factory(bandwidthMeter);
    }

    private MediaSource buildLocalSource(Uri uri) {
        return new ExtractorMediaSource.Factory(new DefaultDataSourceFactory(mContext.get(), USER_AGENT)).createMediaSource(uri);
    }

    private MediaSource buildMediaSource(Uri uri) {
        return new ExtractorMediaSource.Factory(new DefaultHttpDataSourceFactory(USER_AGENT)).createMediaSource(uri);
    }

    private class PlayerListener extends Player.DefaultEventListener {
        @Override public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            String stateString;
            switch (playbackState) {
                case Player.STATE_IDLE:
                    stateString = "ExoPlayer.STATE_IDLE      -";
                    break;
                case Player.STATE_BUFFERING:
                    stateString = "ExoPlayer.STATE_BUFFERING -";
                    break;
                case Player.STATE_READY:
                    if (mSpeechToggle) {
                        mSpeechToggle = false;
                        mPlayer.setPlayWhenReady(true);
                        mSpeechHandler.removeCallbacks(mPauseSpeech);
                        mSpeechHandler.postDelayed(mPauseSpeech, mSpeechDelay);
                    }
                    stateString = "ExoPlayer.STATE_READY     - " + System.currentTimeMillis();
                    break;
                case Player.STATE_ENDED:
                    stateString = "ExoPlayer.STATE_ENDED     -";
                    break;
                default:
                    stateString = "UNKNOWN_STATE             -";
                    break;
            }

            Log.d("Player State", "Changed state to " + stateString + " playWhenReady: " + playWhenReady);
        }
    }

}

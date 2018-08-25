package com.neosolusi.expresslingua.data.network;

import android.content.Context;
import android.content.SharedPreferences;
import android.util.Log;

import com.google.gson.GsonBuilder;
import com.neosolusi.expresslingua.BuildConfig;

import java.io.File;

import okhttp3.Cache;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

import static com.neosolusi.expresslingua.AppConstants.API_TOKEN;
import static com.neosolusi.expresslingua.AppConstants.AUTHORIZATION;
import static com.neosolusi.expresslingua.AppConstants.BASE_URL;
import static com.neosolusi.expresslingua.AppConstants.EXPRESSLINGUA_ANDROID_APP;
import static com.neosolusi.expresslingua.AppConstants.USER_AGENT;
import static okhttp3.logging.HttpLoggingInterceptor.Level.BODY;
import static okhttp3.logging.HttpLoggingInterceptor.Level.NONE;

public class NetworkUtils {

    // Singleton Instantiation
    private static final Object LOCK = new Object();
    private static NetworkUtils mInstance;
    private final Retrofit mRetrofit;

    private NetworkUtils(Context context, SharedPreferences preferences) {
        Cache cache = cache(context);
        Interceptor interceptor = urlAndHeaderInterceptor(preferences);
        HttpLoggingInterceptor logging = httpLoggingInterceptor();
        OkHttpClient httpClient = okHttpClient(interceptor, logging, cache);

        mRetrofit = new Retrofit.Builder()
                .baseUrl(BASE_URL)
                .client(httpClient)
                .addConverterFactory(GsonConverterFactory.create(gsonBuilder().create()))
                .build();
    }

    public static Retrofit getWorker(Context context, SharedPreferences preferences) {
        if (mInstance == null) {
            synchronized (LOCK) {
                mInstance = new NetworkUtils(context, preferences);
            }
        }

        return mInstance.mRetrofit;
    }

    private GsonBuilder gsonBuilder() {
        return new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss");
    }

    private OkHttpClient okHttpClient(Interceptor interceptor, HttpLoggingInterceptor logging, Cache cache) {
        return new OkHttpClient.Builder()
                .addInterceptor(interceptor)
                .addInterceptor(logging)
                .cache(cache)
                .build();
    }

    private Interceptor urlAndHeaderInterceptor(SharedPreferences preferences) {
        return chain -> {
            Request request = chain.request();
            Request.Builder builder = request.newBuilder();
            builder.addHeader(USER_AGENT, EXPRESSLINGUA_ANDROID_APP);
            builder.addHeader(AUTHORIZATION, "Bearer " + preferences.getString(API_TOKEN, ""));

            return chain.proceed(builder.build());
        };
    }

    private HttpLoggingInterceptor httpLoggingInterceptor() {
        HttpLoggingInterceptor httpLog = new HttpLoggingInterceptor(message -> Log.d("HttpLogging", message));

        httpLog.setLevel(BuildConfig.DEBUG ? BODY : NONE);

        return httpLog;
    }

    private Cache cache(Context context) {
        Cache cache = null;

        try {
            cache = new Cache(new File(context.getApplicationContext().getCacheDir(), "http-cache"), 10 * 1024 * 1024);
        } catch (Exception e) {
            Log.e("HttpCache", "Could not create cache!");
        }

        return cache;
    }

}

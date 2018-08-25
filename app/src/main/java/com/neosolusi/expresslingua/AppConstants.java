package com.neosolusi.expresslingua;

public class AppConstants {

    /* HTTP Headers */
//    public static final String BASE_URL = "http://new-neosolusi.ddns.net:8000/expresslingua_api/";
//    public static final String BASE_URL = "http://192.168.0.112/expresslingua_api/";
    public static final String BASE_URL = "http://dev.expresslingua.com/expresslingua_api/";
//    public static final String BASE_URL = "http://fire.expresslingua.com/expresslingua_api/";
    public static final String BASE_URL_USER_PROFILE = BASE_URL + "public/profile/";
    public static final String BASE_URL_USER_PROFILE_THUMB = BASE_URL + "public/thumb_profile/";
    public static final String BASE_URL_GROUP_PROFILE = BASE_URL + "public/group/";
    public static final String BASE_URL_GROUP_PROFILE_THUMB = BASE_URL + "public/thumb_group/";
    public static final String USER_AGENT = "User-Agent";
    public static final String EXPRESSLINGUA_ANDROID_APP = "Express_Lingua";
    public static final String AUTHORIZATION = "Authorization";
    public static final String API_TOKEN = "api_token";

    /* Firebase **/
    public static final String DEFAULT_PASSWORD = "wRSsS0W9yfaVKfBS";

    /* Setting File */
    public static final String PREFERENCE_FILE_KEY = "com.neosolusi.expresslingua.PREFERENCE_FILE_KEY";
    public static final String PREFERENCE_SETTING_VALUES = "com.neosolusi.expresslingua.PREFERENCE_SETTING_VALUES";
    public static final String PREFERENCE_MAX_ALLOWED_FLASHCARD = "com.neosolusi.expresslingua.PREFERENCE_MAX_ALLOWED_FLASHCARD";
    public static final String PREFERENCE_MAX_DAILY_REVIEW_CARD = "com.neosolusi.expresslingua.PREFERENCE_MAX_DAILY_REVIEW_CARD";
    public static final String PREFERENCE_MAX_DAILY_NEW_CARD = "com.neosolusi.expresslingua.PREFERENCE_MAX_DAILY_NEW_CARD";
    public static final String PREFERENCE_MAX_DAILY_FLUENCY_CARD = "com.neosolusi.expresslingua.PREFERENCE_MAX_DAILY_FLUENCY_CARD";
    public static final String PREFERENCE_MAX_CHALLENGE_ORANGE = "com.neosolusi.expresslingua.PREFERENCE_MAX_CHALLENGE_ORANGE";
    public static final String PREFERENCE_REVIEWED_CARD = "com.neosolusi.expresslingua.PREFERENCE_REVIEWED_CARD";
    public static final String PREFERENCE_NEWED_CARD = "com.neosolusi.expresslingua.PREFERENCE_NEWED_CARD";
    public static final String PREFERENCE_FRESH_STORIES = "com.neosolusi.expresslingua.PREFERENCE_FRESH_STORIES";
    public static final String PREFERENCE_FRESH_LESSONS = "com.neosolusi.expresslingua.PREFERENCE_FRESH_LESSONS";
    public static final String PREFERENCE_LAST_REVIEW_DATE = "com.neosolusi.expresslingua.PREFERENCE_LAST_REVIEW_DATE";
    public static final String PREFERENCE_LAST_SYNC_EPISODE = "com.neosolusi.expresslingua.PREFERENCE_LAST_SYNC_EPISODE";
    public static final String PREFERENCE_LAST_SYNC_DICTIONARY = "com.neosolusi.expresslingua.PREFERENCE_LAST_SYNC_DICTIONARY";
    public static final String PREFERENCE_LAST_SYNC_FLASHCARD = "com.neosolusi.expresslingua.PREFERENCE_LAST_SYNC_FLASHCARD";
    public static final String PREFERENCE_LAST_SYNC_READING = "com.neosolusi.expresslingua.PREFERENCE_LAST_SYNC_READING";
    public static final String PREFERENCE_LAST_SYNC_READING_INFO = "com.neosolusi.expresslingua.PREFERENCE_LAST_SYNC_READING_INFO";
    public static final String PREFERENCE_LAST_SYNC_READING_USER = "com.neosolusi.expresslingua.PREFERENCE_LAST_SYNC_READING_USER";
    public static final String PREFERENCE_LAST_EPISODE_ID = "com.neosolusi.expresslingua.PREFERENCE_LAST_EPISODE_ID";
    public static final String PREFERENCE_LAST_LESSON_ID = "com.neosolusi.expresslingua.PREFERENCE_LAST_LESSON_ID";
    public static final String PREFERENCE_LAST_LESSON_AUDIO = "com.neosolusi.expresslingua.PREFERENCE_LAST_LESSON_AUDIO";
    public static final String PREFERENCE_MENU_PLAY_AS = "com.neosolusi.expresslingua.PREFERENCE_MENU_PLAY_AS";
    public static final String PREFERENCE_MIN_PASS_LESSON = "com.neosolusi.expresslingua.PREFERENCE_MIN_PASS_LESSON";

    /** Typeface */
    public static final String ROOT = "fonts/";
    public static final String FONTAWESOME = ROOT + "fontawesome-webfont.ttf";
    public static final String EALING = ROOT + "EALINGRegular.otf";

    /** Broadcast Intent */
    public static final String BROADCAST_APP_BOOT = "android.intent.action.BOOT_COMPLETED";
    public static final String BROADCAST_APP_DESTROY = "com.neosolusi.expresslingua.APP_DESTROY";
    public static final String BROADCAST_LOGIN_FAILED = "com.neosolusi.expresslingua.LOGIN_FAILED";
    public static final String BROADCAST_REGISTER_FAILED = "com.neosolusi.expresslingua.REGISTER_FAILED";
    public static final String BROADCAST_UPLOAD_SUCCESS = "com.neosolusi.expresslingua.UPLOAD_SUCCESS";
    public static final String BROADCAST_UPLOAD_GROUP_IMAGE_SUCCESS = "com.neosolusi.expresslingua.BROADCAST_UPLOAD_GROUP_IMAGE_SUCCESS";
    public static final String BROADCAST_UPLOAD_USER_IMAGE_SUCCESS = "com.neosolusi.expresslingua.BROADCAST_UPLOAD_USER_IMAGE_SUCCESS";
    public static final String BROADCAST_SERVICE_CONFIG = "com.neosolusi.expresslingua.SERVICE_CONFIG";
    public static final String BROADCAST_INITIAL_DATA_COMPLETE = "com.neosolusi.expresslingua.BROADCAST_INITIAL_DATA_COMPLETE";
    public static final String BROADCAST_PLAYER_ERROR = "com.neosolusi.expresslingua.BROADCAST_PLAYER_ERROR";
    public static final String BROADCAST_PLAYER_PAUSE = "com.neosolusi.expresslingua.BROADCAST_PLAYER_PAUSE";
    public static final String BROADCAST_PLAYER_FINISH = "com.neosolusi.expresslingua.BROADCAST_PLAYER_FINISH";
    public static final String BROADCAST_FCM_MESSAGE = "com.neosolusi.expresslingua.FCM_MESSAGE";

    /** Broadcast Extra */
    public static final String BROADCAST_MESSAGE = "broadcast_message";

    /** Global Variable */
    public static final int DEFAULT_ANIMATION_LENGTH = 250;
    public static final int DEFAULT_LOCAL_WORD_CONSTANT = 5;
    public static final int DEFAULT_MAX_ALLOWED_FLASHCARD = 1500;
    public static final int DEFAULT_CHALLENGE_COUNT_DOWN = 50000;
    public static final int DEFAULT_LESSON_TO_SHOW_HARD_CHALLENGE = 20;
}

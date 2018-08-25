package com.neosolusi.expresslingua;

import android.app.ActivityManager;
import android.app.ActivityOptions;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.os.IBinder;
import android.support.transition.Slide;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.DecelerateInterpolator;
import android.view.inputmethod.InputMethodManager;
import android.widget.TextView;

import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Reading;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class AppUtils {

    private static Typeface mTypeface;

    public static void addFragmentToActivity(FragmentManager manager, Fragment fragment, int id, String tag) {
        FragmentTransaction transaction = manager.beginTransaction();
        transaction.setCustomAnimations(android.R.anim.fade_in, android.R.anim.fade_out);
        transaction.replace(id, fragment);
        transaction.addToBackStack(tag);
        transaction.commitAllowingStateLoss();
    }

    public static void startActivity(Context context, Class target) {
        Intent intent = new Intent(context, target);
        Bundle animation = ActivityOptions.makeCustomAnimation(context, R.anim.animation, R.anim.animation2).toBundle();
        context.startActivity(intent, animation);
    }

    public static void startActivityWithExtra(Context context, Class target, Bundle bundle) {
        Intent intent = new Intent(context, target);
        Bundle animation = ActivityOptions.makeCustomAnimation(context, R.anim.animation, R.anim.animation2).toBundle();
        intent.putExtra("extra", bundle);
        context.startActivity(intent, animation);
    }

    public static boolean isWord(String text) {
        boolean result = true;
        String[] words = text.split(" ");
        List<String> tempWord = new ArrayList<>();

        if (text.length() >= 1) {
            if (words.length > 1) {
                for (String word : words) {
                    if (word.length() >= 1) tempWord.add(word);
                }
                if (tempWord.size() > 1) result = false;
            }
        }

        return result;
    }

    public static String normalizeString(String text) {
        return text.trim().replaceAll("[^a-zA-Z0-9'’`\\s]", "");
    }

    public static String normalizeStringWithoutThickMark(String text) {
        return text.trim().replaceAll("[^a-zA-Z0-9\\s]", "");
    }

    public static String normalizeStringForChallenge(String text) {
        String normalizeText;
        normalizeText = text.replaceFirst("^\\d\\s", "");
        normalizeText = normalizeText.replaceFirst("^\\S\\s", "");

        return normalizeText;
    }

    public static String normalizePhone(String phone) {
        String phoneNumber = phone.replace(" ", "").replace("-", "").trim();
        if (phoneNumber.substring(0, 2).equals("+62")) {
            return "0" + phoneNumber.substring(3, 20);
        } else if (phoneNumber.substring(0, 1).equals("8")) {
            return "0" + phoneNumber;
        } else {
            return phoneNumber;
        }
    }

    public static boolean isDigitOnly(String text) {
        Pattern pattern = Pattern.compile("\\D", Pattern.CASE_INSENSITIVE);
        Matcher matcher = pattern.matcher(text);
        return matcher.find();
    }

    public static String replaceDigitWithWord(String text) {
        if (text.contains(" 1 ")) {
            text = text.replace(" 1 ", " one ");
        } else if (text.contains(" 2 ")) {
            text = text.replace(" 2 ", " two ");
        } else if (text.contains(" 3 ")) {
            text = text.replace(" 3 ", " three ");
        } else if (text.contains(" 4 ")) {
            text = text.replace(" 4 ", " four ");
        } else if (text.contains(" 5 ")) {
            text = text.replace(" 5 ", " five ");
        } else if (text.contains(" 6 ")) {
            text = text.replace(" 6 ", " six ");
        } else if (text.contains(" 7 ")) {
            text = text.replace(" 7 ", " seven ");
        } else if (text.contains(" 8 ")) {
            text = text.replace(" 8 ", " eight ");
        } else if (text.contains(" 9 ")) {
            text = text.replace(" 9 ", " nine ");
        }

        return text;
    }

    public static String leftTrim(String text) {
        return text.replaceAll("^\\s+", "");
    }

    public static String rightTrim(String text) {
        return text.replaceAll("\\s+$", "");
    }

    public static boolean isBeforeToday(long date) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMdd", Locale.getDefault());

        Date dateToCheck = new Date();
        dateToCheck.setTime(date);

        String strLastDate = dateFormat.format(dateToCheck);
        String strNowDate = dateFormat.format(new Date());

        return strLastDate.compareTo(strNowDate) == -1;
    }

    public static String versionName(Context context) {
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName(), 0);
            return pInfo.versionName;
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    public static void showKeyboard(Context context) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
    }

    public static void dismissKeyboard(Context context, IBinder token) {
        InputMethodManager imm = (InputMethodManager) context.getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) imm.hideSoftInputFromWindow(token, 0);
    }

    public static boolean isServiceRunning(Class<?> serviceClass, Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);

        assert manager != null;
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) return true;
        }

        return false;
    }

    public static boolean isConnectedToInternet(Context context) {
        ConnectivityManager connectivity = (ConnectivityManager) context.getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (connectivity != null) {
            NetworkInfo[] infos = connectivity.getAllNetworkInfo();
            if (infos != null)
                for (NetworkInfo info : infos) {
                    if (info.getState() == NetworkInfo.State.CONNECTED) return true;
                }

        }
        return false;
    }

    public static String[] getSamples(Flashcard flashcard) {
        Realm mRealm = Realm.getDefaultInstance();
        int loop = 0;
        String[] samples = new String[3];
        RealmResults<Reading> readings = mRealm.where(Reading.class)
                .contains("sentence", " " + flashcard.getCard() + " ", Case.INSENSITIVE).or()
                .contains("sentence", " " + flashcard.getCard(), Case.INSENSITIVE).or()
                .contains("sentence", flashcard.getCard() + " ", Case.INSENSITIVE)
                .findAll();
        StringBuilder sb = new StringBuilder();
        for (Reading reading : readings) {
            String[] split = reading.getSentence().split("[.]");
            String text = "";
            if (split.length > 1) {
                for (String findText : split) {
                    String card = flashcard.getCard().toLowerCase();
                    String sentence = findText.toLowerCase();
//                        if (sentence.matches(".*(?<=\\W"+card+"|^"+card+")(?=\\W|"+card+"$).*")
//                        if (sentence.matches(".*"+card+"(?<=\\W?"+card+"|^)(?=\\W|$).*")
                    if (sentence.matches(".*(?<=\\W" + card + "|^" + card + ")(?=\\W|" + card + "$).*")
                            && !sb.toString().contains(findText)) {
                        text = findText;
                        break;
                    }
                }
                if (text.isEmpty()) continue;
            } else {
                String card = flashcard.getCard().toLowerCase();
                String sentence = reading.getSentence().toLowerCase();
//                    if (sentence.matches(".*(?<=\\W"+card+"|^"+card+")(?=\\W|"+card+"$).*")
//                    if (sentence.matches(".*"+card+"(?<=\\W?"+card+"|^)(?=\\W|$).*")
                if (sentence.matches(".*(?<=\\W" + card + "|^" + card + ")(?=\\W|" + card + "$).*")
                        && !sb.toString().contains(reading.getSentence())) {
                    text = reading.getSentence();
                } else continue;
            }

            samples[loop] = AppUtils.leftTrim(text);
            sb.append("- ").append(text);
            loop++;
            if (loop == 3) break;
            sb.append("\n");
        }

        mRealm.close();
        return samples;
    }

    public static String[] getSamples(Dictionary dictionary) {
        Realm mRealm = Realm.getDefaultInstance();
        int loop = 0;
        String[] samples = new String[3];
        RealmResults<Reading> readings = mRealm.where(Reading.class)
                .contains("sentence", " " + dictionary.getWord() + " ", Case.INSENSITIVE).or()
                .contains("sentence", " " + dictionary.getWord(), Case.INSENSITIVE).or()
                .contains("sentence", dictionary.getWord() + " ", Case.INSENSITIVE)
                .findAll();
        StringBuilder sb = new StringBuilder();
        for (Reading reading : readings) {
            String[] split = reading.getSentence().split("[.]");
            String[] splitTranslate = reading.getTranslation().split("[.]");
            String text = "";
            if (split.length > 1) {
                int loop2 = 0;
                for (String findText : split) {
                    String card = dictionary.getWord().toLowerCase();
                    String sentence = findText.toLowerCase();
//                        if (sentence.matches(".*(?<=\\W"+card+"|^"+card+")(?=\\W|"+card+"$).*")
//                        if (sentence.matches(".*"+card+"(?<=\\W?"+card+"|^)(?=\\W|$).*")
                    if (sentence.matches(".*(?<=\\W" + card + "|^" + card + ")(?=\\W|" + card + "$).*")
                            && !sb.toString().contains(findText)) {
                        text = findText;

                        String[] splitDictionaryTranslation = dictionary.getTranslation().split("[,]");
                        for (String findTranslationText : splitDictionaryTranslation) {
                            String word = findTranslationText.trim().toLowerCase();
                            if (splitTranslate[loop2].matches(".*(?<=\\W" + word + "|^" + word + ")(?=\\W|" + word + "$).*")) {
                                // ada
                                break;
                            }
                        }

                        break;
                    }
                    loop2++;
                }
                if (text.isEmpty()) continue;
            } else {
                String card = dictionary.getWord().toLowerCase();
                String sentence = reading.getSentence().toLowerCase();
//                    if (sentence.matches(".*(?<=\\W"+card+"|^"+card+")(?=\\W|"+card+"$).*")
//                    if (sentence.matches(".*"+card+"(?<=\\W?"+card+"|^)(?=\\W|$).*")
                if (sentence.matches(".*(?<=\\W" + card + "|^" + card + ")(?=\\W|" + card + "$).*")
                        && !sb.toString().contains(reading.getSentence())) {
                    text = reading.getSentence();
                } else continue;
            }

            samples[loop] = AppUtils.leftTrim(text);
            sb.append("- ").append(text);
            loop++;
            if (loop == 3) break;
            sb.append("\n");
        }

        mRealm.close();
        return samples;
    }

    public static void performFragmentTransition(Fragment prev, Fragment next) {
        Slide exitSlide = new Slide();
        exitSlide.setDuration(250); //500
        exitSlide.setStartDelay(100); //200
        exitSlide.setInterpolator(new DecelerateInterpolator(2));
        exitSlide.setSlideEdge(Gravity.START);
        prev.setExitTransition(exitSlide);

        Slide reenterSlide = new Slide();
        reenterSlide.setDuration(500); //1000
        reenterSlide.setInterpolator(new DecelerateInterpolator(3));
        reenterSlide.setSlideEdge(Gravity.START);
        prev.setReenterTransition(reenterSlide);

        Slide enterSlide = new Slide();
        enterSlide.setDuration(500); //1000
        enterSlide.setInterpolator(new DecelerateInterpolator(3));
        enterSlide.setSlideEdge(Gravity.END);
        next.setEnterTransition(enterSlide);

        Slide returnSlide = new Slide();
        returnSlide.setDuration(250); //500
        returnSlide.setStartDelay(100); //200
        returnSlide.setInterpolator(new DecelerateInterpolator(2));
        returnSlide.setSlideEdge(Gravity.END);
        next.setReturnTransition(returnSlide);
    }

    public static int screenWidth() {
        return Resources.getSystem().getDisplayMetrics().widthPixels;
    }

    public static float screenDensity() {
        return Resources.getSystem().getDisplayMetrics().density;
    }

    public static Typeface getTypeface(Context context) {
        if (mTypeface == null) {
//            mTypeface = Typeface.createFromAsset(context.getAssets(), AppConstants.FONTAWESOME);
            mTypeface = Typeface.createFromAsset(context.getAssets(), AppConstants.EALING);
        }

        return mTypeface;
    }

    public static void markAsIconContainer(Context context, View v) {
        if (v instanceof ViewGroup) {
            ViewGroup vg = (ViewGroup) v;
            for (int i = 0; i < vg.getChildCount(); i++) {
                View child = vg.getChildAt(i);
                markAsIconContainer(context, child);
            }
        } else if (v instanceof TextView) {
            ((TextView) v).setTypeface(getTypeface(context));
        }
    }

}

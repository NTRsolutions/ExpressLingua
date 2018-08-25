package com.neosolusi.expresslingua.util;

import android.util.Log;

import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Reading;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmResults;

public class ExampleGenerator {

    public static List<HashMap<String, String>> getSamples(Dictionary dictionary) {
        Realm mRealm = Realm.getDefaultInstance();
        RealmResults<Reading> readings = findReadings(dictionary);
        List<HashMap<String, String>> listSample = new ArrayList<>();

        for (Reading reading : readings) {
            if (reading.getTranslation() == null || reading.getTranslation().trim().isEmpty()) continue;

            HashMap<String, String> sampleMap;
            String[] splitReading = reading.getSentence().split("[.]");

            if (splitReading.length > 1) {
                sampleMap = findSampleMultiSentences(dictionary, reading);
            } else {
                sampleMap = findSampleSingleSentence(dictionary, reading);
            }

            if (sampleMap.size() == 0) continue;

            listSample.add(sampleMap);

            if (listSample.size() == 3) break;
        }

        mRealm.close();
        return listSample;
    }

    private static HashMap<String, String> findSampleMultiSentences(Dictionary dictionary, Reading reading) {
        HashMap<String, String> sampleMap = new HashMap<>();
        String[] splitReading = reading.getSentence().split("[.]");
        String[] splitReadingTranslate = reading.getTranslation().split("[.]");
        int i = 0;
        for (String findText : splitReading) {
            String card = dictionary.getWord().toLowerCase();
            String sentence = findText.toLowerCase();
            if (sentence.matches(".*(?<=\\W" + card + "|^" + card + ")(?=\\W|" + card + "$).*")) {
                try {
                    sampleMap.put("word", dictionary.getWord());
                    sampleMap.put("translation", dictionary.getTranslation());
                    sampleMap.put("samples", findText);
                    sampleMap.put("sample_translation", splitReadingTranslate[i]);

                    if (isDictionaryTranslateFoundOnReading(dictionary, splitReadingTranslate[i])) {
                        sampleMap.put("exists", "Ya");
                        break;
                    } else {
                        sampleMap.put("exists", "Tidak");
                    }
                } catch (ArrayIndexOutOfBoundsException e) {
                    Log.e("ExampleGenerator", e.getMessage());
                    sampleMap.put("sample_translation", reading.getTranslation());
                    sampleMap.put("exists", "Jumlah kalimat tidak sama");
                }
            }
            i++;
        }
        return sampleMap;
    }

    private static HashMap<String, String> findSampleSingleSentence(Dictionary dictionary, Reading reading) {
        HashMap<String, String> sampleMap = new HashMap<>();
        String card = dictionary.getWord().toLowerCase();
        String sentence = reading.getSentence().toLowerCase();
        if (sentence.matches(".*(?<=\\W" + card + "|^" + card + ")(?=\\W|" + card + "$).*")) {
            sampleMap.put("word", dictionary.getWord());
            sampleMap.put("translation", dictionary.getTranslation());
            sampleMap.put("samples", reading.getSentence());
            sampleMap.put("sample_translation", reading.getTranslation());

            if (isDictionaryTranslateFoundOnReading(dictionary, reading.getTranslation())) {
                sampleMap.put("exists", "Ya");
            } else {
                sampleMap.put("exists", "Tidak");
            }
        }
        return sampleMap;
    }

    private static boolean isDictionaryTranslateFoundOnReading(Dictionary dictionary, String readingTranslate) {
        String[] splitDictionaryTranslation = dictionary.getTranslation().split("[,]");
        for (String findText : splitDictionaryTranslation) {
            String word = findText.trim().toLowerCase().replace("(", "").replace(")", "");
            if (readingTranslate.toLowerCase().matches(".*(?<=\\W" + word + "|^" + word + ")(?=\\W|" + word + "$).*")) {
                return true;
            }
        }
        return false;
    }

    private static RealmResults<Reading> findReadings(Dictionary dictionary) {
        Realm mRealm = Realm.getDefaultInstance();
        return mRealm.where(Reading.class)
                .contains("sentence", " " + dictionary.getWord() + " ", Case.INSENSITIVE).or()
                .contains("sentence", " " + dictionary.getWord(), Case.INSENSITIVE).or()
                .contains("sentence", dictionary.getWord() + " ", Case.INSENSITIVE)
                .findAll();
    }

}

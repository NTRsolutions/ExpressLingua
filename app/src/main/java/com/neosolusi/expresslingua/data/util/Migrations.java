package com.neosolusi.expresslingua.data.util;

import java.util.Date;

import io.realm.DynamicRealm;
import io.realm.RealmMigration;
import io.realm.RealmObjectSchema;
import io.realm.RealmSchema;

public class Migrations implements RealmMigration {
    @Override public void migrate(DynamicRealm realm, long oldVersion, long newVersion) {
        final RealmSchema schema = realm.getSchema();

        if (oldVersion == 7) {
            // *******************************************************************************
            // Create Group schema
            // *******************************************************************************
            RealmObjectSchema groupSchema = schema.create("Group");
            groupSchema.addField("translate", int.class);

            // *******************************************************************************
            // Create Group schema
            // *******************************************************************************
            RealmObjectSchema flashcardSchema = schema.create("Flashcard");
            flashcardSchema.addField("read_repeat", int.class);
        }

        if (oldVersion == 6) {
            // *******************************************************************************
            // Create ChallengeHard schema
            // *******************************************************************************
            RealmObjectSchema challengeHardSchema = schema.create("ChallengeHard");
            challengeHardSchema.addField("id", long.class);
            challengeHardSchema.addField("reference", long.class);
            challengeHardSchema.addField("category", String.class);
            challengeHardSchema.addField("seen", boolean.class);
            challengeHardSchema.addField("skip", boolean.class);
            challengeHardSchema.addField("correct", boolean.class);
            challengeHardSchema.addField("selected", int.class);
            challengeHardSchema.addField("datecreated", Date.class);
            challengeHardSchema.addField("datemodified", Date.class);
            challengeHardSchema.addPrimaryKey("id");

            // *******************************************************************************
            // Migrate Reading
            // *******************************************************************************
            RealmObjectSchema readingSchema = schema.get("Reading");
            readingSchema.addField("kal_panjang", int.class);

            // *******************************************************************************
            // Migrate Reading
            // *******************************************************************************
            RealmObjectSchema userSchema = schema.get("User");
            userSchema.addField("avatar", String.class);
        }

        if (oldVersion == 5) {
            // *******************************************************************************
            // Migrate Group
            // *******************************************************************************
            RealmObjectSchema groupSchema = schema.get("Group");
            groupSchema.addField("privacy", int.class);
            groupSchema.addField("datecreated", Date.class);
            groupSchema.addField("datemodified", Date.class);

            // *******************************************************************************
            // Migrate Member
            // *******************************************************************************
            RealmObjectSchema memberSchema = schema.get("Member");
            memberSchema.addField("approved", int.class);
            memberSchema.addField("permission", int.class);
            memberSchema.addField("uploaded", boolean.class);
            memberSchema.addField("datecreated", Date.class);
            memberSchema.addField("datemodified", Date.class);
        }

        if (oldVersion == 4) {
            // *******************************************************************************
            // Migrate Member
            // *******************************************************************************
            RealmObjectSchema memberSchema = schema.get("Member");
            memberSchema.addField("new_user_id", String.class)
                    .transform(obj -> obj.setString("new_user_id", ""))
                    .removeField("user_id")
                    .renameField("new_user_id", "user_id");
        }

        if (oldVersion == 3) {
            // *******************************************************************************
            // Migrate ReadingInfo
            // *******************************************************************************
            RealmObjectSchema readingInfoSchema = schema.get("ReadingInfo");
            readingInfoSchema.addField("sentences_count", int.class);
            readingInfoSchema.addField("words_count", int.class);

            // *******************************************************************************
            // Migrate Contact
            // *******************************************************************************
            RealmObjectSchema contactSchema = schema.get("Contact");
            contactSchema.addField("id", long.class);
            contactSchema.addField("phone", String.class);
            contactSchema.addField("name", String.class);
            contactSchema.addField("imageUrl", String.class);
            contactSchema.addField("isActive", boolean.class);
            contactSchema.addField("lastUpdate", Date.class);
            contactSchema.addPrimaryKey("id");

            // *******************************************************************************
            // Migrate ReadingInfoMeta
            // *******************************************************************************
            RealmObjectSchema readingInfoMetaSchema = schema.get("ReadingInfoMeta");
            readingInfoMetaSchema.addField("id", long.class);
            readingInfoMetaSchema.addField("menu_id", long.class);
            readingInfoMetaSchema.addField("wordCount", int.class);
            readingInfoMetaSchema.addField("wordMarked", int.class);
            readingInfoMetaSchema.addField("sentenceCount", int.class);
            readingInfoMetaSchema.addField("sentenceMarked", int.class);
            readingInfoMetaSchema.addPrimaryKey("id");
        }

        if (oldVersion == 2) {
            // *******************************************************************************
            // Migrate Group
            // *******************************************************************************
            RealmObjectSchema groupSchema = schema.get("Group");
            groupSchema.addField("id", long.class);
            groupSchema.addField("nama", String.class);
            groupSchema.addField("description", String.class);
            groupSchema.addField("url", String.class);
            groupSchema.addField("admin", String.class);
            groupSchema.addField("member_count", int.class);
            groupSchema.addPrimaryKey("id");

            // *******************************************************************************
            // Migrate Member
            // *******************************************************************************
            RealmObjectSchema memberSchema = schema.get("Member");
            memberSchema.addField("id", long.class);
            memberSchema.addField("group_id", long.class);
            memberSchema.addField("user_id", long.class);
            memberSchema.addField("url", String.class);
            memberSchema.addPrimaryKey("id");
        }

        if (oldVersion == 1) {
            // *******************************************************************************
            // Migrate Dictionary
            // *******************************************************************************
            RealmObjectSchema dictionarySchema = schema.get("Dictionary");
            dictionarySchema.addField("local_word", int.class);
        }

        if (oldVersion == 0) {
            // *******************************************************************************
            // Migrate User
            // *******************************************************************************
            RealmObjectSchema userSchema = schema.get("User");
            userSchema.addField("manufacture", String.class);
            userSchema.addField("api_version", String.class);
            userSchema.addField("app_version", String.class);

            // *******************************************************************************
            // Migrate Reading
            // *******************************************************************************
            RealmObjectSchema readingSchema = schema.get("Reading");
            readingSchema.addField("sec", Integer.class);
            readingSchema.addField("bookmarked", boolean.class);
            readingSchema.addField("datecreated", Date.class);
            readingSchema.addField("datemodified", Date.class);
            readingSchema.addField("start_duration", String.class);
            readingSchema.addField("end_duration", String.class);

            // *******************************************************************************
            // Migrate Flashcard
            // *******************************************************************************
            RealmObjectSchema flashcardSchema = schema.get("Flashcard");
            flashcardSchema.removeField("actor");
            flashcardSchema.removeField("definition");
            flashcardSchema.removeField("audio");
            flashcardSchema.removeField("picture");
            flashcardSchema.removeField("notes");
            flashcardSchema.removeField("file_id");
            flashcardSchema.addField("reference", long.class);
            flashcardSchema.addField("easy_counter", int.class);

            // *******************************************************************************
            // Migrate Episode
            // *******************************************************************************
            RealmObjectSchema episodeSchema = schema.get("Episode");
            episodeSchema.addField("new_episode_id", long.class)
                    .transform(obj -> obj.setLong("new_episode_id", obj.getLong("episode_id")))
                    .removeField("episode_id")
                    .renameField("new_episode_id", "episode_id");

            episodeSchema.removePrimaryKey();
            episodeSchema.addPrimaryKey("episode_id");

            episodeSchema.addField("datecreated", Date.class);
            episodeSchema.addField("datemodified", Date.class);

            episodeSchema.removeField("id");

            // *******************************************************************************
            // Migrate ReadingInfo
            // *******************************************************************************
            RealmObjectSchema readingInfoSchema = schema.get("ReadingInfo");
            readingInfoSchema.addField("new_menu_id", long.class)
                    .transform(obj -> obj.setLong("new_menu_id", obj.getLong("menu_id")))
                    .removeField("menu_id")
                    .renameField("new_menu_id", "menu_id");

            readingInfoSchema.removePrimaryKey();
            readingInfoSchema.addPrimaryKey("menu_id");

            readingInfoSchema.addField("datecreated", Date.class);
            readingInfoSchema.addField("datemodified", Date.class);

            readingInfoSchema.removeField("id");

            // *******************************************************************************
            // Migrate Notification
            // *******************************************************************************
            RealmObjectSchema notificationSchema = schema.get("Notification");
            notificationSchema.addPrimaryKey("id");

            // *******************************************************************************
            // Create Dictionary schema
            // *******************************************************************************
            RealmObjectSchema dictionarySchema = schema.create("Dictionary");
            dictionarySchema.addField("id", long.class);
            dictionarySchema.addField("word", String.class);
            dictionarySchema.addField("translation", String.class);
            dictionarySchema.addField("category", String.class);
            dictionarySchema.addField("definition", String.class);
            dictionarySchema.addField("audio", String.class);
            dictionarySchema.addField("picture", String.class);
            dictionarySchema.addField("notes", String.class);
            dictionarySchema.addField("datecreated", Date.class);
            dictionarySchema.addField("datemodified", Date.class);
            dictionarySchema.addPrimaryKey("id");

            // *******************************************************************************
            // Create Challenge schema
            // *******************************************************************************
            RealmObjectSchema challengeSchema = schema.create("Challenge");
            challengeSchema.addField("id", long.class);
            challengeSchema.addField("reference", long.class);
            challengeSchema.addField("category", String.class);
            challengeSchema.addField("seen", boolean.class);
            challengeSchema.addField("skip", boolean.class);
            challengeSchema.addField("correct", boolean.class);
            challengeSchema.addField("selected", int.class);
            challengeSchema.addField("datecreated", Date.class);
            challengeSchema.addField("datemodified", Date.class);
            challengeSchema.addPrimaryKey("id");
        }
    }
}

package com.neosolusi.expresslingua.data.network;

import com.neosolusi.expresslingua.data.entity.Challenge;
import com.neosolusi.expresslingua.data.entity.Contact;
import com.neosolusi.expresslingua.data.entity.Dictionary;
import com.neosolusi.expresslingua.data.entity.Episode;
import com.neosolusi.expresslingua.data.entity.Flashcard;
import com.neosolusi.expresslingua.data.entity.Group;
import com.neosolusi.expresslingua.data.entity.Member;
import com.neosolusi.expresslingua.data.entity.MemberProgress;
import com.neosolusi.expresslingua.data.entity.Notification;
import com.neosolusi.expresslingua.data.entity.Reading;
import com.neosolusi.expresslingua.data.entity.ReadingInfo;
import com.neosolusi.expresslingua.data.entity.ServiceConfig;
import com.neosolusi.expresslingua.data.entity.User;

import java.util.List;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Path;
import retrofit2.http.Query;
import retrofit2.http.Streaming;

public interface ExpressLinguaApi {

    @FormUrlEncoded @POST("api/user/register")
    Call<Wrapper<User>> register(
            @Field("userid") String userid,
            @Field("email") String email,
            @Field("password") String password,
            @Field("commercial_status") int commercial,
            @Field("cell_no") String phone,
            @Field("address") String address,
            @Field("city") String city,
            @Field("province") String province,
            @Field("country") String country,
            @Field("gps_latitude") double latitude,
            @Field("gps_longitude") double longitude);

    @FormUrlEncoded @POST("api/user/login")
    Call<Wrapper<User>> login(@Field("userid") String email, @Field("password") String password);

    @FormUrlEncoded @POST("api/user/update")
    Call<Wrapper<User>> updateUser(
            @Field("userid") String userid,
            @Field("gps_latitude") double lat,
            @Field("gps_longitude") double lng,
            @Field("manufacture") String manufacture,
            @Field("android_version") String api_version,
            @Field("apps_version") String app_version);

    @Multipart
    @POST("api/user/avatar")
    Call<Wrapper<String>> uploadUserImage(
            @Part MultipartBody.Part image,
            @Part("name") RequestBody name,
            @Part("userid") RequestBody userid);

    @FormUrlEncoded @POST("api/user/message_token")
    Call<Wrapper<String>> uploadMessageToken(
            @Field("userid") String userid,
            @Field("token") String token);

    @FormUrlEncoded @POST("api/user/search/phone")
    Call<Wrapper<User>> findUser(@Field("cell_no") String phone);

    @GET("api/reading_info/get_content")
    Call<WrapperList<ReadingInfo>> getReadingInfo(@Query("date") String date);

    @GET("api/readings/get_content")
    Call<WrapperList<Reading>> getReading(@Query("date") String date);

    @FormUrlEncoded
    @POST("api/readings/user/get_content")
    Call<WrapperList<Reading>> getReadingUser(@Field("userid") String userid, @Field("date") String date);

    @GET("api/dictionary/get_content")
    Call<WrapperList<Dictionary>> getDictionaries(@Query("date") String date);

    @GET("api/episode/get_content")
    Call<WrapperList<Episode>> getEpisodes(@Query("date") String date);

    @FormUrlEncoded @POST("api/flashcard/sync")
    Call<Wrapper<Flashcard>> uploadFlashcard(
            @Field("word") String word,
            @Field("translation") String translation,
            @Field("category") String category,
            @Field("definition") String definition,
            @Field("audio") String audio,
            @Field("picture") String picture,
            @Field("note") String note,
            @Field("mastering_level") int level,
            @Field("file_id") String file_id,
            @Field("datecreated") String datecreated,
            @Field("datemodified") String datemodified,
            @Field("userid") String userid,
            @Field("read") int already_read);

    @FormUrlEncoded @POST("api/flashcard/get_content")
    Call<WrapperList<Flashcard>> getFlashcardUser(@Field("userid") String userid, @Field("date") String date);

    @FormUrlEncoded @POST("api/readings/user/sync")
    Call<Wrapper<Reading>> uploadReading(
            @Field("file_id") int fileId,
            @Field("sequence_no") int sequence,
            @Field("sentence") String sentence,
            @Field("mastering_level") int level,
            @Field("userid") String userid);

    @Streaming
    @GET("public/audio/{file}")
    Call<ResponseBody> downloadAudioFile(@Path("file") String fileName);

    @GET("api/notification/get_content")
    Call<WrapperList<Notification>> getNotification();

    @GET("api/user/configuration")
    Call<WrapperList<ServiceConfig>> config();

    @FormUrlEncoded @POST("api/challenge/user/store")
    Call<Wrapper<Challenge>> uploadChallenge(
            @Field("userid") String userid,
            @Field("not_seen") int notSeen,
            @Field("skipped") int skipped,
            @Field("incorrect") int incorrect,
            @Field("correct") int correct,
            @Field("w_red") int w_red,
            @Field("w_orange") int w_orange,
            @Field("w_yellow") int w_yellow,
            @Field("w_green") int w_green,
            @Field("w_blue") int w_blue,
            @Field("s_red") int s_red,
            @Field("s_orange") int s_orange,
            @Field("s_yellow") int s_yellow,
            @Field("s_green") int s_green,
            @Field("s_blue") int s_blue);

    @POST("users/cobain")
    Call<WrapperList<Contact>> uploadContacts(@Body List<Contact> contacts);

    @GET("api/group/show")
    Call<WrapperList<Group>> getGroups();

    @FormUrlEncoded
    @POST("api/group/create")
    Call<Wrapper<Group>> createGroup(
            @Field("groupName") String name,
            @Field("groupOwner") String owner,
            @Field("groupPrivacy") int privacy,
            @Field("groupTranslate") int translate,
            @Field("remarks") String remarks,
            @Field("img_url") String url);

    @FormUrlEncoded
    @POST("api/group/edit")
    Call<Wrapper<Group>> updateGroup(
            @Field("groupId") long id,
            @Field("groupName") String name,
            @Field("groupOwner") String owner,
            @Field("groupPrivacy") int privacy,
            @Field("groupTranslate") int translate,
            @Field("remarks") String remarks,
            @Field("img_url") String url);

    @Multipart
    @POST("group_mobile/do_upload")
    Call<Wrapper<String>> uploadGroupImage(
            @Part MultipartBody.Part image,
            @Part("name") RequestBody name,
            @Part("groupId") RequestBody groupId);

    @GET("api/group/member/show")
    Call<WrapperList<Member>> getMembers();

    @GET("api/group/member/progress")
    Call<Wrapper<MemberProgress>> getMemberProgress(@Query("id") String memberId);

    @FormUrlEncoded
    @POST("api/group/member/create")
    Call<Wrapper<Member>> createMember(
            @Field("groupId") long groupId,
            @Field("userid") String userId,
            @Field("approved") int approved,
            @Field("permission") int permission);
}

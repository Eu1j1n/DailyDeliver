package com.example.dailydeliver;

import com.example.dailydeliver.Chatting.ChatMessage;
import com.example.dailydeliver.Chatting.Message;
import com.example.dailydeliver.Fragment.HomeData;
import com.example.dailydeliver.Fragment.PostDetailData;
import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonPrimitive;

import java.util.List;
import java.util.Map;

import okhttp3.MultipartBody;
import okhttp3.RequestBody;
import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.Field;
import retrofit2.http.FieldMap;
import retrofit2.http.FormUrlEncoded;
import retrofit2.http.GET;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;
import retrofit2.http.Query;

public interface ApiService {


    @FormUrlEncoded
    @POST("sendChatRoomList.php")
    Call<JsonArray> getChatRoomList(
            @Field("userID") String receivedID
    );

    @FormUrlEncoded
    @POST("updateBasicImage.php")
    Call<Void> updateBasicImage(@Field("receiveID") String receiveID);

    @GET("getToken.php")
    Call<JsonObject> getToken(@Query("orderPeople") String orderPeople);


    @POST("receiveFCM.php")
    @FormUrlEncoded
    Call<ResponseBody> sendFCMToken(
            @Field("userID") String userID,
            @Field("token") String token
    );


    @GET("sendPostDetail.php")
    Call<List<PostDetailData>> getPostDetail(
            @Query("title") String title,
            @Query("location") String location,
            @Query("price") String price,
            @Query("userName") String userName
    );


    @GET("send_post.php")
    Call<List<HomeData>> getPosts();

    @Multipart
    @POST("uploadPostImage.php")
        // 서버 업로드 API 엔드포인트
    Call<ResponseBody> uploadPostImage(
            @Part MultipartBody.Part imagePart,
            @Part("receiveID") RequestBody userId
    );

    @FormUrlEncoded
    @POST("insertPost.php")
    Call<Void> sendPost(
            @FieldMap Map<String, String> imageNames,
            @Field("title") String title,
            @Field("location") String location,
            @Field("sendTime") String sendTime,
            @Field("price") String price,
            @Field("userName") String userName,
            @Field("description") String description,
            @Field("latitude") double latitude,
            @Field("longitude") double longitude
    );


    @POST("lastMessageTime.php")
    @FormUrlEncoded
    Call<ResponseBody> getLastMessageTime(
            @Field("roomName") String roomName
    );

    @POST("lastMessage.php")
    @FormUrlEncoded
    Call<ResponseBody> getLastMessage(
            @Field("roomName") String roomName
    );

    @POST("unReadMessageCount.php")
    @FormUrlEncoded
    Call<Map<String, Integer>> getUnreadMessageCount(
            @Field("receivedID") String receivedID
    );

    @POST("readStatus.php")
    @FormUrlEncoded
    Call<Void> updateReadStatus(@Field("roomName") String roomName);


    @POST("otherReadStatus.php")
    @FormUrlEncoded
    Call<ResponseBody> otherUpdateReadStatus(@Field("messageID") String messageID);

    @POST("checkReadStatus.php")
    @FormUrlEncoded
    Call<Integer> checkMessageReadStatus(@Field("messageID") String messageID);


    @FormUrlEncoded
    @POST("check_id.php")
    Call<ResponseBody> checkIdAvailability(
            @Field("receiveID") String receiveID);

    @FormUrlEncoded
    @POST("login.php")
        //
    Call<ResponseBody> loginUser(
            @Field("receiveID") String receiveID,
            @Field("receivePassword") String receivePassword);

    @FormUrlEncoded
    @POST("getImageFileName.php")
    Call<ImageResponse> getImageFileName(@Field("receiveID") String receiveID);

    @Multipart
    @POST("editImage.php")
        // 서버 업로드 API 엔드포인트
    Call<ResponseBody> editImage(
            @Part MultipartBody.Part imagePart,
            @Part("receiveID") RequestBody userId
    );


    @FormUrlEncoded
    @POST("register.php")
        // 실제 서버의 PHP 스크립트 경로
    Call<ResponseBody> registerUser(

            @Field("name") String name,
            @Field("password") String password,
            @Field("address") String address,
            @Field("phoneNumber") String phoneNumber,
            @Field("bank") String bank,
            @Field("account") String account,
            @Field("receiveID") String receiveID);

    @FormUrlEncoded
    @POST("receive_chat.php")
    Call<Void> sendChatMessage(@Field("saveMessage") String saveMessage);

    @GET("send_chat.php")
    Call<List<ChatMessage>> getChatHistory(@Query("roomName") String roomName);




    @Multipart
    @POST("uploadProfileImage.php")
        // 서버 업로드 API 엔드포인트
    Call<ResponseBody> uploadImage(
            @Part MultipartBody.Part imagePart,
            @Part("receiveID") RequestBody userId
    );

    @Multipart
    @POST("uploadChatImage.php")
    Call<ResponseBody> uploadChatImage(
            @Part("folderName") RequestBody folderName,
            @Part MultipartBody.Part image,
            @Part("fileName") RequestBody fileName
    );


}


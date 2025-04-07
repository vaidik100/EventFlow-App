package com.example.eventflow.network;

import okhttp3.MultipartBody;
import retrofit2.Call;
import retrofit2.http.Headers;
import retrofit2.http.Multipart;
import retrofit2.http.POST;
import retrofit2.http.Part;

public interface ImgurApiService {

    @Headers({
            "Authorization: Client-ID 54320005be28046"
    })
    @Multipart
    @POST("image")
    Call<ImgurResponse> uploadImage(@Part MultipartBody.Part image);
}

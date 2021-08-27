
package com.example.awesomephotosapp;
import java.util.List;

import okhttp3.ResponseBody;
import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Header;
import retrofit2.http.Headers;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface UserService {

    //get photo list
    @GET("photos")
    Call<List<ModelClass>> getListOfPhotos(@Header("Authorization")String clientId,
                                           @Query("per_page") String per_page);

    @GET("{url}")
    Call<ResponseBody> getPhotos(@Path("url")String url);

}

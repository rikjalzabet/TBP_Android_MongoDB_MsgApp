package hr.foi.msgappmongodb.DataClass

import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.*
import retrofit2.Response

interface ApiService {
    @GET("users/{username}")
    suspend fun getUserByUsername(@Path("username") username: String): User?

    @GET("users")
    suspend fun getAllUsers(): List<User>

    @GET("messages/{username1}/{username2}")
    suspend fun getMessagesBetweenUsers(
        @Path("username1") username1: String?,
        @Path("username2") username2: String?
    ): List<Message>

    @POST("messages")
    suspend fun sendMessage(
        @Field("sender") sender: String?,
        @Field("receiver") receiver: String?,
        @Field("message") message: String?,
        @Field("type") type: String,
        @Field("imageBase64") imageBase64: String? = null
    ): Response<Message>

    @Multipart
    @POST("upload-video")
    suspend fun uploadVideo(
        @Part video: MultipartBody.Part
    ): Response<ResponseBody>
}

fun getApiService(): ApiService {
    val retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:3000/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    return retrofit.create(ApiService::class.java)
}

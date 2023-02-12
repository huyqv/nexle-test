package wee.digital.sample.data

import com.google.gson.JsonObject
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.POST
import wee.digital.sample.data.model.User

@JvmSuppressWildcards
interface ApiService {

    @POST("auth/signin")
    suspend fun signIn(@Body body: JsonObject): User

    @POST("auth/signup")
    suspend fun signUp(@Body body: JsonObject): User

    @POST("auth/logout")
    suspend fun signOut(@Header("Authorization") token: String): Response<ResponseBody?>
}
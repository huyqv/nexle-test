package wee.digital.sample.data.model

import com.google.gson.annotations.SerializedName


class User(
    @SerializedName("_id")
    val id: String,

    @SerializedName("firstName")
    val firstName: String,

    @SerializedName("lastName")
    val lastName: String,

    @SerializedName("displayName")
    val displayName: String,

    @SerializedName("email")
    val email: String,

    @SerializedName("token")
    val token: String,

    @SerializedName("refreshToken")
    val refreshToken: String,
)
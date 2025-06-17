package com.example.geowarning.User

import com.google.gson.annotations.SerializedName

data class User(
    @SerializedName("email") val email: String,
    @SerializedName("password") val password: String,
    @SerializedName("position") val position: String? = null
)
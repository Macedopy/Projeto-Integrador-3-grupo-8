package com.example.geowarning.network
import com.example.geowarning.User.User
import com.example.geowarning.Location.LocationData
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query


interface ApiService {

    @GET("listLocationsRelatorio")
    fun getLocations(): Call<List<LocationData>>

}
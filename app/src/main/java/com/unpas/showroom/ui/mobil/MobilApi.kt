package com.unpas.showroom.ui.mobil

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MobilApi {
    @POST("mobil")
    suspend fun addMobil(@Body mobilData: MobilData): Response<ResponseBody>
}
package com.unpas.showroom.ui.motor

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface MotorApi {
    @POST("sepeda-motor")
    suspend fun addMotor(@Body motorData: MotorData): Response<ResponseBody>
}
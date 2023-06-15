package com.unpas.showroom.ui.promo

import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface PromoApi {
    @POST("promo")
    suspend fun addPromo(@Body promoData: PromoData): Response<ResponseBody>
}
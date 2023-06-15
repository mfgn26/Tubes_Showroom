package com.unpas.showroom.ui.promo

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface PromoDao {
    @Query("SELECT * FROM PromoData")
    suspend fun getAllPromos(): List<PromoData>

    @Insert
    suspend fun insertPromo(promo: PromoData)

    @Update
    suspend fun updatePromo(promo: PromoData)

    @Delete
    suspend fun deletePromo(promo: PromoData)
}
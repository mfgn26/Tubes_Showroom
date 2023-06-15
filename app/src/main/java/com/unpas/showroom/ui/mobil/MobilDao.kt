package com.unpas.showroom.ui.mobil

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface MobilDao {
    @Query("SELECT * FROM MobilData")
    suspend fun getAllMobils(): List<MobilData>

    @Insert
    suspend fun insertMobil(mobil: MobilData)

    @Update
    suspend fun updateMobil(mobil: MobilData)

    @Delete
    suspend fun deleteMobil(mobil: MobilData)
}
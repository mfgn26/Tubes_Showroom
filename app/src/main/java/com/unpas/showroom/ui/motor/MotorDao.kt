package com.unpas.showroom.ui.motor

import androidx.room.Dao
import androidx.room.Query
import androidx.room.Insert
import androidx.room.Update
import androidx.room.Delete

@Dao
interface MotorDao {
    @Query("SELECT * FROM MotorData")
    suspend fun getAllMotors(): List<MotorData>

    @Insert
    suspend fun insertMotor(motor: MotorData)

    @Update
    suspend fun updateMotor(motor: MotorData)

    @Delete
    suspend fun deleteMotor(motor: MotorData)
}
package com.unpas.showroom.ui.motor

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MotorData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val model: String,
    val warna: String,
    val kapasitas: Int,
    val tanggal_rilis: String,
    val harga: Int
)
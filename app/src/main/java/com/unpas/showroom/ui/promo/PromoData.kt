package com.unpas.showroom.ui.promo

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class PromoData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val model: String,
    val tanggal_awal: String,
    val tanggal_akhir: String,
    val persentase: Int
)
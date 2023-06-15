package com.unpas.showroom.ui.mobil

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity
data class MobilData(
    @PrimaryKey(autoGenerate = true)
    val id: Int,
    val merk: String,
    val model: String,
    val bahan_bakar: String,
    val dijual: Boolean,
    val deskripsi: String
) {
    enum class BahanBakar {
        Bensin,
        Solar,
        Listrik
    }
}
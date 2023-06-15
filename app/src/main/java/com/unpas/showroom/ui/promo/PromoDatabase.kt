package com.unpas.showroom.ui.promo

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [PromoData::class],
    version = 1
)
abstract class PromoDatabase : RoomDatabase(){

    abstract fun promoDao() : PromoDao

    companion object {

        @Volatile private var instance : PromoDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            PromoDatabase::class.java,
            "promo.db"
        ).build()

    }
}

package com.unpas.showroom.ui.mobil

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MobilData::class],
    version = 1
)
abstract class MobilDatabase : RoomDatabase(){

    abstract fun mobilDao() : MobilDao

    companion object {

        @Volatile private var instance : MobilDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            MobilDatabase::class.java,
            "mobil.db"
        ).build()

    }
}

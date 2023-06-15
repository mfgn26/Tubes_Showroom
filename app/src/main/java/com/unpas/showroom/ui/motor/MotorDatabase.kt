package com.unpas.showroom.ui.motor

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase

@Database(
    entities = [MotorData::class],
    version = 1
)
abstract class MotorDatabase : RoomDatabase(){

    abstract fun motorDao() : MotorDao

    companion object {

        @Volatile private var instance : MotorDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK){
            instance ?: buildDatabase(context).also {
                instance = it
            }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context.applicationContext,
            MotorDatabase::class.java,
            "motor.db"
        ).build()

    }
}

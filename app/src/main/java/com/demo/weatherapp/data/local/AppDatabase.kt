package com.demo.weatherapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import com.demo.weatherapp.data.local.dao.UserDao
import com.demo.weatherapp.data.local.dao.WeatherDao
import com.demo.weatherapp.data.local.entity.UserEntity
import com.demo.weatherapp.data.local.entity.WeatherRecordEntity

@Database(
    entities = [UserEntity::class, WeatherRecordEntity::class],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun weatherDao(): WeatherDao
}

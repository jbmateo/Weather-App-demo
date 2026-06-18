package com.demo.weatherapp.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.demo.weatherapp.data.local.entity.WeatherRecordEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WeatherDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(record: WeatherRecordEntity)

    @Query("SELECT * FROM weather_records ORDER BY fetchedAtMillis DESC")
    fun observeHistory(): Flow<List<WeatherRecordEntity>>
}

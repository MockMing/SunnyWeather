package com.example.sunnyweather.android.logic.dao

import android.content.Context
import androidx.core.content.edit
import com.example.sunnyweather.android.SunnyWeatherApplication
import com.example.sunnyweather.android.logic.model.Place
import com.google.gson.Gson

object PlaceDao {

    // 保存信息
    fun savePlace(place: Place) {
        sharedPreferences().edit {
            putString("place", Gson().toJson(place))
        }
    }

    // 获取保存的信息
    fun getSavedPlace(): Place {
        val placeJson = sharedPreferences().getString("place", "")
        return Gson().fromJson(placeJson, Place::class.java)
    }

    // 判断是否已存有数据
    fun isPlaceSaved() = sharedPreferences().contains("place")

    private fun sharedPreferences() = SunnyWeatherApplication.context.getSharedPreferences("sunny_weather", Context.MODE_PRIVATE)

}
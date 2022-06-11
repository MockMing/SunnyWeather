package com.example.sunnyweather.android.ui.weather

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModel
import com.example.sunnyweather.android.logic.Repository
import com.example.sunnyweather.android.logic.model.Location

class WeatherViewModel : ViewModel() {

    private val locationLiveData = MutableLiveData<Location>()

    var locationLng = ""

    var locationLat = ""

    var placeName = ""

    // 2.使用Transformations的switchMap()方法观察locationLiveData对象
    val weatherLiveData = Transformations.switchMap(locationLiveData) { location ->
        // 3.在转换函数中调用仓库层定义的refreshWeather()方法
        Repository.refreshWeather(location.lng, location.lat)
        // 4.仓库层返回的LiveData对象就可以转换成可供Activity观察的LiveData对象
    }

    // 1.定义refreshWeather()方法来刷新信息，传入经纬度并封装为Location对象，赋值给locationLiveData对象
    fun refreshWeather(lng: String, lat: String) {
        locationLiveData.value = Location(lng, lat)
    }

}
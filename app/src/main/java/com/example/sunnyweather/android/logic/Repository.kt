package com.example.sunnyweather.android.logic

import android.util.Log
import androidx.lifecycle.liveData
import com.example.sunnyweather.android.logic.dao.PlaceDao
import com.example.sunnyweather.android.logic.model.Place
import com.example.sunnyweather.android.logic.model.Weather
import com.example.sunnyweather.android.logic.network.SunnyWeatherNetwork
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import java.lang.Exception
import java.lang.RuntimeException
import kotlin.coroutines.CoroutineContext

// 仓库
object Repository {

    // 为了能将异步获取的数据以响应式编程的方式通知给上一层，通常会返回一个LiveData对象，在fire中实现返回

    // 搜索城市信息
    fun searchPlaces(query: String) = fire(Dispatchers.IO) {
        val placeResponse = SunnyWeatherNetwork.searchPlaces(query)
        if (placeResponse.status == "ok") {
            val places = placeResponse.places
            Result.success(places)
        } else {
            Result.failure(RuntimeException("response status is ${placeResponse.status}"))
        }
    }

    // 提供一个刷新天气信息方法，同时完成获取实时天气信息和未来天气信息
    // 线程参数类型指定为Dispatchers.IO，这样代码块中的所有代码都运行在子线程中
    fun refreshWeather(lng: String, lat: String) = fire(Dispatchers.IO) {

        coroutineScope {

            // 用async并发执行获取实时天气信息与获取未来天气信息的请求以提升运行效率
            val deferredRealtime = async {
                SunnyWeatherNetwork.getRealtimeWeather(lng, lat)
            }
            val deferredDaily = async {
                SunnyWeatherNetwork.getDailyWeather(lng, lat)
            }

            // 分别调用Deferred对象的await()确保这两个请求都成功响应再执行下一步
            val realtimeResponse = deferredRealtime.await()
            val dailyResponse = deferredDaily.await()

            if (realtimeResponse.status == "ok" && dailyResponse.status == "ok") {
                val weather = Weather(realtimeResponse.result.realtime, dailyResponse.result.daily)
                Result.success(weather)
            } else {
                Result.failure(
                    RuntimeException(
                        "realtime response status is ${realtimeResponse.status}" +
                                "daily response status is ${dailyResponse.status}"
                    )
                )
            }

        }

    }

    // 在统一入口函数中进行封装，只进行一次try catch处理
    // liveData()是lifecycle-livedata-ktx库提供的函数，它可以自动构建并返回一个LiveData对象，然后在它的代码块中提供一个挂起函数的上下文，这样我们就可以在liveData()函数的代码块中调用任意的挂起函数了。
    // 在liveData()函数的代码块中，我们是拥有挂起函数上下文的，当回调到Lambda表达式中，代码就没有挂起函数上下文了，但实际上Lambda表达式中的代码一定也是在挂起函数中运行的。
    // 为了解决这个问题，我们需要在函数类型前声明一个suspend关键字，以表示所有传入的Lambda表达式中的代码也是拥有挂起函数上下文的。
    private fun <T> fire(
        context: CoroutineContext,
        block: suspend () -> Result<T>
    ) = liveData<Result<T>>(context) {

        val result = try {
            block()
        } catch (e: Exception) {
            Result.failure<T>(e)
        }
        // emit()方法类似于调用LiveData的setValue()方法来通知数据变化，只不过这里我们无法直接取得返回的LiveData对象，所以lifecycle-livedata-ktx库提供了这样一个替代方法。
        emit(result)

    }

    // 用SharedPreference储存城市信息，这里的写法是简化的不标准写法
    fun savePlace(place: Place) = PlaceDao.savePlace(place)

    fun getSavedPlace() = PlaceDao.getSavedPlace()

    fun isPlaceSaved() = PlaceDao.isPlaceSaved()



}
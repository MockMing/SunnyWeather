package com.example.sunnyweather.android

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context

class SunnyWeatherApplication : Application() {

    companion object {
        // 方便在项目中任何位置调用Context,如：SunnyWeatherApplication.context
        // @SuppressLint("StaticFieldLeak")用来忽略内存泄漏风险
        @SuppressLint("StaticFieldLeak")
        lateinit var context: Context

        // 彩云天气开发者平台的令牌，方便调用
        const val TOKEN = "4mQjq95lzvpiowIo"
    }

    override fun onCreate() {
        super.onCreate()
        // 以静态变量的形式获取Context对象
        context = applicationContext
    }


}
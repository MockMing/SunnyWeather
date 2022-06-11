package com.example.sunnyweather.android.logic.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

// 简化获取Service接口动态代理对象的过程
object ServiceCreator {

    private const val BASE_URL = "https://api.caiyunapp.com/"

    // 构建Retrofit对象
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    // 提供一个外部可见的create方法，获取动态代理对象时传入相应的Service接口即可
    fun <T> create(serviceClass: Class<T>): T = retrofit.create(serviceClass)

    // 使用泛型实化使调用接口更简便
    inline fun <reified T> create(): T = create(T::class.java)

}
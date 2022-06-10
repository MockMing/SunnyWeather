package com.example.sunnyweather.android.logic.model

import com.google.gson.annotations.SerializedName

// 地点响应
data class PlaceResponse (
    // 请求的状态，ok即为成功
    val status: String,
    // 地点数组，包含了数个与查询的关键字关系度较高的地区信息
    val places: List<Place>
        )

// 地点
data class Place (
    // 名称，如：北京市
    val name: String,
    // 经纬度
    val location: Location,
    // 详细地址，如：中国 北京市 丰台区 莲花池东路118号
    // 由于JSON中一些字段的命名可能与Kotlin的命名规范不太一致，因此这里使用了@SerializedName注解的方式，来让JSON字段和Kotlin字段之间建立映射关系
    @SerializedName("formatted_address")
    val address: String
        )

// 位置
data class Location (
    // 经度
    val lng: String,
    // 纬度
    val lat: String
        )
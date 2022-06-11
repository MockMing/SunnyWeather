package com.example.sunnyweather.android.ui.weather

import android.content.Context
import android.graphics.Color
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.sunnyweather.android.R
import com.example.sunnyweather.android.databinding.ActivityWeatherBinding
import com.example.sunnyweather.android.logic.model.Weather
import com.example.sunnyweather.android.logic.model.getSky
import java.text.SimpleDateFormat
import java.util.*

class WeatherActivity : AppCompatActivity() {

    // 注意这里不要加private，不然Adapter调用不到
    lateinit var activityWeatherBinding: ActivityWeatherBinding

    // 这里也不要加private，不然Adapter调用不到
    val viewModel by lazy {
        ViewModelProvider(this).get(WeatherViewModel::class.java)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_weather)

        activityWeatherBinding = ActivityWeatherBinding.inflate(layoutInflater)
        setContentView(activityWeatherBinding.root)

        // 透明状态栏实现
        // 关键代码,沉浸
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 设置专栏栏和导航栏的底色，透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            window.navigationBarDividerColor = Color.TRANSPARENT
        }

        // 设置沉浸后专栏栏和导航字体的颜色 true=黑色 false=白色
        ViewCompat.getWindowInsetsController(findViewById<FrameLayout>(android.R.id.content))?.let { controller ->
            controller.isAppearanceLightStatusBars = false
            controller.isAppearanceLightNavigationBars = false
        }





        // 从Intent中取出经纬度坐标和地区名称，并赋值到WeatherViewModel的相应变量中
        if(viewModel.locationLng.isEmpty()) {
            viewModel.locationLng = intent.getStringExtra("location_lng") ?: ""
        }

        if(viewModel.locationLat.isEmpty()) {
            viewModel.locationLat = intent.getStringExtra("location_lat") ?: ""
        }

        if(viewModel.placeName.isEmpty()) {
            viewModel.placeName = intent.getStringExtra("place_name") ?: ""
        }

        // 对weatherLiveData对象进行观察，当获取到服务器返回的天气数据时，就调用showWeatherInfo()方法进行解析与展示
        viewModel.weatherLiveData.observe(this, Observer { result ->
            val weather = result.getOrNull()

            if (weather != null) {
                showWeatherInfo(weather)
            } else {
                Toast.makeText(this, "无法成功获取天气信息", Toast.LENGTH_SHORT).show()
                result.exceptionOrNull()?.printStackTrace()
            }

            // 刷新完成后将刷新进度条设置为不可见
            activityWeatherBinding.swipeRefresh.isRefreshing = false
        })

        // 给刷新进度条设置颜色
        activityWeatherBinding.swipeRefresh.setColorSchemeResources(R.color.purple_500)

        refreshWeather()

        activityWeatherBinding.swipeRefresh.setOnRefreshListener {
            refreshWeather()
        }

        // 滑动菜单的逻辑处理
        // 点击切换城市按钮时显示滑动菜单
        activityWeatherBinding.nowLayoutInclude.navBtn.setOnClickListener {
            activityWeatherBinding.drawerLayout.openDrawer(GravityCompat.START)
        }

        activityWeatherBinding.drawerLayout.addDrawerListener(object : DrawerLayout.DrawerListener {

            override fun onDrawerStateChanged(newState: Int) { }

            override fun onDrawerSlide(drawerView: View, slideOffset: Float) { }

            override fun onDrawerOpened(drawerView: View) { }

            // 当滑动菜单被隐藏的时候，同时也要隐藏输入法
            override fun onDrawerClosed(drawerView: View) {
                val manager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                manager.hideSoftInputFromWindow(drawerView.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
            }

        })



    }

    // 刷新天气的方法
    // 同样，这里也不要加private，不然Adapter调用不到
    fun refreshWeather() {

        // 调用WeatherViewModel的refreshWeather()方法来执行一次刷新天气的请求
        viewModel.refreshWeather(viewModel.locationLng, viewModel.locationLat)

        // 刷新时将刷新进度条设置为可见
        activityWeatherBinding.swipeRefresh.isRefreshing = true

    }

    // 从Weather对象中获取数据，然后显示到相应的控件上
    private fun showWeatherInfo(weather: Weather) {
        activityWeatherBinding.nowLayoutInclude.placeName.text = viewModel.placeName
        val realtime = weather.realtime
        val daily = weather.daily

        // 填充now.xml布局中的数据
        val currentTempText = "${realtime.temperature.toInt()}℃"
        activityWeatherBinding.nowLayoutInclude.currentTemp.text = currentTempText
        activityWeatherBinding.nowLayoutInclude.currentSky.text = getSky(realtime.skycon).info
        val currentPM25Text = "空气指数 ${realtime.airQuality.aqi.chn.toInt()}"
        activityWeatherBinding.nowLayoutInclude.currentAQI.text = currentPM25Text
        activityWeatherBinding.nowLayoutInclude.nowLayout.setBackgroundResource(getSky(realtime.skycon).bg)

        // 填充forecast.xml布局中的数据
        // 先清空
        activityWeatherBinding.forecastLayoutInclude.forecastLayout.removeAllViews()

        val days = daily.skycon.size
        for (i in 0 until days) {
            val skycon = daily.skycon[i]
            val temperature = daily.temperature[i]
            val view = LayoutInflater.from(this).inflate(R.layout.forecast_item, activityWeatherBinding.forecastLayoutInclude.forecastLayout, false)
            val dateInfo = view.findViewById(R.id.dateInfo) as TextView
            val skyIcon = view.findViewById(R.id.skyIcon) as ImageView
            val skyInfo = view.findViewById(R.id.skyInfo) as TextView
            val temperatureInfo = view.findViewById(R.id.temperatureInfo) as TextView
            val simpleDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            dateInfo.text = simpleDateFormat.format(skycon.date)
            val sky = getSky(skycon.value)
            skyIcon.setImageResource(sky.icon)
            skyInfo.text = sky.info
            val tempText = "${temperature.min.toInt()} ~ ${temperature.max.toInt()} ℃"
            temperatureInfo.text = tempText
            activityWeatherBinding.forecastLayoutInclude.forecastLayout.addView(view)
        }

        // 填充life_index.xml布局中的数据
        val lifeIndex = daily.lifeIndex
        activityWeatherBinding.lifeIndexLayoutInclude.coldRiskText.text = lifeIndex.coldRisk[0].desc
        activityWeatherBinding.lifeIndexLayoutInclude.dressingText.text = lifeIndex.dressing[0].desc
        activityWeatherBinding.lifeIndexLayoutInclude.ultravioletText.text = lifeIndex.ultraviolet[0].desc
        activityWeatherBinding.lifeIndexLayoutInclude.carWashingText.text = lifeIndex.carWashing[0].desc

        // 将整体(ScrollView)设为可见
        activityWeatherBinding.weatherLayout.visibility = View.VISIBLE
    }

}
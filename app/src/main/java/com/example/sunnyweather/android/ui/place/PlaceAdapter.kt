package com.example.sunnyweather.android.ui.place

import android.app.Activity
import android.content.Intent
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.RecyclerView
import com.example.sunnyweather.android.R
import com.example.sunnyweather.android.SunnyWeatherApplication
import com.example.sunnyweather.android.databinding.ActivityWeatherBinding
import com.example.sunnyweather.android.databinding.FragmentPlaceBinding
import com.example.sunnyweather.android.databinding.PlaceItemBinding
import com.example.sunnyweather.android.logic.model.Place
import com.example.sunnyweather.android.ui.weather.WeatherActivity

class PlaceAdapter(private val fragment: PlaceFragment, private val placeList: List<Place>) : RecyclerView.Adapter<PlaceAdapter.ViewHolder>() {

    // 为了在onBindViewHolder中获取parent，在这里延迟定义一个参数存储parent参数
    private lateinit var saveParent: ViewGroup

    inner class ViewHolder(view: View, binding: PlaceItemBinding) : RecyclerView.ViewHolder(binding.root) {
        val placeName: TextView = binding.placeName
        val placeAddress: TextView = binding.placeAddress
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val binding = PlaceItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)

        val view = LayoutInflater.from(parent.context).inflate(R.layout.place_item, parent, false)

        // 将parent赋值给saveParent
        saveParent = parent

        // 在这里获取position只能得到-1 （原因未知）
//        val holder = ViewHolder(view, binding)
//        holder.itemView.setOnClickListener {
//            val position = holder.absoluteAdapterPosition
//
//            Log.d("CustomLog", "点击的项为：$position")
//
//            val place = placeList[position]
//            val intent = Intent(SunnyWeatherApplication.context, WeatherActivity::class.java).apply {
//                putExtra("location_lng", place.location.lng)
//                putExtra("location_lat", place.location.lat)
//                putExtra("place_name", place.name)
//            }
//            fragment.startActivity(intent)
//            fragment.activity?.finish()
//        }

        return ViewHolder(view, binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val place = placeList[position]
        holder.placeName.text = place.name
        holder.placeAddress.text = place.address

        // 从搜索城市界面跳转到天气界面
        // 把onCreateViewHolder中的点击事件挪到onBindViewHolder()中可以正常取到position的值
        holder.itemView.setOnClickListener {
            val clickedPosition = holder.absoluteAdapterPosition
            val clickedPlace = placeList[clickedPosition]
            val activity = fragment.activity

            // 如果是在WeatherActivity中，那么就关闭滑动菜单，给WeatherViewModel赋值新的经纬度坐标和地区名称，然后刷新城市的天气信息
            if (activity is WeatherActivity) {
                activity.activityWeatherBinding.drawerLayout.closeDrawers()
                activity.viewModel.locationLng = clickedPlace.location.lng
                activity.viewModel.locationLat = place.location.lat
                activity.viewModel.placeName = place.name
                activity.refreshWeather()
            }
            // 如果是在MainActivity中，就需要进行跳转
            else {
                val intent = Intent(SunnyWeatherApplication.context, WeatherActivity::class.java).apply {
                    putExtra("location_lng", clickedPlace.location.lng)
                    putExtra("location_lat", clickedPlace.location.lat)
                    putExtra("place_name", clickedPlace.name)
                }
                fragment.startActivity(intent)
                fragment.activity?.finish()
            }
            // 保存当前城市信息
            fragment.viewModel.savePlace(place)






        }
    }

    override fun getItemCount() = placeList.size

}
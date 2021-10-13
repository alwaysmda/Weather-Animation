package com.xodus.sample

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.xodus.weatheranimation.WeatherAnimation

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        WeatherAnimation(findViewById(R.id.main_clParent)).fall(200, 15F, 70, WeatherAnimation.FallItem.FallType.RAIN).start()
        WeatherAnimation(findViewById(R.id.main_clParent)).fall(30, 5F, 90, WeatherAnimation.FallItem.FallType.CLOUD_DARK).start()
        WeatherAnimation(findViewById(R.id.main_clParent)).lightning(10, true).start()
    }
}
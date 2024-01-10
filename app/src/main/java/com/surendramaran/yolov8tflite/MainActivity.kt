package com.surendramaran.yolov8tflite

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.surendramaran.yolov8tflite.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val fragmentManager = supportFragmentManager

        val transaction = fragmentManager.beginTransaction()
        val countFragment = CountFragment.getInstance()
        transaction.add(R.id.fragment_count_container, countFragment)

        transaction.commit()
    }
}

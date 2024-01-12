package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.view.View
import android.widget.ImageView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import com.surendramaran.yolov8tflite.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var countFragment: CountFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        countFragment = CountFragment.getInstance()
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_count_container, countFragment)
            .commit()

        // Initialize ActionBarDrawerToggle
        val toggle = ActionBarDrawerToggle(
            this,
            activityMainBinding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )

        // Set drawer listener
        activityMainBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        manageDrawerBehavior()
    }

    private fun manageDrawerBehavior() {
        val menuButton = findViewById<ImageView>(R.id.menuBtn)
        menuButton.setOnClickListener {
            if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            } else {
                activityMainBinding.drawerLayout.openDrawer(GravityCompat.START)
            }
        }

        findViewById<View>(R.id.mainContent).setOnClickListener {
            if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }
}

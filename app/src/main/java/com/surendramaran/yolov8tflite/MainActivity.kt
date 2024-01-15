package com.surendramaran.yolov8tflite

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.GravityCompat
import androidx.fragment.app.FragmentManager
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import com.surendramaran.yolov8tflite.databinding.ActivityMainBinding
import com.surendramaran.yolov8tflite.fragments.CameraFragment

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var navController: NavController

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        supportFragmentManager.beginTransaction()
            .replace(R.id.camera_container, CameraFragment())
            .commit()

        val toggle = ActionBarDrawerToggle(
            this,
            activityMainBinding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
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


        findViewById<TextView>(R.id.homeSidebarItem).setOnClickListener {
            if (navController.currentDestination?.id != R.id.camera_fragment) {
                navController.navigate(R.id.camera_fragment)
            }
            if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        findViewById<TextView>(R.id.treeListSidebarItem).setOnClickListener {
            if (navController.currentDestination?.id != R.id.tree_list_fragment) {
                navController.navigate(R.id.tree_list_fragment)
            }
            if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }
    }

    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        val fragmentManager: FragmentManager = supportFragmentManager
        if (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStack()
        } else {
            super.onBackPressed()
        }
    }
}

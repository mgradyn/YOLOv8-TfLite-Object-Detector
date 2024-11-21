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
import dji.v5.common.error.IDJIError
import dji.v5.common.register.DJISDKInitEvent
import dji.v5.manager.SDKManager
import dji.v5.manager.interfaces.SDKManagerCallback

class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding
    private lateinit var navController: NavController
    private val TAG = "FFBCounter"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)

        val navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController

        val toggle = ActionBarDrawerToggle(
            this,
            activityMainBinding.drawerLayout,
            R.string.navigation_drawer_open,
            R.string.navigation_drawer_close
        )
        activityMainBinding.drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        manageDrawerBehavior()
        findViewById<TextView>(R.id.homeSidebarItem)
            .setBackgroundColor(resources.getColor(R.color.selected_sidebar_background))
    }

    private fun registerApp() {
        SDKManager.getInstance().init(this, object : SDKManagerCallback {
            override fun onRegisterSuccess() {
                Log.i(TAG, "FFBCounter onRegisterSuccess")
            }

            override fun onRegisterFailure(error: IDJIError) {
                Log.i(TAG, "FFBCounter onRegisterFailure")
            }

            override fun onProductDisconnect(productId: Int) {
                Log.i(TAG, "FFBCounter onProductDisconnect")
            }

            override fun onProductConnect(productId: Int) {
                Log.i(TAG, "FFBCounter onProductConnect")
            }

            override fun onProductChanged(productId: Int) {
                Log.i(TAG, "FFBCounter onProductChanged")
            }

            override fun onInitProcess(event: DJISDKInitEvent, totalProcess: Int) {
                Log.i(TAG, "FFBCounter onInitProcess")
                if (event == DJISDKInitEvent.INITIALIZE_COMPLETE) {
                    Log.i(TAG, "myApp start registerApp")
                    SDKManager.getInstance().registerApp()
                }
            }

            override fun onDatabaseDownloadProgress(current: Long, total: Long) {
                Log.i(TAG, "myApp onDatabaseDownloadProgress")
            }
        })
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
                findViewById<TextView>(R.id.homeSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.selected_sidebar_background))
                findViewById<TextView>(R.id.treeListSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.white))
                findViewById<TextView>(R.id.droneListSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.white))
            }
            if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        findViewById<TextView>(R.id.treeListSidebarItem).setOnClickListener {
            if (navController.currentDestination?.id != R.id.tree_list_fragment) {
                navController.navigate(R.id.tree_list_fragment)
                findViewById<TextView>(R.id.treeListSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.selected_sidebar_background))
                findViewById<TextView>(R.id.homeSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.white))
                findViewById<TextView>(R.id.droneListSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.white))
            }
            if (activityMainBinding.drawerLayout.isDrawerOpen(GravityCompat.START)) {
                activityMainBinding.drawerLayout.closeDrawer(GravityCompat.START)
            }
        }

        findViewById<TextView>(R.id.droneListSidebarItem).setOnClickListener {
            if (navController.currentDestination?.id != R.id.drone_list_fragment) {
                navController.navigate(R.id.drone_list_fragment)
                findViewById<TextView>(R.id.droneListSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.selected_sidebar_background))
                findViewById<TextView>(R.id.homeSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.white))
                findViewById<TextView>(R.id.treeListSidebarItem)
                    .setBackgroundColor(resources.getColor(R.color.white))
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

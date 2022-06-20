package com.learning.image.detection

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.navigation.findNavController
import com.learning.image.detection.databinding.ActivityMainBinding


class MainActivity : AppCompatActivity() {
    private lateinit var activityMainBinding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityMainBinding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(activityMainBinding.root)
    }

    override fun onResume() {
        super.onResume()
        activityMainBinding.screenContainer.postDelayed({
            hideSystemUI()
        }, 1000L)
    }

    private fun hideSystemUI() {
        WindowCompat.setDecorFitsSystemWindows(window, false)
        WindowInsetsControllerCompat(
            window,
            activityMainBinding.screenContainer
        ).let { controller ->
            controller.hide(WindowInsetsCompat.Type.systemBars())
            controller.systemBarsBehavior =
                WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        }
    }


    override fun onBackPressed() {
        activityMainBinding.screenContainer.findNavController().navigateUp()
        finish()
    }

}
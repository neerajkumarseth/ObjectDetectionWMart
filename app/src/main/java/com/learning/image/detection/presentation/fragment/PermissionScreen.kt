package com.learning.image.detection.presentation.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.Navigation
import com.learning.image.detection.R

class PermissionScreen : Fragment() {

    private val requestPermissionLauncher =
        registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted: Boolean ->
            if (isGranted) {
                navigateToCamera()
            } else {
                Toast.makeText(context, getString(R.string.permission_deny), Toast.LENGTH_LONG)
                    .show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        when (PackageManager.PERMISSION_GRANTED) {
            ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) -> {
                navigateToCamera()
            }
            else -> {
                requestPermissionLauncher.launch(
                    Manifest.permission.CAMERA
                )
            }
        }
    }

    private fun navigateToCamera() {
        lifecycleScope.launchWhenStarted {
            Navigation.findNavController(requireActivity(), R.id.screen_container)
                .navigate(R.id.cameraFragment)
        }
    }
}

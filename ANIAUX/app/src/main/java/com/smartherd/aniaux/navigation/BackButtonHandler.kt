package com.smartherd.aniaux.navigation

import androidx.activity.OnBackPressedCallback
import androidx.activity.OnBackPressedDispatcherOwner
import androidx.compose.runtime.staticCompositionLocalOf

// Define a LocalBackPressedDispatcher using static composition local
private val LocalBackPressedDispatcher =
    staticCompositionLocalOf<OnBackPressedDispatcherOwner?> { null }

// ComposableBackNavigationHandler class to handle back navigation
private class ComposableBackNavigationHandler(enabled: Boolean) : OnBackPressedCallback(enabled) {

    // Function to define what happens when back is pressed
    lateinit var onBackPressed: () -> Unit

    override fun handleOnBackPressed() {
        // Invoke the onBackPressed function when back is pressed
        if (isEnabled) {
            onBackPressed()
        }
    }
}

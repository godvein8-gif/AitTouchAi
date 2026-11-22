package com.nexusai.assistant

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.nexusai.assistant.navigation.AppNavigation
import com.nexusai.assistant.ui.theme.NexusTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        (application as NexusApplication).activityHolder.current = this
        enableEdgeToEdge()
        setContent {
            NexusTheme {
                AppNavigation()
            }
        }
    }

    override fun onDestroy() {
        (application as NexusApplication).activityHolder.clear(this)
        super.onDestroy()
    }
}

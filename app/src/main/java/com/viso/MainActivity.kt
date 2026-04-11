package com.viso

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.produceState
import com.viso.data.datastore.ConfigDataStore
import com.viso.ui.navigation.Screen
import com.viso.ui.navigation.VisoNavGraph
import com.viso.ui.theme.VisoTheme
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    @Inject
    lateinit var configDataStore: ConfigDataStore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        setContent {
            val startDestination by produceState<String?>(initialValue = null) {
                val config = configDataStore.getConfig()
                value = if (config.onboardingDone) Screen.Home.route else Screen.Onboarding.route
            }

            VisoTheme {
                startDestination?.let { dest ->
                    VisoNavGraph(startDestination = dest)
                }
            }
        }
    }
}

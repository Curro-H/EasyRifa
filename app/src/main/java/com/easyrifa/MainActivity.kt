package com.easyrifa

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.rememberNavController
import com.easyrifa.ui.navigation.AppNavHost
import com.easyrifa.ui.theme.EasyRifaTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            EasyRifaTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
            }
        }
    }
}

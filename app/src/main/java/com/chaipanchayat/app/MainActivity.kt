package com.chaipanchayat.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.chaipanchayat.app.ui.navigation.MainAppNavigation
import com.chaipanchayat.app.ui.theme.ChaiPanchayatTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            ChaiPanchayatTheme {
                MainAppNavigation()
            }
        }
    }
}

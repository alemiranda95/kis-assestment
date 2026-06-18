package com.assestment.kis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.rememberNavController
import com.assestment.kis.presentation.ds.KisTheme
import com.assestment.kis.presentation.focus.navigation.FocusRoute
import com.assestment.kis.presentation.focus.navigation.focusGraph

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KisTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = FocusRoute) {
                    focusGraph()
                }
            }
        }
    }
}

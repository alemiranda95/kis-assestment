package com.assestment.kis

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.assestment.kis.presentation.core.designsystem.theme.KisTheme
import com.assestment.kis.presentation.focus.FocusScreen

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KisTheme {
                FocusScreen()
            }
        }
    }
}

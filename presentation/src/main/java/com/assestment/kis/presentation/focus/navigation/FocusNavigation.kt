package com.assestment.kis.presentation.focus.navigation

import androidx.navigation.NavGraphBuilder
import androidx.navigation.compose.composable
import com.assestment.kis.presentation.focus.FocusRoot
import kotlinx.serialization.Serializable

@Serializable
data object FocusRoute

fun NavGraphBuilder.focusGraph() {
    composable<FocusRoute> {
        FocusRoot()
    }
}

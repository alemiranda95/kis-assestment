package com.assestment.kis.presentation.focus

import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performClick
import assertk.assertThat
import assertk.assertions.isEqualTo
import com.assestment.kis.presentation.core.designsystem.theme.KisTheme
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
@Config(sdk = [33])
class FocusScreenTest {

    @get:Rule
    val composeTestRule = createComposeRule()

    @Test
    fun `shows the start control when idle`() {
        composeTestRule.setContent {
            KisTheme { FocusScreen(state = FocusState(), onAction = {}) }
        }

        composeTestRule.onNodeWithText("Start session").assertIsDisplayed()
    }

    @Test
    fun `shows the stop control when a session is active`() {
        composeTestRule.setContent {
            KisTheme { FocusScreen(state = FocusState(sessionActive = true), onAction = {}) }
        }

        composeTestRule.onNodeWithText("Stop session").assertIsDisplayed()
    }

    @Test
    fun `tapping start emits the start action`() {
        var action: FocusAction? = null
        composeTestRule.setContent {
            KisTheme { FocusScreen(state = FocusState(), onAction = { action = it }) }
        }

        composeTestRule.onNodeWithText("Start session").performClick()

        assertThat(action).isEqualTo(FocusAction.StartSession)
    }
}

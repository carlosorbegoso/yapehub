package org.sysarp.project.ui

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import org.sysarp.project.App
import org.sysarp.project.ui.theme.YapeHubTheme

@RunWith(AndroidJUnit4::class)
class YapeHubUITest {
    
    @get:Rule
    val composeTestRule = createComposeRule()
    
    @Test
    fun testAppLaunches() {
        // Given - Launch the app
        composeTestRule.setContent {
            YapeHubTheme {
                App()
            }
        }
        
        // Then - Verify splash screen appears
        composeTestRule.onNodeWithText("YapeHub").assertIsDisplayed()
    }
    
    @Test
    fun testSplashScreenContent() {
        // Given
        composeTestRule.setContent {
            YapeHubTheme {
                App()
            }
        }
        
        // Then - Verify splash screen elements
        composeTestRule.onNodeWithText("YapeHub").assertIsDisplayed()
        composeTestRule.onNodeWithContentDescription("YapeHub Logo").assertIsDisplayed()
    }
}

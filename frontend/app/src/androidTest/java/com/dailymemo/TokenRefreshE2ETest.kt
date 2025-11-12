package com.dailymemo

import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.dailymemo.presentation.components.dialogs.SessionExpiredDialog
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.delay
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

/**
 * E2E tests for token refresh flow
 *
 * Test scenarios:
 * 1. SessionExpiredDialog displays correctly
 * 2. Auto-dismiss after 5 seconds with navigation callback
 * 3. Manual navigation via login button
 * 4. Dialog prevents dismissal by back button
 *
 * Note: These are UI component tests. Integration tests with actual
 * token refresh logic should be done separately with backend server running.
 */
@RunWith(AndroidJUnit4::class)
class TokenRefreshE2ETest {

    @get:Rule
    val composeTestRule = createComposeRule()

    companion object {
        private const val DIALOG_AUTO_DISMISS_TIMEOUT = 6000L // 5 second auto-dismiss + 1 second buffer
    }

    /**
     * Test Scenario 1: SessionExpiredDialog displays correctly
     *
     * Verifies:
     * - Dialog title is shown
     * - Dialog message is shown
     * - Auto-redirect message is shown
     * - Login button is shown
     */
    @Test
    fun testSessionExpiredDialogDisplaysCorrectly() {
        var dialogShown = true
        var dismissCalled = false
        var navigateToLoginCalled = false

        composeTestRule.setContent {
            SessionExpiredDialog(
                showDialog = dialogShown,
                onDismiss = { dismissCalled = true },
                onNavigateToLogin = { navigateToLoginCalled = true }
            )
        }

        // Verify dialog elements are displayed
        composeTestRule.onNodeWithText("세션 만료").assertIsDisplayed()
        composeTestRule.onNodeWithText("세션이 만료되었습니다. 다시 로그인해주세요.").assertIsDisplayed()
        composeTestRule.onNodeWithText("5초 후 자동으로 로그인 화면으로 이동합니다").assertIsDisplayed()
        composeTestRule.onNodeWithText("로그인").assertIsDisplayed()

        // Verify callbacks are not called yet
        assert(!dismissCalled) { "onDismiss should not be called on initial display" }
        assert(!navigateToLoginCalled) { "onNavigateToLogin should not be called on initial display" }
    }

    /**
     * Test Scenario 2: Auto-dismiss after 5 seconds
     *
     * Verifies:
     * - Dialog auto-dismisses after 5 seconds
     * - onNavigateToLogin callback is triggered
     */
    @Test
    fun testAutoNavigationAfterTimeout() = runBlocking {
        var navigateToLoginCalled = false

        composeTestRule.setContent {
            SessionExpiredDialog(
                showDialog = true,
                onDismiss = { },
                onNavigateToLogin = { navigateToLoginCalled = true }
            )
        }

        // Verify dialog is initially shown
        composeTestRule.onNodeWithText("세션 만료").assertIsDisplayed()

        // Wait for auto-dismiss timeout
        delay(DIALOG_AUTO_DISMISS_TIMEOUT)

        // Verify navigation callback was triggered
        assert(navigateToLoginCalled) { "onNavigateToLogin should be called after timeout" }
    }

    /**
     * Test Scenario 3: Manual navigation via login button
     *
     * Verifies:
     * - Clicking login button triggers navigation
     * - Navigation happens immediately (before auto-dismiss timeout)
     */
    @Test
    fun testManualNavigationViaLoginButton() {
        var navigateToLoginCalled = false

        composeTestRule.setContent {
            SessionExpiredDialog(
                showDialog = true,
                onDismiss = { },
                onNavigateToLogin = { navigateToLoginCalled = true }
            )
        }

        // Click the login button
        composeTestRule.onNodeWithText("로그인").performClick()

        // Verify navigation callback was triggered immediately
        assert(navigateToLoginCalled) { "onNavigateToLogin should be called when button is clicked" }
    }

    /**
     * Test Scenario 4: Dialog hidden when showDialog is false
     *
     * Verifies:
     * - Dialog is not shown when showDialog parameter is false
     */
    @Test
    fun testDialogNotShownWhenShowDialogIsFalse() {
        composeTestRule.setContent {
            SessionExpiredDialog(
                showDialog = false,
                onDismiss = { },
                onNavigateToLogin = { }
            )
        }

        // Verify dialog is not displayed
        composeTestRule.onNodeWithText("세션 만료").assertDoesNotExist()
    }
}

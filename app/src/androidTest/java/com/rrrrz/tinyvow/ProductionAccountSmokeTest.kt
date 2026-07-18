package com.rrrrz.tinyvow

import android.util.Log
import androidx.compose.ui.test.SemanticsMatcher
import androidx.compose.ui.test.hasClickAction
import androidx.compose.ui.test.hasContentDescription
import androidx.compose.ui.test.hasSetTextAction
import androidx.compose.ui.test.hasText
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.performClick
import androidx.compose.ui.test.performScrollTo
import androidx.compose.ui.test.performTextReplacement
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import org.junit.Rule
import org.junit.Assume.assumeTrue
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class ProductionAccountSmokeTest {
    @get:Rule
    val composeRule = createAndroidComposeRule<MainActivity>()

    @Test
    fun registerDisplayDeleteAndReturnToAnonymousAccount() {
        mark("start")
        val arguments = InstrumentationRegistry.getArguments()
        val email = arguments.getString("accountEmail")
        val password = arguments.getString("accountPassword")
        assumeTrue("accountEmail instrumentation argument is required", !email.isNullOrBlank())
        assumeTrue("accountPassword instrumentation argument is required", !password.isNullOrBlank())
        val testEmail = requireNotNull(email)
        val testPassword = requireNotNull(password)
        val displayName = arguments.getString("accountDisplayName") ?: "Production UI Test"
        val context = InstrumentationRegistry.getInstrumentation().targetContext

        val me = context.getString(R.string.home_me)
        val signInOrRegister = context.getString(R.string.account_sign_in_or_register)
        val createAccount = context.getString(R.string.account_create_account)
        val register = context.getString(R.string.account_register_action)
        val emailUnverified = context.getString(R.string.account_email_unverified)
        val membershipAndSpending = context.getString(R.string.account_membership_and_spending)
        val deleteAccount = context.getString(R.string.me_delete_account)
        val loginTitle = context.getString(R.string.account_login_title)

        waitFor(hasContentDescription(me))
        mark("home-visible")
        composeRule.onNode(hasContentDescription(me)).performClick()
        waitFor(hasText(signInOrRegister))
        mark("me-visible")
        composeRule.onNode(hasText(signInOrRegister) and hasClickAction()).performClick()

        waitFor(hasText(createAccount))
        mark("account-entry-visible")
        composeRule.onNode(hasText(createAccount) and hasClickAction()).performClick()
        waitForEditableFields(4)
        mark("registration-form-visible")
        composeRule.onAllNodes(hasSetTextAction())[0].performTextReplacement(displayName)
        composeRule.onAllNodes(hasSetTextAction())[1].performTextReplacement(testEmail)
        composeRule.onAllNodes(hasSetTextAction())[2].performTextReplacement(testPassword)
        composeRule.onAllNodes(hasSetTextAction())[3].performTextReplacement(testPassword)
        composeRule.onNode(hasText(register) and hasClickAction()).performClick()

        waitFor(hasText(testEmail), timeoutMillis = 30_000)
        mark("registration-complete")
        waitFor(hasText(emailUnverified))
        waitFor(hasText(membershipAndSpending))
        mark("profile-verified")

        composeRule.onNode(hasText(deleteAccount) and hasClickAction())
            .performScrollTo()
            .performClick()
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodes(hasText(deleteAccount) and hasClickAction())
                .fetchSemanticsNodes().size >= 2
        }
        mark("delete-dialog-visible")
        val deleteNodes = composeRule.onAllNodes(hasText(deleteAccount) and hasClickAction())
        val confirmDeleteIndex = deleteNodes.fetchSemanticsNodes().lastIndex
        deleteNodes[confirmDeleteIndex].performClick()

        waitFor(hasText(signInOrRegister), timeoutMillis = 30_000)
        mark("deletion-complete")
        composeRule.onNode(hasText(signInOrRegister) and hasClickAction()).performClick()
        waitFor(hasText(loginTitle), timeoutMillis = 30_000)
        mark("replacement-session-ready")
    }

    private fun waitFor(
        matcher: SemanticsMatcher,
        timeoutMillis: Long = 20_000,
    ) {
        composeRule.waitUntil(timeoutMillis) {
            composeRule.onAllNodes(matcher).fetchSemanticsNodes().isNotEmpty()
        }
    }

    private fun waitForEditableFields(count: Int) {
        composeRule.waitUntil(20_000) {
            composeRule.onAllNodes(hasSetTextAction()).fetchSemanticsNodes().size == count
        }
    }

    private fun mark(step: String) {
        Log.i(TAG, "step=$step")
    }

    private companion object {
        const val TAG = "ProductionAccountSmoke"
    }
}

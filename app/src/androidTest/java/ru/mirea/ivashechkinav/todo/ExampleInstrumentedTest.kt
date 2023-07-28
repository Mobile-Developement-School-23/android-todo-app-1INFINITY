package ru.mirea.ivashechkinav.todo

import androidx.compose.ui.test.assertTextContains
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performTextInput
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.ext.junit.rules.ActivityScenarioRule
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import dagger.Component
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.RuleChain
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runner.RunWith
import org.junit.runners.model.Statement
import ru.mirea.ivashechkinav.todo.di.components.AppComponent
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.TaskTags.INPUT_FIELD
import javax.inject.Singleton


@RunWith(AndroidJUnit4::class)
class ExampleInstrumentedTest {
    @Test
    fun useAppContext() {
        // Context of the app under test.
        val appContext = InstrumentationRegistry.getInstrumentation().targetContext
        assertEquals("ru.mirea.ivashechkinav.todo", appContext.packageName)
    }
}

@RunWith(AndroidJUnit4::class)
class MyTest {
    private val component = MockComponentRule(InstrumentationRegistry.getInstrumentation().targetContext)
    private var activityTestRule = ActivityScenarioRule(MainActivity::class.java)
    private val composeTestRule = createAndroidComposeRule<MainActivity>()

    @Rule
    @JvmField
    var chain: TestRule = RuleChain
            .outerRule(component)
            .around(activityTestRule)
            .around(composeTestRule)

    @Test
    fun endToEndTest() {
        onView(withId(R.id.floatingActionButton)).perform(ViewActions.click())
        composeTestRule.onNodeWithTag(INPUT_FIELD).performTextInput("Bebra")
        composeTestRule.onNodeWithTag(INPUT_FIELD).assertTextContains("Bebra")
        //composeTestRule.onNodeWithTag(INPUT_FIELD).assertExists()
    }
}
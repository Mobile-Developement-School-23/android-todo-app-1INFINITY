package ru.mirea.ivashechkinav.todo

import android.content.Context
import dagger.BindsInstance
import dagger.Component
import dagger.Module
import org.junit.rules.TestRule
import org.junit.runner.Description
import org.junit.runners.model.Statement
import ru.mirea.ivashechkinav.todo.di.components.AppComponent
import ru.mirea.ivashechkinav.todo.di.components.AppContext
import javax.inject.Singleton

@Module
class MockModule () {
//    @Provides
//    @Singleton
//    fun provideSomething(): Something = mockk(relaxed = true)
}
//MockComponent
@Component(modules = [MockModule::class])
interface MockComponent : AppComponent {
//    fun getSomething(): Something
@Component.Factory
interface Factory {
    fun create(
        @BindsInstance
        @AppContext
        appContext: Context
    ): AppComponent
}
}
class MockComponentRule(val context: Context) : TestRule {
    private val mMockComponent: MockComponent

    override fun apply(base: Statement, description: Description?): Statement {
        return object : Statement() {
            @Throws(Throwable::class)
            override fun evaluate() {
                val application: App = context.applicationContext as App
                // Set the MockComponent before the test runs
                application.appComponent = mMockComponent
                // Run tests
                base.evaluate()
            }
        }
    }

    init {
        val application: App = context.applicationContext as App
        mMockComponent = DaggerMockComponent.factory().create(appContext = application.applicationContext)
    }

    //fun getMockSomething() = mMockComponent.getSomething()
}
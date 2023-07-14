package ru.mirea.ivashechkinav.todo.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.data.settings.SettingStorage
import ru.mirea.ivashechkinav.todo.data.settings.UiTheme
import ru.mirea.ivashechkinav.todo.databinding.ActivityMainBinding
import ru.mirea.ivashechkinav.todo.di.components.ActivityComponent
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingStorage: SettingStorage

    private lateinit var binding: ActivityMainBinding
    lateinit var activityComponent: ActivityComponent
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent =
            (application as App)
                .appComponent
                .activityComponentFactory()
                .create()
                .also { it.inject(this) }

        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.CREATED) {
                settingStorage.themeFlow.collect { theme ->
                    theme?.let { AppCompatDelegate.setDefaultNightMode(it.value) }
                }
            }
        }
        setContentView(binding.root)
    }
}
package ru.mirea.ivashechkinav.todo.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.app.ActivityCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.data.settings.SettingStorage
import ru.mirea.ivashechkinav.todo.databinding.ActivityMainBinding
import ru.mirea.ivashechkinav.todo.di.components.ActivityComponent
import ru.mirea.ivashechkinav.todo.presentation.notifications.AlarmScheduler
import javax.inject.Inject

class MainActivity : AppCompatActivity() {

    @Inject
    lateinit var settingStorage: SettingStorage

    @Inject
    lateinit var alarmScheduler: AlarmScheduler

    private lateinit var binding: ActivityMainBinding
    lateinit var activityComponent: ActivityComponent
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent =
            (application as App)
                .appComponent
                .activityComponentFactory()
                .create(this)
        activityComponent.inject(this)
        binding = ActivityMainBinding.inflate(layoutInflater)

        lifecycleScope.launch {
            settingStorage.themeFlow.collect { theme ->
                theme?.let { AppCompatDelegate.setDefaultNightMode(it.value) }
            }
        }
        requestPermissions()
        setContentView(binding.root)
    }

    private fun requestPermissions() {
        val permissions = arrayOf(
            Manifest.permission.POST_NOTIFICATIONS,
            Manifest.permission.SCHEDULE_EXACT_ALARM
        )
        ActivityCompat.requestPermissions(this, permissions, PERMISSIONS_REQUEST_CODE)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSIONS_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                alarmScheduler.scheduleAlarm()
            } else {
                alarmScheduler.cancelAlarm()
            }
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
    }

    companion object {
        private const val PERMISSIONS_REQUEST_CODE = 123
    }
}
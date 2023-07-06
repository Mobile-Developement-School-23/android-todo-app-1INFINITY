package ru.mirea.ivashechkinav.todo.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.databinding.ActivityMainBinding
import ru.mirea.ivashechkinav.todo.di.components.ActivityComponent

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    lateinit var activityComponent: ActivityComponent
        private set

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        activityComponent = (application as App).appComponent.activityComponentFactory().create()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
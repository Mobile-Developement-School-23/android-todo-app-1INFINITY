package ru.mirea.ivashechkinav.todo.presentation

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import ru.mirea.ivashechkinav.todo.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
    }
}
package ru.mirea.ivashechkinav.todo.di.components

import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainViewController
import javax.inject.Inject

@MainFragmentViewScope
class MainFragmentBootstrapper @Inject constructor(
    private val mainViewController: MainViewController
)
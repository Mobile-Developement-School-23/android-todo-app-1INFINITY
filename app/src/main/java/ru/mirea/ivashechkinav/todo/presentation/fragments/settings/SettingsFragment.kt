package ru.mirea.ivashechkinav.todo.presentation.fragments.settings

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract
import javax.inject.Inject

class SettingsFragment: Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val vm: SettingsViewModel by viewModels { viewModelFactory }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (requireActivity() as MainActivity)
            .activityComponent
            .settingsFragmentComponentFactory()
            .create()
            .inject(this)

        val root = inflater.inflate(R.layout.fragment_settings, container, false).apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                val state = vm.themeState.collectAsStateWithLifecycle()
                AppTheme {
                    SettingFragmentComposable(
                        viewModel = vm,
                        state = state,
                    )
                }
            }
        }
        initViewModelObservers()
        return root
    }
    private fun initViewModelObservers() {
        lifecycleScope.launch {
            vm.effect.collect { effect ->
                when (effect) {
                    is SettingsViewModel.UiEffect.ToBackFragment -> navigateBack()
                }
            }
        }
    }
    private fun navigateBack() {
        findNavController().popBackStack()
    }
}
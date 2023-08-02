package ru.mirea.ivashechkinav.todo.presentation.fragments.settings

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.ui.platform.ComposeView
import androidx.fragment.app.Fragment
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import javax.inject.Inject

class SettingsFragment : Fragment() {

    @Inject
    lateinit var vm: SettingsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (requireActivity() as MainActivity)
            .activityComponent
            .settingsFragmentComponentFactory()
            .create(fragment = this@SettingsFragment)
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
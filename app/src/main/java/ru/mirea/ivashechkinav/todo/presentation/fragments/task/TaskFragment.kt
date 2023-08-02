package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import android.app.DatePickerDialog
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
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.UiEffect
import java.util.Calendar
import javax.inject.Inject

class TaskFragment : Fragment() {

    @Inject
    lateinit var factory: ViewModelProvider.Factory

    private val vm: TaskViewModel by lazy {
        ViewModelProvider(this, factory)[TaskViewModel::class.java]
    }

    private val args: TaskFragmentArgs by navArgs()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        (requireActivity() as MainActivity)
            .activityComponent
            .taskFragmentComponentFactory()
            .create(fragment = this@TaskFragment)
            .inject(this)

        val root = inflater.inflate(R.layout.fragment_task, container, false).apply {
            findViewById<ComposeView>(R.id.composeView).setContent {
                val state = vm.uiState.collectAsStateWithLifecycle()
                AppTheme {
                    TaskFragmentComposable(
                        state = state,
                        viewModel = vm
                    )
                }
            }
        }
        loadArgs()
        initViewModelObservers()
        return root
    }

    private fun initViewModelObservers() {
        lifecycleScope.launch {
            vm.effect.collect { effect ->
                when (effect) {
                    is UiEffect.ShowSnackbar -> {
                        Snackbar.make(
                            requireView(),
                            parseSnackbarMessages(effect.message),
                            Snackbar.LENGTH_SHORT
                        ).show()
                    }

                    is UiEffect.ToBackFragment -> findNavController().popBackStack()
                    is UiEffect.ShowDatePicker -> showDatePicker()
                }
            }
        }
    }

    private fun loadArgs() {
        args.taskId?.let { vm.onTodoItemIdLoaded(it) }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(requireActivity(), { view, pickerYear, monthOfYear, dayOfMonth ->
            c.set(Calendar.YEAR, pickerYear)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            vm.onDeadlineSelected(c.timeInMillis)
        }, year, month, day)

        dpd.show()
    }

    private fun parseSnackbarMessages(message: TaskContract.SnackbarMessage): String {
        val resId = when (message) {
            TaskContract.SnackbarMessage.UnknownError -> R.string.unknown_error_message
            TaskContract.SnackbarMessage.ServerError -> R.string.server_error_message
            TaskContract.SnackbarMessage.ConnectionMissing -> R.string.connection_missing_message
            TaskContract.SnackbarMessage.MissingText -> R.string.description_needed_message
        }
        return getString(resId)
    }
}

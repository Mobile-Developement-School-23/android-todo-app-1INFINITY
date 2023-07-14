package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import android.app.DatePickerDialog
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
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
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.EffectUi
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.EventUi
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import javax.inject.Inject

class TaskFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val vm: TaskViewModel by viewModels { viewModelFactory }

    private val args: TaskFragmentArgs by navArgs()

    override fun onAttach(context: Context) {
        super.onAttach(context)
        (requireActivity() as MainActivity)
            .activityComponent
            .taskFragmentComponentFactory()
            .create()
            .inject(this)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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
                    is EffectUi.ShowSnackbar -> {
                        Snackbar.make(requireView(), effect.message, Snackbar.LENGTH_SHORT).show()
                    }

                    is EffectUi.ToBackFragment -> findNavController().popBackStack()
                    is EffectUi.ShowDatePicker -> showDatePicker()
                }
            }
        }
    }

    private fun loadArgs() {
        args.taskId?.let {
            vm.setEvent(
                EventUi.OnTodoItemIdLoaded(it)
            )
        }
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

            vm.setEvent(
                EventUi.OnDeadlineSelected(c.timeInMillis)
            )
        }, year, month, day)

        dpd.show()
    }
}

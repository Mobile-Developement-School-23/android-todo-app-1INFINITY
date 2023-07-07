package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import android.app.DatePickerDialog
import android.os.Bundle
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.FlowPreview
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.debounce
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.databinding.FragmentTaskBinding
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.core.textChanges
import java.text.SimpleDateFormat
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.TaskContract.*
import java.util.*
import javax.inject.Inject

@OptIn(FlowPreview::class, ExperimentalCoroutinesApi::class)
class TaskFragment : Fragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val vm: TaskViewModel by viewModels { viewModelFactory }

    private lateinit var binding: FragmentTaskBinding
    private val args: TaskFragmentArgs by navArgs()
    private var popupMenu: PopupMenu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        (requireActivity() as MainActivity)
            .activityComponent
            .taskFragmentComponentFactory()
            .create()
            .inject(this)
        loadArgs()
        initEditTextObserve()
        initButtons()
        initPopUpMenu()
        initDateBlock()
        initViewModelObservers()
        return binding.root
    }

    private fun initViewModelObservers() {
        lifecycleScope.launch {
            vm.uiState.collectLatest { state ->
                setDeadlineDate(state.deadlineTimestamp)
                changeImportanceValue(state.importance)
                changeDeleteBlockColor(state)

                if (state.viewState == FragmentViewState.Loading)
                    binding.edTodoItemText.setText(state.text)
            }
        }
        lifecycleScope.launch {
            vm.effect.collect { effect ->
                when (effect) {
                    is EffectUi.ShowSnackbar -> {
                        Snackbar.make(binding.root, effect.message, Snackbar.LENGTH_SHORT).show()
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

    private fun initEditTextObserve() {
        lifecycleScope.launch {
            binding.edTodoItemText.textChanges().debounce(300).collectLatest {
                vm.setEvent(EventUi.OnTodoTextEdited(it.toString()))
            }
        }
    }

    private fun changeDeleteBlockColor(state: UiState) {
        if (state.creationTimestamp != null || !state.text.isNullOrEmpty()) {
            val redColor =
                AppCompatResources.getColorStateList(requireContext(), R.color.color_light_red)
            binding.imDelete.imageTintList = redColor
            binding.tvDelete.setTextColor(redColor)
        } else {
            val disabledColor =
                AppCompatResources.getColorStateList(requireContext(), R.color.label_light_disable)
            binding.imDelete.imageTintList = disabledColor
            binding.tvDelete.setTextColor(disabledColor)
        }
    }

    private fun initButtons() {
        binding.btnSave.setOnClickListener {
            vm.setEvent(EventUi.OnSaveButtonClicked)
        }
        binding.imDelete.setOnClickListener {
            vm.setEvent(EventUi.OnDeleteButtonClicked)
        }
        binding.imCancel.setOnClickListener { vm.setEvent(EventUi.OnBackButtonClicked) }
    }

    private fun initPopUpMenu() {
        popupMenu = PopupMenu(requireContext(), binding.vPopupMenuAnchor)
        val currentPopupMenu = popupMenu ?: return
        currentPopupMenu.inflate(R.menu.popup_menu)
        val highElement: MenuItem = currentPopupMenu.menu.getItem(2)
        highElement.title = SpannableString(highElement.title).apply {
            setSpan(
                ForegroundColorSpan(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.color_light_red
                    )
                ), 0, this.length, 0
            )
        }
        binding.flImportanceMenu.setOnClickListener { currentPopupMenu.show() }
        currentPopupMenu.setOnMenuItemClickListener(this::onMenuItemClick)
    }
    private fun onMenuItemClick(menuItem: MenuItem): Boolean {
        val newImportance = when (menuItem.itemId) {
            R.id.menu_item_none -> Importance.LOW
            R.id.menu_item_low -> Importance.COMMON
            R.id.menu_item_high -> Importance.HIGH
            else -> Importance.LOW
        }
        vm.setEvent(
            EventUi.OnImportanceSelected(newImportance)
        )
        return true
    }
    private fun changeImportanceValue(importance: Importance) {
        val currentPopupMenu = popupMenu ?: return
        when (importance) {
            Importance.LOW -> {
                binding.tvImportanceValue.text = currentPopupMenu.menu.getItem(0).title
            }

            Importance.COMMON -> {
                binding.tvImportanceValue.text = currentPopupMenu.menu.getItem(1).title
            }

            Importance.HIGH -> {
                binding.tvImportanceValue.text = currentPopupMenu.menu.getItem(2).title
            }
        }
    }

    private fun initDateBlock() {
        binding.constrainLayoutDatePicker.setOnClickListener {
            showDatePicker()
        }

        binding.swDeadline.setOnClickListener {
            vm.setEvent(
                EventUi.OnDeadlineSwitchChanged(
                    binding.swDeadline.isChecked
                )
            )
        }
    }

    private fun showDatePicker() {
        val c = Calendar.getInstance()
        val year = c.get(Calendar.YEAR)
        val month = c.get(Calendar.MONTH)
        val day = c.get(Calendar.DAY_OF_MONTH)
        val dpd = DatePickerDialog(requireActivity(), { view, year, monthOfYear, dayOfMonth ->
            c.set(Calendar.YEAR, year)
            c.set(Calendar.MONTH, monthOfYear)
            c.set(Calendar.DAY_OF_MONTH, dayOfMonth)

            vm.setEvent(
                EventUi.OnDeadlineSelected(c.timeInMillis)
            )
        }, year, month, day)

        dpd.show()
    }

    private fun setDeadlineDate(timestamp: Long?) {
        if (timestamp == null) {
            binding.tvDeadlineDate.text = ""
            binding.swDeadline.isChecked = false
            return
        }
        binding.tvDeadlineDate.text = dateFormat.format(timestamp)
        binding.swDeadline.isChecked = true
    }

    companion object {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    }
}
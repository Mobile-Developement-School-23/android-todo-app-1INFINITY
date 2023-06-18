package ru.mirea.ivashechkinav.todo.presentation.fragments

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
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.App
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.FragmentTaskBinding
import ru.mirea.ivashechkinav.todo.domain.repository.TodoItemsRepository
import ru.mirea.ivashechkinav.todo.presentation.models.TodoItemUI
import java.text.SimpleDateFormat
import java.util.*

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding
    private val args: TaskFragmentArgs by navArgs()
    private lateinit var repository: TodoItemsRepository
    private val todoItemUI = TodoItemUI()
    private var popupMenu: PopupMenu? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)

        repository = (requireActivity().application as App).repository

        loadArgs()
        initEditTextObserve()
        initButtons()
        initPopUpMenu()
        initDateBlock()
        return binding.root
    }

    private fun loadArgs() {
        val todoItem = repository.getItemById(args.taskId ?: return) ?: return

        binding.swDeadline.isChecked = false
        todoItem.deadlineTimestamp?.let {
            binding.swDeadline.isChecked = true
            binding.tvDeadlineDate.text = dateFormat.format(it)
            todoItemUI.deadlineTimestamp = it
        }
        todoItemUI.id = todoItem.id

        todoItemUI.text = todoItem.text
        binding.edTodoItemText.setText(todoItem.text)

        todoItemUI.importance = todoItem.importance
        todoItemUI.isComplete = todoItem.isComplete

    }

    private fun initEditTextObserve() {
        changeDeleteBlockColor(todoItemUI.text ?: "")
        binding.edTodoItemText.addTextChangedListener {
            val text = it.toString()
            todoItemUI.text = text
            changeDeleteBlockColor(text)
        }
    }
    private fun changeDeleteBlockColor(text: String) {
        if(text != "") {
            val redColor = AppCompatResources.getColorStateList(requireContext(), R.color.color_light_red)
            binding.imDelete.imageTintList = redColor
            binding.tvDelete.setTextColor(redColor)
        } else {
            val disabledColor = AppCompatResources.getColorStateList(requireContext(), R.color.label_light_disable)
            binding.imDelete.imageTintList = disabledColor
            binding.tvDelete.setTextColor(disabledColor)
        }
    }
    private fun initButtons() {
        binding.btnSave.setOnClickListener {
            val currentTime = System.currentTimeMillis()
            lifecycleScope.launch {
                val newItem = TodoItem(
                    id = todoItemUI.id ?: UUID.randomUUID().toString(),
                    text = todoItemUI.text ?: "",
                    importance = todoItemUI.importance,
                    deadlineTimestamp = todoItemUI.deadlineTimestamp,
                    isComplete = false,
                    creationTimestamp = todoItemUI.creationTimestamp ?: currentTime,
                    changeTimestamp = currentTime
                )
                if (todoItemUI.id == null) {
                    repository.addItem(newItem)
                } else {
                    repository.updateItem(newItem)
                }

                findNavController().popBackStack()
            }
        }
        binding.imDelete.setOnClickListener {
            todoItemUI.id?.let {
                lifecycleScope.launch {
                    repository.deleteItemById(it)
                    findNavController().popBackStack()
                }
            }
        }
        binding.imCancel.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initPopUpMenu() {
        popupMenu = PopupMenu(requireContext(), binding.vPopupMenuAnchor)
        val currentPopupMenu = popupMenu ?: return
        currentPopupMenu.inflate(R.menu.popup_menu)
        val highElement: MenuItem = currentPopupMenu.menu.getItem(2)
        val s = SpannableString(highElement.title)
        s.setSpan(
            ForegroundColorSpan(
                ContextCompat.getColor(
                    requireContext(),
                    R.color.color_light_red
                )
            ), 0, s.length, 0
        )
        highElement.title = s

        changeImportanceValue(todoItemUI.importance)

        binding.flImportanceMenu.setOnClickListener { currentPopupMenu.show() }

        currentPopupMenu.setOnMenuItemClickListener { menuItem ->
            val newImportance = when (menuItem.itemId) {
                R.id.menu_item_none -> {
                    Importance.LOW
                }
                R.id.menu_item_low -> {
                    Importance.COMMON
                }
                R.id.menu_item_high -> {
                    Importance.HIGH
                }
                else -> Importance.LOW
            }
            todoItemUI.importance = newImportance
            changeImportanceValue(newImportance)
            true
        }
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
            else -> {
                throw UnsupportedOperationException("Unknown Importance value: ${todoItemUI.importance}")
            }
        }
    }

    private fun initDateBlock() {
        binding.constrainLayoutDatePicker.setOnClickListener {
            showDatePicker()
        }

        binding.swDeadline.setOnClickListener {
            if (!binding.swDeadline.isChecked) {
                todoItemUI.deadlineTimestamp = null
                binding.tvDeadlineDate.text = ""
            } else {
                showDatePicker()
            }
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
            binding.tvDeadlineDate.text = dateFormat.format(c.time)
            binding.swDeadline.isChecked = true
            todoItemUI.deadlineTimestamp = c.timeInMillis
        }, year, month, day)

        dpd.show()
    }

    companion object {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
    }
}
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
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.data.models.TodoItem
import ru.mirea.ivashechkinav.todo.data.repository.TodoItemsRepositoryImpl
import ru.mirea.ivashechkinav.todo.databinding.FragmentTaskBinding
import java.text.SimpleDateFormat
import java.util.*

class TaskFragment : Fragment() {

    private lateinit var binding: FragmentTaskBinding
    private val args: TaskFragmentArgs by navArgs()
    private val repository = TodoItemsRepositoryImpl()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTaskBinding.inflate(inflater, container, false)
        loadArgs()
        initButtons()
        initPopUpMenu()
        initDatePicker()
        return binding.root
    }

    private fun loadArgs() {
        val todoItem = repository.getItemById(args.taskId ?: return) ?: return
        binding.edTodoItemText.setText(todoItem.text)
        binding.swDeadline.isChecked = todoItem.deadlineTimestamp != null
    }

    private fun initButtons() {
        binding.btnSave.setOnClickListener { findNavController().popBackStack() }
        binding.imDelete.setOnClickListener { findNavController().popBackStack() }
        binding.imCancel.setOnClickListener { findNavController().popBackStack() }
    }

    private fun initPopUpMenu() {
        val popupMenu = PopupMenu(requireContext(), binding.vPopupMenuAnchor)
        popupMenu.inflate(R.menu.popup_menu)
        val highElement: MenuItem = popupMenu.menu.getItem(2)
        val s = SpannableString(highElement.title)
        s.setSpan(ForegroundColorSpan(ContextCompat.getColor(requireContext(), R.color.color_light_red)), 0, s.length, 0)
        highElement.title = s

        binding.flImportanceMenu.setOnClickListener {popupMenu.show()}

        popupMenu.setOnMenuItemClickListener { menuItem ->
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
            //Set importance to item
            true
        }
    }
    private fun initDatePicker() {
        val dateFormat = SimpleDateFormat("d MMMM yyyy", Locale.getDefault())
        binding.constrainLayoutDatePicker.setOnClickListener {
            val c = Calendar.getInstance()
            val year = c.get(Calendar.YEAR)
            val month = c.get(Calendar.MONTH)
            val day = c.get(Calendar.DAY_OF_MONTH)


            val dpd = DatePickerDialog(requireActivity(), { view, year, monthOfYear, dayOfMonth ->
                c.set(Calendar.YEAR, year)
                c.set(Calendar.MONTH, monthOfYear)
                c.set(Calendar.DAY_OF_MONTH, dayOfMonth)
                binding.tvDeadlineDate.text = dateFormat.format(c.time)

            }, year, month, day)

            dpd.show()
        }
    }
}
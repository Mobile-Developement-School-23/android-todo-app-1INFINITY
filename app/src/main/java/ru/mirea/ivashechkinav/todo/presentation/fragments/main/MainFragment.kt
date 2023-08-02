package ru.mirea.ivashechkinav.todo.presentation.fragments.main

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.databinding.FragmentMainBinding
import ru.mirea.ivashechkinav.todo.di.components.MainFragmentViewComponent
import ru.mirea.ivashechkinav.todo.presentation.MainActivity
import ru.mirea.ivashechkinav.todo.presentation.adapters.RoundedItemDecorator
import ru.mirea.ivashechkinav.todo.presentation.adapters.SwipeTodoItemCallback
import ru.mirea.ivashechkinav.todo.presentation.adapters.TodoAdapter
import ru.mirea.ivashechkinav.todo.presentation.fragments.main.MainContract.UiEffect
import javax.inject.Inject

class MainFragment : Fragment() {

    private var viewComponent: MainFragmentViewComponent? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = FragmentMainBinding.inflate(inflater, container, false)
        val fragmentComponent = (requireActivity() as MainActivity)
            .activityComponent
            .mainFragmentComponentFactory()
            .create(
                fragment = this@MainFragment
            )
        viewComponent = fragmentComponent.mainFragmentViewComponentFactory().create(
            navController = findNavController(),
            binding = binding,
            lifecycleOwner = this@MainFragment
        ).apply { boot()  }
        return binding.root
    }

    override fun onDestroyView() {
        viewComponent = null
        super.onDestroyView()
    }


}

package ru.mirea.ivashechkinav.todo.presentation.fragments.task

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Divider
import androidx.compose.material.ExperimentalMaterialApi
import androidx.compose.material.MaterialTheme
import androidx.compose.material.ModalBottomSheetLayout
import androidx.compose.material.ModalBottomSheetValue
import androidx.compose.material.Surface
import androidx.compose.material.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.BottomSheetContent
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.DeadlineBlock
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.DeleteBlock
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.ImportanceSelector
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.TaskInputField
import ru.mirea.ivashechkinav.todo.presentation.fragments.task.components.TaskTopBar
import java.time.Instant
import java.time.ZoneId
import java.util.UUID

@OptIn(ExperimentalMaterialApi::class)
@Composable
fun TaskFragmentComposable(
    viewModel: TaskViewModel?,
    state: State<TaskContract.UiState>,
) {
    val sheetState = rememberModalBottomSheetState(ModalBottomSheetValue.Hidden)
    val scope = rememberCoroutineScope()
    Surface(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colors.background)
    ) {
        ModalBottomSheetLayout(
            sheetState = sheetState,
            sheetShape = RoundedCornerShape(
                topStart = 16.dp,
                topEnd = 16.dp,
            ),
            sheetContent = {
                BottomSheetContent(
                    importanceSelected = state.value.importance,
                    onImportanceSelected = {
                        viewModel?.importanceSelected(it)
                        scope.launch { sheetState.hide() }
                    })
            }
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colors.background)
            ) {
                TaskTopBar(
                    onCloseClick = { viewModel?.cancelButtonClicked() },
                    onSaveClick = { viewModel?.saveButtonClicked() },
                )

                TaskInputField(
                    text = state.value.text ?: "",
                    onChanged = { viewModel?.todoTextEdited(it) },
                )

                Spacer(modifier = Modifier.height(16.dp))

                ImportanceSelector(
                    importance = state.value.importance,
                    onClick = { scope.launch { sheetState.show() } },
                )

                Divider()

                DeadlineBlock(
                    deadline = state.value.deadlineTimestamp?.let { it ->
                        Instant.ofEpochMilli(it)
                            .let { it.atZone(ZoneId.systemDefault()).toLocalDate() }
                    },
                    clearDeadline = {
                        viewModel?.deadlineSwitchChanged(isChecked = false)
                    },
                    showDatePicker = {
                        viewModel?.deadlineSwitchChanged(isChecked = true)
                    },
                )

                Divider()

                DeleteBlock(
                    enabled = state.value.creationTimestamp != null || !state.value.text.isNullOrEmpty(),
                    onClick = { viewModel?.deleteButtonClicked() },
                )
            }
        }
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TaskFragmentPreview() {
    val state = remember {
        mutableStateOf(
            TaskContract.UiState(
                id = UUID.randomUUID().toString(),
                text = "",
                importance = Importance.LOW,
                isComplete = false,
                creationTimestamp = System.currentTimeMillis(),
                deadlineTimestamp = System.currentTimeMillis(),
            ),
        )
    }
    AppTheme {
        TaskFragmentComposable(
            null,
            state
        )
    }
}
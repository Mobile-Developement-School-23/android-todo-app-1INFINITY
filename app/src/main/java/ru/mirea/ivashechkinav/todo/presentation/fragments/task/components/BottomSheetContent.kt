package ru.mirea.ivashechkinav.todo.presentation.fragments.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.selection.selectable
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme

@Composable
fun BottomSheetContent(
    importanceSelected: Importance,
    onImportanceSelected: (Importance) -> Unit,
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colors.background)
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(text = stringResource(id = R.string.importance_text))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 16.dp),
                horizontalArrangement = Arrangement.Center,
            ) {
                Importance.values().forEach { importance ->
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 16.dp)
                            .clip(MaterialTheme.shapes.medium)
                            .selectable(
                                selected = importance == importanceSelected,
                                onClick = { onImportanceSelected(importance) }
                            )
                            .background(
                                if (importance == importanceSelected) MaterialTheme.colors.secondary
                                else MaterialTheme.colors.background
                            )
                            .padding(8.dp)
                    ) {
                        Text(
                            text = when (importance) {
                                Importance.LOW -> "Нет"
                                Importance.COMMON -> "Низкий"
                                Importance.HIGH -> "!!Высокий"
                            },
                            style = MaterialTheme.typography.button,
                            color = if (importance == importanceSelected) MaterialTheme.colors.onSecondary
                            else MaterialTheme.colors.onBackground
                        )
                    }
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
        }
    }
}

@Preview(widthDp = 500)
@Composable
fun BottomSheetContentPreview() {
    AppTheme {
        BottomSheetContent(importanceSelected = Importance.COMMON, onImportanceSelected = {})
    }
}
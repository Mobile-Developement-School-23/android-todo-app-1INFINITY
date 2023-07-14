package ru.mirea.ivashechkinav.todo.presentation.fragments.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.models.Importance
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.red
import ru.mirea.ivashechkinav.todo.presentation.fragments.tertiary

@Composable
fun ImportanceSelector(
    importance: Importance,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(
                horizontal = 16.dp,
                vertical = 16.dp
            ),
    ) {
        Text(
            text = stringResource(id = R.string.importance_text),
            style = MaterialTheme.typography.body1,
            color = MaterialTheme.colors.onBackground,
        )
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            when (importance) {
                Importance.LOW -> "Нет"
                Importance.COMMON -> "Низкий"
                Importance.HIGH -> "!!Высокий"
            },
            style = MaterialTheme.typography.subtitle1,
            color =
            if(importance == Importance.HIGH)
                MaterialTheme.colors.red
            else
                MaterialTheme.colors.tertiary,
        )
    }
}

@Preview
@Composable
fun ImportanceSelectorPreview() {
    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colors.background)) {
            ImportanceSelector(
                importance = Importance.LOW,
                onClick = {}
            )
        }
    }
}
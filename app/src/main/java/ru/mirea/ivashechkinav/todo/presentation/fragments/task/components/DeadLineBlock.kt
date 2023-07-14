package ru.mirea.ivashechkinav.todo.presentation.fragments.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Switch
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.tertiary
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

@Composable
fun DeadlineBlock(
    deadline: LocalDate?,
    clearDeadline: () -> Unit,
    showDatePicker: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Column {
            Text(
                text = stringResource(id = R.string.done_before),
                style = MaterialTheme.typography.body1,
                color = MaterialTheme.colors.onBackground,
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = deadline?.format(
                    DateTimeFormatter.ofLocalizedDate(
                        FormatStyle.LONG
                    )
                ) ?: "",
                style = MaterialTheme.typography.subtitle1,
                color = MaterialTheme.colors.tertiary,
            )
        }
        Switch(checked = deadline != null, onCheckedChange = {
            if (it)
                showDatePicker()
            else
                clearDeadline()
        })
    }
}

@Preview
@Composable
fun DeadlineSelectorPreview() {
    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colors.background)) {
            DeadlineBlock(
                deadline = LocalDate.now(),
                clearDeadline = {},
                showDatePicker = {}
            )
        }
    }
}
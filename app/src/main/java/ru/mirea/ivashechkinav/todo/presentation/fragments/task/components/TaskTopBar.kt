package ru.mirea.ivashechkinav.todo.presentation.fragments.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Icon
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme

@Composable
fun TaskTopBar(
    onCloseClick: () -> Unit,
    onSaveClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            modifier = Modifier
                .clickable(onClick = onCloseClick)
                .padding(16.dp),
            imageVector = Icons.Filled.Close,
            tint = MaterialTheme.colors.onBackground,
            contentDescription = null
        )
        Text(
            modifier = Modifier
                .clickable(onClick = onSaveClick)
                .padding(16.dp),
            style = MaterialTheme.typography.button,
            text = stringResource(id = R.string.save_string).uppercase(),
            color = MaterialTheme.colors.secondary,
        )
    }
}

@Preview
@Composable
fun TaskTopBarPreview() {
    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colors.background)) {
            TaskTopBar(
                onCloseClick = {},
                onSaveClick = {}
            )
        }
    }
}
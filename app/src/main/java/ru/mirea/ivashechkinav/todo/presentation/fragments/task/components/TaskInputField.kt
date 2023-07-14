package ru.mirea.ivashechkinav.todo.presentation.fragments.task.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material.Card
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.separator
import ru.mirea.ivashechkinav.todo.presentation.fragments.tertiary

@Composable
fun TaskInputField(
    text: String,
    onChanged: (String) -> Unit,
) {
    Card(
        elevation = 6.dp,
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .clip(shape = MaterialTheme.shapes.large)
            .background(MaterialTheme.colors.surface)
    ) {
        BasicTextField(
            value = text,
            cursorBrush = SolidColor(MaterialTheme.colors.onSurface),
            textStyle = MaterialTheme.typography.body1.copy(
                color = MaterialTheme.colors.onSurface
            ),

            onValueChange = onChanged,
            decorationBox = { innerTextField ->
                Box(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth()
                        .border(1.dp, color = MaterialTheme.colors.separator)
                        .padding(
                            horizontal = 8.dp,
                            vertical = 8.dp
                        ),
                ) {
                    if (text.isEmpty()) {
                        Text(
                            text = stringResource(id = R.string.todo_item_text_hint),
                            style = MaterialTheme.typography.body1,
                            color = MaterialTheme.colors.tertiary
                        )
                    }
                    innerTextField()
                }
            }
        )
    }
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun TaskInputFieldPreview() {
    AppTheme {
        Surface(modifier = Modifier.background(MaterialTheme.colors.background)) {
            TaskInputField(text = "My task text", onChanged = {})
        }
    }
}
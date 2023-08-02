package ru.mirea.ivashechkinav.todo.presentation.fragments.settings

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.Icon
import androidx.compose.material.IconButton
import androidx.compose.material.MaterialTheme
import androidx.compose.material.RadioButton
import androidx.compose.material.Scaffold
import androidx.compose.material.Text
import androidx.compose.material.TopAppBar
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import ru.mirea.ivashechkinav.todo.R
import ru.mirea.ivashechkinav.todo.data.settings.UiTheme
import ru.mirea.ivashechkinav.todo.presentation.fragments.AppTheme

@Composable
fun SettingFragmentComposable(
    viewModel: SettingsViewModel?,
    state: State<UiTheme?>,
) {
    val context = LocalContext.current

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.settings_text)) },
                navigationIcon = {
                    IconButton(onClick = { viewModel?.navigateBack() }) {
                        Icon(
                            Icons.Default.Close,
                            contentDescription = context.getString(R.string.back_description)
                        )
                    }
                },
                backgroundColor = MaterialTheme.colors.surface
            )
        },
        content = { padding ->
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Text(
                    text = stringResource(R.string.choose_theme_text),
                    style = MaterialTheme.typography.h2
                )

                Row(
                    modifier = Modifier
                        .align(Alignment.CenterHorizontally)
                        .padding(top = 8.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .height(80.dp)
                            .width(100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RadioButton(
                            modifier = Modifier.semantics {
                                contentDescription =
                                    context.getString(R.string.choose_light_theme_description)
                            },
                            selected = state.value == UiTheme.LIGHT,
                            onClick = { viewModel?.onThemeSelected(UiTheme.LIGHT) }
                        )
                        Text(text = "Light")
                    }

                    Column(
                        modifier = Modifier
                            .height(80.dp)
                            .width(100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RadioButton(
                            modifier = Modifier.semantics {
                                contentDescription =
                                    context.getString(R.string.choose_system_theme_description)
                            },
                            selected = state.value == UiTheme.SYSTEM,
                            onClick = { viewModel?.onThemeSelected(UiTheme.SYSTEM) }
                        )
                        Text(text = "System")
                    }

                    Column(
                        modifier = Modifier
                            .height(80.dp)
                            .width(100.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        RadioButton(
                            modifier = Modifier.semantics {
                                contentDescription =
                                    context.getString(R.string.choose_dark_theme_description)
                            },
                            selected = state.value == UiTheme.DARK,
                            onClick = { viewModel?.onThemeSelected(UiTheme.DARK) }
                        )
                        Text(text = "Dark")
                    }

                }
            }
        }
    )
}

@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_NO)
@Preview(uiMode = android.content.res.Configuration.UI_MODE_NIGHT_YES)
@Composable
fun SettingFragmentPreview() {
    val state = remember {
        mutableStateOf(
            UiTheme.LIGHT
        )
    }
    AppTheme {
        SettingFragmentComposable(viewModel = null, state = state)
    }
}



package de.mm20.launcher2.plugin.openweathermap

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.dynamicDarkColorScheme
import androidx.compose.material3.dynamicLightColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.net.toUri
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

class SettingsActivity : AppCompatActivity() {

    private var apiKey by mutableStateOf("")
    private var apiKeyState by mutableStateOf<ApiKeyState?>(null)
    private var saving by mutableStateOf(false)
    private val apiClient = OwmApiClient(this)
    private var savedApiKey = apiClient.apiKey

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent {
            val savedApiKey by savedApiKey.collectAsState("")
            LaunchedEffect(savedApiKey) {
                apiKey = savedApiKey ?: ""
            }
            val darkMode = isSystemInDarkTheme()
            val theme = if (darkMode) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicDarkColorScheme(this)
                } else {
                    darkColorScheme()
                }
            } else {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                    dynamicLightColorScheme(this)
                } else {
                    lightColorScheme()
                }
            }
            MaterialTheme(theme) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(MaterialTheme.colorScheme.surface)
                        .systemBarsPadding()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.Start,
                ) {

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(stringResource(R.string.setup_step_n, 1), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.instruction_register, "OpenWeatherMap"),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )
                        Text(
                            "https://home.openweathermap.org/api_keys",
                            style = MaterialTheme.typography.bodyMedium,
                            textDecoration = TextDecoration.Underline,
                            color = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.padding(top = 8.dp).clickable {
                                startActivity(Intent(Intent.ACTION_VIEW).apply {
                                    data = "https://home.openweathermap.org/api_keys".toUri()
                                })
                            }
                        )
                    }

                    HorizontalDivider()

                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp),
                        horizontalAlignment = Alignment.Start,
                    ) {
                        Text(stringResource(R.string.setup_step_n, 2), style = MaterialTheme.typography.titleLarge)
                        Text(
                            stringResource(R.string.instruction_enter_key),
                            style = MaterialTheme.typography.bodyMedium,
                            modifier = Modifier.padding(top = 8.dp)
                        )

                        Box(
                            modifier = Modifier
                                .padding(vertical = 16.dp)
                                .background(
                                    MaterialTheme.colorScheme.secondaryContainer,
                                    MaterialTheme.shapes.medium
                                )
                        ) {
                            Text(
                                stringResource(R.string.note_api_key),
                                style = MaterialTheme.typography.bodySmall,
                                modifier = Modifier.padding(16.dp),
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                            )
                        }

                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally,
                        ) {
                            OutlinedTextField(
                                modifier = Modifier.fillMaxWidth(),
                                enabled = apiKeyState != ApiKeyState.Saving,
                                value = apiKey,
                                onValueChange = {
                                    apiKey = it
                                    apiKeyState = null
                                },
                                label = {
                                    Text(stringResource(id = R.string.input_api_key))
                                },
                                singleLine = true,
                                supportingText = when (apiKeyState) {
                                    ApiKeyState.Saved -> {
                                        { Text(stringResource(R.string.api_key_state_saved)) }
                                    }

                                    ApiKeyState.Invalid -> {
                                        { Text(stringResource(R.string.api_key_state_invalid)) }
                                    }

                                    ApiKeyState.Error -> {
                                        { Text(stringResource(R.string.api_key_state_error)) }
                                    }

                                    else -> null
                                },
                                isError = apiKeyState == ApiKeyState.Invalid || apiKeyState == ApiKeyState.Error,
                                trailingIcon = when (apiKeyState) {
                                    ApiKeyState.Saved -> {
                                        { Icon(Icons.Rounded.CheckCircle, null) }
                                    }

                                    ApiKeyState.Invalid -> {
                                        { Icon(Icons.Rounded.Error, null) }
                                    }

                                    ApiKeyState.Error -> {
                                        { Icon(Icons.Rounded.Error, null) }
                                    }

                                    else -> null
                                },
                            )
                        }
                        Button(
                            enabled = apiKeyState != ApiKeyState.Saving && apiKey.isNotBlank() && apiKey != savedApiKey,
                            contentPadding = ButtonDefaults.ButtonWithIconContentPadding,
                            onClick = { saveApiKey(apiKey) },
                            modifier = Modifier.align(Alignment.End)
                        ) {
                            if (apiKeyState == ApiKeyState.Saving) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(ButtonDefaults.IconSize),
                                    color = LocalContentColor.current,
                                    strokeWidth = 2.dp,
                                )
                            } else {
                                Icon(
                                    Icons.Rounded.Save,
                                    null,
                                    modifier = Modifier.size(ButtonDefaults.IconSize)
                                )
                            }
                            Text(
                                text = stringResource(R.string.api_key_save_button),
                                modifier = Modifier.padding(start = ButtonDefaults.IconSpacing)
                            )
                        }
                    }
                }
            }
        }
    }

    private fun saveApiKey(apiKey: String) {
        lifecycleScope.launch {
            apiKeyState = ApiKeyState.Saving
            try {
                val valid = apiClient.testApiKey(apiKey)
                if (valid) {
                    apiClient.setApiKey(apiKey)
                    apiKeyState = ApiKeyState.Saved
                } else {
                    apiKeyState = ApiKeyState.Invalid
                }
            } catch (e: Exception) {
                apiKeyState = ApiKeyState.Error
                Log.e("SettingsActivity", "An error occured", e)
            }
        }
    }
}

enum class ApiKeyState {
    Saving,
    Saved,
    Invalid,
    Error,
}
package de.mm20.launcher2.plugin.openweathermap

import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CornerSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Error
import androidx.compose.material.icons.rounded.Save
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.LocalContentColor
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
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
import androidx.compose.ui.unit.dp
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
                        .background(MaterialTheme.colorScheme.surface),
                    horizontalAlignment = Alignment.CenterHorizontally,
                ) {

                    Spacer(
                        modifier = Modifier
                            .weight(1f)
                            .statusBarsPadding()
                    )
                    Surface(
                        modifier = Modifier
                            .fillMaxWidth()
                            .weight(1.5f),
                        color = MaterialTheme.colorScheme.surfaceContainer,
                        shape = MaterialTheme.shapes.extraLarge.copy(
                            bottomEnd = CornerSize(0), bottomStart = CornerSize(0)
                        )
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .padding(16.dp)
                                .navigationBarsPadding()
                        ) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                            ) {
                                Text(
                                    text = stringResource(id = R.string.app_name),
                                    style = MaterialTheme.typography.headlineSmall
                                )
                                Column(
                                    modifier = Modifier
                                        .weight(1f)
                                        .fillMaxWidth(),
                                    verticalArrangement = Arrangement.Center,
                                    horizontalAlignment = Alignment.CenterHorizontally,
                                ) {
                                    TextField(
                                        modifier = Modifier.fillMaxWidth(),
                                        enabled = apiKeyState != ApiKeyState.Saving,
                                        value = apiKey, onValueChange = {
                                            apiKey = it
                                            apiKeyState = null
                                        }, label = {
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
                                        isError = apiKeyState == ApiKeyState.Invalid ||  apiKeyState == ApiKeyState.Error,
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
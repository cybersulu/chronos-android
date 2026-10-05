package com.example

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.notification.CountdownAlertReceiver
import com.example.ui.screens.AddEditCountdownScreen
import com.example.ui.screens.CountdownDetailScreen
import com.example.ui.screens.CountdownListScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.CountdownViewModel
import com.example.widget.CountdownAppWidgetProvider

sealed interface Screen {
    data object List : Screen
    data class Detail(val timerId: Long) : Screen
    data object Add : Screen
    data class Edit(val timerId: Long) : Screen
}

class MainActivity : ComponentActivity() {

    private val viewModel: CountdownViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        val initialTimerId = extractTimerId(intent)

        // Ensure widgets are scheduled to tick every minute on the minute
        CountdownAppWidgetProvider.scheduleNextMinuteTick(this)

        setContent {
            MyApplicationTheme {
                MainApp(
                    viewModel = viewModel,
                    initialTimerId = initialTimerId
                )
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
    }

    private fun extractTimerId(intent: Intent?): Long? {
        if (intent == null) return null
        val notifId = intent.getLongExtra(CountdownAlertReceiver.EXTRA_TIMER_ID, -1L)
        if (notifId != -1L) return notifId
        val widgetId = intent.getLongExtra(CountdownAppWidgetProvider.EXTRA_WIDGET_TIMER_ID, -1L)
        if (widgetId != -1L) return widgetId
        return null
    }
}

@Composable
fun MainApp(
    viewModel: CountdownViewModel,
    initialTimerId: Long?
) {
    var currentScreen by remember {
        mutableStateOf<Screen>(
            if (initialTimerId != null) Screen.Detail(initialTimerId) else Screen.List
        )
    }

    // Request notification permission on Android 13+
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
        val permissionLauncher = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.RequestPermission(),
            onResult = { /* User response handled */ }
        )
        LaunchedEffect(Unit) {
            permissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    Surface(modifier = Modifier.fillMaxSize()) {
        AnimatedContent(
            targetState = currentScreen,
            transitionSpec = {
                when {
                    targetState is Screen.Detail || targetState is Screen.Add || targetState is Screen.Edit -> {
                        slideInHorizontally { width -> width } + fadeIn() togetherWith
                                slideOutHorizontally { width -> -width / 2 } + fadeOut()
                    }
                    else -> {
                        slideInHorizontally { width -> -width / 2 } + fadeIn() togetherWith
                                slideOutHorizontally { width -> width } + fadeOut()
                    }
                }
            },
            label = "screen_transition"
        ) { screen ->
            when (screen) {
                is Screen.List -> {
                    CountdownListScreen(
                        viewModel = viewModel,
                        onNavigateToDetail = { currentScreen = Screen.Detail(it) },
                        onNavigateToAdd = { currentScreen = Screen.Add },
                        onNavigateToEdit = { currentScreen = Screen.Edit(it) }
                    )
                }

                is Screen.Detail -> {
                    CountdownDetailScreen(
                        timerId = screen.timerId,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.List },
                        onNavigateToEdit = { currentScreen = Screen.Edit(it) }
                    )
                }

                is Screen.Add -> {
                    AddEditCountdownScreen(
                        timerId = 0L,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.List }
                    )
                }

                is Screen.Edit -> {
                    AddEditCountdownScreen(
                        timerId = screen.timerId,
                        viewModel = viewModel,
                        onNavigateBack = { currentScreen = Screen.Detail(screen.timerId) }
                    )
                }
            }
        }
    }
}

package com.autoreply.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.navigation.NavOptions
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.autoreply.app.data.ModeRepository
import com.autoreply.app.data.SessionLogPreferences
import com.autoreply.app.data.WhitelistPreferences
import com.autoreply.app.ui.AboutScreen
import com.autoreply.app.ui.MainScreen
import com.autoreply.app.ui.MainViewModel
import com.autoreply.app.ui.RequestPermissionsIfNeeded
import com.autoreply.app.ui.SessionLogScreen
import com.autoreply.app.ui.SessionLogViewModel
import com.autoreply.app.ui.SettingsScreen
import com.autoreply.app.ui.WhitelistScreen
import com.autoreply.app.ui.WhitelistViewModel
import com.autoreply.app.ui.theme.AutoReplyTheme

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val repository = ModeRepository(applicationContext)
        setContent {
            AutoReplyTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    val snackbarHostState = remember { androidx.compose.material3.SnackbarHostState() }
                    val viewModel = remember { MainViewModel(repository) }
                    val whitelistPrefs = remember { WhitelistPreferences(applicationContext) }
                    val whitelistViewModel = remember { WhitelistViewModel(whitelistPrefs) }
                    val sessionLogPrefs = remember { SessionLogPreferences(applicationContext) }
                    val sessionLogViewModel = remember { SessionLogViewModel(sessionLogPrefs) }

                    val navOptions = remember {
                        NavOptions.Builder().setLaunchSingleTop(true).build()
                    }
                    val onOpenSettings = remember(navController) { { navController.navigate("settings", navOptions) } }
                    val onOpenWhitelist = remember(navController) { { navController.navigate("whitelist", navOptions) } }
                    val onOpenSessionLog = remember(navController) { { navController.navigate("sessionlog", navOptions) } }
                    val onOpenAbout = remember(navController) { { navController.navigate("about", navOptions) } }
                    val onBack: () -> Unit = remember(navController) {
                        { navController.popBackStack(); Unit }
                    }

                    RequestPermissionsIfNeeded()
                    NavHost(
                        navController = navController,
                        startDestination = "main",
                        enterTransition = { slideInHorizontally(initialOffsetX = { it }, animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)) },
                        exitTransition = { slideOutHorizontally(targetOffsetX = { -it }, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)) },
                        popEnterTransition = { slideInHorizontally(initialOffsetX = { -it }, animationSpec = tween(200)) + fadeIn(animationSpec = tween(200)) },
                        popExitTransition = { slideOutHorizontally(targetOffsetX = { it }, animationSpec = tween(200)) + fadeOut(animationSpec = tween(200)) }
                    ) {
                        composable("main") {
                            MainScreen(
                                viewModel = viewModel,
                                whitelistViewModel = whitelistViewModel,
                                onOpenSettings = onOpenSettings,
                                onOpenWhitelist = onOpenWhitelist,
                                sessionLogViewModel = sessionLogViewModel,
                                onOpenSessionLog = onOpenSessionLog,
                                onOpenAbout = onOpenAbout,
                                snackbarHostState = snackbarHostState
                            )
                        }
                        composable("settings") {
                            SettingsScreen(viewModel = viewModel, onBack = onBack)
                        }
                        composable("whitelist") {
                            WhitelistScreen(viewModel = whitelistViewModel, onBack = onBack)
                        }
                        composable("sessionlog") {
                            SessionLogScreen(viewModel = sessionLogViewModel, onBack = onBack)
                        }
                        composable("about") {
                            AboutScreen(onBack = onBack)
                        }
                    }
                }
            }
        }
    }
}

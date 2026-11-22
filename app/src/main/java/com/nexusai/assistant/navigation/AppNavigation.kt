package com.nexusai.assistant.navigation

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.nexusai.assistant.NexusApplication
import com.nexusai.assistant.ui.components.AiOrb
import com.nexusai.assistant.ui.components.OrbState
import com.nexusai.assistant.ui.screens.AuthViewModel
import com.nexusai.assistant.ui.screens.CallHistoryScreen
import com.nexusai.assistant.ui.screens.CallHistoryViewModel
import com.nexusai.assistant.ui.screens.ChatScreen
import com.nexusai.assistant.ui.screens.ChatViewModel
import com.nexusai.assistant.ui.screens.ConversationsScreen
import com.nexusai.assistant.ui.screens.ConversationsViewModel
import com.nexusai.assistant.ui.screens.DashboardScreen
import com.nexusai.assistant.ui.screens.IncomingCallScreen
import com.nexusai.assistant.ui.screens.LoginScreen
import com.nexusai.assistant.ui.screens.OtpVerificationScreen
import com.nexusai.assistant.ui.screens.ReceptionistViewModel
import com.nexusai.assistant.ui.screens.BackendStatusScreen
import com.nexusai.assistant.ui.screens.DataViewModel
import com.nexusai.assistant.ui.screens.PrivacyScreen
import com.nexusai.assistant.ui.screens.ProfileScreen
import com.nexusai.assistant.ui.screens.SessionState
import com.nexusai.assistant.ui.screens.SettingsScreen
import com.nexusai.assistant.ui.screens.SettingsViewModel
import com.nexusai.assistant.ui.screens.SignupScreen
import com.nexusai.assistant.ui.screens.VoiceScreen

@Composable
fun AppNavigation() {
    val app = LocalContext.current.applicationContext as NexusApplication
    val authVm: AuthViewModel = viewModel(factory = AuthViewModel.factory(app.authRepository))
    val sessionState by authVm.sessionState.collectAsStateWithLifecycle()

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        when (sessionState) {
            SessionState.Loading -> Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) { AiOrb(state = OrbState.Thinking) }

            else -> MainNav(authVm = authVm, sessionState = sessionState)
        }
    }
}

@Composable
private fun MainNav(authVm: AuthViewModel, sessionState: SessionState) {
    val context = LocalContext.current
    val app = context.applicationContext as NexusApplication
    val navController = rememberNavController()
    val start = remember {
        if (sessionState is SessionState.LoggedIn) Routes.DASHBOARD else Routes.LOGIN
    }
    val backStack by navController.currentBackStackEntryAsState()
    val route = backStack?.destination?.route

    // Keeps navigation in sync with login, logout and account deletion.
    LaunchedEffect(sessionState, route) {
        if (route == null) return@LaunchedEffect
        val onAuthScreen = route in Routes.authRoutes
        if (sessionState is SessionState.LoggedIn && onAuthScreen) {
            navController.navigate(Routes.DASHBOARD) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        } else if (sessionState is SessionState.LoggedOut && !onAuthScreen) {
            navController.navigate(Routes.LOGIN) {
                popUpTo(navController.graph.id) { inclusive = true }
                launchSingleTop = true
            }
        }
    }

    val phone = (sessionState as? SessionState.LoggedIn)?.session?.phoneE164.orEmpty()
    val dataVm: DataViewModel = viewModel(
        factory = viewModelFactory {
            initializer { DataViewModel(app.chatRepository, app.callRepository, app.settingsStore) }
        }
    )

    NavHost(navController = navController, startDestination = start) {
        composable(Routes.LOGIN) {
            LoginScreen(
                authVm = authVm,
                onOtpSent = { navController.navigate(Routes.OTP) },
                onGoSignup = { navController.navigate(Routes.SIGNUP) }
            )
        }
        composable(Routes.SIGNUP) {
            SignupScreen(
                authVm = authVm,
                onOtpSent = { navController.navigate(Routes.OTP) },
                onGoLogin = { navController.popBackStack() }
            )
        }
        composable(Routes.OTP) {
            OtpVerificationScreen(
                authVm = authVm,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.DASHBOARD) {
            DashboardScreen(
                phone = phone,
                isDemo = app.authRepository.isDemo,
                onOpen = { target -> navController.navigate(target) },
                onLogout = authVm::logout
            )
        }
        composable(
            route = Routes.CHAT_PATTERN,
            arguments = listOf(
                navArgument("conversationId") {
                    type = NavType.LongType
                    defaultValue = -1L
                }
            )
        ) { entry ->
            val id = entry.arguments?.getLong("conversationId") ?: -1L
            val vm: ChatViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ChatViewModel(app.chatRepository, id.takeIf { it >= 0 }) }
                }
            )
            ChatScreen(vm = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.CONVERSATIONS) {
            val vm: ConversationsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ConversationsViewModel(app.chatRepository) }
                }
            )
            ConversationsScreen(
                vm = vm,
                onBack = { navController.popBackStack() },
                onOpen = { id -> navController.navigate(Routes.chat(id)) },
                onNewChat = { navController.navigate(Routes.chat(-1L)) }
            )
        }
        composable(Routes.INCOMING_CALL) {
            val vm: ReceptionistViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { ReceptionistViewModel(app.settingsStore, app.callRepository) }
                }
            )
            IncomingCallScreen(
                vm = vm,
                phone = phone,
                onBack = { navController.popBackStack() },
                onOpenHistory = { navController.navigate(Routes.CALL_HISTORY) }
            )
        }
        composable(Routes.CALL_HISTORY) {
            val vm: CallHistoryViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { CallHistoryViewModel(app.callRepository) }
                }
            )
            CallHistoryScreen(vm = vm, onBack = { navController.popBackStack() })
        }
        composable(Routes.VOICE) {
            VoiceScreen(onBack = { navController.popBackStack() })
        }
        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(
                factory = viewModelFactory {
                    initializer { SettingsViewModel(app.settingsStore) }
                }
            )
            SettingsScreen(
                vm = vm,
                aiIsMock = app.aiProvider.isMock,
                onBack = { navController.popBackStack() }
            )
        }
        composable(Routes.PRIVACY) {
            PrivacyScreen(vm = dataVm, onBack = { navController.popBackStack() })
        }
        composable(Routes.PROFILE) {
            ProfileScreen(
                phone = phone,
                isDemo = app.authRepository.isDemo,
                onBack = { navController.popBackStack() },
                onLogout = authVm::logout,
                onDeleteAccount = { dataVm.wipeEverythingAsync { authVm.deleteAccount() } }
            )
        }
        composable(Routes.BACKEND_STATUS) {
            BackendStatusScreen(
                api = app.backendApi,
                isDemo = app.authRepository.isDemo,
                onBack = { navController.popBackStack() }
            )
        }
    }
}

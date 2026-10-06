package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import com.example.data.firebase.FirebaseRepository
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.PostDetailScreen
import com.example.ui.screens.ReporterLoginScreen
import com.example.ui.screens.ReporterProfileScreen
import com.example.ui.screens.ReporterUploadScreen
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DrikqNewsTheme

sealed interface Screen {
    data object Login : Screen
    data object Home : Screen
    data object ReporterLogin : Screen
    data object ReporterUpload : Screen
    data object AdminPanel : Screen
    data class PostDetail(val postId: String) : Screen
    data class ReporterProfile(val reporterId: String) : Screen
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            DrikqNewsTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = DarkBackground
                ) {
                    DrikqNewsAppNavigation()
                }
            }
        }
    }
}

@Composable
fun DrikqNewsAppNavigation() {
    val currentUser by FirebaseRepository.currentUser.collectAsState()
    var currentScreen by remember(currentUser) {
        mutableStateOf<Screen>(if (currentUser != null) Screen.Home else Screen.Login)
    }

    when (val screen = currentScreen) {
        is Screen.Login -> {
            LoginScreen(
                onLoginSuccess = { currentScreen = Screen.Home },
                onNavigateToReporterLogin = { currentScreen = Screen.ReporterLogin },
                onNavigateToAdminPanel = { currentScreen = Screen.AdminPanel }
            )
        }
        is Screen.Home -> {
            HomeScreen(
                onNavigateToReporterProfile = { repId -> currentScreen = Screen.ReporterProfile(repId) },
                onNavigateToPostDetail = { postId -> currentScreen = Screen.PostDetail(postId) },
                onNavigateToReporterUpload = { currentScreen = Screen.ReporterUpload },
                onNavigateToAdminPanel = { currentScreen = Screen.AdminPanel },
                onLogout = { currentScreen = Screen.Login }
            )
        }
        is Screen.ReporterLogin -> {
            BackHandler { currentScreen = if (currentUser != null) Screen.Home else Screen.Login }
            ReporterLoginScreen(
                onLoginSuccess = { currentScreen = Screen.ReporterUpload },
                onBack = { currentScreen = if (currentUser != null) Screen.Home else Screen.Login }
            )
        }
        is Screen.ReporterUpload -> {
            BackHandler { currentScreen = Screen.Home }
            ReporterUploadScreen(
                onUploadCompleteRedirectToFeed = { currentScreen = Screen.Home },
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.AdminPanel -> {
            BackHandler { currentScreen = if (currentUser != null) Screen.Home else Screen.Login }
            AdminPanelScreen(
                onBack = { currentScreen = if (currentUser != null) Screen.Home else Screen.Login }
            )
        }
        is Screen.PostDetail -> {
            BackHandler { currentScreen = Screen.Home }
            PostDetailScreen(
                postId = screen.postId,
                onBack = { currentScreen = Screen.Home }
            )
        }
        is Screen.ReporterProfile -> {
            BackHandler { currentScreen = Screen.Home }
            ReporterProfileScreen(
                reporterId = screen.reporterId,
                onBack = { currentScreen = Screen.Home },
                onPostClick = { postId -> currentScreen = Screen.PostDetail(postId) }
            )
        }
    }
}

package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.unit.dp
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.Post
import com.example.ui.screens.AdminPanelScreen
import com.example.ui.screens.FullScreenDetailScreen
import com.example.ui.screens.HomeFeedScreen
import com.example.ui.screens.LoginScreen
import com.example.ui.screens.ReporterLoginScreen
import com.example.ui.screens.ReporterProfileScreen
import com.example.ui.screens.ReporterUploadScreen
import com.example.ui.screens.UserProfileScreen
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.theme.NewsRed
import com.example.ui.theme.SlateGray

enum class AppDestination {
    LOGIN,
    REPORTER_LOGIN,
    REPORTER_UPLOAD,
    ADMIN_PANEL,
    MAIN_FEED_CONTAINER,
    FULLSCREEN_DETAIL,
    REPORTER_PROFILE
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MyApplicationTheme {
                DrikqNewsApp()
            }
        }
    }
}

@Composable
fun DrikqNewsApp() {
    var currentDestination by remember { mutableStateOf(AppDestination.LOGIN) }
    var selectedBottomTab by remember { mutableIntStateOf(0) } // 0 = Home, 1 = Profile

    var selectedPostForDetail by remember { mutableStateOf<Post?>(null) }
    var selectedReporterIdForProfile by remember { mutableStateOf<String?>(null) }

    val currentUser by FirebaseRepository.currentUser.collectAsState()

    // Navigation Back Handling
    BackHandler(enabled = currentDestination != AppDestination.LOGIN) {
        when (currentDestination) {
            AppDestination.REPORTER_LOGIN -> currentDestination = AppDestination.LOGIN
            AppDestination.REPORTER_UPLOAD -> currentDestination = AppDestination.LOGIN
            AppDestination.ADMIN_PANEL -> currentDestination = AppDestination.LOGIN
            AppDestination.FULLSCREEN_DETAIL -> currentDestination = AppDestination.MAIN_FEED_CONTAINER
            AppDestination.REPORTER_PROFILE -> currentDestination = AppDestination.MAIN_FEED_CONTAINER
            AppDestination.MAIN_FEED_CONTAINER -> {
                if (selectedBottomTab != 0) {
                    selectedBottomTab = 0
                } else {
                    currentDestination = AppDestination.LOGIN
                }
            }
            else -> {}
        }
    }

    when (currentDestination) {
        AppDestination.LOGIN -> {
            LoginScreen(
                onLoginSuccess = {
                    selectedBottomTab = 0
                    currentDestination = AppDestination.MAIN_FEED_CONTAINER
                },
                onNavigateToReporterLogin = {
                    currentDestination = AppDestination.REPORTER_LOGIN
                },
                onNavigateToAdminPanel = {
                    currentDestination = AppDestination.ADMIN_PANEL
                }
            )
        }

        AppDestination.REPORTER_LOGIN -> {
            ReporterLoginScreen(
                onLoginSuccess = {
                    currentDestination = AppDestination.REPORTER_UPLOAD
                },
                onBackToMainLogin = {
                    currentDestination = AppDestination.LOGIN
                }
            )
        }

        AppDestination.REPORTER_UPLOAD -> {
            ReporterUploadScreen(
                onUploadCompleteRedirectToFeed = {
                    selectedBottomTab = 0
                    currentDestination = AppDestination.MAIN_FEED_CONTAINER
                },
                onBack = {
                    currentDestination = AppDestination.LOGIN
                }
            )
        }

        AppDestination.ADMIN_PANEL -> {
            AdminPanelScreen(
                onBack = {
                    currentDestination = AppDestination.LOGIN
                }
            )
        }

        AppDestination.FULLSCREEN_DETAIL -> {
            selectedPostForDetail?.let { post ->
                FullScreenDetailScreen(
                    post = post,
                    onBack = {
                        currentDestination = AppDestination.MAIN_FEED_CONTAINER
                    },
                    onReporterClick = { repId ->
                        selectedReporterIdForProfile = repId
                        currentDestination = AppDestination.REPORTER_PROFILE
                    }
                )
            } ?: run {
                currentDestination = AppDestination.MAIN_FEED_CONTAINER
            }
        }

        AppDestination.REPORTER_PROFILE -> {
            selectedReporterIdForProfile?.let { repId ->
                ReporterProfileScreen(
                    reporterId = repId,
                    onBack = {
                        currentDestination = AppDestination.MAIN_FEED_CONTAINER
                    },
                    onPostClick = { post ->
                        selectedPostForDetail = post
                        currentDestination = AppDestination.FULLSCREEN_DETAIL
                    }
                )
            } ?: run {
                currentDestination = AppDestination.MAIN_FEED_CONTAINER
            }
        }

        AppDestination.MAIN_FEED_CONTAINER -> {
            Scaffold(
                bottomBar = {
                    NavigationBar(
                        containerColor = DarkSurface,
                        tonalElevation = 8.dp,
                        modifier = Modifier.testTag("main_bottom_nav")
                    ) {
                        // Tab 0: Home
                        NavigationBarItem(
                            selected = selectedBottomTab == 0,
                            onClick = { selectedBottomTab = 0 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedBottomTab == 0) Icons.Filled.Home else Icons.Outlined.Home,
                                    contentDescription = "Home"
                                )
                            },
                            label = { Text("Home") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = NewsRed,
                                indicatorColor = NewsRed,
                                unselectedIconColor = SlateGray,
                                unselectedTextColor = SlateGray
                            ),
                            modifier = Modifier.testTag("bottom_nav_home")
                        )

                        // Tab 1: Profile (last tab)
                        NavigationBarItem(
                            selected = selectedBottomTab == 1,
                            onClick = { selectedBottomTab = 1 },
                            icon = {
                                Icon(
                                    imageVector = if (selectedBottomTab == 1) Icons.Filled.Person else Icons.Outlined.Person,
                                    contentDescription = "Profile"
                                )
                            },
                            label = { Text("Profile") },
                            colors = NavigationBarItemDefaults.colors(
                                selectedIconColor = Color.White,
                                selectedTextColor = NewsRed,
                                indicatorColor = NewsRed,
                                unselectedIconColor = SlateGray,
                                unselectedTextColor = SlateGray
                            ),
                            modifier = Modifier.testTag("bottom_nav_profile")
                        )
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    if (selectedBottomTab == 0) {
                        HomeFeedScreen(
                            onPostClick = { post ->
                                selectedPostForDetail = post
                                currentDestination = AppDestination.FULLSCREEN_DETAIL
                            },
                            onReporterClick = { repId ->
                                selectedReporterIdForProfile = repId
                                currentDestination = AppDestination.REPORTER_PROFILE
                            }
                        )
                    } else {
                        UserProfileScreen(
                            onLogout = {
                                currentDestination = AppDestination.LOGIN
                            },
                            onNavigateToReporterPortal = {
                                currentDestination = AppDestination.REPORTER_LOGIN
                            },
                            onNavigateToAdminPanel = {
                                currentDestination = AppDestination.ADMIN_PANEL
                            }
                        )
                    }
                }
            }
        }
    }
}

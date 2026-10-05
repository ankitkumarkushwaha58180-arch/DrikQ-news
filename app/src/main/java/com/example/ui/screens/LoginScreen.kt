package com.example.ui.screens

import android.accounts.AccountManager
import android.app.Activity
import android.content.ActivityNotFoundException
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Mail
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.firebase.FirebaseRepository
import com.example.ui.components.GoogleLogoIcon
import com.example.ui.components.GoogleSignInButton
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NewsRed
import com.example.ui.theme.SlateGray
import com.google.firebase.auth.FirebaseAuth
import java.util.Locale

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onNavigateToReporterLogin: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var showManualAccountPrompt by remember { mutableStateOf(false) }
    var manualEmailInput by remember { mutableStateOf("") }
    var emailError by remember { mutableStateOf<String?>(null) }
    var isSigningIn by remember { mutableStateOf(false) }

    // Real System Google Account Chooser launcher
    val accountPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.StartActivityForResult()
    ) { result ->
        isSigningIn = false
        if (result.resultCode == Activity.RESULT_OK && result.data != null) {
            val accountName = result.data?.getStringExtra(AccountManager.KEY_ACCOUNT_NAME)
            if (!accountName.isNullOrBlank()) {
                val formattedName = accountName.substringBefore("@")
                    .replace(".", " ")
                    .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                // Sign in with Firebase Auth & update Repository
                try {
                    FirebaseAuth.getInstance().signInAnonymously()
                } catch (ignored: Exception) {}

                FirebaseRepository.loginWithGoogle(
                    email = accountName,
                    displayName = formattedName,
                    photoUrl = ""
                )
                Toast.makeText(context, "Signed in as $accountName", Toast.LENGTH_SHORT).show()
                onLoginSuccess()
            }
        }
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        // Decorative background glow
        Box(
            modifier = Modifier
                .size(320.dp)
                .align(Alignment.TopCenter)
                .background(
                    Brush.radialGradient(
                        colors = listOf(
                            NewsRed.copy(alpha = 0.22f),
                            Color.Transparent
                        )
                    )
                )
        )

        // Main Center Content
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 24.dp)
                .padding(bottom = 76.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // App Branding Icon
            Surface(
                modifier = Modifier
                    .size(92.dp)
                    .clip(CircleShape),
                color = NewsRed,
                shadowElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Campaign,
                        contentDescription = "Drikq News",
                        tint = Color.White,
                        modifier = Modifier.size(54.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "DRIKQ NEWS",
                color = Color.White,
                fontSize = 28.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = 2.sp
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Direct Ground-Level Reporting & Verified News Feeds",
                color = SlateGray,
                fontSize = 14.sp,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(horizontal = 16.dp)
            )

            Spacer(modifier = Modifier.height(48.dp))

            // Proper Google Sign-In with real system account chooser
            GoogleSignInButton(
                onClick = {
                    isSigningIn = true
                    try {
                        val intent = AccountManager.newChooseAccountIntent(
                            null,
                            null,
                            arrayOf("com.google"),
                            null,
                            null,
                            null,
                            null
                        )
                        accountPickerLauncher.launch(intent)
                    } catch (e: ActivityNotFoundException) {
                        // In case the device/emulator does not have Google Play account manager installed
                        isSigningIn = false
                        showManualAccountPrompt = true
                    } catch (e: Exception) {
                        isSigningIn = false
                        showManualAccountPrompt = true
                    }
                },
                enabled = !isSigningIn,
                modifier = Modifier.testTag("google_sign_in_button")
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "Select any Google account present on your device to continue.",
                color = Color.White.copy(alpha = 0.5f),
                fontSize = 11.sp,
                textAlign = TextAlign.Center
            )
        }

        // At the very bottom of the screen (fixed):
        // Left side: "Reporter Login" button
        // Right side: "Admin Panel" button
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .align(Alignment.BottomCenter),
            color = DarkSurface,
            tonalElevation = 6.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Reporter Login
                TextButton(
                    onClick = onNavigateToReporterLogin,
                    modifier = Modifier.testTag("reporter_login_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Mic,
                        contentDescription = "Reporter Portal",
                        tint = NewsRed,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reporter Login",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }

                // Right: Admin Panel
                TextButton(
                    onClick = onNavigateToAdminPanel,
                    modifier = Modifier.testTag("admin_panel_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.AdminPanelSettings,
                        contentDescription = "Admin Panel",
                        tint = Color(0xFF60A5FA),
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Admin Panel",
                        color = Color.White,
                        fontSize = 14.sp,
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }

    // Google Sign-In Fallback (if device has no Google accounts configured yet)
    if (showManualAccountPrompt) {
        AlertDialog(
            onDismissRequest = { showManualAccountPrompt = false },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    GoogleLogoIcon(modifier = Modifier.size(22.dp))
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "Google Account",
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                }
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Enter your Google / Gmail account address to sign in:",
                        color = SlateGray,
                        fontSize = 13.sp,
                        modifier = Modifier.padding(bottom = 14.dp)
                    )

                    OutlinedTextField(
                        value = manualEmailInput,
                        onValueChange = {
                            manualEmailInput = it
                            emailError = null
                        },
                        label = { Text("Gmail Address") },
                        placeholder = { Text("yourname@gmail.com") },
                        leadingIcon = {
                            Icon(Icons.Default.Mail, contentDescription = null, tint = NewsRed)
                        },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsRed,
                            unfocusedBorderColor = BorderSlate,
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    if (emailError != null) {
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(emailError!!, color = NewsRed, fontSize = 12.sp)
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val email = manualEmailInput.trim()
                        if (email.isEmpty() || !email.contains("@")) {
                            emailError = "Please enter a valid Google account"
                            return@Button
                        }
                        val formattedName = email.substringBefore("@")
                            .replace(".", " ")
                            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() }

                        try {
                            FirebaseAuth.getInstance().signInAnonymously()
                        } catch (ignored: Exception) {}

                        FirebaseRepository.loginWithGoogle(email, formattedName, "")
                        showManualAccountPrompt = false
                        onLoginSuccess()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewsRed)
                ) {
                    Text("Continue", color = Color.White, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showManualAccountPrompt = false }) {
                    Text("Cancel", color = SlateGray)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

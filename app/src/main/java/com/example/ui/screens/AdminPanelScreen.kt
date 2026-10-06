package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.data.model.PushNotificationItem
import com.example.data.model.Reporter
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NewsRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.launch

@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val isAdminLoggedIn by FirebaseRepository.isAdminLoggedIn.collectAsState()
    val reporters by FirebaseRepository.reporters.collectAsState()
    val posts by FirebaseRepository.posts.collectAsState()
    val policy by FirebaseRepository.policy.collectAsState()
    val notifications by FirebaseRepository.notifications.collectAsState()

    var adminPasscode by remember { mutableStateOf("") }
    var loginError by remember { mutableStateOf<String?>(null) }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Reporters", "Content", "Privacy Policy", "Push Alerts")

    var showAddReporterDialog by remember { mutableStateOf(false) }
    var newlyCreatedReporter by remember { mutableStateOf<Reporter?>(null) }
    var isSavingReporter by remember { mutableStateOf(false) }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        if (!isAdminLoggedIn) {
            // Admin Login Gate
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                IconButton(
                    onClick = onBack,
                    modifier = Modifier
                        .align(Alignment.Start)
                        .testTag("admin_back_button")
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Back",
                        tint = TextPrimary
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))

                Surface(
                    modifier = Modifier.size(76.dp),
                    shape = CircleShape,
                    color = Color(0xFF2563EB).copy(alpha = 0.12f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(
                            imageVector = Icons.Default.Shield,
                            contentDescription = null,
                            tint = Color(0xFF2563EB),
                            modifier = Modifier.size(42.dp)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                Text(
                    text = "Editorial Admin Access",
                    color = TextPrimary,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )

                Text(
                    text = "Control reporters, content verification, and notifications.",
                    color = TextSecondary,
                    fontSize = 13.sp,
                    modifier = Modifier.padding(top = 6.dp)
                )

                Spacer(modifier = Modifier.height(32.dp))

                OutlinedTextField(
                    value = adminPasscode,
                    onValueChange = {
                        adminPasscode = it
                        loginError = null
                    },
                    label = { Text("Admin Passcode") },
                    placeholder = { Text("Enter Passcode") },
                    visualTransformation = PasswordVisualTransformation(),
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
                    leadingIcon = {
                        Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF2563EB))
                    },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = Color(0xFF2563EB),
                        unfocusedBorderColor = BorderLight,
                        focusedTextColor = TextPrimary,
                        unfocusedTextColor = TextPrimary
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.fillMaxWidth()
                )

                if (loginError != null) {
                    Spacer(modifier = Modifier.height(10.dp))
                    Text(text = loginError!!, color = NewsRed, fontSize = 13.sp)
                }

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val ok = FirebaseRepository.loginAdmin(adminPasscode)
                        if (!ok) {
                            loginError = "Incorrect password"
                        }
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                ) {
                    Text("Enter Editorial Console", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        } else {
            // Main Admin Panel
            Column(modifier = Modifier.fillMaxSize()) {
                // Admin Header
                Surface(
                    color = LightSurface,
                    shadowElevation = 2.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 16.dp, vertical = 10.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            IconButton(onClick = onBack) {
                                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                            }
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = "Admin Console",
                                color = TextPrimary,
                                fontSize = 20.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }

                        TextButton(onClick = { FirebaseRepository.logoutAdmin() }) {
                            Text("Log Out", color = NewsRed, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                // Tabs
                ScrollableTabRow(
                    selectedTabIndex = selectedTabIndex,
                    containerColor = LightSurface,
                    contentColor = TextPrimary,
                    edgePadding = 16.dp
                ) {
                    tabTitles.forEachIndexed { index, title ->
                        Tab(
                            selected = selectedTabIndex == index,
                            onClick = { selectedTabIndex = index },
                            text = {
                                Text(
                                    text = title,
                                    fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                    color = if (selectedTabIndex == index) NewsRed else TextSecondary
                                )
                            }
                        )
                    }
                }

                // Tab Content
                Box(modifier = Modifier.weight(1f)) {
                    when (selectedTabIndex) {
                        0 -> AdminReportersTab(
                            reporters = reporters,
                            onDeleteReporter = { repId -> FirebaseRepository.deleteReporter(repId) }
                        )
                        1 -> AdminContentTab(
                            posts = posts,
                            onApprove = { id -> FirebaseRepository.approvePost(id) },
                            onDelete = { id -> FirebaseRepository.deletePost(id) }
                        )
                        2 -> AdminPolicyTab(
                            currentPolicy = policy,
                            onSave = { p, t ->
                                FirebaseRepository.updatePolicy(p, t)
                                Toast.makeText(context, "Policies updated!", Toast.LENGTH_SHORT).show()
                            }
                        )
                        3 -> AdminNotificationsTab(
                            posts = posts,
                            history = notifications,
                            onSend = { title, body, postId ->
                                FirebaseRepository.sendPushBroadcast(title, body, postId)
                                Toast.makeText(context, "Push broadcasted!", Toast.LENGTH_SHORT).show()
                            }
                        )
                    }

                    // Floating Action Button
                    if (selectedTabIndex == 0) {
                        FloatingActionButton(
                            onClick = { showAddReporterDialog = true },
                            containerColor = NewsRed,
                            contentColor = Color.White,
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .padding(20.dp)
                        ) {
                            Icon(Icons.Default.Add, contentDescription = "Add Reporter")
                        }
                    }
                }
            }
        }
    }

    // Add Reporter Dialog
    if (showAddReporterDialog) {
        var name by remember { mutableStateOf("") }
        var mobile by remember { mutableStateOf("") }
        var address by remember { mutableStateOf("") }
        var selectedPhotoUri by remember { mutableStateOf<Uri?>(null) }

        val photoPicker = rememberLauncherForActivityResult(
            contract = ActivityResultContracts.PickVisualMedia()
        ) { uri: Uri? ->
            selectedPhotoUri = uri
        }

        AlertDialog(
            onDismissRequest = { if (!isSavingReporter) showAddReporterDialog = false },
            title = {
                Text(
                    text = "Add New Ground Reporter",
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .verticalScroll(rememberScrollState()),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Box(
                        modifier = Modifier
                            .size(86.dp)
                            .clip(CircleShape)
                            .background(LightBackground)
                            .border(2.dp, NewsRed, CircleShape)
                            .clickable(enabled = !isSavingReporter) {
                                photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        if (selectedPhotoUri != null) {
                            AsyncImage(
                                model = ImageRequest.Builder(context)
                                    .data(selectedPhotoUri)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = "Selected Photo",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            Icon(
                                imageVector = Icons.Default.Person,
                                contentDescription = null,
                                tint = TextSecondary,
                                modifier = Modifier.size(46.dp)
                            )
                        }

                        Box(
                            modifier = Modifier
                                .align(Alignment.BottomEnd)
                                .size(26.dp)
                                .clip(CircleShape)
                                .background(NewsRed),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.CameraAlt, contentDescription = null, tint = Color.White, modifier = Modifier.size(15.dp))
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    OutlinedButton(
                        onClick = {
                            photoPicker.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
                        },
                        enabled = !isSavingReporter,
                        shape = RoundedCornerShape(8.dp),
                        colors = ButtonDefaults.outlinedButtonColors(contentColor = NewsRed),
                        border = androidx.compose.foundation.BorderStroke(1.dp, NewsRed),
                        modifier = Modifier.height(34.dp)
                    ) {
                        Icon(Icons.Default.CameraAlt, contentDescription = null, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = if (selectedPhotoUri != null) "Change Photo" else "Upload Profile Photo",
                            fontSize = 11.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }

                    Spacer(modifier = Modifier.height(16.dp))

                    OutlinedTextField(
                        value = name,
                        onValueChange = { name = it },
                        label = { Text("Full Name *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsRed,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = mobile,
                        onValueChange = { mobile = it },
                        label = { Text("Mobile Number *") },
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsRed,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(10.dp))

                    OutlinedTextField(
                        value = address,
                        onValueChange = { address = it },
                        label = { Text("Address / Beat *") },
                        minLines = 2,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = NewsRed,
                            unfocusedBorderColor = BorderLight,
                            focusedTextColor = TextPrimary,
                            unfocusedTextColor = TextPrimary
                        ),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "System will auto-generate User ID & Password upon save.",
                        color = Color(0xFFD97706),
                        fontSize = 11.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (name.isNotBlank() && mobile.isNotBlank() && address.isNotBlank()) {
                            isSavingReporter = true
                            coroutineScope.launch {
                                val rep = FirebaseRepository.createReporterWithBunny(
                                    context = context,
                                    name = name.trim(),
                                    mobile = mobile.trim(),
                                    address = address.trim(),
                                    photoUri = selectedPhotoUri
                                )
                                isSavingReporter = false
                                showAddReporterDialog = false
                                newlyCreatedReporter = rep
                            }
                        }
                    },
                    enabled = !isSavingReporter && name.isNotBlank() && mobile.isNotBlank() && address.isNotBlank(),
                    colors = ButtonDefaults.buttonColors(containerColor = NewsRed)
                ) {
                    Text(if (isSavingReporter) "Saving..." else "Save Reporter", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showAddReporterDialog = false }, enabled = !isSavingReporter) {
                    Text("Cancel", color = TextSecondary)
                }
            },
            containerColor = LightSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }

    // Credentials Share Dialog
    if (newlyCreatedReporter != null) {
        val rep = newlyCreatedReporter!!
        AlertDialog(
            onDismissRequest = { newlyCreatedReporter = null },
            title = {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.CheckCircle, contentDescription = null, tint = SuccessGreen)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Reporter Created!", color = TextPrimary, fontWeight = FontWeight.Bold)
                }
            },
            text = {
                Column {
                    Text("Share these login credentials with ${rep.name}:", color = TextSecondary, fontSize = 13.sp)
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = LightBackground),
                        shape = RoundedCornerShape(10.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Text("User ID: ${rep.id}", color = NewsRed, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Spacer(modifier = Modifier.height(6.dp))
                            Text("Password: ${rep.password}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        clip.setPrimaryClip(
                            ClipData.newPlainText(
                                "Drikq Reporter Credentials",
                                "Drikq News Reporter Portal\nUser ID: ${rep.id}\nPassword: ${rep.password}"
                            )
                        )
                        Toast.makeText(context, "Credentials copied to clipboard!", Toast.LENGTH_SHORT).show()
                        newlyCreatedReporter = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewsRed)
                ) {
                    Text("Copy Credentials", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { newlyCreatedReporter = null }) {
                    Text("Close", color = TextSecondary)
                }
            },
            containerColor = LightSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

@Composable
fun AdminReportersTab(
    reporters: List<Reporter>,
    onDeleteReporter: (String) -> Unit
) {
    if (reporters.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No ground reporters added yet. Tap '+' to create.", color = TextSecondary)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(reporters, key = { it.id }) { reporter ->
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current)
                                .data(reporter.photoUrl)
                                .crossfade(true)
                                .build(),
                            contentDescription = reporter.name,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.size(50.dp).clip(CircleShape).background(LightBackground)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(reporter.name, color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 15.sp)
                            Text("ID: ${reporter.id} • ${reporter.mobile}", color = TextSecondary, fontSize = 12.sp)
                            Text(reporter.address, color = TextPrimary.copy(alpha = 0.8f), fontSize = 11.sp)
                            Text("Password: ${reporter.password}", color = Color(0xFFD97706), fontSize = 11.sp, fontWeight = FontWeight.SemiBold)
                        }
                        IconButton(onClick = { onDeleteReporter(reporter.id) }) {
                            Icon(Icons.Default.Delete, contentDescription = "Delete", tint = NewsRed)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminContentTab(
    posts: List<Post>,
    onApprove: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    if (posts.isEmpty()) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Text("No posts uploaded from reporters yet.", color = TextSecondary)
        }
    } else {
        LazyColumn(modifier = Modifier.fillMaxSize().padding(16.dp)) {
            items(posts, key = { it.id }) { post ->
                val isApproved = post.status == PostStatus.APPROVED
                Card(
                    modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = LightSurface),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(post.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = post.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.size(90.dp).clip(RoundedCornerShape(8.dp)).background(LightBackground)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Surface(
                                    color = if (isApproved) SuccessGreen else Color(0xFFF59E0B),
                                    shape = RoundedCornerShape(4.dp)
                                ) {
                                    Text(
                                        text = post.status.name,
                                        color = Color.White,
                                        fontSize = 9.sp,
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(post.title, color = TextPrimary, fontSize = 13.sp, fontWeight = FontWeight.Bold, maxLines = 2)
                                Spacer(modifier = Modifier.height(2.dp))
                                Text("${post.place} • Reporter: ${post.reporterName}", color = TextSecondary, fontSize = 11.sp)
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(post.description, color = TextPrimary.copy(alpha = 0.8f), fontSize = 12.sp, maxLines = 2)
                        Spacer(modifier = Modifier.height(12.dp))

                        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                            if (!isApproved) {
                                Button(
                                    onClick = { onApprove(post.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve to Feed", fontSize = 12.sp)
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                            }
                            OutlinedButton(
                                onClick = { onDelete(post.id) },
                                colors = ButtonDefaults.outlinedButtonColors(contentColor = NewsRed),
                                border = androidx.compose.foundation.BorderStroke(1.dp, NewsRed),
                                shape = RoundedCornerShape(8.dp),
                                modifier = Modifier.height(36.dp)
                            ) {
                                Icon(Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(16.dp))
                                Spacer(modifier = Modifier.width(4.dp))
                                Text("Delete", fontSize = 12.sp)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AdminPolicyTab(
    currentPolicy: com.example.data.model.AppPolicy,
    onSave: (String, String) -> Unit
) {
    var policyText by remember(currentPolicy) { mutableStateOf(currentPolicy.privacyPolicy) }
    var termsText by remember(currentPolicy) { mutableStateOf(currentPolicy.termsAndConditions) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Privacy Policy Text", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = policyText,
            onValueChange = { policyText = it },
            minLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Text("Terms & Conditions Text", color = TextPrimary, fontSize = 15.sp, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedTextField(
            value = termsText,
            onValueChange = { termsText = it },
            minLines = 4,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))
        Button(
            onClick = { onSave(policyText.trim(), termsText.trim()) },
            colors = ButtonDefaults.buttonColors(containerColor = NewsRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Text("Save & Sync with App", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

@Composable
fun AdminNotificationsTab(
    posts: List<Post>,
    history: List<PushNotificationItem>,
    onSend: (String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(16.dp)
    ) {
        Text("Broadcast Push Notification", color = TextPrimary, fontSize = 16.sp, fontWeight = FontWeight.Bold)
        Text("Send breaking news alerts to all subscribers", color = TextSecondary, fontSize = 12.sp)

        Spacer(modifier = Modifier.height(16.dp))
        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title (e.g., Breaking News)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Message Body") },
            minLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderLight,
                focusedTextColor = TextPrimary,
                unfocusedTextColor = TextPrimary
            ),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))
        Button(
            onClick = {
                if (title.isNotBlank() && body.isNotBlank()) {
                    onSend(title.trim(), body.trim(), selectedPostId)
                    title = ""
                    body = ""
                    selectedPostId = null
                }
            },
            enabled = title.isNotBlank() && body.isNotBlank(),
            colors = ButtonDefaults.buttonColors(containerColor = NewsRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth().height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null, tint = Color.White)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Broadcast Alert", fontWeight = FontWeight.Bold, color = Color.White)
        }
    }
}

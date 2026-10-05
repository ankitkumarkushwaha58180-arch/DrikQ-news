package com.example.ui.screens

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.widget.Toast
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
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.Send
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AdminPanelSettings
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.ContentPaste
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.NotificationsActive
import androidx.compose.material.icons.filled.People
import androidx.compose.material.icons.filled.Policy
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.SecondaryScrollableTabRow
import androidx.compose.material3.Surface
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.TabRowDefaults
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.MediaType
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.data.model.Reporter
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NewsRed
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AdminPanelScreen(
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val isAdminLoggedIn by FirebaseRepository.isAdminLoggedIn.collectAsState()

    var adminPassInput by remember { mutableStateOf("admin") }
    var adminLoginError by remember { mutableStateOf<String?>(null) }

    if (!isAdminLoggedIn) {
        // Admin Login Barrier Screen
        AdminLoginGate(
            passInput = adminPassInput,
            onPassChange = { adminPassInput = it; adminLoginError = null },
            errorMessage = adminLoginError,
            onSubmit = {
                val ok = FirebaseRepository.authenticateAdmin(adminPassInput)
                if (!ok) adminLoginError = "Invalid admin password. Default is 'admin' or 'admin123'."
            },
            onBack = onBack,
            modifier = modifier
        )
        return
    }

    var selectedTabIndex by remember { mutableIntStateOf(0) }
    val tabTitles = listOf("Reporters", "Content", "Privacy Policy", "Notifications")

    // State for Add/Edit Reporter Dialog
    var showAddReporterDialog by remember { mutableStateOf(false) }
    var editingReporter by remember { mutableStateOf<Reporter?>(null) }
    var newlyCreatedReporter by remember { mutableStateOf<Reporter?>(null) }

    val reporters by FirebaseRepository.reporters.collectAsState()
    val posts by FirebaseRepository.posts.collectAsState()
    val policy by FirebaseRepository.policy.collectAsState()
    val notifications by FirebaseRepository.notifications.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Admin App Bar
            Surface(
                color = DarkSurface,
                shadowElevation = 4.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(
                            onClick = onBack,
                            modifier = Modifier.testTag("admin_back_button")
                        ) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = "Back",
                                tint = Color.White
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.AdminPanelSettings,
                            contentDescription = null,
                            tint = Color(0xFF60A5FA),
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Admin Panel",
                            color = Color.White,
                            fontSize = 18.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }

                    TextButton(onClick = { FirebaseRepository.logoutAdmin() }) {
                        Text("Log Out", color = NewsRed, fontSize = 13.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            // Tab Navigation
            TabRow(
                selectedTabIndex = selectedTabIndex,
                containerColor = DarkSurface,
                contentColor = Color.White,
                indicator = { tabPositions ->
                    TabRowDefaults.PrimaryIndicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTabIndex]),
                        color = NewsRed
                    )
                }
            ) {
                tabTitles.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTabIndex == index,
                        onClick = { selectedTabIndex = index },
                        text = {
                            Text(
                                text = title,
                                fontSize = 12.sp,
                                fontWeight = if (selectedTabIndex == index) FontWeight.Bold else FontWeight.Normal,
                                color = if (selectedTabIndex == index) Color.White else SlateGray
                            )
                        },
                        modifier = Modifier.testTag("admin_tab_$index")
                    )
                }
            }

            // Content Area based on Tab
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxWidth()
            ) {
                when (selectedTabIndex) {
                    0 -> ReporterManagementSection(
                        reporters = reporters,
                        onAddNewClick = {
                            editingReporter = null
                            showAddReporterDialog = true
                        },
                        onEditClick = { rep ->
                            editingReporter = rep
                            showAddReporterDialog = true
                        },
                        onDeleteClick = { repId ->
                            FirebaseRepository.deleteReporter(repId)
                            Toast.makeText(context, "Reporter deleted", Toast.LENGTH_SHORT).show()
                        }
                    )
                    1 -> ContentManagementSection(
                        posts = posts,
                        onApprove = { id ->
                            FirebaseRepository.approvePost(id)
                            Toast.makeText(context, "Post Approved for Home Feed!", Toast.LENGTH_SHORT).show()
                        },
                        onDelete = { id ->
                            FirebaseRepository.deletePost(id)
                            Toast.makeText(context, "Post removed", Toast.LENGTH_SHORT).show()
                        }
                    )
                    2 -> PrivacyPolicySection(
                        currentPolicy = policy.privacyPolicy,
                        currentTerms = policy.termsAndConditions,
                        onSave = { p, t ->
                            FirebaseRepository.updatePrivacyPolicy(p, t)
                            Toast.makeText(context, "Privacy Policy & Terms updated in Firebase!", Toast.LENGTH_SHORT).show()
                        }
                    )
                    3 -> PushNotificationSection(
                        posts = posts,
                        history = notifications,
                        onSend = { title, body, postId ->
                            FirebaseRepository.sendPushNotification(title, body, postId)
                            Toast.makeText(context, "Broadcast Notification sent to all users!", Toast.LENGTH_SHORT).show()
                        }
                    )
                }
            }
        }
    }

    // Dialog: Add / Edit Reporter
    if (showAddReporterDialog) {
        AddEditReporterDialog(
            reporterToEdit = editingReporter,
            onDismiss = { showAddReporterDialog = false },
            onSave = { name, mobile, address, photo ->
                showAddReporterDialog = false
                if (editingReporter != null) {
                    FirebaseRepository.updateReporter(
                        editingReporter!!.copy(name = name, mobile = mobile, address = address, photoUrl = photo)
                    )
                    Toast.makeText(context, "Reporter updated", Toast.LENGTH_SHORT).show()
                } else {
                    // System must auto-generate unique User ID and Password
                    val created = FirebaseRepository.createReporter(name, mobile, address, photo)
                    newlyCreatedReporter = created
                }
            }
        )
    }

    // Dialog: Display Auto-Generated Credentials to Admin
    if (newlyCreatedReporter != null) {
        AlertDialog(
            onDismissRequest = { newlyCreatedReporter = null },
            icon = {
                Icon(
                    imageVector = Icons.Default.CheckCircle,
                    contentDescription = null,
                    tint = SuccessGreen,
                    modifier = Modifier.size(36.dp)
                )
            },
            title = {
                Text(
                    text = "Reporter Credentials Generated",
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            },
            text = {
                Column(modifier = Modifier.fillMaxWidth()) {
                    Text(
                        text = "Share these auto-generated login credentials with ${newlyCreatedReporter?.name}:",
                        color = SlateGray,
                        fontSize = 13.sp
                    )
                    Spacer(modifier = Modifier.height(14.dp))
                    Card(
                        colors = CardDefaults.cardColors(containerColor = DarkBackground),
                        shape = RoundedCornerShape(10.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Column(modifier = Modifier.padding(14.dp)) {
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("User ID:", color = SlateGray, fontSize = 12.sp)
                                Text(
                                    text = newlyCreatedReporter?.id ?: "",
                                    color = NewsRed,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("Password:", color = SlateGray, fontSize = 12.sp)
                                Text(
                                    text = newlyCreatedReporter?.password ?: "",
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val clip = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                        val data = ClipData.newPlainText(
                            "Reporter Credentials",
                            "Drikq News Reporter Login\nUser ID: ${newlyCreatedReporter?.id}\nPassword: ${newlyCreatedReporter?.password}"
                        )
                        clip.setPrimaryClip(data)
                        Toast.makeText(context, "Credentials copied to clipboard!", Toast.LENGTH_SHORT).show()
                        newlyCreatedReporter = null
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = NewsRed)
                ) {
                    Icon(imageVector = Icons.Default.ContentPaste, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Copy & Done", color = Color.White)
                }
            },
            containerColor = DarkSurface,
            shape = RoundedCornerShape(16.dp)
        )
    }
}

// ----------------------------------------------------
// Section A: Reporter Management
// ----------------------------------------------------
@Composable
fun ReporterManagementSection(
    reporters: List<Reporter>,
    onAddNewClick: () -> Unit,
    onEditClick: (Reporter) -> Unit,
    onDeleteClick: (String) -> Unit
) {
    Box(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp)
                .padding(bottom = 72.dp)
        ) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = "Authorized Ground Reporters (${reporters.size})",
                        color = Color.White,
                        fontSize = 15.sp,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            items(reporters, key = { it.id }) { reporter ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 6.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(14.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(reporter.photoUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = reporter.name,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                            )
                            Spacer(modifier = Modifier.width(12.dp))
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = reporter.name,
                                    color = Color.White,
                                    fontSize = 15.sp,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "ID: ${reporter.id} • Tel: ${reporter.mobile}",
                                    color = SlateGray,
                                    fontSize = 12.sp
                                )
                                Text(
                                    text = reporter.address,
                                    color = Color.White.copy(alpha = 0.7f),
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Divider(color = BorderSlate)
                        Spacer(modifier = Modifier.height(8.dp))

                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Pass: ${reporter.password}",
                                color = Color(0xFFFBBF24),
                                fontSize = 11.sp,
                                fontWeight = FontWeight.Medium
                            )

                            Row {
                                IconButton(
                                    onClick = { onEditClick(reporter) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Edit,
                                        contentDescription = "Edit Reporter",
                                        tint = Color(0xFF60A5FA),
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.width(8.dp))
                                IconButton(
                                    onClick = { onDeleteClick(reporter.id) },
                                    modifier = Modifier.size(32.dp)
                                ) {
                                    Icon(
                                        imageVector = Icons.Default.Delete,
                                        contentDescription = "Delete Reporter",
                                        tint = NewsRed,
                                        modifier = Modifier.size(18.dp)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // Floating Action Button: Add New Reporter
        FloatingActionButton(
            onClick = onAddNewClick,
            containerColor = NewsRed,
            contentColor = Color.White,
            shape = CircleShape,
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(20.dp)
                .testTag("admin_add_reporter_fab")
        ) {
            Icon(imageVector = Icons.Default.Add, contentDescription = "Add Reporter")
        }
    }
}

// ----------------------------------------------------
// Section B: Content Management
// ----------------------------------------------------
@Composable
fun ContentManagementSection(
    posts: List<Post>,
    onApprove: (String) -> Unit,
    onDelete: (String) -> Unit
) {
    var statusFilter by remember { mutableStateOf("ALL") }

    val filtered = when (statusFilter) {
        "PENDING" -> posts.filter { it.status == PostStatus.PENDING }
        "APPROVED" -> posts.filter { it.status == PostStatus.APPROVED }
        else -> posts
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Filter Chips
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 12.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf("ALL" to "All (${posts.size})", "PENDING" to "Pending (${posts.count { it.status == PostStatus.PENDING }})", "APPROVED" to "Approved (${posts.count { it.status == PostStatus.APPROVED }})").forEach { (filterKey, label) ->
                val selected = statusFilter == filterKey
                Surface(
                    color = if (selected) NewsRed else DarkSurface,
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, if (selected) NewsRed else BorderSlate),
                    modifier = Modifier.clickable { statusFilter = filterKey }
                ) {
                    Text(
                        text = label,
                        color = if (selected) Color.White else SlateGray,
                        fontSize = 12.sp,
                        fontWeight = if (selected) FontWeight.Bold else FontWeight.Normal,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    )
                }
            }
        }

        LazyColumn(modifier = Modifier.fillMaxSize()) {
            items(filtered, key = { it.id }) { post ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 8.dp),
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(modifier = Modifier.fillMaxWidth()) {
                            // Thumbnail
                            AsyncImage(
                                model = ImageRequest.Builder(LocalContext.current)
                                    .data(post.thumbnailUrl)
                                    .crossfade(true)
                                    .build(),
                                contentDescription = post.title,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .size(90.dp)
                                    .clip(RoundedCornerShape(8.dp))
                            )

                            Spacer(modifier = Modifier.width(12.dp))

                            // Details
                            Column(modifier = Modifier.weight(1f)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Surface(
                                        color = if (post.status == PostStatus.APPROVED) SuccessGreen else Color(0xFFF59E0B),
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

                                    Text(
                                        text = post.mediaType.name,
                                        color = SlateGray,
                                        fontSize = 10.sp,
                                        fontWeight = FontWeight.SemiBold
                                    )
                                }

                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = post.title,
                                    color = Color.White,
                                    fontSize = 13.sp,
                                    fontWeight = FontWeight.Bold,
                                    maxLines = 2
                                )
                                Spacer(modifier = Modifier.height(2.dp))
                                Text(
                                    text = "${post.place} • Reporter: ${post.reporterName}",
                                    color = SlateGray,
                                    fontSize = 11.sp
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = post.description,
                            color = Color.White.copy(alpha = 0.8f),
                            fontSize = 12.sp,
                            maxLines = 3
                        )

                        Spacer(modifier = Modifier.height(12.dp))
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.End,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            if (post.status == PostStatus.PENDING) {
                                Button(
                                    onClick = { onApprove(post.id) },
                                    colors = ButtonDefaults.buttonColors(containerColor = SuccessGreen),
                                    shape = RoundedCornerShape(8.dp),
                                    modifier = Modifier.height(36.dp)
                                ) {
                                    Icon(Icons.Default.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                                    Spacer(modifier = Modifier.width(4.dp))
                                    Text("Approve", fontSize = 12.sp, fontWeight = FontWeight.Bold)
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

// ----------------------------------------------------
// Section C: Privacy Policy
// ----------------------------------------------------
@Composable
fun PrivacyPolicySection(
    currentPolicy: String,
    currentTerms: String,
    onSave: (String, String) -> Unit
) {
    var policyText by remember(currentPolicy) { mutableStateOf(currentPolicy) }
    var termsText by remember(currentTerms) { mutableStateOf(currentTerms) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Editorial Privacy Policy",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "This legal notice is synchronized to Firebase and shown on the user Profile screen.",
            color = SlateGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = policyText,
            onValueChange = { policyText = it },
            label = { Text("Privacy Policy Content") },
            minLines = 6,
            maxLines = 10,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(20.dp))

        Text(
            text = "Terms & Conditions",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = termsText,
            onValueChange = { termsText = it },
            label = { Text("Terms & Conditions Content") },
            minLines = 6,
            maxLines = 10,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(24.dp))

        Button(
            onClick = { onSave(policyText, termsText) },
            colors = ButtonDefaults.buttonColors(containerColor = NewsRed),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.Default.Policy, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Update Policy in Firebase", fontWeight = FontWeight.Bold)
        }
    }
}

// ----------------------------------------------------
// Section D: Push Notification
// ----------------------------------------------------
@Composable
fun PushNotificationSection(
    posts: List<Post>,
    history: List<com.example.data.model.PushNotificationItem>,
    onSend: (String, String, String?) -> Unit
) {
    var title by remember { mutableStateOf("") }
    var body by remember { mutableStateOf("") }
    var selectedPostId by remember { mutableStateOf<String?>(null) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(16.dp)
    ) {
        Text(
            text = "Broadcast Push Notification",
            color = Color.White,
            fontSize = 16.sp,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Send an urgent news notification to all subscribers via Firebase Cloud Messaging",
            color = SlateGray,
            fontSize = 12.sp
        )

        Spacer(modifier = Modifier.height(16.dp))

        OutlinedTextField(
            value = title,
            onValueChange = { title = it },
            label = { Text("Notification Title (e.g., Breaking News)") },
            singleLine = true,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedTextField(
            value = body,
            onValueChange = { body = it },
            label = { Text("Message Body") },
            minLines = 3,
            maxLines = 5,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = NewsRed,
                unfocusedBorderColor = BorderSlate,
                focusedTextColor = Color.White,
                unfocusedTextColor = Color.White
            ),
            shape = RoundedCornerShape(12.dp),
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(14.dp))

        Text(
            text = "Link to Video/Content (Optional):",
            color = SlateGray,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold
        )
        Spacer(modifier = Modifier.height(6.dp))

        posts.take(3).forEach { post ->
            val isSelected = selectedPostId == post.id
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isSelected) NewsRed.copy(alpha = 0.2f) else DarkSurface)
                    .border(1.dp, if (isSelected) NewsRed else BorderSlate, RoundedCornerShape(8.dp))
                    .clickable { selectedPostId = if (isSelected) null else post.id }
                    .padding(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = post.title,
                    color = Color.White,
                    fontSize = 12.sp,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
                if (isSelected) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = "Selected",
                        tint = NewsRed,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

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
            modifier = Modifier
                .fillMaxWidth()
                .height(50.dp)
        ) {
            Icon(Icons.AutoMirrored.Filled.Send, contentDescription = null)
            Spacer(modifier = Modifier.width(8.dp))
            Text("Send Push Broadcast", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(24.dp))
        Text(
            text = "Recent Notifications Broadcasts (${history.size})",
            color = Color.White,
            fontSize = 14.sp,
            fontWeight = FontWeight.Bold
        )
        Spacer(modifier = Modifier.height(8.dp))

        history.forEach { item ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp),
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(8.dp)
            ) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(item.title, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                        Text("Broadcast", color = SuccessGreen, fontSize = 10.sp)
                    }
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(item.body, color = SlateGray, fontSize = 11.sp)
                }
            }
        }
    }
}

// ----------------------------------------------------
// Dialog: Add / Edit Reporter
// ----------------------------------------------------
@Composable
fun AddEditReporterDialog(
    reporterToEdit: Reporter?,
    onDismiss: () -> Unit,
    onSave: (String, String, String, String) -> Unit
) {
    var name by remember { mutableStateOf(reporterToEdit?.name ?: "") }
    var mobile by remember { mutableStateOf(reporterToEdit?.mobile ?: "") }
    var address by remember { mutableStateOf(reporterToEdit?.address ?: "") }
    var photoUrl by remember {
        mutableStateOf(
            reporterToEdit?.photoUrl
                ?: "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80"
        )
    }

    val sampleAvatars = listOf(
        "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
        "https://images.unsplash.com/photo-1500648767791-00dcc994a43e?auto=format&fit=crop&w=400&q=80"
    )

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = if (reporterToEdit != null) "Edit Reporter" else "Add New Ground Reporter",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
            ) {
                // Profile Picture selection
                Text("Select Profile Picture:", color = SlateGray, fontSize = 12.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    sampleAvatars.forEach { url ->
                        val isSelected = photoUrl == url
                        AsyncImage(
                            model = ImageRequest.Builder(LocalContext.current).data(url).build(),
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(44.dp)
                                .clip(CircleShape)
                                .border(2.dp, if (isSelected) NewsRed else Color.Transparent, CircleShape)
                                .clickable { photoUrl = url }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(14.dp))

                // Full Name
                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Full Name *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewsRed,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Mobile Number
                OutlinedTextField(
                    value = mobile,
                    onValueChange = { mobile = it },
                    label = { Text("Mobile Number *") },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewsRed,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                Spacer(modifier = Modifier.height(10.dp))

                // Address
                OutlinedTextField(
                    value = address,
                    onValueChange = { address = it },
                    label = { Text("Address / District Beat *") },
                    minLines = 2,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NewsRed,
                        unfocusedBorderColor = BorderSlate,
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    ),
                    modifier = Modifier.fillMaxWidth()
                )

                if (reporterToEdit == null) {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        text = "Note: System will auto-generate unique User ID and Password upon save.",
                        color = Color(0xFFFBBF24),
                        fontSize = 11.sp
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (name.isNotBlank() && mobile.isNotBlank() && address.isNotBlank()) {
                        onSave(name.trim(), mobile.trim(), address.trim(), photoUrl)
                    }
                },
                enabled = name.isNotBlank() && mobile.isNotBlank() && address.isNotBlank(),
                colors = ButtonDefaults.buttonColors(containerColor = NewsRed)
            ) {
                Text(if (reporterToEdit != null) "Update" else "Create Reporter", color = Color.White)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = SlateGray)
            }
        },
        containerColor = DarkSurface,
        shape = RoundedCornerShape(16.dp)
    )
}

// ----------------------------------------------------
// Admin Login Gate Screen
// ----------------------------------------------------
@Composable
fun AdminLoginGate(
    passInput: String,
    onPassChange: (String) -> Unit,
    errorMessage: String?,
    onSubmit: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(DarkBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Surface(
                modifier = Modifier
                    .size(72.dp)
                    .clip(CircleShape),
                color = Color(0xFF1E3A8A)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = Icons.Default.Shield,
                        contentDescription = "Admin Shield",
                        tint = Color(0xFF93C5FD),
                        modifier = Modifier.size(40.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            Text(
                text = "Bureau Admin Access",
                color = Color.White,
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold
            )

            Text(
                text = "Manage reporters, editorial approvals, policy & push alerts",
                color = SlateGray,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(32.dp))

            OutlinedTextField(
                value = passInput,
                onValueChange = onPassChange,
                label = { Text("Admin Passcode / Password") },
                leadingIcon = {
                    Icon(imageVector = Icons.Default.Lock, contentDescription = null, tint = Color(0xFF60A5FA))
                },
                visualTransformation = PasswordVisualTransformation(),
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = Color(0xFF60A5FA),
                    unfocusedBorderColor = BorderSlate,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth()
            )

            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = errorMessage, color = NewsRed, fontSize = 12.sp)
            }

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onSubmit,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF2563EB)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Text("Enter Admin Dashboard", color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(16.dp))

            TextButton(onClick = onBack) {
                Text("Cancel & Return to Login", color = SlateGray)
            }
        }
    }
}

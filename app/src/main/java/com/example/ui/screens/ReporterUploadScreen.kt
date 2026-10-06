package com.example.ui.screens

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AddPhotoAlternate
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.CloudUpload
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.MediaType
import com.example.data.model.Reporter
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NewsRed
import com.example.ui.theme.SlateGray
import com.example.ui.theme.SuccessGreen
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ReporterUploadScreen(
    onUploadCompleteRedirectToFeed: () -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val reporter by FirebaseRepository.currentReporter.collectAsState()

    var mediaType by remember { mutableStateOf(MediaType.VIDEO) }
    var selectedMediaUri by remember { mutableStateOf<Uri?>(null) }

    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var place by remember { mutableStateOf("") }

    var isUploading by remember { mutableStateOf(false) }
    var uploadProgress by remember { mutableIntStateOf(0) }
    var validationError by remember { mutableStateOf<String?>(null) }

    // Real Device Gallery Picker Launcher
    val mediaPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri: Uri? ->
        if (uri != null) {
            selectedMediaUri = uri
            validationError = null
        }
    }

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
                .verticalScroll(rememberScrollState())
                .padding(20.dp)
        ) {
            // Header Bar
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    IconButton(
                        onClick = onBack,
                        enabled = !isUploading,
                        modifier = Modifier.testTag("upload_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "Back",
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Reporter Upload Desk",
                        color = Color.White,
                        fontSize = 20.sp,
                        fontWeight = FontWeight.Bold
                    )
                }

                // Reporter Badge
                if (reporter != null) {
                    Surface(
                        color = DarkSurface,
                        shape = RoundedCornerShape(20.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            if (reporter?.photoUrl?.isNotBlank() == true) {
                                AsyncImage(
                                    model = ImageRequest.Builder(context)
                                        .data(reporter?.photoUrl)
                                        .crossfade(true)
                                        .build(),
                                    contentDescription = null,
                                    modifier = Modifier
                                        .size(24.dp)
                                        .clip(CircleShape)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                            }
                            Text(
                                text = reporter?.id ?: "",
                                color = NewsRed,
                                fontSize = 12.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Step 1: Media Type Selection
            Text(
                text = "1. SELECT MEDIA TYPE",
                color = SlateGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                // Video News Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isUploading) {
                            mediaType = MediaType.VIDEO
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                            )
                        }
                        .border(
                            width = 2.dp,
                            color = if (mediaType == MediaType.VIDEO) NewsRed else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mediaType == MediaType.VIDEO) DarkSurface else DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Videocam,
                            contentDescription = "Video",
                            tint = if (mediaType == MediaType.VIDEO) NewsRed else SlateGray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Video News",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }

                Spacer(modifier = Modifier.width(12.dp))

                // Photo Story Option
                Card(
                    modifier = Modifier
                        .weight(1f)
                        .clickable(enabled = !isUploading) {
                            mediaType = MediaType.PHOTO
                            mediaPickerLauncher.launch(
                                PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                            )
                        }
                        .border(
                            width = 2.dp,
                            color = if (mediaType == MediaType.PHOTO) NewsRed else Color.Transparent,
                            shape = RoundedCornerShape(12.dp)
                        ),
                    colors = CardDefaults.cardColors(
                        containerColor = if (mediaType == MediaType.PHOTO) DarkSurface else DarkBackground
                    ),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Image,
                            contentDescription = "Photo",
                            tint = if (mediaType == MediaType.PHOTO) NewsRed else SlateGray,
                            modifier = Modifier.size(22.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "Photo Story",
                            color = Color.White,
                            fontSize = 14.sp,
                            fontWeight = FontWeight.SemiBold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Step 2: Media Preview / Real Device Gallery Picker Box
            Text(
                text = "2. CHOOSE FILE (BUNNY.NET EDGE STORAGE)",
                color = SlateGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clickable(enabled = !isUploading) {
                        val request = if (mediaType == MediaType.VIDEO) {
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.VideoOnly)
                        } else {
                            PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                        }
                        mediaPickerLauncher.launch(request)
                    },
                colors = CardDefaults.cardColors(containerColor = DarkSurface),
                shape = RoundedCornerShape(14.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate)
            ) {
                if (selectedMediaUri != null) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(selectedMediaUri)
                                .crossfade(true)
                                .build(),
                            contentDescription = "Selected media preview",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )

                        Surface(
                            color = Color.Black.copy(alpha = 0.75f),
                            shape = RoundedCornerShape(6.dp),
                            modifier = Modifier
                                .padding(10.dp)
                                .align(Alignment.BottomStart)
                        ) {
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                            ) {
                                Icon(
                                    imageVector = Icons.Default.CheckCircle,
                                    contentDescription = null,
                                    tint = SuccessGreen,
                                    modifier = Modifier.size(14.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    text = if (mediaType == MediaType.VIDEO) "Video selected (Tap to change)" else "Photo selected (Tap to change)",
                                    color = Color.White,
                                    fontSize = 11.sp
                                )
                            }
                        }
                    }
                } else {
                    Column(
                        modifier = Modifier.fillMaxSize(),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Surface(
                            modifier = Modifier.size(60.dp),
                            shape = CircleShape,
                            color = NewsRed.copy(alpha = 0.15f)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = if (mediaType == MediaType.VIDEO) Icons.Default.VideoLibrary else Icons.Default.AddPhotoAlternate,
                                    contentDescription = null,
                                    tint = NewsRed,
                                    modifier = Modifier.size(32.dp)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.height(12.dp))
                        Text(
                            text = if (mediaType == MediaType.VIDEO) "Tap to choose Video from Gallery" else "Tap to choose Photo from Gallery",
                            color = Color.White,
                            fontSize = 15.sp,
                            fontWeight = FontWeight.Bold
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Direct Edge PUT Upload to Bunny.net (Zone: drikq-news)",
                            color = SlateGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Step 3: Required Fields
            Text(
                text = "3. NEWS DETAILS (REQUIRED)",
                color = SlateGray,
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp
            )
            Spacer(modifier = Modifier.height(8.dp))

            // Title (required)
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    validationError = null
                },
                label = { Text("Headline / Title *") },
                placeholder = { Text("e.g. Breaking: High-Speed Flyover Opens to Public") },
                singleLine = false,
                maxLines = 2,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NewsRed,
                    unfocusedBorderColor = BorderSlate,
                    focusedLabelColor = NewsRed,
                    unfocusedLabelColor = SlateGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_title_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Place / Location name (required)
            OutlinedTextField(
                value = place,
                onValueChange = {
                    place = it
                    validationError = null
                },
                label = { Text("Place / Location Name *") },
                placeholder = { Text("e.g. Sector 5, Downtown Crossway") },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = null,
                        tint = NewsRed
                    )
                },
                singleLine = true,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NewsRed,
                    unfocusedBorderColor = BorderSlate,
                    focusedLabelColor = NewsRed,
                    unfocusedLabelColor = SlateGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_place_input")
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Description (required - unlimited length)
            OutlinedTextField(
                value = description,
                onValueChange = {
                    description = it
                    validationError = null
                },
                label = { Text("Full News Description *") },
                placeholder = { Text("Provide complete eyewitness details, verified facts, and ground impact (no length limit)...") },
                minLines = 5,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = NewsRed,
                    unfocusedBorderColor = BorderSlate,
                    focusedLabelColor = NewsRed,
                    unfocusedLabelColor = SlateGray,
                    focusedTextColor = Color.White,
                    unfocusedTextColor = Color.White
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("upload_description_input")
            )

            if (validationError != null) {
                Spacer(modifier = Modifier.height(10.dp))
                Text(
                    text = validationError!!,
                    color = NewsRed,
                    fontSize = 13.sp,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Real-time upload progress bar from 0% to 100%
            AnimatedVisibility(visible = isUploading) {
                Card(
                    colors = CardDefaults.cardColors(containerColor = DarkSurface),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                text = "Uploading to Bunny.net Edge Storage...",
                                color = Color.White,
                                fontSize = 13.sp,
                                fontWeight = FontWeight.SemiBold
                            )
                            Text(
                                text = "$uploadProgress%",
                                color = NewsRed,
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { uploadProgress / 100f },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(8.dp)
                                .clip(RoundedCornerShape(4.dp)),
                            color = NewsRed,
                            trackColor = BorderSlate
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Target: https://sg.storage.bunnycdn.com/drikq-news/posts/",
                            color = SlateGray,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            // Upload Button
            Button(
                onClick = {
                    if (selectedMediaUri == null) {
                        validationError = "Please select a ${if (mediaType == MediaType.VIDEO) "video" else "photo"} from your gallery"
                        return@Button
                    }
                    if (title.isBlank()) {
                        validationError = "Title is required"
                        return@Button
                    }
                    if (place.isBlank()) {
                        validationError = "Place / Location name is required"
                        return@Button
                    }
                    if (description.isBlank()) {
                        validationError = "Description is required"
                        return@Button
                    }

                    val currentRep = reporter ?: Reporter(
                        id = "REP-DIRECT",
                        name = "Staff Reporter",
                        mobile = "+91 98000 00000",
                        address = place
                    )

                    isUploading = true
                    uploadProgress = 0
                    validationError = null

                    coroutineScope.launch {
                        val result = FirebaseRepository.uploadPostWithBunny(
                            context = context,
                            fileUri = selectedMediaUri!!,
                            title = title.trim(),
                            description = description.trim(),
                            place = place.trim(),
                            mediaType = mediaType,
                            reporter = currentRep,
                            onProgress = { p -> uploadProgress = p }
                        )

                        if (result.isSuccess) {
                            Toast.makeText(context, "Uploaded to Bunny CDN! Status: Pending Approval", Toast.LENGTH_LONG).show()
                            delay(400)
                            onUploadCompleteRedirectToFeed()
                        } else {
                            isUploading = false
                            validationError = "Bunny upload failed: ${result.exceptionOrNull()?.message}"
                        }
                    }
                },
                enabled = !isUploading,
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = NewsRed),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("submit_upload_button")
            ) {
                Icon(
                    imageVector = Icons.Default.CloudUpload,
                    contentDescription = null,
                    tint = Color.White
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = if (isUploading) "Uploading to Bunny ($uploadProgress%)..." else "Submit News for Approval",
                    fontSize = 16.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        }
    }
}

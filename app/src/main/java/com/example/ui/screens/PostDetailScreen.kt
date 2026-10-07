package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Divider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.firebase.FirebaseRepository
import com.example.ui.components.MediaContentView
import com.example.ui.theme.BorderLight
import com.example.ui.theme.LightBackground
import com.example.ui.theme.LightSurface
import com.example.ui.theme.NewsRed
import com.example.ui.theme.TextPrimary
import com.example.ui.theme.TextSecondary

@Composable
fun PostDetailScreen(
    postId: String,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    val posts by FirebaseRepository.posts.collectAsState()
    val post = posts.find { it.id == postId }
    val currentUser by FirebaseRepository.currentUser.collectAsState()
    val currentUserId = currentUser?.uid ?: "guest"

    if (post == null) {
        Box(
            modifier = modifier.fillMaxSize().background(LightBackground),
            contentAlignment = Alignment.Center
        ) {
            Text("Story not found", color = TextPrimary)
        }
        return
    }

    val isLiked = post.likedByUsers.contains(currentUserId)

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(LightBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
        ) {
            // Top Nav
            Surface(
                color = LightSurface,
                shadowElevation = 2.dp,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 8.dp, vertical = 6.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = TextPrimary)
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Eyewitness Report", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }
            }

            // Media Player
            MediaContentView(
                mediaUrl = post.mediaUrl,
                mediaType = post.mediaType,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(320.dp),
                onPlayStarted = {
                    FirebaseRepository.recordView(post.id)
                }
            )

            Column(modifier = Modifier.padding(18.dp)) {
                Surface(
                    color = NewsRed.copy(alpha = 0.12f),
                    shape = RoundedCornerShape(8.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Default.LocationOn, contentDescription = null, tint = NewsRed, modifier = Modifier.size(14.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(post.place, color = NewsRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    text = post.title,
                    color = TextPrimary,
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    lineHeight = 28.sp
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Reporter Header with Follow button
                val reporters by FirebaseRepository.reporters.collectAsState()
                val reporter = reporters.find { it.id == post.reporterId }
                val isFollowed = reporter?.followedByUsers?.contains(currentUserId) == true

                Surface(
                    color = LightSurface,
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, BorderLight),
                    shadowElevation = 1.dp,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        AsyncImage(
                            model = ImageRequest.Builder(context)
                                .data(post.reporterPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80" })
                                .crossfade(true)
                                .build(),
                            contentDescription = null,
                            modifier = Modifier.size(44.dp).clip(CircleShape).background(LightBackground)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Filed by ${post.reporterName}", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                            Text("Reporter ID: ${post.reporterId}", color = TextSecondary, fontSize = 11.sp)
                        }

                        Button(
                            onClick = { FirebaseRepository.toggleFollowReporter(post.reporterId, currentUserId) },
                            colors = ButtonDefaults.buttonColors(
                                containerColor = if (isFollowed) Color(0xFFE2E8F0) else NewsRed,
                                contentColor = if (isFollowed) TextPrimary else Color.White
                            ),
                            shape = RoundedCornerShape(14.dp),
                            contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                            modifier = Modifier.height(32.dp)
                        ) {
                            Text(if (isFollowed) "Following" else "Follow", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Actions Bar
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { FirebaseRepository.toggleLike(post.id, currentUserId) }) {
                        Icon(
                            imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                            contentDescription = "Like",
                            tint = if (isLiked) NewsRed else TextSecondary
                        )
                    }
                    Text("${post.likesCount} Likes", color = TextPrimary, fontWeight = FontWeight.Bold)

                    Spacer(modifier = Modifier.width(20.dp))

                    Icon(Icons.Default.Visibility, contentDescription = null, tint = TextSecondary, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("${post.viewsCount} Views", color = TextSecondary)

                    Spacer(modifier = Modifier.weight(1f))

                    IconButton(
                        onClick = {
                            val streamText = if (post.mediaUrl.isNotBlank()) "\n\nStream: ${post.mediaUrl}" else ""
                            val intent = Intent(Intent.ACTION_SEND).apply {
                                type = "text/plain"
                                putExtra(Intent.EXTRA_TEXT, "🚨 ${post.title}\n📍 ${post.place}\n\n${post.description}$streamText")
                            }
                            context.startActivity(Intent.createChooser(intent, "Share Report"))
                        }
                    ) {
                        Icon(Icons.Default.Share, contentDescription = "Share", tint = TextSecondary)
                    }
                }

                Divider(color = BorderLight, modifier = Modifier.padding(vertical = 12.dp))

                Text("Full Eyewitness Description", color = TextPrimary, fontWeight = FontWeight.Bold, fontSize = 17.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = post.description,
                    color = TextPrimary.copy(alpha = 0.85f),
                    fontSize = 15.sp,
                    lineHeight = 24.sp
                )
            }
        }
    }
}

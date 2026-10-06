package com.example.ui.screens

import android.content.Intent
import androidx.compose.foundation.background
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Campaign
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.example.data.firebase.FirebaseRepository
import com.example.data.model.Post
import com.example.data.model.PostStatus
import com.example.ui.components.MediaContentView
import com.example.ui.theme.BorderSlate
import com.example.ui.theme.DarkBackground
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.NewsRed
import com.example.ui.theme.SlateGray

@Composable
fun HomeScreen(
    onNavigateToReporterProfile: (String) -> Unit,
    onNavigateToPostDetail: (String) -> Unit,
    onNavigateToReporterUpload: () -> Unit,
    onNavigateToAdminPanel: () -> Unit,
    onLogout: () -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedTab by remember { mutableIntStateOf(0) }

    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = DarkSurface,
                tonalElevation = 8.dp
            ) {
                NavigationBarItem(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    icon = { Icon(Icons.Default.Home, contentDescription = "Home") },
                    label = { Text("Home") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NewsRed,
                        selectedTextColor = NewsRed,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray,
                        indicatorColor = DarkSurface
                    )
                )
                NavigationBarItem(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    icon = { Icon(Icons.Default.Person, contentDescription = "Profile") },
                    label = { Text("Profile") },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = NewsRed,
                        selectedTextColor = NewsRed,
                        unselectedIconColor = SlateGray,
                        unselectedTextColor = SlateGray,
                        indicatorColor = DarkSurface
                    )
                )
            }
        },
        containerColor = DarkBackground,
        modifier = modifier
            .fillMaxSize()
            .statusBarsPadding()
            .navigationBarsPadding()
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (selectedTab) {
                0 -> HomeFeedView(
                    onReporterClick = onNavigateToReporterProfile,
                    onPostClick = onNavigateToPostDetail
                )
                1 -> ProfileScreen(
                    onLogout = onLogout,
                    onNavigateToReporterLogin = onNavigateToReporterUpload,
                    onNavigateToAdminPanel = onNavigateToAdminPanel
                )
            }
        }
    }
}

@Composable
fun HomeFeedView(
    onReporterClick: (String) -> Unit,
    onPostClick: (String) -> Unit
) {
    val context = LocalContext.current
    val posts by FirebaseRepository.posts.collectAsState()
    val reporters by FirebaseRepository.reporters.collectAsState()
    val currentUser by FirebaseRepository.currentUser.collectAsState()
    val currentUserId = currentUser?.uid ?: "guest"

    // Only Approved posts should appear in Home Feed
    val approvedPosts = posts.filter { it.status == PostStatus.APPROVED }

    Column(modifier = Modifier.fillMaxSize()) {
        // App Top Bar
        Surface(
            color = DarkSurface,
            tonalElevation = 4.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(34.dp),
                        shape = CircleShape,
                        color = NewsRed
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Campaign, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "DRIKQ FEED",
                            color = Color.White,
                            fontSize = 17.sp,
                            fontWeight = FontWeight.ExtraBold,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Bunny.net Verified News Stream",
                            color = SlateGray,
                            fontSize = 10.sp
                        )
                    }
                }

                Surface(
                    color = NewsRed.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(NewsRed)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "LIVE",
                            color = NewsRed,
                            fontSize = 10.sp,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        if (approvedPosts.isEmpty()) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(32.dp)) {
                    Icon(Icons.Default.Campaign, contentDescription = null, tint = SlateGray, modifier = Modifier.size(54.dp))
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "No Approved News Stories Yet",
                        color = Color.White,
                        fontSize = 17.sp,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        text = "Uploaded stories by reporters undergo editorial review in Admin Console before appearing here.",
                        color = SlateGray,
                        fontSize = 12.sp,
                        textAlign = androidx.compose.ui.text.style.TextAlign.Center
                    )
                }
            }
        } else {
            LazyColumn(modifier = Modifier.fillMaxSize()) {
                items(approvedPosts, key = { it.id }) { post ->
                    val reporter = reporters.find { it.id == post.reporterId }
                    val isFollowed = reporter?.followedByUsers?.contains(currentUserId) == true
                    val isLiked = post.likedByUsers.contains(currentUserId)

                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        colors = CardDefaults.cardColors(containerColor = DarkSurface),
                        shape = RoundedCornerShape(16.dp),
                        border = androidx.compose.foundation.BorderStroke(1.dp, BorderSlate)
                    ) {
                        Column {
                            // Reporter Header
                            Row(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(12.dp),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Row(
                                    modifier = Modifier
                                        .weight(1f)
                                        .clickable { onReporterClick(post.reporterId) },
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    AsyncImage(
                                        model = ImageRequest.Builder(context)
                                            .data(post.reporterPhotoUrl.ifBlank { "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80" })
                                            .crossfade(true)
                                            .build(),
                                        contentDescription = post.reporterName,
                                        contentScale = ContentScale.Crop,
                                        modifier = Modifier.size(40.dp).clip(CircleShape).background(DarkBackground)
                                    )
                                    Spacer(modifier = Modifier.width(10.dp))
                                    Column {
                                        Text(
                                            text = post.reporterName,
                                            color = Color.White,
                                            fontSize = 14.sp,
                                            fontWeight = FontWeight.Bold
                                        )
                                        Text(
                                            text = post.place,
                                            color = SlateGray,
                                            fontSize = 11.sp
                                        )
                                    }
                                }

                                Button(
                                    onClick = { FirebaseRepository.toggleFollowReporter(post.reporterId, currentUserId) },
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (isFollowed) BorderSlate else NewsRed
                                    ),
                                    shape = RoundedCornerShape(16.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                                    modifier = Modifier.height(32.dp)
                                ) {
                                    Text(
                                        text = if (isFollowed) "Following" else "Follow",
                                        fontSize = 11.sp,
                                        fontWeight = FontWeight.Bold
                                    )
                                }
                            }

                            // Media Player with Media3 ExoPlayer or Coil Photo
                            MediaContentView(
                                mediaUrl = post.mediaUrl,
                                mediaType = post.mediaType,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(260.dp),
                                onContentClick = {
                                    onPostClick(post.id)
                                },
                                onPlayStarted = {
                                    FirebaseRepository.recordView(post.id)
                                }
                            )

                            // Title & Description - Clicking opens Full Description Screen
                            Column(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .padding(14.dp)
                            ) {
                                Text(
                                    text = post.title,
                                    color = Color.White,
                                    fontSize = 16.sp,
                                    fontWeight = FontWeight.Bold,
                                    lineHeight = 22.sp,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPostClick(post.id) }
                                )
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = post.description,
                                    color = Color.White.copy(alpha = 0.75f),
                                    fontSize = 13.sp,
                                    maxLines = 2,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .clickable { onPostClick(post.id) }
                                )
                                Text(
                                    text = "Read full report →",
                                    color = NewsRed,
                                    fontSize = 12.sp,
                                    fontWeight = FontWeight.Bold,
                                    modifier = Modifier
                                        .padding(top = 4.dp)
                                        .clickable { onPostClick(post.id) }
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // TEMPORARY TESTING REQUIREMENT: "Show the mediaUrl as small text under every post temporarily for testing"
                                Surface(
                                    color = DarkBackground,
                                    shape = RoundedCornerShape(6.dp),
                                    modifier = Modifier.fillMaxWidth()
                                ) {
                                    Text(
                                        text = "CDN: ${post.mediaUrl}",
                                        color = Color(0xFF60A5FA),
                                        fontSize = 10.sp,
                                        fontFamily = FontFamily.Monospace,
                                        modifier = Modifier.padding(6.dp)
                                    )
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Actions: Likes, Views, Share
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        IconButton(onClick = { FirebaseRepository.toggleLike(post.id, currentUserId) }) {
                                            Icon(
                                                imageVector = if (isLiked) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                                contentDescription = "Like",
                                                tint = if (isLiked) NewsRed else Color.White
                                            )
                                        }
                                        Text(
                                            text = "${post.likesCount}",
                                            color = Color.White,
                                            fontWeight = FontWeight.Bold,
                                            fontSize = 13.sp
                                        )

                                        Spacer(modifier = Modifier.width(16.dp))

                                        Icon(Icons.Default.Visibility, contentDescription = null, tint = SlateGray, modifier = Modifier.size(16.dp))
                                        Spacer(modifier = Modifier.width(4.dp))
                                        Text(
                                            text = "${post.viewsCount} views",
                                            color = SlateGray,
                                            fontSize = 12.sp
                                        )
                                    }

                                    IconButton(
                                        onClick = {
                                            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                                                type = "text/plain"
                                                putExtra(
                                                    Intent.EXTRA_TEXT,
                                                    "🚨 DRIKQ NEWS BREAKING\n${post.title}\n📍 ${post.place}\n\nStream on Bunny CDN: ${post.mediaUrl}"
                                                )
                                            }
                                            context.startActivity(Intent.createChooser(shareIntent, "Share News"))
                                        }
                                    ) {
                                        Icon(Icons.Default.Share, contentDescription = "Share", tint = Color.White)
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

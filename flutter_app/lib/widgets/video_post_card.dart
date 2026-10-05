import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import '../models/post_model.dart';
import '../services/firebase_service.dart';
import '../theme/app_theme.dart';
import '../screens/full_screen_media_screen.dart';
import '../screens/reporter_profile_screen.dart';

class VideoPostCard extends StatelessWidget {
  final PostModel post;

  const VideoPostCard({super.key, required this.post});

  @override
  Widget build(BuildContext context) {
    final service = Provider.of<FirebaseService>(context);
    final currentUserId = service.currentUser?.uid ?? 'guest';
    final isLiked = post.likedByUsers.contains(currentUserId);

    final reporter = service.reporters.cast<dynamic>().firstWhere(
          (r) => r.id == post.reporterId,
          orElse: () => null,
        );
    final isFollowed =
        reporter != null && reporter.followedByUsers.contains(currentUserId);

    return Card(
      margin: const EdgeInsets.symmetric(horizontal: 12, vertical: 8),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.start,
        children: [
          // Reporter Header
          Padding(
            padding: const EdgeInsets.all(12.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                GestureDetector(
                  onTap: () {
                    Navigator.push(
                      context,
                      MaterialPageRoute(
                        builder: (_) => ReporterProfileScreen(
                            reporterId: post.reporterId),
                      ),
                    );
                  },
                  child: Row(
                    children: [
                      CircleAvatar(
                        radius: 20,
                        backgroundImage: NetworkImage(post.reporterPhotoUrl),
                      ),
                      const SizedBox(width: 10),
                      Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(
                            post.reporterName,
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                              fontSize: 14,
                            ),
                          ),
                          Row(
                            children: [
                              const Icon(Icons.location_on,
                                  color: AppTheme.primaryRed, size: 12),
                              const SizedBox(width: 2),
                              Text(
                                post.place,
                                style: const TextStyle(
                                  color: AppTheme.textGray,
                                  fontSize: 11,
                                ),
                              ),
                            ],
                          ),
                        ],
                      ),
                    ],
                  ),
                ),

                // Follow button
                ElevatedButton.icon(
                  onPressed: () {
                    service.toggleFollowReporter(post.reporterId, currentUserId);
                  },
                  style: ElevatedButton.styleFrom(
                    backgroundColor:
                        isFollowed ? AppTheme.borderSlate : AppTheme.primaryRed,
                    foregroundColor: Colors.white,
                    padding:
                        const EdgeInsets.symmetric(horizontal: 12, vertical: 4),
                    minimumSize: const Size(80, 32),
                    shape: RoundedRectangleBorder(
                      borderRadius: BorderRadius.circular(16),
                    ),
                  ),
                  icon: Icon(isFollowed ? Icons.check : Icons.person_add,
                      size: 14),
                  label: Text(
                    isFollowed ? 'Following' : 'Follow',
                    style: const TextStyle(fontSize: 11, fontWeight: FontWeight.bold),
                  ),
                ),
              ],
            ),
          ),

          // Media Player / Photo Container
          GestureDetector(
            onTap: () {
              service.incrementView(post.id);
              Navigator.push(
                context,
                MaterialPageRoute(
                  builder: (_) => FullScreenMediaScreen(post: post),
                ),
              );
            },
            child: Stack(
              children: [
                Container(
                  height: 250,
                  width: double.infinity,
                  color: Colors.black,
                  child: Image.network(
                    post.thumbnailUrl,
                    fit: BoxFit.cover,
                    errorBuilder: (_, __, ___) => const Center(
                      child: Icon(Icons.broken_image, color: AppTheme.textGray),
                    ),
                  ),
                ),
                Positioned(
                  top: 10,
                  left: 10,
                  child: Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppTheme.primaryRed,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    child: Text(
                      post.mediaType == MediaType.video ? 'VIDEO' : 'PHOTO',
                      style: const TextStyle(
                        color: Colors.white,
                        fontSize: 10,
                        fontWeight: FontWeight.bold,
                      ),
                    ),
                  ),
                ),
                if (post.mediaType == MediaType.video)
                  const Positioned.fill(
                    child: Center(
                      child: CircleAvatar(
                        radius: 26,
                        backgroundColor: Colors.black54,
                        child: Icon(Icons.play_arrow,
                            color: Colors.white, size: 34),
                      ),
                    ),
                  ),
              ],
            ),
          ),

          // Post Title
          Padding(
            padding: const EdgeInsets.fromLTRB(14, 10, 14, 4),
            child: Text(
              post.title,
              maxLines: 2,
              overflow: TextOverflow.ellipsis,
              style: const TextStyle(
                color: Colors.white,
                fontWeight: FontWeight.bold,
                fontSize: 15,
                height: 1.3,
              ),
            ),
          ),

          // Actions Row: Like, Views, Share
          Padding(
            padding: const EdgeInsets.symmetric(horizontal: 8.0, vertical: 4.0),
            child: Row(
              mainAxisAlignment: MainAxisAlignment.spaceBetween,
              children: [
                Row(
                  children: [
                    // Like button + count
                    IconButton(
                      icon: Icon(
                        isLiked ? Icons.favorite : Icons.favorite_border,
                        color: isLiked ? AppTheme.primaryRed : Colors.white,
                      ),
                      onPressed: () {
                        service.toggleLike(post.id, currentUserId);
                      },
                    ),
                    Text(
                      '${post.likesCount}',
                      style: const TextStyle(
                        color: Colors.white,
                        fontWeight: FontWeight.bold,
                        fontSize: 13,
                      ),
                    ),
                    const SizedBox(width: 16),

                    // Views count
                    const Icon(Icons.visibility,
                        color: AppTheme.textGray, size: 18),
                    const SizedBox(width: 4),
                    Text(
                      '${post.viewsCount} views',
                      style: const TextStyle(
                        color: AppTheme.textGray,
                        fontSize: 12,
                      ),
                    ),
                  ],
                ),

                // Share button
                IconButton(
                  icon: const Icon(Icons.share, color: Colors.white),
                  onPressed: () {
                    Share.share(
                      '🚨 DRIKQ NEWS BREAKING REPORT 🚨\n\n${post.title}\n📍 ${post.place}\nReporter: ${post.reporterName}\n\nDownload Drikq News App: https://play.google.com/store/apps/details?id=com.aistudio.drikqnews.rtvzxp',
                    );
                  },
                ),
              ],
            ),
          ),
        ],
      ),
    );
  }
}

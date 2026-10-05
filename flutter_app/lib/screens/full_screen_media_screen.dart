import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import 'package:share_plus/share_plus.dart';
import '../models/post_model.dart';
import '../services/firebase_service.dart';
import '../theme/app_theme.dart';
import 'reporter_profile_screen.dart';

class FullScreenMediaScreen extends StatelessWidget {
  final PostModel post;

  const FullScreenMediaScreen({super.key, required this.post});

  @override
  Widget build(BuildContext context) {
    final service = Provider.of<FirebaseService>(context);
    final currentUid = service.currentUser?.uid ?? 'guest';
    final isLiked = post.likedByUsers.contains(currentUid);

    return Scaffold(
      backgroundColor: Colors.black,
      appBar: AppBar(
        backgroundColor: Colors.black,
        title: const Text('Eyewitness Report'),
        actions: [
          IconButton(
            icon: const Icon(Icons.share),
            onPressed: () {
              Share.share(
                '🚨 DRIKQ NEWS BREAKING REPORT 🚨\n\n${post.title}\n📍 ${post.place}\nReporter: ${post.reporterName}\n\n${post.description}\n\nDownload Drikq News App: https://play.google.com/store/apps/details?id=com.aistudio.drikqnews.rtvzxp',
              );
            },
          ),
        ],
      ),
      body: SingleChildScrollView(
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            // Media Frame (Video or Photo)
            Container(
              height: 320,
              width: double.infinity,
              color: Colors.black,
              child: Image.network(
                post.mediaUrl,
                fit: BoxFit.contain,
              ),
            ),

            Padding(
              padding: const EdgeInsets.all(18.0),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Container(
                    padding:
                        const EdgeInsets.symmetric(horizontal: 8, vertical: 4),
                    decoration: BoxDecoration(
                      color: AppTheme.primaryRed.withOpacity(0.2),
                      borderRadius: BorderRadius.circular(6),
                    ),
                    child: Row(
                      mainAxisSize: MainAxisSize.min,
                      children: [
                        const Icon(Icons.location_on,
                            color: AppTheme.primaryRed, size: 14),
                        const SizedBox(width: 4),
                        Text(
                          post.place,
                          style: const TextStyle(
                            color: AppTheme.primaryRed,
                            fontWeight: FontWeight.bold,
                            fontSize: 12,
                          ),
                        ),
                      ],
                    ),
                  ),
                  const SizedBox(height: 12),
                  Text(
                    post.title,
                    style: const TextStyle(
                      color: Colors.white,
                      fontSize: 22,
                      fontWeight: FontWeight.bold,
                      height: 1.3,
                    ),
                  ),
                  const SizedBox(height: 14),

                  // Likes & Views Counter
                  Row(
                    children: [
                      IconButton(
                        icon: Icon(
                          isLiked ? Icons.favorite : Icons.favorite_border,
                          color: isLiked ? AppTheme.primaryRed : Colors.white,
                        ),
                        onPressed: () =>
                            service.toggleLike(post.id, currentUid),
                      ),
                      Text(
                        '${post.likesCount} Likes',
                        style: const TextStyle(
                            color: Colors.white, fontWeight: FontWeight.bold),
                      ),
                      const SizedBox(width: 20),
                      const Icon(Icons.visibility,
                          color: AppTheme.textGray, size: 18),
                      const SizedBox(width: 4),
                      Text(
                        '${post.viewsCount} Views',
                        style: const TextStyle(color: AppTheme.textGray),
                      ),
                    ],
                  ),
                  const Divider(color: AppTheme.borderSlate),
                  const SizedBox(height: 8),

                  // Reporter Attribution
                  InkWell(
                    onTap: () {
                      Navigator.push(
                        context,
                        MaterialPageRoute(
                          builder: (_) => ReporterProfileScreen(
                              reporterId: post.reporterId),
                        ),
                      );
                    },
                    child: Container(
                      padding: const EdgeInsets.all(12),
                      decoration: BoxDecoration(
                        color: AppTheme.darkSurface,
                        borderRadius: BorderRadius.circular(12),
                      ),
                      child: Row(
                        children: [
                          CircleAvatar(
                            radius: 22,
                            backgroundImage:
                                NetworkImage(post.reporterPhotoUrl),
                          ),
                          const SizedBox(width: 12),
                          Expanded(
                            child: Column(
                              crossAxisAlignment: CrossAxisAlignment.start,
                              children: [
                                Text(
                                  'Filed by ${post.reporterName}',
                                  style: const TextStyle(
                                    color: Colors.white,
                                    fontWeight: FontWeight.bold,
                                  ),
                                ),
                                const Text(
                                  'Tap to view reporter profile',
                                  style: TextStyle(
                                      color: AppTheme.textGray, fontSize: 11),
                                ),
                              ],
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),

                  const SizedBox(height: 18),
                  const Text(
                    'Full Eyewitness Report',
                    style: TextStyle(
                      color: Colors.white,
                      fontSize: 16,
                      fontWeight: FontWeight.bold,
                    ),
                  ),
                  const SizedBox(height: 8),
                  Text(
                    post.description,
                    style: const TextStyle(
                      color: Colors.white70,
                      fontSize: 15,
                      height: 1.5,
                    ),
                  ),
                ],
              ),
            ),
          ],
        ),
      ),
    );
  }
}

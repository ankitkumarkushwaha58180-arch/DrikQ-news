import 'package:flutter/material.dart';
import 'package:provider/provider.dart';
import '../models/reporter_model.dart';
import '../services/firebase_service.dart';
import '../theme/app_theme.dart';
import 'full_screen_media_screen.dart';

class ReporterProfileScreen extends StatelessWidget {
  final String reporterId;

  const ReporterProfileScreen({super.key, required this.reporterId});

  @override
  Widget build(BuildContext context) {
    final service = Provider.of<FirebaseService>(context);
    final currentUid = service.currentUser?.uid ?? 'guest';

    final reporter = service.reporters.cast<ReporterModel?>().firstWhere(
          (r) => r?.id == reporterId,
          orElse: () => null,
        );

    final reporterPosts = service.approvedPosts
        .where((p) => p.reporterId == reporterId)
        .toList();

    if (reporter == null) {
      return Scaffold(
        backgroundColor: AppTheme.darkBackground,
        appBar: AppBar(title: const Text('Reporter Profile')),
        body: const Center(
          child: Text('Reporter not found', style: TextStyle(color: Colors.white)),
        ),
      );
    }

    final isFollowed = reporter.followedByUsers.contains(currentUid);

    return Scaffold(
      backgroundColor: AppTheme.darkBackground,
      appBar: AppBar(
        title: const Text('Reporter Profile'),
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(16.0),
        child: Column(
          children: [
            // Reporter Header Card
            Container(
              padding: const EdgeInsets.all(20),
              decoration: BoxDecoration(
                color: AppTheme.darkSurface,
                borderRadius: BorderRadius.circular(16),
                border: Border.all(color: AppTheme.borderSlate),
              ),
              child: Column(
                children: [
                  CircleAvatar(
                    radius: 46,
                    backgroundImage: NetworkImage(reporter.photoUrl),
                  ),
                  const SizedBox(height: 12),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      Text(
                        reporter.name,
                        style: const TextStyle(
                          color: Colors.white,
                          fontSize: 20,
                          fontWeight: FontWeight.bold,
                        ),
                      ),
                      const SizedBox(width: 6),
                      const Icon(Icons.verified, color: Colors.blueAccent, size: 18),
                    ],
                  ),
                  const SizedBox(height: 4),
                  Row(
                    mainAxisAlignment: MainAxisAlignment.center,
                    children: [
                      const Icon(Icons.location_on,
                          color: AppTheme.primaryRed, size: 14),
                      const SizedBox(width: 4),
                      Text(
                        reporter.address,
                        style: const TextStyle(
                            color: AppTheme.textGray, fontSize: 12),
                      ),
                    ],
                  ),
                  const SizedBox(height: 18),

                  // Stats Row
                  Container(
                    padding: const EdgeInsets.symmetric(vertical: 12),
                    decoration: BoxDecoration(
                      color: AppTheme.darkBackground,
                      borderRadius: BorderRadius.circular(12),
                    ),
                    child: Row(
                      mainAxisAlignment: MainAxisAlignment.spaceEvenly,
                      children: [
                        _buildStat('Followers', '${reporter.followersCount}'),
                        Container(height: 30, width: 1, color: AppTheme.borderSlate),
                        _buildStat('Following', '${reporter.followingCount}'),
                        Container(height: 30, width: 1, color: AppTheme.borderSlate),
                        _buildStat('Stories', '${reporterPosts.length}'),
                      ],
                    ),
                  ),
                  const SizedBox(height: 18),

                  // Follow / Unfollow Button
                  ElevatedButton.icon(
                    style: ElevatedButton.styleFrom(
                      backgroundColor:
                          isFollowed ? AppTheme.borderSlate : AppTheme.primaryRed,
                      foregroundColor: Colors.white,
                      minimumSize: const Size(double.infinity, 44),
                      shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12),
                      ),
                    ),
                    onPressed: () {
                      service.toggleFollowReporter(reporter.id, currentUid);
                    },
                    icon: Icon(isFollowed ? Icons.check : Icons.person_add),
                    label: Text(
                      isFollowed ? 'Following' : 'Follow Reporter',
                      style: const TextStyle(fontWeight: FontWeight.bold),
                    ),
                  ),
                ],
              ),
            ),
            const SizedBox(height: 24),

            // List of Reporter Posts
            Align(
              alignment: Alignment.centerLeft,
              child: Text(
                'Published News Stories (${reporterPosts.length})',
                style: const TextStyle(
                  color: Colors.white,
                  fontSize: 16,
                  fontWeight: FontWeight.bold,
                ),
              ),
            ),
            const SizedBox(height: 12),

            if (reporterPosts.isEmpty)
              const Padding(
                padding: EdgeInsets.all(32.0),
                child: Text('No approved reports yet.',
                    style: TextStyle(color: AppTheme.textGray)),
              )
            else
              ListView.builder(
                shrinkWrap: true,
                physics: const NeverScrollableScrollPhysics(),
                itemCount: reporterPosts.length,
                itemBuilder: (ctx, index) {
                  final post = reporterPosts[index];
                  return Card(
                    margin: const EdgeInsets.only(bottom: 10),
                    child: ListTile(
                      contentPadding: const EdgeInsets.all(8),
                      leading: ClipRRect(
                        borderRadius: BorderRadius.circular(8),
                        child: Image.network(
                          post.thumbnailUrl,
                          width: 60,
                          height: 60,
                          fit: BoxFit.cover,
                        ),
                      ),
                      title: Text(
                        post.title,
                        maxLines: 2,
                        overflow: TextOverflow.ellipsis,
                        style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 13,
                        ),
                      ),
                      subtitle: Text(
                        '${post.place} • ${post.likesCount} likes',
                        style: const TextStyle(
                            color: AppTheme.textGray, fontSize: 11),
                      ),
                      onTap: () {
                        Navigator.push(
                          context,
                          MaterialPageRoute(
                            builder: (_) => FullScreenMediaScreen(post: post),
                          ),
                        );
                      },
                    ),
                  );
                },
              ),
          ],
        ),
      ),
    );
  }

  Widget _buildStat(String label, String value) {
    return Column(
      children: [
        Text(
          value,
          style: const TextStyle(
            color: Colors.white,
            fontWeight: FontWeight.bold,
            fontSize: 16,
          ),
        ),
        const SizedBox(height: 2),
        Text(
          label,
          style: const TextStyle(color: AppTheme.textGray, fontSize: 11),
        ),
      ],
    );
  }
}

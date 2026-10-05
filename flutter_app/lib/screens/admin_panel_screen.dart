import 'package:flutter/material.dart';
import 'package:flutter/services.dart';
import 'package:provider/provider.dart';
import '../models/post_model.dart';
import '../models/reporter_model.dart';
import '../services/firebase_service.dart';
import '../theme/app_theme.dart';

class AdminPanelScreen extends StatefulWidget {
  const AdminPanelScreen({super.key});

  @override
  State<AdminPanelScreen> createState() => _AdminPanelScreenState();
}

class _AdminPanelScreenState extends State<AdminPanelScreen>
    with SingleTickerProviderStateMixin {
  late TabController _tabController;
  final _adminPasswordController = TextEditingController(text: 'admin');
  String? _adminError;

  @override
  void initState() {
    super.initState();
    _tabController = TabController(length: 4, vsync: this);
  }

  @override
  void dispose() {
    _tabController.dispose();
    super.dispose();
  }

  void _showAddReporterDialog() {
    final nameController = TextEditingController();
    final mobileController = TextEditingController();
    final addressController = TextEditingController();
    String? selectedPhotoPath;

    showDialog(
      context: context,
      builder: (ctx) => StatefulBuilder(
        builder: (ctx, setDialogState) => AlertDialog(
          backgroundColor: AppTheme.darkSurface,
          title: const Text('Add New Ground Reporter',
              style: TextStyle(color: Colors.white, fontWeight: FontWeight.bold)),
          content: SingleChildScrollView(
            child: Column(
              mainAxisSize: MainAxisSize.min,
              children: [
                // Circular profile photo preview
                GestureDetector(
                  onTap: () async {
                    // Pick profile photo from phone gallery
                  },
                  child: Stack(
                    alignment: Alignment.bottomRight,
                    children: [
                      CircleAvatar(
                        radius: 38,
                        backgroundColor: AppTheme.darkBackground,
                        child: const Icon(Icons.person, size: 40, color: AppTheme.textGray),
                      ),
                      Container(
                        padding: const EdgeInsets.all(4),
                        decoration: const BoxDecoration(
                          color: AppTheme.primaryRed,
                          shape: BoxShape.circle,
                        ),
                        child: const Icon(Icons.camera_alt, color: Colors.white, size: 16),
                      ),
                    ],
                  ),
                ),
                const SizedBox(height: 8),
                TextButton.icon(
                  onPressed: () {
                    // Gallery pick
                  },
                  icon: const Icon(Icons.photo_library, size: 16, color: AppTheme.primaryRed),
                  label: const Text('Upload Profile Photo',
                      style: TextStyle(color: AppTheme.primaryRed, fontSize: 12)),
                ),
                const SizedBox(height: 12),
                TextField(
                  controller: nameController,
                  decoration: const InputDecoration(labelText: 'Full Name *'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: mobileController,
                  decoration: const InputDecoration(labelText: 'Mobile Number *'),
                ),
                const SizedBox(height: 10),
                TextField(
                  controller: addressController,
                  decoration: const InputDecoration(labelText: 'Address / Beat *'),
                ),
                const SizedBox(height: 12),
                const Text(
                  'Note: System will auto-generate unique User ID and Password upon save.',
                  style: TextStyle(color: Colors.amber, fontSize: 11),
                ),
              ],
            ),
          ),
          actions: [
            TextButton(
              onPressed: () => Navigator.pop(ctx),
              child: const Text('Cancel', style: TextStyle(color: AppTheme.textGray)),
            ),
            ElevatedButton(
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.primaryRed,
                foregroundColor: Colors.white,
              ),
              onPressed: () async {
                if (nameController.text.isNotEmpty &&
                    mobileController.text.isNotEmpty &&
                    addressController.text.isNotEmpty) {
                  Navigator.pop(ctx);
                  final service =
                      Provider.of<FirebaseService>(context, listen: false);
                  final newRep = await service.createReporter(
                    name: nameController.text.trim(),
                    mobile: mobileController.text.trim(),
                    address: addressController.text.trim(),
                    photoUrl: selectedPhotoPath ?? 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80',
                  );
                  _showGeneratedCredentialsDialog(newRep);
                }
              },
              child: const Text('Create Reporter'),
            ),
          ],
        ),
      ),
    );
  }
        ],
      ),
    );
  }

  void _showGeneratedCredentialsDialog(ReporterModel reporter) {
    showDialog(
      context: context,
      builder: (ctx) => AlertDialog(
        backgroundColor: AppTheme.darkSurface,
        title: const Row(
          children: [
            Icon(Icons.check_circle, color: Colors.green),
            SizedBox(width: 8),
            Text('Reporter Created!'),
          ],
        ),
        content: Column(
          mainAxisSize: MainAxisSize.min,
          crossAxisAlignment: CrossAxisAlignment.start,
          children: [
            Text('Share these credentials with ${reporter.name}:',
                style: const TextStyle(color: AppTheme.textGray)),
            const SizedBox(height: 12),
            Container(
              padding: const EdgeInsets.all(12),
              decoration: BoxDecoration(
                color: AppTheme.darkBackground,
                borderRadius: BorderRadius.circular(8),
              ),
              child: Column(
                crossAxisAlignment: CrossAxisAlignment.start,
                children: [
                  Text('User ID: ${reporter.id}',
                      style: const TextStyle(
                          color: AppTheme.primaryRed,
                          fontWeight: FontWeight.bold,
                          fontSize: 16)),
                  const SizedBox(height: 4),
                  Text('Password: ${reporter.password}',
                      style: const TextStyle(
                          color: Colors.white,
                          fontWeight: FontWeight.bold,
                          fontSize: 16)),
                ],
              ),
            ),
          ],
        ),
        actions: [
          ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppTheme.primaryRed,
              foregroundColor: Colors.white,
            ),
            onPressed: () {
              Clipboard.setData(ClipboardData(
                  text:
                      'Drikq News Reporter Login\nUser ID: ${reporter.id}\nPassword: ${reporter.password}'));
              Navigator.pop(ctx);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Credentials copied to clipboard!')),
              );
            },
            icon: const Icon(Icons.copy, size: 16),
            label: const Text('Copy Credentials'),
          ),
        ],
      ),
    );
  }

  @override
  Widget build(BuildContext context) {
    final service = Provider.of<FirebaseService>(context);

    // Admin login gate
    if (!service.isAdminLoggedIn) {
      return Scaffold(
        backgroundColor: AppTheme.darkBackground,
        appBar: AppBar(title: const Text('Admin Panel')),
        body: Center(
          child: SingleChildScrollView(
            padding: const EdgeInsets.all(28.0),
            child: Column(
              mainAxisAlignment: MainAxisAlignment.center,
              children: [
                const Icon(Icons.shield, size: 64, color: Color(0xFF60A5FA)),
                const SizedBox(height: 16),
                const Text(
                  'Editorial Admin Access',
                  style: TextStyle(fontSize: 22, fontWeight: FontWeight.bold),
                ),
                const SizedBox(height: 8),
                const Text(
                  'Manage reporters, content approvals, privacy policy & alerts',
                  textAlign: TextAlign.center,
                  style: TextStyle(color: AppTheme.textGray, fontSize: 13),
                ),
                const SizedBox(height: 32),
                TextField(
                  controller: _adminPasswordController,
                  obscureText: true,
                  decoration: const InputDecoration(
                    labelText: 'Admin Password (default: admin)',
                    prefixIcon: Icon(Icons.lock),
                    filled: true,
                    fillColor: AppTheme.darkSurface,
                  ),
                ),
                if (_adminError != null) ...[
                  const SizedBox(height: 8),
                  Text(_adminError!, style: const TextStyle(color: Colors.redAccent)),
                ],
                const SizedBox(height: 20),
                ElevatedButton(
                  style: ElevatedButton.styleFrom(
                    backgroundColor: const Color(0xFF2563EB),
                    foregroundColor: Colors.white,
                    minimumSize: const Size(double.infinity, 50),
                    shape: RoundedRectangleBorder(
                        borderRadius: BorderRadius.circular(12)),
                  ),
                  onPressed: () {
                    final ok =
                        service.loginAdmin(_adminPasswordController.text);
                    if (!ok) {
                      setState(() =>
                          _adminError = 'Invalid passcode. Default is "admin"');
                    }
                  },
                  child: const Text('Enter Admin Panel',
                      style: TextStyle(fontWeight: FontWeight.bold)),
                ),
              ],
            ),
          ),
        ),
      );
    }

    return Scaffold(
      backgroundColor: AppTheme.darkBackground,
      appBar: AppBar(
        title: const Text('Editorial Admin Panel'),
        actions: [
          TextButton(
            onPressed: () => service.logoutAdmin(),
            child: const Text('Log Out',
                style: TextStyle(color: AppTheme.primaryRed, fontWeight: FontWeight.bold)),
          ),
        ],
        bottom: TabBar(
          controller: _tabController,
          indicatorColor: AppTheme.primaryRed,
          labelColor: Colors.white,
          unselectedLabelColor: AppTheme.textGray,
          isScrollable: true,
          tabs: const [
            Tab(text: 'Reporters'),
            Tab(text: 'Content'),
            Tab(text: 'Privacy Policy'),
            Tab(text: 'Notifications'),
          ],
        ),
      ),
      body: TabBarView(
        controller: _tabController,
        children: [
          // Section A: Reporter Management
          _buildReporterManagement(service),

          // Section B: Content Management
          _buildContentManagement(service),

          // Section C: Privacy Policy
          _buildPrivacyPolicySection(service),

          // Section D: Push Notification
          _buildPushNotificationSection(service),
        ],
      ),
      floatingActionButton: _tabController.index == 0
          ? FloatingActionButton(
              backgroundColor: AppTheme.primaryRed,
              onPressed: _showAddReporterDialog,
              child: const Icon(Icons.add, color: Colors.white),
            )
          : null,
    );
  }

  Widget _buildReporterManagement(FirebaseService service) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: service.reporters.length,
      itemBuilder: (ctx, index) {
        final reporter = service.reporters[index];
        return Card(
          margin: const EdgeInsets.only(bottom: 12),
          child: Padding(
            padding: const EdgeInsets.all(12.0),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  children: [
                    CircleAvatar(
                      radius: 24,
                      backgroundImage: NetworkImage(reporter.photoUrl),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Text(reporter.name,
                              style: const TextStyle(
                                  color: Colors.white,
                                  fontWeight: FontWeight.bold,
                                  fontSize: 15)),
                          Text('ID: ${reporter.id} • ${reporter.mobile}',
                              style: const TextStyle(
                                  color: AppTheme.textGray, fontSize: 12)),
                          Text(reporter.address,
                              style: const TextStyle(
                                  color: Colors.white70, fontSize: 11)),
                        ],
                      ),
                    ),
                    IconButton(
                      icon: const Icon(Icons.delete, color: AppTheme.primaryRed),
                      onPressed: () => service.deleteReporter(reporter.id),
                    ),
                  ],
                ),
                const Divider(color: AppTheme.borderSlate),
                Row(
                  mainAxisAlignment: MainAxisAlignment.spaceBetween,
                  children: [
                    Text('Password: ${reporter.password}',
                        style: const TextStyle(
                            color: Colors.amber,
                            fontSize: 12,
                            fontWeight: FontWeight.bold)),
                    Text(
                        'Followers: ${reporter.followersCount} | Reports: 12',
                        style: const TextStyle(
                            color: AppTheme.textGray, fontSize: 11)),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildContentManagement(FirebaseService service) {
    return ListView.builder(
      padding: const EdgeInsets.all(16),
      itemCount: service.posts.length,
      itemBuilder: (ctx, index) {
        final post = service.posts[index];
        final isApproved = post.status == 'approved';

        return Card(
          margin: const EdgeInsets.only(bottom: 14),
          child: Padding(
            padding: const EdgeInsets.all(12),
            child: Column(
              crossAxisAlignment: CrossAxisAlignment.start,
              children: [
                Row(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    ClipRRect(
                      borderRadius: BorderRadius.circular(8),
                      child: Image.network(
                        post.thumbnailUrl,
                        width: 90,
                        height: 90,
                        fit: BoxFit.cover,
                      ),
                    ),
                    const SizedBox(width: 12),
                    Expanded(
                      child: Column(
                        crossAxisAlignment: CrossAxisAlignment.start,
                        children: [
                          Container(
                            padding: const EdgeInsets.symmetric(
                                horizontal: 6, vertical: 2),
                            decoration: BoxDecoration(
                              color: isApproved ? Colors.green : Colors.amber,
                              borderRadius: BorderRadius.circular(4),
                            ),
                            child: Text(
                              post.status.toUpperCase(),
                              style: const TextStyle(
                                  fontSize: 9,
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white),
                            ),
                          ),
                          const SizedBox(height: 4),
                          Text(post.title,
                              maxLines: 2,
                              style: const TextStyle(
                                  fontWeight: FontWeight.bold,
                                  color: Colors.white,
                                  fontSize: 13)),
                          const SizedBox(height: 2),
                          Text('${post.place} • Reporter: ${post.reporterName}',
                              style: const TextStyle(
                                  color: AppTheme.textGray, fontSize: 11)),
                        ],
                      ),
                    ),
                  ],
                ),
                const SizedBox(height: 10),
                Text(post.description,
                    maxLines: 2,
                    style:
                        const TextStyle(color: Colors.white70, fontSize: 12)),
                const SizedBox(height: 12),
                Row(
                  mainAxisAlignment: MainAxisAlignment.end,
                  children: [
                    if (!isApproved) ...[
                      ElevatedButton.icon(
                        style: ElevatedButton.styleFrom(
                            backgroundColor: Colors.green),
                        onPressed: () => service.approvePost(post.id),
                        icon: const Icon(Icons.check, size: 16),
                        label: const Text('Approve'),
                      ),
                      const SizedBox(width: 8),
                    ],
                    OutlinedButton.icon(
                      style: OutlinedButton.styleFrom(
                          foregroundColor: AppTheme.primaryRed),
                      onPressed: () => service.deletePost(post.id),
                      icon: const Icon(Icons.delete, size: 16),
                      label: const Text('Delete'),
                    ),
                  ],
                ),
              ],
            ),
          ),
        );
      },
    );
  }

  Widget _buildPrivacyPolicySection(FirebaseService service) {
    final policyCtrl = TextEditingController(text: service.privacyPolicy);
    final termsCtrl = TextEditingController(text: service.termsAndConditions);

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Privacy Policy Text',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 8),
          TextField(
            controller: policyCtrl,
            maxLines: 6,
            decoration: const InputDecoration(filled: true),
          ),
          const SizedBox(height: 20),
          const Text('Terms & Conditions Text',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 8),
          TextField(
            controller: termsCtrl,
            maxLines: 6,
            decoration: const InputDecoration(filled: true),
          ),
          const SizedBox(height: 20),
          ElevatedButton(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppTheme.primaryRed,
              foregroundColor: Colors.white,
              minimumSize: const Size(double.infinity, 50),
            ),
            onPressed: () {
              service.updatePolicies(policyCtrl.text, termsCtrl.text);
              ScaffoldMessenger.of(context).showSnackBar(
                const SnackBar(content: Text('Policies updated in Firebase!')),
              );
            },
            child: const Text('Save & Publish to App'),
          ),
        ],
      ),
    );
  }

  Widget _buildPushNotificationSection(FirebaseService service) {
    final titleCtrl = TextEditingController();
    final bodyCtrl = TextEditingController();
    String? selectedPostId;

    return SingleChildScrollView(
      padding: const EdgeInsets.all(16),
      child: Column(
        crossAxisAlignment: CrossAxisAlignment.stretch,
        children: [
          const Text('Broadcast Push Notification',
              style: TextStyle(fontWeight: FontWeight.bold, fontSize: 16)),
          const SizedBox(height: 4),
          const Text('Send breaking news alerts to all app subscribers',
              style: TextStyle(color: AppTheme.textGray, fontSize: 12)),
          const SizedBox(height: 16),
          TextField(
            controller: titleCtrl,
            decoration: const InputDecoration(
                labelText: 'Title (e.g. Breaking News)', filled: true),
          ),
          const SizedBox(height: 12),
          TextField(
            controller: bodyCtrl,
            maxLines: 3,
            decoration: const InputDecoration(
                labelText: 'Message Body', filled: true),
          ),
          const SizedBox(height: 20),
          ElevatedButton.icon(
            style: ElevatedButton.styleFrom(
              backgroundColor: AppTheme.primaryRed,
              foregroundColor: Colors.white,
              minimumSize: const Size(double.infinity, 50),
            ),
            onPressed: () {
              if (titleCtrl.text.isNotEmpty && bodyCtrl.text.isNotEmpty) {
                service.sendPushNotification(
                    titleCtrl.text.trim(), bodyCtrl.text.trim(), selectedPostId);
                titleCtrl.clear();
                bodyCtrl.clear();
                ScaffoldMessenger.of(context).showSnackBar(
                  const SnackBar(
                      content: Text('Push notification broadcasted to all users!')),
                );
              }
            },
            icon: const Icon(Icons.send),
            label: const Text('Send Broadcast Notification'),
          ),
        ],
      ),
    );
  }
}

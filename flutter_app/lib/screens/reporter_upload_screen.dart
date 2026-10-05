import 'dart:io';
import 'package:flutter/material.dart';
import 'package:image_picker/image_picker.dart';
import 'package:provider/provider.dart';
import '../models/post_model.dart';
import '../services/firebase_service.dart';
import '../theme/app_theme.dart';
import 'home_feed_screen.dart';

class ReporterUploadScreen extends StatefulWidget {
  const ReporterUploadScreen({super.key});

  @override
  State<ReporterUploadScreen> createState() => _ReporterUploadScreenState();
}

class _ReporterUploadScreenState extends State<ReporterUploadScreen> {
  final _titleController = TextEditingController();
  final _descriptionController = TextEditingController();
  final _placeController = TextEditingController();

  MediaType _selectedMediaType = MediaType.video;
  File? _pickedMediaFile;
  final ImagePicker _picker = ImagePicker();

  bool _isUploading = false;
  double _uploadProgress = 0.0;
  String? _errorMessage;

  void _pickMedia() async {
    final XFile? file;
    if (_selectedMediaType == MediaType.video) {
      file = await _picker.pickVideo(source: ImageSource.gallery);
    } else {
      file = await _picker.pickImage(source: ImageSource.gallery);
    }

    if (file != null) {
      setState(() => _pickedMediaFile = File(file!.path));
    }
  }

  void _handleSubmit() async {
    final title = _titleController.text.trim();
    final description = _descriptionController.text.trim();
    final place = _placeController.text.trim();

    if (title.isEmpty) {
      setState(() => _errorMessage = 'Title is required');
      return;
    }
    if (place.isEmpty) {
      setState(() => _errorMessage = 'Place / Location name is required');
      return;
    }
    if (description.isEmpty) {
      setState(() => _errorMessage = 'Description is required');
      return;
    }

    setState(() {
      _isUploading = true;
      _uploadProgress = 0.0;
      _errorMessage = null;
    });

    final service = Provider.of<FirebaseService>(context, listen: false);
    final success = await service.uploadPost(
      title: title,
      description: description,
      place: place,
      mediaType: _selectedMediaType,
      mediaFile: _pickedMediaFile,
      onProgress: (progress) {
        setState(() => _uploadProgress = progress);
      },
    );

    if (success && mounted) {
      ScaffoldMessenger.of(context).showSnackBar(
        const SnackBar(
          content: Text('News uploaded! Status: Pending Editorial Approval'),
          backgroundColor: Colors.green,
        ),
      );
      // After successful upload -> automatically redirect to Home Feed
      Navigator.pushAndRemoveUntil(
        context,
        MaterialPageRoute(builder: (_) => const MainHomeScreen()),
        (route) => false,
      );
    } else {
      setState(() {
        _isUploading = false;
        _errorMessage = 'Upload failed. Please try again.';
      });
    }
  }

  @override
  Widget build(BuildContext context) {
    final service = Provider.of<FirebaseService>(context);
    final reporter = service.currentReporter;

    return Scaffold(
      backgroundColor: AppTheme.darkBackground,
      appBar: AppBar(
        title: const Text('Reporter Upload Desk'),
        actions: [
          if (reporter != null)
            Padding(
              padding: const EdgeInsets.only(right: 16.0),
              child: Center(
                child: Container(
                  padding:
                      const EdgeInsets.symmetric(horizontal: 10, vertical: 4),
                  decoration: BoxDecoration(
                    color: AppTheme.primaryRed.withOpacity(0.2),
                    borderRadius: BorderRadius.circular(16),
                  ),
                  child: Text(
                    reporter.id,
                    style: const TextStyle(
                      color: AppTheme.primaryRed,
                      fontWeight: FontWeight.bold,
                      fontSize: 12,
                    ),
                  ),
                ),
              ),
            ),
        ],
      ),
      body: SingleChildScrollView(
        padding: const EdgeInsets.all(20.0),
        child: Column(
          crossAxisAlignment: CrossAxisAlignment.stretch,
          children: [
            // Media Type Selector
            const Text(
              '1. SELECT MEDIA TYPE',
              style: TextStyle(
                color: AppTheme.textGray,
                fontSize: 12,
                fontWeight: FontWeight.bold,
                letterSpacing: 1,
              ),
            ),
            const SizedBox(height: 8),
            Row(
              children: [
                Expanded(
                  child: GestureDetector(
                    onTap: _isUploading
                        ? null
                        : () => setState(() => _selectedMediaType = MediaType.video),
                    child: Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: _selectedMediaType == MediaType.video
                            ? AppTheme.darkSurface
                            : AppTheme.darkBackground,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: _selectedMediaType == MediaType.video
                              ? AppTheme.primaryRed
                              : AppTheme.borderSlate,
                          width: 2,
                        ),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.videocam,
                            color: _selectedMediaType == MediaType.video
                                ? AppTheme.primaryRed
                                : AppTheme.textGray,
                          ),
                          const SizedBox(width: 8),
                          const Text(
                            'Video News',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
                const SizedBox(width: 12),
                Expanded(
                  child: GestureDetector(
                    onTap: _isUploading
                        ? null
                        : () => setState(() => _selectedMediaType = MediaType.photo),
                    child: Container(
                      padding: const EdgeInsets.all(14),
                      decoration: BoxDecoration(
                        color: _selectedMediaType == MediaType.photo
                            ? AppTheme.darkSurface
                            : AppTheme.darkBackground,
                        borderRadius: BorderRadius.circular(12),
                        border: Border.all(
                          color: _selectedMediaType == MediaType.photo
                              ? AppTheme.primaryRed
                              : AppTheme.borderSlate,
                          width: 2,
                        ),
                      ),
                      child: Row(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            Icons.photo,
                            color: _selectedMediaType == MediaType.photo
                                ? AppTheme.primaryRed
                                : AppTheme.textGray,
                          ),
                          const SizedBox(width: 8),
                          const Text(
                            'Photo Story',
                            style: TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.bold,
                            ),
                          ),
                        ],
                      ),
                    ),
                  ),
                ),
              ],
            ),
            const SizedBox(height: 20),

            // Media Picker & Preview
            GestureDetector(
              onTap: _isUploading ? null : _pickMedia,
              child: Container(
                height: 180,
                decoration: BoxDecoration(
                  color: AppTheme.darkSurface,
                  borderRadius: BorderRadius.circular(14),
                  border: Border.all(color: AppTheme.borderSlate),
                ),
                child: _pickedMediaFile != null
                    ? ClipRRect(
                        borderRadius: BorderRadius.circular(14),
                        child: Image.file(
                          _pickedMediaFile!,
                          fit: BoxFit.cover,
                          width: double.infinity,
                        ),
                      )
                    : Column(
                        mainAxisAlignment: MainAxisAlignment.center,
                        children: [
                          Icon(
                            _selectedMediaType == MediaType.video
                                ? Icons.video_call
                                : Icons.add_photo_alternate,
                            size: 48,
                            color: AppTheme.primaryRed,
                          ),
                          const SizedBox(height: 8),
                          Text(
                            _selectedMediaType == MediaType.video
                                ? 'Tap to select or record video'
                                : 'Tap to select high-res photo',
                            style: const TextStyle(
                              color: Colors.white,
                              fontWeight: FontWeight.w600,
                            ),
                          ),
                          const SizedBox(height: 4),
                          const Text(
                            'Direct upload to Firebase Storage',
                            style: TextStyle(
                              color: AppTheme.textGray,
                              fontSize: 12,
                            ),
                          ),
                        ],
                      ),
              ),
            ),
            const SizedBox(height: 24),

            // Required Fields
            const Text(
              '2. NEWS DETAILS (REQUIRED)',
              style: TextStyle(
                color: AppTheme.textGray,
                fontSize: 12,
                fontWeight: FontWeight.bold,
                letterSpacing: 1,
              ),
            ),
            const SizedBox(height: 12),

            // Title (required)
            TextField(
              controller: _titleController,
              decoration: InputDecoration(
                labelText: 'Title / Headline *',
                labelStyle: const TextStyle(color: AppTheme.textGray),
                hintText: 'e.g. Breaking: Major Metro Line Reopens',
                filled: true,
                fillColor: AppTheme.darkSurface,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: AppTheme.borderSlate),
                ),
              ),
            ),
            const SizedBox(height: 14),

            // Place / Location name (required)
            TextField(
              controller: _placeController,
              decoration: InputDecoration(
                labelText: 'Place / Location Name *',
                labelStyle: const TextStyle(color: AppTheme.textGray),
                prefixIcon: const Icon(Icons.location_on, color: AppTheme.primaryRed),
                hintText: 'e.g. City Junction, Sector 4',
                filled: true,
                fillColor: AppTheme.darkSurface,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: AppTheme.borderSlate),
                ),
              ),
            ),
            const SizedBox(height: 14),

            // Description (required)
            TextField(
              controller: _descriptionController,
              maxLines: 4,
              decoration: InputDecoration(
                labelText: 'Description *',
                labelStyle: const TextStyle(color: AppTheme.textGray),
                hintText: 'Provide complete verified facts and impact...',
                filled: true,
                fillColor: AppTheme.darkSurface,
                border: OutlineInputBorder(
                  borderRadius: BorderRadius.circular(12),
                  borderSide: const BorderSide(color: AppTheme.borderSlate),
                ),
              ),
            ),

            if (_errorMessage != null) ...[
              const SizedBox(height: 14),
              Text(
                _errorMessage!,
                style: const TextStyle(color: Colors.redAccent, fontSize: 13),
              ),
            ],

            const SizedBox(height: 20),

            // Real-time upload progress bar from 0% to 100%
            if (_isUploading) ...[
              Container(
                padding: const EdgeInsets.all(16),
                decoration: BoxDecoration(
                  color: AppTheme.darkSurface,
                  borderRadius: BorderRadius.circular(12),
                ),
                child: Column(
                  crossAxisAlignment: CrossAxisAlignment.start,
                  children: [
                    Row(
                      mainAxisAlignment: MainAxisAlignment.spaceBetween,
                      children: [
                        const Text(
                          'Uploading to Firebase Storage...',
                          style: TextStyle(
                            color: Colors.white,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                        Text(
                          '${(_uploadProgress * 100).toInt()}%',
                          style: const TextStyle(
                            color: AppTheme.primaryRed,
                            fontWeight: FontWeight.bold,
                          ),
                        ),
                      ],
                    ),
                    const SizedBox(height: 10),
                    LinearProgressIndicator(
                      value: _uploadProgress,
                      color: AppTheme.primaryRed,
                      backgroundColor: AppTheme.borderSlate,
                      minHeight: 8,
                      borderRadius: BorderRadius.circular(4),
                    ),
                    const SizedBox(height: 6),
                    const Text(
                      'Setting status to "pending" by default',
                      style: TextStyle(color: AppTheme.textGray, fontSize: 11),
                    ),
                  ],
                ),
              ),
              const SizedBox(height: 20),
            ],

            // Submit Button
            ElevatedButton.icon(
              onPressed: _isUploading ? null : _handleSubmit,
              icon: const Icon(Icons.cloud_upload),
              label: Text(
                _isUploading ? 'Uploading...' : 'Submit News for Approval',
                style: const TextStyle(fontSize: 16, fontWeight: FontWeight.bold),
              ),
              style: ElevatedButton.styleFrom(
                backgroundColor: AppTheme.primaryRed,
                foregroundColor: Colors.white,
                minimumSize: const Size(double.infinity, 52),
                shape: RoundedRectangleBorder(
                  borderRadius: BorderRadius.circular(12),
                ),
              ),
            ),
          ],
        ),
      ),
    );
  }
}

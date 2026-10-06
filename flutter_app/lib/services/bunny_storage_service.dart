import 'dart:io';
import 'package:http/http.dart' as http;

class BunnyStorageService {
  static const String storageZoneName = 'drikq-news';
  static const String accessKey = '8acf1dfd-20d8-4cde-bfb7ee476c77-a29d-48c3';
  static const String storageUploadEndpoint = 'https://sg.storage.bunnycdn.com';
  static const String publicCdnBaseUrl = 'https://Drikq-news.b-cdn.net';

  /// Uploads a file to Bunny.net Edge Storage via HTTP PUT request.
  /// URL format: https://sg.storage.bunnycdn.com/drikq-news/{folder}/{fileName}
  /// Header: AccessKey: 8acf1dfd-20d8-4cde-bfb7ee476c77-a29d-48c3
  static Future<String?> uploadFile({
    required File file,
    required String folder,
    required String fileName,
    String? mimeType,
    Function(double progress)? onProgress,
  }) async {
    try {
      final uploadUrl = Uri.parse('$storageUploadEndpoint/$storageZoneName/$folder/$fileName');
      final bytes = await file.readAsBytes();

      final request = http.Request('PUT', uploadUrl)
        ..headers['AccessKey'] = accessKey
        ..headers['Content-Type'] = mimeType ?? 'application/octet-stream'
        ..bodyBytes = bytes;

      onProgress?.call(0.5);

      final response = await request.send();

      if (response.statusCode == 200 || response.statusCode == 201) {
        onProgress?.call(1.0);
        return '$publicCdnBaseUrl/$folder/$fileName';
      } else {
        return null;
      }
    } catch (e) {
      return null;
    }
  }

  /// Deletes a file from Bunny Edge Storage using HTTP DELETE request.
  static Future<bool> deleteFile(String pathOrCdnUrl) async {
    try {
      String relativePath = pathOrCdnUrl;
      if (pathOrCdnUrl.startsWith(publicCdnBaseUrl)) {
        relativePath = pathOrCdnUrl.replaceFirst(publicCdnBaseUrl, '').replaceFirst('/', '');
      }

      final deleteUrl = Uri.parse('$storageUploadEndpoint/$storageZoneName/$relativePath');
      final response = await http.delete(deleteUrl, headers: {
        'AccessKey': accessKey,
      });

      return response.statusCode == 200;
    } catch (_) {
      return false;
    }
  }
}

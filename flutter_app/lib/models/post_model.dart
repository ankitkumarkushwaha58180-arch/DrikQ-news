enum MediaType { video, photo }

class PostModel {
  final String id;
  final String title;
  final String description;
  final String place;
  final MediaType mediaType;
  final String mediaUrl;
  final String thumbnailUrl;
  final String reporterId;
  final String reporterName;
  final String reporterPhotoUrl;
  final String status; // 'pending' or 'approved'
  int likesCount;
  int viewsCount;
  final int timestamp;
  List<String> likedByUsers;

  PostModel({
    required this.id,
    required this.title,
    required this.description,
    required this.place,
    required this.mediaType,
    required this.mediaUrl,
    required this.thumbnailUrl,
    required this.reporterId,
    required this.reporterName,
    required this.reporterPhotoUrl,
    this.status = 'pending',
    this.likesCount = 0,
    this.viewsCount = 0,
    required this.timestamp,
    this.likedByUsers = const [],
  });

  factory PostModel.fromMap(String id, Map<dynamic, dynamic> map) {
    return PostModel(
      id: id,
      title: map['title'] ?? '',
      description: map['description'] ?? '',
      place: map['place'] ?? '',
      mediaType: (map['mediaType'] == 'VIDEO' || map['mediaType'] == 'video')
          ? MediaType.video
          : MediaType.photo,
      mediaUrl: map['mediaUrl'] ?? '',
      thumbnailUrl: map['thumbnailUrl'] ?? (map['mediaUrl'] ?? ''),
      reporterId: map['reporterId'] ?? '',
      reporterName: map['reporterName'] ?? '',
      reporterPhotoUrl: map['reporterPhotoUrl'] ?? '',
      status: (map['status'] ?? 'pending').toString().toLowerCase(),
      likesCount: map['likesCount'] ?? 0,
      viewsCount: map['viewsCount'] ?? 0,
      timestamp: map['timestamp'] ?? DateTime.now().millisecondsSinceEpoch,
      likedByUsers: map['likedByUsers'] != null
          ? List<String>.from(map['likedByUsers'])
          : [],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'title': title,
      'description': description,
      'place': place,
      'mediaType': mediaType == MediaType.video ? 'VIDEO' : 'PHOTO',
      'mediaUrl': mediaUrl,
      'thumbnailUrl': thumbnailUrl,
      'reporterId': reporterId,
      'reporterName': reporterName,
      'reporterPhotoUrl': reporterPhotoUrl,
      'status': status,
      'likesCount': likesCount,
      'viewsCount': viewsCount,
      'timestamp': timestamp,
      'likedByUsers': likedByUsers,
    };
  }
}

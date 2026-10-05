class ReporterModel {
  final String id;
  final String name;
  final String mobile;
  final String address;
  final String photoUrl;
  final String password;
  int followersCount;
  int followingCount;
  List<String> followedByUsers;

  ReporterModel({
    required this.id,
    required this.name,
    required this.mobile,
    required this.address,
    required this.photoUrl,
    required this.password,
    this.followersCount = 0,
    this.followingCount = 0,
    this.followedByUsers = const [],
  });

  factory ReporterModel.fromMap(String id, Map<dynamic, dynamic> map) {
    return ReporterModel(
      id: id,
      name: map['name'] ?? '',
      mobile: map['mobile'] ?? '',
      address: map['address'] ?? '',
      photoUrl: map['photoUrl'] ?? '',
      password: map['password'] ?? '',
      followersCount: map['followersCount'] ?? 0,
      followingCount: map['followingCount'] ?? 0,
      followedByUsers: map['followedByUsers'] != null
          ? List<String>.from(map['followedByUsers'])
          : [],
    );
  }

  Map<String, dynamic> toMap() {
    return {
      'id': id,
      'name': name,
      'mobile': mobile,
      'address': address,
      'photoUrl': photoUrl,
      'password': password,
      'followersCount': followersCount,
      'followingCount': followingCount,
      'followedByUsers': followedByUsers,
    };
  }
}

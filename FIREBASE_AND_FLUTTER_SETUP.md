# Drikq News — Firebase & Flutter Architecture Guide

This document contains the exact Firebase Realtime Database structure, Firebase Security Rules, Flutter dependencies, and setup instructions for **Drikq News**.

---

## 1. Firebase Realtime Database Structure (JSON Tree)

```json
{
  "settings": {
    "policy": {
      "privacyPolicy": "Drikq News is committed to protecting your privacy. We collect minimal device information and authentication tokens solely to provide real-time news delivery and reporter attribution. Content uploaded by verified reporters undergoes editorial approval before publishing to our public feed. We do not sell or share personal user data with third-party advertisers.",
      "termsAndConditions": "By using Drikq News, you agree to access verified news content responsibly. Reporters must verify facts before submission. Content violating local laws, promoting hate speech, or containing unauthorized private media will be promptly removed and credentials revoked."
    }
  },
  "reporters": {
    "REP-1042": {
      "id": "REP-1042",
      "name": "Ravi Sharma",
      "mobile": "+91 98765 43210",
      "address": "Central Bureau, New Delhi",
      "photoUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
      "password": "RepPass#1042",
      "followersCount": 1420,
      "followingCount": 18,
      "createdAt": 1728000000000
    },
    "REP-2098": {
      "id": "REP-2098",
      "name": "Priya Sen",
      "mobile": "+91 91234 56789",
      "address": "East Zone Desk, Kolkata",
      "photoUrl": "https://images.unsplash.com/photo-1573496359142-b8d87734a5a2?auto=format&fit=crop&w=400&q=80",
      "password": "RepPass#2098",
      "followersCount": 2890,
      "followingCount": 35,
      "createdAt": 1728000000000
    }
  },
  "posts": {
    "post-101": {
      "id": "post-101",
      "title": "New High-Speed Metro Corridor Inaugurated in City Center",
      "description": "The state-of-the-art elevated metro corridor spans 18 kilometers, reducing peak-hour commute times by over 45 minutes.",
      "place": "Downtown Metro Junction",
      "mediaType": "VIDEO",
      "mediaUrl": "https://firebasestorage.googleapis.com/v0/b/project-825493226391.appspot.com/o/videos%2Fpost-101.mp4?alt=media",
      "thumbnailUrl": "https://images.unsplash.com/photo-1517649763962-0c623266ddc0?auto=format&fit=crop&w=1080&q=80",
      "reporterId": "REP-1042",
      "reporterName": "Ravi Sharma",
      "reporterPhotoUrl": "https://images.unsplash.com/photo-1534528741775-53994a69daeb?auto=format&fit=crop&w=400&q=80",
      "status": "APPROVED",
      "likesCount": 342,
      "viewsCount": 4120,
      "timestamp": 1728000000000
    },
    "post-104": {
      "id": "post-104",
      "title": "Smart Agriculture Drone Expo Highlights Green Innovations",
      "description": "Farmers tested AI-driven spray drones and moisture sensors aimed at reducing water waste by 30% across rural districts.",
      "place": "Agri-Tech Pavilion",
      "mediaType": "PHOTO",
      "mediaUrl": "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=1080&q=80",
      "thumbnailUrl": "https://images.unsplash.com/photo-1500937386664-56d1dfef3854?auto=format&fit=crop&w=1080&q=80",
      "reporterId": "REP-3401",
      "reporterName": "Arjun Mehta",
      "reporterPhotoUrl": "https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?auto=format&fit=crop&w=400&q=80",
      "status": "PENDING",
      "likesCount": 0,
      "viewsCount": 14,
      "timestamp": 1728000000000
    }
  },
  "notifications": {
    "notif-1": {
      "id": "notif-1",
      "title": "Breaking: High-Speed Metro Corridor Open",
      "body": "Commuter trains are officially rolling! Tap to view ground footage.",
      "postId": "post-101",
      "timestamp": 1728000000000
    }
  },
  "users": {
    "usr_sample_uid": {
      "uid": "usr_sample_uid",
      "name": "Reader Name",
      "email": "reader@example.com",
      "photoUrl": "https://images.unsplash.com/photo-1535713875002-d1d0cf377fde?auto=format&fit=crop&w=200&q=80",
      "followingReporters": {
        "REP-1042": true
      },
      "likedPosts": {
        "post-101": true
      }
    }
  }
}
```

---

## 2. Firebase Security Rules (`database.rules.json`)

```json
{
  "rules": {
    ".read": false,
    ".write": false,

    "settings": {
      ".read": true,
      ".write": "auth != null && auth.token.admin == true"
    },

    "posts": {
      ".read": true,
      ".indexOn": ["status", "reporterId", "timestamp"],
      "$postId": {
        // Any user can read approved posts; only authenticated or reporters can see pending
        ".read": "data.child('status').val() === 'APPROVED' || auth != null",
        // Reporter can create post with initial status 'PENDING'; Admin can approve/edit/delete
        ".write": "auth != null",
        "likesCount": {
          ".write": "auth != null"
        },
        "viewsCount": {
          ".write": true
        }
      }
    },

    "reporters": {
      ".read": true,
      "$reporterId": {
        ".read": true,
        ".write": "auth != null"
      }
    },

    "notifications": {
      ".read": true,
      ".write": "auth != null && auth.token.admin == true"
    },

    "users": {
      "$uid": {
        ".read": "auth != null && auth.uid === $uid",
        ".write": "auth != null && auth.uid === $uid"
      }
    }
  }
}
```

### Storage Security Rules (`storage.rules`)
```javascript
rules_version = '2';
service firebase.storage {
  match /b/{bucket}/o {
    match /posts/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null
                   && request.resource.size < 50 * 1024 * 1024; // 50MB max
    }
    match /reporters/{allPaths=**} {
      allow read: if true;
      allow write: if request.auth != null;
    }
  }
}
```

---

## 3. Flutter `pubspec.yaml` Dependencies

If building or compiling the Flutter client codebase, add the following packages:

```yaml
name: drikq_news
description: "Drikq News - Ground Reporter Feed, Upload & Admin Platform"
publish_to: 'none'
version: 1.0.0+1

environment:
  sdk: '>=3.2.0 <4.0.0'

dependencies:
  flutter:
    sdk: flutter

  # Firebase Core & Services
  firebase_core: ^3.6.0
  firebase_auth: ^5.3.1
  firebase_database: ^11.1.4
  firebase_storage: ^12.3.4
  firebase_messaging: ^15.1.3
  google_sign_in: ^6.2.1

  # State Management & Architecture
  flutter_riverpod: ^2.6.1

  # Media & Video Player
  video_player: ^2.9.2
  chewie: ^1.8.5
  image_picker: ^1.1.2
  cached_network_image: ^3.4.1

  # UI & Utilities
  flutter_staggered_grid_view: ^0.7.0
  intl: ^0.19.0
  share_plus: ^10.1.1
  flutter_svg: ^2.0.14
  shimmer: ^3.0.0
  uuid: ^4.5.1

dev_dependencies:
  flutter_test:
    sdk: flutter
  flutter_lints: ^4.0.0
```

---

## 4. Setup Instructions

1. **Firebase Console**:
   - Create project `project-825493226391`.
   - Enable **Authentication** -> **Google Sign-In** provider.
   - Enable **Realtime Database** (location `asia-southeast1`) -> URL: `https://drikq-f9a39-default-rtdb.asia-southeast1.firebasedatabase.app/`.
   - Enable **Cloud Storage** for media uploads.
2. **Android App**:
   - Download `google-services.json` from Firebase console and place it into `app/google-services.json`.
3. **Admin Credential**:
   - Default login code: `admin` or `admin123`.
4. **Reporter Credentials**:
   - Pre-seeded reporters:
     - `REP-1042` / `RepPass#1042`
     - `REP-2098` / `RepPass#2098`
     - `REP-3401` / `RepPass#3401`
   - Additional reporters can be generated directly via **Admin Panel -> Reporters -> Add New Reporter**.

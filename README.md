<div align="center">

# ಶಾಲೆ-ವಿಕಾಸ — Shaale-Vikas

### 🏫 School-Alumni Bridge App

[![Android](https://img.shields.io/badge/Platform-Android-green?logo=android)](https://android.com)
[![Kotlin](https://img.shields.io/badge/Language-Kotlin-purple?logo=kotlin)](https://kotlinlang.org)
[![Firebase](https://img.shields.io/badge/Backend-Firebase-orange?logo=firebase)](https://firebase.google.com)
[![Gemini AI](https://img.shields.io/badge/AI-Gemini-blue?logo=google)](https://ai.google.dev)
[![License](https://img.shields.io/badge/License-MIT-red)](LICENSE)
[![MindMatrix](https://img.shields.io/badge/MindMatrix-VTU%20Internship-brightgreen)](https://mindmatrix.io)

**Empowering rural school alumni to support their alma mater
through transparent, accountable, and measurable action.**

[⬇️ Download APK](#-download) • 
[📱 Features](#-features) • 
[🛠 Tech Stack](#-tech-stack) •  
[🚀 Setup](#-setup-guide)

</div>

---

## 📖 About

**Shaale-Vikas** (Kannada: *School Development*) is a mobile-first 
Android application that bridges rural government schools with their 
alumni networks.

> Rural schools face chronic infrastructure deficits — leaking roofs,
> broken furniture, absent sanitation. Government funding arrives months
> late. Alumni want to help but don't know what's needed.

**Shaale-Vikas** solves this by creating a transparent platform where:
- 👑 **Headmasters** publish specific school needs with photos
- 🎓 **Alumni** browse needs, pledge support, and track impact
- 📷 **Before/After photos** prove every contribution made a difference

---

## ⬇️ Download

<div align="center">

[![Download APK](https://img.shields.io/badge/⬇️%20Download%20APK-Shaale--Vikas%20v1.0.0-2E7D32?style=for-the-badge)](https://firebasestorage.googleapis.com/v0/b/micro-dynamo-490606-e7.firebasestorage.app/o/app-debug.apk?alt=media&token=965266b7-1bde-4712-a1e8-4ea58f4ccc76)

[![Live Demo](https://img.shields.io/badge/🌐%20Live%20Site-shaale--vikas--15.web.app-1565C0?style=for-the-badge)](https://firebasestorage.googleapis.com/v0/b/micro-dynamo-490606-e7.firebasestorage.app/o/app-debug.apk?alt=media&token=965266b7-1bde-4712-a1e8-4ea58f4ccc76)

> ⚠️ Enable **Install from Unknown Sources** in Android Settings
> before installing the APK

</div>

---

## ✨ Features

### 👑 Admin (Headmaster) Features
| Feature | Description |
|---------|-------------|
| 📋 Publish Needs | Add needs with title, category, description, cost estimate |
| 🤖 AI Assistant | Gemini AI suggests descriptions and cost estimates |
| 📷 Photo Upload | Upload Before photo at creation, After photo on completion |
| ✅ Mark Fulfilled | Mark needs as fulfilled with after photo proof |
| 🗑️ Delete Needs | Remove unpledged needs from dashboard |
| 📊 Track Pledges | View real-time funding progress per need |

### 🎓 Alumni Features
| Feature | Description |
|---------|-------------|
| 📱 Needs Dashboard | Browse all active needs with search and filters |
| 🤝 Pledge Support | Pledge funds or donate items for any need |
| 📈 Progress Tracking | Real-time funding progress bars |
| 🏆 Hall of Fame | Gamified leaderboard with rank tiers |
| 🖼️ Impact Gallery | Before/After photo pairs of completed work |
| 👤 Profile | Photo upload, pledge history, settings |
| 📤 Share | Share Hall of Fame card to WhatsApp/Instagram |

### 🤖 GenAI Features
| Feature | Description |
|---------|-------------|
| ✨ Title Suggestion | AI generates clear need titles from partial input |
| 📝 Description Generation | AI writes descriptions based on title + category + urgency |
| 💰 Cost Estimation | AI suggests realistic cost estimates |
| 🔄 Fallback Mode | Template-based suggestions when AI unavailable |

---

## 🏆 Rank System

Alumni earn **Pledge Points (PP)** based on contributions.
**1 PP = ₹100**

| Rank | PP Range | Emoji | Color |
|------|----------|-------|-------|
| Unranked | 0 – 399 | ⬜ | Grey |
| Bronze III / II / I | 400 – 699 | 🥉 | Bronze |
| Silver III / II / I | 700 – 999 | 🥈 | Silver |
| Gold IV / III / II / I | 1,000 – 1,799 | 🥇 | Gold |
| Platinum IV / III / II / I | 1,800 – 2,999 | 💠 | Cyan |
| Diamond V / IV / III / II / I | 3,000 – 4,999 | 💎 | Blue |
| Ruby | 5,000+ | ❤️‍🔥 | Red |

---

## 🛠 Tech Stack

```
┌─────────────────────────────────────────────────────┐
│                   SHAALE-VIKAS                      │
├─────────────────┬───────────────────────────────────┤
│  Layer          │  Technology                       │
├─────────────────┼───────────────────────────────────┤
│  Language       │  Kotlin                           │
│  Architecture   │  MVVM + Repository Pattern        │
│  UI             │  Material Design 3, XML Layouts   │
│  Navigation     │  Jetpack Navigation Component     │
│  DI             │  Hilt (Dagger)                    │
│  Async          │  Coroutines + StateFlow            │
│  LiveData       │  SingleLiveEvent + RepeatOnLC     │
├─────────────────┼───────────────────────────────────┤
│  Auth           │  Firebase Authentication          │
│  Database       │  Firebase Firestore (Real-time)   │
│  Storage        │  Firebase Cloud Storage           │
│  Notifications  │  Firebase Cloud Messaging (FCM)   │
│  Hosting        │  Firebase Hosting                 │
├─────────────────┼───────────────────────────────────┤
│  AI             │  Gemini API (gemini-1.5-flash)    │
│  Images         │  Glide with CircleCrop            │
│  Min SDK        │  Android 8.0 (API 26)             │
└─────────────────┴───────────────────────────────────┘
```

---

## 🚀 Setup Guide

### Prerequisites
```
✅ Android Studio (Hedgehog or later)
✅ JDK 17
✅ Android device or emulator (API 26+)
✅ Firebase account
✅ Google AI Studio account (for Gemini API)
```

### Step 1: Clone Repository
```bash
git clone https://github.com/giteverstore/Shaale-Vikas.git
cd ShaaleVikas
```

### Step 2: Firebase Setup
```
1. Go to console.firebase.google.com
2. Create project: ShaaleVikas
3. Add Android app: com.shaalevikas.app
4. Download google-services.json
5. Place in: app/google-services.json
6. Enable Authentication (Email/Password)
7. Create Firestore Database (asia-south1)
8. Enable Storage (asia-south1)
```

### Step 3: Add Gemini API Key
```kotlin
// In CreateNeedViewModel.kt
private val generativeModel = GenerativeModel(
    modelName = "gemini-1.5-flash",
    apiKey    = "YOUR_GEMINI_API_KEY_HERE"
)
```

Get API key from: [aistudio.google.com](https://aistudio.google.com)

### Step 4: Create Admin User

**In Firebase Authentication:**
```
Authentication → Users → Add User
Email: admin@shaalevikas.com
Password: Admin@123
Copy the generated UID
```

**In Firestore:**
```
users/{paste-uid-here} {
    uid: "paste-uid-here"
    displayName: "Headmaster"
    email: "admin@shaalevikas.com"
    role: "admin"
    graduationYear: 0
    city: "School City"
    district: "School District"
    totalPledgeValue: 0
    badgeTier: "Gold"
    profileImageUrl: ""
    emailVerified: true
}
```

### Step 5: Create Firestore Indexes
```
Firestore → Indexes → Create Index

Index 1: needs (status ASC, createdAt DESC)
Index 2: needs (status ASC, fulfilledAt DESC)
Index 3: pledges (alumniId ASC, timestamp DESC)
Index 4: pledges (needId ASC, timestamp DESC)
```

### Step 6: Build and Run
```bash
# Sync gradle
File → Sync Project with Gradle Files

# Build
Build → Make Project

# Run
Run → Run 'app'
```

---

## 📱 App Flow

```
┌─────────────────────────────────────────────┐
│                 LOGIN SCREEN                │
│          👑 Admin  |  🎓 Alumni             │
└──────────┬──────────────────┬───────────────┘
           │                  │
    ┌──────▼──────┐    ┌──────▼──────┐
    │    ADMIN    │    │   ALUMNI    │
    │   FLOW      │    │    FLOW     │
    └──────┬──────┘    └──────┬──────┘
           │                  │
    ┌──────▼──────┐    ┌──────▼──────┐
    │  Dashboard  │    │  Register   │
    │  + FAB (+)  │    │  (4 steps)  │
    └──────┬──────┘    └──────┬──────┘
           │                  │
    ┌──────▼──────┐    ┌──────▼──────┐
    │Create Need  │    │  Dashboard  │
    │+ AI Suggest │    │  Browse     │
    └──────┬──────┘    └──────┬──────┘
           │                  │
    ┌──────▼──────┐    ┌──────▼──────┐
    │Mark Fulfill │    │   Pledge    │
    │After Photo  │    │Funds/Items  │
    └──────┬──────┘    └──────┬──────┘
           │                  │
    ┌──────▼──────┐    ┌──────▼──────┐
    │   Gallery   │    │Hall of Fame │
    │Before/After │    │Rank System  │
    └─────────────┘    └─────────────┘
```

---

## 🎯 Impact Goals

```
🏘️  Community Ownership
    Citizens actively maintain local schools

📚  Education Quality
    Infrastructure gaps resolved faster

🔍  Transparency & Trust
    Photo-evidenced proof of every repair

🎓  Alumni Re-engagement
    Pride and belonging through giving back

💻  Digital Literacy
    Headmasters adopt digital school management
```

---

## 👨‍💻 Developer

<div align="center">

**Avinash V Abbigeri**

[![GitHub](https://img.shields.io/badge/GitHub-giteverstore-black?logo=github)](https://github.com/giteverstore)

*MindMatrix VTU Internship Program*
*Android App Development using GenAI*
*Education and Infrastructure Track*

</div>

---

## 📄 License

```
MIT License

Copyright (c) 2026 giteverstore

Permission is hereby granted, free of charge,
to any person obtaining a copy of this software
to deal in the Software without restriction.
```

---

<div align="center">


⭐ Star this repo if you found it helpful!

</div>

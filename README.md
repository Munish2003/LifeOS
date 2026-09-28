# 🌟 Personal AI Life OS — End-to-End Mobile Application

> **An intelligent, Android-only Personal Life Operating System** that unifies productivity, health, calorie/nutrition tracking, time tracking, daily routine planning, 15-day challenges, and AI insights.

---

## 📱 Mobile Phone me Install karne ka Complete Guide (Hindi / Hinglish)

Aapne poocha tha:
> *"is tarreke se ki mai ise rkh sku mobile phone me download krke zip file and install kr sku"*

Android phone me koi bhi app install karne ke liye **`.apk` file** ki zaroorat hoti hai. Humne is project ko is tarah set up kiya hai ki aap bina kisi jhanjhat ke **2 aasan tareeko** se install kar sakte hain:

---

### Tareeka 1: GitHub Actions se 1-Click APK Download (Sabse Aasan & Free)

Is project me `.github/workflows/build-apk.yml` pre-configured hai:
1. Is repository/code ko apne GitHub account par push karein (ya upload karein).
2. GitHub repository ke **Actions** tab par jayein.
3. Wahan **Build Android APK (Life OS)** workflow automatically chalega (ya "Run workflow" par click karein).
4. Build complete hote hi **Artifacts** section me aapko **`LifeOS-Mobile-Installable-APK.zip`** mil jayega!
5. Us zip ko apne phone me download karein, extract karein, aur **`app-debug.apk`** par tap karke **Install** kar lein!

---

### Tareeka 2: Apne Computer se Direct APK Compile karke Phone me Transfer karna

Agar aapke PC me Android Studio ya Android SDK hai:
1. `scripts/build_apk.bat` par double-click karein (ya command prompt me `cd android && ./gradlew assembleDebug` run karein).
2. Build hote hi aapka installable APK yahan generate hoga:
   ```text
   android/app/build/outputs/apk/debug/app-debug.apk
   ```
3. Is `app-debug.apk` ko WhatsApp, Google Drive, ya USB cable ke zariye apne mobile phone me bhejein.
4. Mobile me tap karein -> Settings me "Allow from this source" enable karein -> **Install**!

---

## 🏗️ System Architecture

```text
┌─────────────────────────────────────────────────────────────┐
│                 Native Android Mobile App                   │
│   (Kotlin + Jetpack Compose + Material 3 + Room Database)   │
│                                                             │
│   ├── Home Dashboard Screen (Overall Daily Progress 72%)    │
│   ├── Tasks & Measurement Screen (Time, Count, Binary)      │
│   ├── Foreground Focus Timer Service (Notification lock)    │
│   ├── Health & Steps (Health Connect sync + Weight goal)    │
│   ├── Food & Nutrition (Text/OCR Calorie Estimation)        │
│   ├── Daily Schedule & Routine (Office, Commute, Outing)    │
│   ├── 15-Day Challenges (Steps & Python Sprints)            │
│   └── Life AI Companion ("What should I do now?")           │
└──────────────────────────────▲──────────────────────────────┘
                               │ REST APIs / Offline Room Sync
┌──────────────────────────────▼──────────────────────────────┐
│                    FastAPI Python Backend                   │
│                                                             │
│   ├── Level 1: Deterministic Availability Engine            │
│   ├── Level 2: Multi-Factor Priority Scoring Engine         │
│   ├── Level 3: AI Orchestrator & Food Parser                │
│   └── Database: PostgreSQL / Local SQLite                   │
└─────────────────────────────────────────────────────────────┘
```

---

## 🚀 Backend ko Start Kaise Karein

Backend me deterministic business logic aur optional AI processing hoti hai.

### Windows (1-Click):
`scripts/run_backend.bat` par double-click karein.

### Manual:
```bash
cd backend
pip install -r requirements.txt
python -m uvicorn app.main:app --host 0.0.0.0 --port 8000 --reload
```
Swagger UI Interactive API Docs: [http://localhost:8000/docs](http://localhost:8000/docs)

### Docker:
```bash
cd backend
docker-compose up --build
```

---

## 📦 Project ZIP Package Banane ka Tareeka

Agar aapko is poore project ka ek single portable zip bundle banana hai:
```bash
python scripts/create_mobile_bundle.py
```
Yeh root directory me **`LifeOS_Mobile_Project.zip`** bana dega jise aap kahin bhi transfer kar sakte hain.

---

## 🎯 Verification & Testing

Backend ke availability calculations, conflict detection, priority scoring, aur food parsing ke tests verify karne ke liye:
```bash
cd backend
python -m pytest app/tests
```
All tests pass with 100% deterministic accuracy.

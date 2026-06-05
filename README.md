# 🥛 Milk Diary — Android App

Fully offline Android app for recording daily milk sales and calculating earnings automatically.

---

## ✅ Ready-to-Install APK

**`MilkDiary.apk`** is in the project root — ready to install directly on your Android phone.

### Install on your phone

1. Copy `MilkDiary.apk` to your phone (USB, WhatsApp, Google Drive, etc.)
2. On your phone: **Settings → Security → Unknown Sources → Enable**
   (or on Android 8+: Settings → Apps → Special App Access → Install Unknown Apps)
3. Open the APK file → **Install**
4. Open **Milk Diary** from your home screen

---

## Build from Source

### Requirements
- Android Studio (Hedgehog / Electric Eel or newer)
- Android SDK with API 34 platform

### Steps
1. Open Android Studio → File → Open → select this folder
2. `local.properties` already has the SDK path set
3. Click **Sync Now** when prompted
4. **Build → Build Bundle(s)/APK(s) → Build APK(s)**
5. APK output: `app/build/outputs/apk/debug/app-debug.apk`

Or from command line:
```
.\gradlew.bat assembleDebug
```

---

## App Features

| Screen | What it does |
|--------|-------------|
| **Dashboard** | Today's milk (cow + buffalo), earnings, monthly totals, pending payment at a glance |
| **Add Daily Record** | Date picker, liters + rate fields, live calculation, auto-fills saved rates |
| **History** | All records newest first, search by date, edit & delete |
| **Monthly Summary** | Navigate months with ← →, totals, average daily earnings |
| **Payments** | Record payments, running pending balance |
| **Settings** | Dark mode, default rates, backup DB, restore, export CSV |

## Technical
- **Min Android**: 5.0 (API 21) — works on almost all phones
- **Target Android**: 14 (API 34)
- **Database**: SQLite, stored at `data/data/com.milkdiary.app/databases/milk_diary.db`
- **Fully offline** — no internet permission
- **APK size**: ~7 MB
- **Currency**: NPR (Rs.)

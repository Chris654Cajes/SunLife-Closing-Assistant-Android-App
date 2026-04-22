# SunLife Closing Assistant — Android App

> A Taglish sales flow assistant for insurance/financial advisors in the Philippines.  
> Built with Kotlin, MVVM, XML Layouts, and Firebase.

![License](https://img.shields.io/badge/license-MIT-blue.svg)
![Platform](https://img.shields.io/badge/platform-Android-green.svg)
![Language](https://img.shields.io/badge/language-Kotlin-orange.svg)
![Architecture](https://img.shields.io/badge/architecture-MVVM-purple.svg)

---

## 📁 Project Structure

```
app/src/main/
├── java/com/closingassistant/
│   ├── data/
│   │   ├── model/
│   │   │   ├── ClientProfile.kt       — Firestore data model
│   │   │   └── SalesScript.kt         — SalesStep, Recommendation, Trigger models
│   │   └── repository/
│   │       ├── AuthRepository.kt      — Firebase Auth (login/register/logout)
│   │       ├── ClientRepository.kt    — Firestore CRUD for client profiles
│   │       └── ScriptRepository.kt    — Taglish script & recommendation engine
│   └── ui/
│       ├── activities/
│       │   ├── SplashActivity.kt
│       │   ├── LoginActivity.kt
│       │   ├── ClientProfileActivity.kt
│       │   ├── SalesFlowActivity.kt
│       │   └── RecommendationActivity.kt
│       └── viewmodels/
│           ├── LoginViewModel.kt
│           ├── ClientProfileViewModel.kt
│           ├── SalesFlowViewModel.kt
│           └── RecommendationViewModel.kt
└── res/
    ├── layout/
    │   ├── activity_splash.xml
    │   ├── activity_login.xml
    │   ├── activity_client_profile.xml
    │   ├── activity_sales_flow.xml
    │   └── activity_recommendation.xml
    ├── values/
    │   ├── colors.xml
    │   ├── themes.xml
    │   ├── strings.xml
    │   ├── dimens.xml
    │   └── ids.xml
    └── drawable/
        ├── bg_primary_gradient.xml
        ├── bg_step_indicator.xml
        ├── bg_script_box.xml
        ├── bg_chip_rounded.xml
        ├── bg_trigger_family.xml
        ├── bg_trigger_security.xml
        └── bg_trigger_future.xml
```

---

## 🚀 Setup Instructions

### 1. Firebase Setup

1. Go to [Firebase Console](https://console.firebase.google.com)
2. Click **Add Project** → Name it `ClosingAssistant`
3. Click **Add app** → Choose Android
4. Enter package name: `com.closingassistant`
5. Download `google-services.json`
6. Replace `app/google-services.json` with your downloaded file

### 2. Enable Firebase Services

In Firebase Console:

**Authentication:**
- Go to **Authentication → Sign-in method**
- Enable **Email/Password**

**Firestore:**
- Go to **Firestore Database → Create database**
- Start in **test mode** for development
- Region: `asia-southeast1` (Singapore, closest to PH)

### 3. Firestore Security Rules (Production)

```
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /agents/{agentId}/clients/{clientId} {
      allow read, write: if request.auth != null && request.auth.uid == agentId;
    }
  }
}
```

### 4. Build & Run

```bash
# Open in Android Studio
# Sync Gradle
# Run on emulator or device (API 24+)
```

---

## 🧠 App Flow

```
SplashActivity
     │
     ├─ (not logged in) → LoginActivity → (login/register)
     │
     └─ (logged in) → ClientProfileActivity
                              │
                              └─ Next → SalesFlowActivity (6-step Taglish script)
                                              │
                                              └─ View Recommendation → RecommendationActivity
                                                                              │
                                                                              └─ Save to Firestore
```

---

## 💡 Script Generation Logic

The `ScriptRepository` generates personalized Taglish scripts based on:

| Client Data         | Script Adaptation                                      |
|---------------------|--------------------------------------------------------|
| Age ≤ 30            | VUL (Variable Unit-Linked) plan recommended            |
| Age 31–45 + deps    | Whole Life + Critical Illness Rider                    |
| Age 31–45, no deps  | VUL Growth-Focused                                     |
| Age > 45            | Endowment / Retirement Plan                            |
| Dependents > 0      | Family protection emotional triggers activated         |
| Concerns field      | Objection-handling script uses client's own words      |
| Monthly Income      | Premium estimate = 8–14% of income + ₱500/dependent   |
| Coverage Amount     | 8–12× annual income based on dependents                |

---

## 🎨 UI Design Tokens

| Token         | Value     | Usage                        |
|---------------|-----------|------------------------------|
| Primary       | `#1A237E` | Header, buttons, badges      |
| Accent        | `#FFD600` | Progress bar, highlights     |
| Background    | `#F5F6FA` | Screen background            |
| Surface       | `#FFFFFF` | Cards                        |
| Text Primary  | `#1A1A2E` | Body text                    |
| Text Secondary| `#5C6BC0` | Labels, hints                |
| Success       | `#00C853` | Save confirmation            |
| Error         | `#D50000` | Validation errors            |

---

## 📦 Key Dependencies

| Library                         | Version   | Purpose                     |
|---------------------------------|-----------|-----------------------------|
| Firebase Auth KTX               | BOM 32.7  | Authentication               |
| Firebase Firestore KTX          | BOM 32.7  | Cloud database               |
| Material Components             | 1.11.0    | UI components                |
| Lifecycle ViewModel KTX         | 2.7.0     | MVVM architecture            |
| Coroutines Android              | 1.7.3     | Async operations             |
| ConstraintLayout                | 2.1.4     | Complex XML layouts          |
| CardView                        | 1.0.0     | Card containers              |

---

## ✅ Architecture Compliance

- ✅ All UI in XML layout files (zero Compose)
- ✅ MVVM — Activities → ViewModels → Repositories
- ✅ LiveData for reactive UI state
- ✅ Firebase Auth + Firestore integrated
- ✅ Coroutines for all async work
- ✅ ViewBinding enabled (no `findViewById`)
- ✅ Kotlin throughout (K2-compatible syntax)
- ✅ Separate files per class

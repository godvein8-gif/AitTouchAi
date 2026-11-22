# NEXUS AI - Folder Structure and Version Plan

## 1. Complete folder structure

```
nexus-ai/
├── .github/workflows/android-build.yml        (Batch 5)
├── .gitignore
├── settings.gradle.kts
├── build.gradle.kts
├── gradle.properties
├── gradle/
│   ├── libs.versions.toml
│   └── wrapper/gradle-wrapper.properties
├── app/
│   ├── build.gradle.kts
│   ├── proguard-rules.pro
│   └── src/
│       ├── main/
│       │   ├── AndroidManifest.xml
│       │   ├── res/ (values, drawable, mipmap)
│       │   └── java/com/nexusai/assistant/
│       │       ├── MainActivity.kt
│       │       ├── NexusApplication.kt
│       │       ├── navigation/AppNavigation.kt
│       │       ├── ui/screens/   (Dashboard, Login, Signup, OtpVerification,
│       │       │                  Chat, Voice, IncomingCall, CallHistory,
│       │       │                  Profile, Settings, Privacy)
│       │       ├── ui/components/ (AiOrb, MessageBubble, StatusIndicator, CallStatusCard)
│       │       ├── ui/theme/     (Color, Theme, Type)
│       │       ├── data/local|remote|repository/
│       │       ├── domain/model|repository|usecase/
│       │       └── service/auth|voice/
│       └── test/java/com/nexusai/assistant/   (unit tests)
├── backend/
│   ├── app/
│   │   ├── main.py, config.py
│   │   ├── routes/ (health, auth, chat, calls)
│   │   ├── services/ (ai, auth, conversation, call)
│   │   ├── websocket/conversation_relay.py
│   │   └── security/ (token_validation, signature_validation)
│   ├── requirements.txt
│   ├── .env.example
│   └── README.md
└── README.md
```

## 2. Dependency and version plan

| Component | Version | Note |
|---|---|---|
| JDK | 17 | Required by AGP 8.x |
| Gradle | 8.9 | Supported by AGP 8.7 |
| Android Gradle Plugin | 8.7.3 | Needs Gradle 8.9+ |
| Kotlin | 2.0.21 | Uses the Compose compiler Gradle plugin |
| KSP | 2.0.21-1.0.28 | Must match Kotlin version exactly |
| Compose BOM | 2024.12.01 | Material 3 |
| Navigation Compose | 2.8.5 | |
| Lifecycle | 2.8.7 | |
| Activity Compose | 1.9.3 | |
| Room | 2.6.1 | Via KSP |
| DataStore | 1.1.1 | |
| Coroutines | 1.9.0 | |
| compileSdk / targetSdk | 35 | minSdk 28 |

## 3. Honest status

- Version numbers were chosen from known-compatible combinations, but I could not
  run a build in this environment (no network), so **nothing is build-verified yet**.
- The first GitHub Actions run is the real test. If it fails, send me the error log
  and I will fix it.
- `gradle-wrapper.jar` is a binary file that I cannot create here. The workflow in
  Batch 5 generates the wrapper automatically, so you do not need to add it by hand.

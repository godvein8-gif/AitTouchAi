# NEXUS AI (Phase 1: DEMO foundation)

Android app (Kotlin, Jetpack Compose) plus a FastAPI backend.

**Phase 1 is a demo.** Login, AI replies, voice and incoming calls are all simulated and
labelled DEMO. No SMS is sent, no real AI is called, no real phone call is answered.

## What works in Phase 1
- Demo mobile-number signup/login (India +91 and others), OTP screen with resend cooldown
  and lockout. Demo code is shown on screen: `123456`.
- Dashboard with 9 modules, animated AI orb, dark cyan/blue theme.
- Chat with mock AI (Hindi/English), history saved on the phone, retry (type `/fail` to test).
- Voice screen UI (no real speech yet), microphone permission flow.
- Call receptionist screen, DEMO call simulator, call history, callback messages.
- Settings, privacy (recording always OFF, delete data), profile (logout, delete account).
- FastAPI backend with mock AI, rate limiting, signature validation, tests.

## What is NOT done yet
- Real OTP (Phase 2), real AI (Phase 2), real voice (Phase 3), real calls (Phase 4).
- Normal SIM calls cannot be intercepted by Android. A cloud number plus carrier call
  forwarding is needed, and NEXUS never turns forwarding on by itself.

## Build the APK with GitHub (no Android Studio)
1. Make a free GitHub account and create a new **private** repository (for example `nexus-ai`).
2. Put the project files in it (see "Upload from your phone" below).
3. Open the repo > **Actions** tab. The build starts by itself on every push to `main`.
   You can also tap **Android Build** > **Run workflow**.
4. Wait about 5 to 10 minutes. A green tick means success. A red cross means the build failed:
   open the run, copy the error text and send it to Claude to fix.
5. Open the finished run, scroll to **Artifacts**, tap **nexus-ai-debug-apk** to download a zip.
6. Unzip it, open `app-debug.apk` and install. Allow "install unknown apps" if Android asks.

The first build is also the first real test of this project. It was not compiled before
upload, so a few errors on the first run are possible and normal.

## Upload from your phone
GitHub's phone website cannot upload folders. Easiest options:

**Option A: Termux (recommended on Android)**
```
pkg install git unzip
cd ~
unzip /sdcard/Download/nexus-ai-complete.zip
cd nexus-ai
git init -b main
git add .
git commit -m "NEXUS AI phase 1"
git remote add origin https://github.com/YOUR_USERNAME/nexus-ai.git
git push -u origin main
```
When it asks for a password, paste a GitHub **personal access token** (GitHub > Settings >
Developer settings > Personal access tokens, with `repo` scope), not your account password.

**Option B:** Use any computer: unzip, then drag the whole folder into the repo page
(Add file > Upload files). Make sure the hidden `.github` folder is included.

## Run the backend (optional in Phase 1)
```
cd backend
python -m venv .venv && source .venv/bin/activate
pip install -r requirements.txt
cp .env.example .env
uvicorn app.main:app --reload
python -m pytest
```
Open http://127.0.0.1:8000/docs. Demo token for testing: `demo-anything`.

## Security rules
- Never commit `.env`, API keys, Firebase service account files or `google-services.json`
  (the `.gitignore` already blocks them).
- AI keys live only on the backend, never in the app.
- Call recording is off by default and not implemented.

## Versions
JDK 17, Gradle 8.9, AGP 8.7.3, Kotlin 2.0.21, KSP 2.0.21-1.0.28, Compose BOM 2024.12.01,
Room 2.6.1, minSdk 28, target/compile SDK 35. Python 3.12 for the backend.

## Next phases
2: real OTP + real AI. 3: real voice. 4: real incoming calls. 5: final testing and release.

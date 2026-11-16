# Food Donation Connector

A native Android application built with **Java** and **XML layouts** designed to connect surplus food donors (restaurants, caterers, grocery stores, individuals) with NGOs, shelters, and community receivers to minimize food waste and support communities in need.

---

## 📱 Features

- **Authentication & Role-Based Profiles**:
  - Sign in and sign up with Firebase Authentication.
  - Choose between **Food Donor** and **NGO / Receiver** roles during registration.
  - Automatic session management and local profile caching with `PreferenceManager`.

- **Interactive Dashboard (`HomeActivity`)**:
  - Role-specific greeting and status badge.
  - Quick action cards to post food, explore available meals, and inspect activity history.
  - Real-time snapshot list of recently posted food donations.

- **Post Food Donations (`PostDonationActivity`)**:
  - Input fields for Food Type / Title, Quantity / Servings, Pickup Address, and Special Instructions.
  - Interactive Date & Time Picker for expiry timestamp.
  - Camera/Gallery photo picker with Base64 compression for instant image storage in Firestore.

- **Browse Available Food (`DonationListActivity`)**:
  - Real-time updates with Firestore snapshot listeners.
  - Live search and filter by food title or pickup address.
  - Pull-to-refresh support (`SwipeRefreshLayout`).

- **Donation Details & Claiming (`DonationDetailActivity`)**:
  - View full donation details, donor contact info, pickup address, and food photo.
  - Direct dialer integration (`Intent.ACTION_DIAL`) to call the contact person.
  - **Dynamic Action Button**:
    - **NGOs/Receivers**: Claim and accept donations with a single tap.
    - **Donors/Claimants**: Mark as Handed Over / Received once food is transferred.

- **Activity & History Tracking (`MyDonationsActivity`)**:
  - For Donors: view all posted food donations and their statuses.
  - For NGOs: view all claimed food donations.
  - Filter chips: *All*, *Available*, *Claimed*, and *Completed*.

---

## 🛠 Tech Stack & Architecture

- **Language**: Java 8 (100% Java)
- **UI Toolkit**: XML Layouts with Material Components 3 (`com.google.android.material:material`)
- **Architecture**: Model-View-Adapter with ViewBinding enabled (`viewBinding true`)
- **Backend / Cloud**:
  - **Firebase Authentication** (Email & Password)
  - **Cloud Firestore** (Real-time NoSQL Database)
- **Image Loading**: Glide + Base64 Bitmap compression
- **Build System**: Gradle with Groovy DSL (`build.gradle`, `settings.gradle`)
- **Minimum SDK**: Android 7.0 (API 24)
- **Target SDK**: Android 14 (API 34)

---

## 📁 Project Directory Structure

```text
Food_donation/
├── .gitignore
├── build.gradle                          # Root Gradle build script (Groovy DSL)
├── settings.gradle                       # Project settings (Groovy DSL)
├── gradle.properties                     # JVM and AndroidX configuration
├── gradlew                               # Unix Gradle wrapper executable
├── gradlew.bat                           # Windows Gradle wrapper executable
├── gradle/wrapper/
│   └── gradle-wrapper.properties
├── app/
│   ├── build.gradle                      # App module build script (Groovy DSL)
│   ├── proguard-rules.pro                # ProGuard / R8 rules
│   ├── google-services.json              # Firebase configuration file
│   └── src/
│       └── main/
│           ├── AndroidManifest.xml
│           ├── java/com/example/fooddonationconnector/
│           │   ├── activities/
│           │   │   ├── LoginActivity.java
│           │   │   ├── SignupActivity.java
│           │   │   ├── HomeActivity.java
│           │   │   ├── PostDonationActivity.java
│           │   │   ├── DonationListActivity.java
│           │   │   ├── DonationDetailActivity.java
│           │   │   └── MyDonationsActivity.java
│           │   ├── adapters/
│           │   │   └── DonationAdapter.java
│           │   ├── models/
│           │   │   ├── Donation.java
│           │   │   └── User.java
│           │   └── utils/
│           │       ├── ImageUtils.java
│           │       └── PreferenceManager.java
│           └── res/
│               ├── drawable/             # Vector icons, badges, gradients
│               ├── layout/               # XML layouts with ViewBinding IDs
│               ├── mipmap-anydpi-v26/    # Adaptive app launcher icons
│               ├── values/               # strings.xml, colors.xml, themes.xml
│               ├── values-night/         # Dark mode themes.xml
│               └── xml/                  # Backup rules & data extraction rules
```

---

## 🚀 Setting Up Firebase

1. Go to the [Firebase Console](https://console.firebase.google.com/) and create a new project.
2. Add an Android app with the package name:
   ```text
   com.example.fooddonationconnector
   ```
3. Download the generated `google-services.json` file and place it in the `app/` folder (replacing the placeholder file).
4. **Enable Firebase Authentication**:
   - In Firebase Console, go to **Build > Authentication > Sign-in method**.
   - Enable **Email/Password**.
5. **Enable Cloud Firestore**:
   - Go to **Build > Firestore Database** and click **Create database**.
   - Start in **Test mode** (or configure secure rules as below).

### Recommended Firestore Security Rules
```javascript
rules_version = '2';
service cloud.firestore {
  match /databases/{database}/documents {
    match /users/{userId} {
      allow read, write: if request.auth != null;
    }
    match /donations/{donationId} {
      allow read, write: if request.auth != null;
    }
  }
}
```

---

## 🔨 Building and Running

1. **In Android Studio**:
   - Open Android Studio, select **Open**, and navigate to this project folder.
   - Wait for Gradle sync to complete.
   - Run the app on an Android Emulator or physical device (API 24+).

2. **Via Command Line**:
   ```powershell
   # Windows
   .\gradlew.bat assembleDebug

   # macOS / Linux
   ./gradlew assembleDebug
   ```
   The APK will be generated at `app/build/outputs/apk/debug/app-debug.apk`.


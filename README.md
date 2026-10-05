# York Lunch Club

An Android mobile application for the York Developer's Lunch Club. Members
record restaurant visits, log meals with per-dish scores for Flavour and Price,
and review their personal dining history. Each user sees only their own meals,
but restaurant average scores are aggregated across all users.

---

## Table of Contents

1. [Features](#features)
2. [Tech Stack](#tech-stack)
3. [Architecture](#architecture)
4. [Project Structure](#project-structure)
5. [Firestore Schema](#firestore-schema)
6. [Score Model](#score-model)
7. [Prerequisites](#prerequisites)
8. [Running the App in Android Studio](#running-the-app-in-android-studio)
9. [Running the Tests](#running-the-tests)
10. [Test Plan](#test-plan)
11. [Design System](#design-system)
12. [Troubleshooting](#troubleshooting)

---

## Features

- **Authentication**: Email/password registration and login via Firebase Auth.
- **Biometric login**: Optional fingerprint/face login backed by
  EncryptedSharedPreferences and the Android Keystore.
- **Restaurant list**: Ten curated York restaurants, each with average Flavour
  and Price scores computed across every user's visits.
- **Restaurant detail**: Per-restaurant overview with image, address, cuisine,
  description, aggregate scores, visit count, and the current user's visit
  history.
- **Record a meal (two-step flow)**:
  1. Select one or more menu items.
  2. Rate each selected item on Flavour (1–5) and Price (1–5).
- **Meal history**: A chronological list of the current user's recorded meals.
- **Meal detail**: View a previously recorded meal, edit it, or jump to the
  associated restaurant.
- **Edit a meal**: Returns to a pre-populated two-step editor; saving updates
  both the meal and the restaurant's aggregate scores.
- **User isolation**: Each user only sees their own meal records.
- **Logout**: Available on every authenticated screen.

---

## Tech Stack

| Layer            | Technology                                  |
|------------------|---------------------------------------------|
| Language         | Kotlin                                      |
| UI               | Jetpack Compose (Material Design 3)         |
| Navigation       | Navigation Compose                          |
| Architecture     | MVVM with `ViewModel` + `StateFlow`         |
| Auth             | Firebase Authentication                     |
| Database         | Cloud Firestore                             |
| Image storage    | Firebase Storage                            |
| Image loading    | Coil                                        |
| Biometrics       | `androidx.biometric`                        |
| Secure storage   | `androidx.security:security-crypto`         |
| Unit testing     | JUnit 4, MockK, kotlinx-coroutines-test     |
| Instrumentation  | AndroidX Test, Compose UI Test, Espresso    |
| End-to-end       | UI Automator                                |
| Build system     | Gradle (Kotlin DSL), Version Catalog        |
| Min SDK / Target | 26 / 36                                     |

---

## Architecture

The app follows a single-activity, MVVM-with-Compose architecture.

```
UI (Composables)
   └── observes StateFlow<UiState> from
ViewModel (Android Architecture Component)
   └── calls suspend functions on
Repository (AuthRepository / RestaurantRepository / MealRepository)
   └── talks to
Firebase (Auth / Firestore / Storage)
```

- **Composables** are stateless and receive state plus callbacks.
- **ViewModels** expose immutable `StateFlow<UiState>` and update a private
  `MutableStateFlow` in response to UI events. They survive configuration
  changes and are scoped via `viewModelStoreOwner` where state needs to be
  shared between sibling screens (e.g. Record Meal Step 1 ↔ Step 2).
- **Repositories** wrap Firebase SDKs and return `Result<T>` so that callers
  can fold success and failure cases explicitly.
- **Navigation** is centralised in `AppNavGraph.kt`. The start destination
  is decided by `FirebaseAuth.currentUser`: logged-in users start on the
  restaurant list, unauthenticated users land on the login screen.

---

## Project Structure

```
com.yorklunchclub.app/
├── MainActivity.kt
├── YorkLunchClubApp.kt
│
├── ui/
│   ├── theme/                  Colour, Theme, Type
│   ├── navigation/             AppNavGraph + Routes
│   ├── components/             ScoreSlider, RestaurantCard, MealCard, ScoreBadge…
│   └── screens/
│       ├── auth/               Login + Register (Screen + ViewModel)
│       ├── restaurants/        Restaurant list + detail (Screen + ViewModel)
│       └── meals/              Record / Edit / History / Detail
│
├── data/
│   ├── model/                  User, Restaurant, MenuItem, Meal, MealItem
│   ├── repository/             AuthRepository, RestaurantRepository, MealRepository
│   └── firebase/               FirestoreCollections constants
│
└── utils/
    ├── BiometricHelper.kt
    └── ScoreCalculator.kt
```

Tests:

```
app/src/test/java/com/yorklunchclub/app/
├── utils/ScoreCalculatorTest.kt              TC-01 → TC-06
└── ui/screens/auth/RegisterViewModelTest.kt  TC-07 → TC-08

app/src/androidTest/java/com/yorklunchclub/app/ui/
├── InstrumentationTests.kt                   TC-09 → TC-13
├── IntegrationTests.kt                       TC-14 → TC-18
└── EndToEndTests.kt                          TC-19 → TC-23
```

---

## Firestore Schema

```
users/{userId}
  ├── fullName  : String
  └── username  : String

restaurants/{restaurantId}
  ├── name             : String
  ├── address          : String
  ├── description      : String
  ├── cuisine          : String
  ├── imageUrl         : String
  ├── galleryUrls      : List<String>
  ├── avgFlavourScore  : Double?   (null when no visits)
  ├── avgPriceScore    : Double?   (null when no visits)
  └── menu/{menuItemId}
        ├── name   : String
        └── price  : Double

meals/{mealId}
  ├── userId          : String       (filter target)
  ├── restaurantId    : String
  ├── restaurantName  : String       (denormalised)
  ├── date            : Timestamp
  └── items/{itemId}
        ├── menuItemId    : String
        ├── menuItemName  : String   (denormalised)
        ├── flavourScore  : Int      (1–5)
        └── priceScore    : Int      (1–5)
```

Security rules enforce:

- Each user can only read/write their own document under `users/{uid}`.
- Restaurants and menu items are read-only to clients.
- Meals are readable by any authenticated user (required for cross-user score
  aggregation), but only the meal's owner can create, update, or delete it.

---

## Score Model

- All scores are integers between **1 and 5**, never floats, never out of 10.
- Each meal has two independent dimensions: **Flavour** and **Price**.
- A **visit average** is the mean of the per-item scores within one meal.
- A **restaurant average** is the mean of all visit averages across all users.
- If no user has visited a restaurant, both averages are `null` and the UI
  displays `"N/A"`.
- When a user saves or edits a meal, `MealRepository.recomputeRestaurantAverages()`
  re-aggregates the restaurant's `avgFlavourScore` and `avgPriceScore` from all
  meals (across all users) and writes them back to the restaurant document.

Implementation lives in `utils/ScoreCalculator.kt`. Scores outside 1–5 raise
`IllegalArgumentException` via `ScoreCalculator.validateScore()`.

---

## Prerequisites

- **Android Studio**: Ladybug (2024.2) or newer recommended.
- **JDK 11** or newer (Gradle is configured with `JavaVersion.VERSION_11`).
- **Android SDK** with API level 36 installed.
- **Emulator or physical device** running Android 8.0 (API 26) or higher,
  with Google Play Services available (Firebase requires it).
- **`google-services.json`** placed in `app/` (Firebase project config). The
  app will not build without this file.
- **Node.js / npm** (only required if you want to use the `npm` test script
  wrappers; Gradle works on its own).

---

## Running the App in Android Studio

1. **Clone the repository**, then open the project root
   (`YorkLunchClub/`) in Android Studio via *File → Open*.
2. Wait for Gradle sync to complete. Accept any SDK install prompts.
3. Make sure `app/google-services.json` is present. If it isn't, download it
   from the Firebase Console for this project and place it at the path above.
4. **Pick a target device** from the device dropdown in the toolbar:
   - To use the emulator: open *Tools → Device Manager*, create or start an
     AVD running API 26+ with the Google Play system image.
   - To use a physical device: enable Developer Options and USB debugging on
     the phone, then plug it in and approve the debugging prompt.
5. Click the green **Run ▶** button (or press `Ctrl+R` on macOS).
6. On first launch you will see the Login screen. Either register a new user
   or sign in with an existing one. After authentication you will land on the
   Restaurants tab and the bottom bar will show Restaurants and My Meals.

### Seeding the database

The ten restaurants are not seeded automatically. Add them to Firestore manually
via the Firebase Console using the IDs `r01` through `r10` from `CLAUDE.md`.
Each restaurant should have its `menu/{menuItemId}` subcollection populated.

### Running from the command line

```bash
./gradlew :app:installDebug        # install on connected device
./gradlew :app:assembleDebug       # build APK only (no install)
```

---

## Running the Tests

The project provides both raw Gradle commands and npm script wrappers
(see `package.json`).

### Unit tests (no device needed)

These run on the host JVM and cover `ScoreCalculator` (TC-01 → TC-06) and
`RegisterViewModel` (TC-07 → TC-08).

```bash
./gradlew :app:testDebugUnitTest
# or
npm run test:unit
```

Run a single test class:

```bash
npm run test:score      # ScoreCalculatorTest only
npm run test:register   # RegisterViewModelTest only
```

### Instrumentation, integration, and end-to-end tests (device or emulator required)

Start an emulator or connect a physical device first, then:

```bash
./gradlew :app:connectedDebugAndroidTest
# or
npm run test:android
```

Run a specific suite only:

```bash
npm run test:instrumentation   # TC-09 → TC-13 (Compose UI components)
npm run test:integration       # TC-14 → TC-18 (cross-screen flows)
npm run test:e2e               # TC-19 → TC-23 (UI Automator)
```

### Full test suite

```bash
npm test          # runs unit tests, then android tests
# or
npm run test:all  # single Gradle invocation
```

### Running tests from Android Studio

- Right-click a test class or function in the editor and choose
  *Run 'testName'*.
- For unit tests, the green gutter icon next to `@Test` annotations executes
  that test on the host JVM.
- For instrumentation tests, ensure a device is selected from the toolbar
  before running; Android Studio will install the test APK alongside the app
  APK and execute the test on-device.
- Test results appear in the *Run* tool window, and HTML reports are written
  to:
  - Unit: `app/build/reports/tests/testDebugUnitTest/index.html`
  - Android: `app/build/reports/androidTests/connected/index.html`

---

## Test Plan

Each test case maps to a function named `tcNN_…`. Categories:

| Category         | TC range      | Location                                     |
|------------------|---------------|----------------------------------------------|
| Unit             | TC-01 → TC-08 | `app/src/test/`                              |
| Instrumentation  | TC-09 → TC-13 | `app/src/androidTest/ui/InstrumentationTests.kt` |
| Integration      | TC-14 → TC-18 | `app/src/androidTest/ui/IntegrationTests.kt`     |
| End-to-end       | TC-19 → TC-23 | `app/src/androidTest/ui/EndToEndTests.kt`        |

| TC#  | What it verifies                                                  |
|------|-------------------------------------------------------------------|
| TC-01 | Score average calculation of a single visit                      |
| TC-02 | Score average calculation across multiple visits                 |
| TC-03 | Score minimum boundary (1 accepted)                              |
| TC-04 | Score maximum boundary (5 accepted)                              |
| TC-05 | Score out of range (0 and 6 throw `IllegalArgumentException`)    |
| TC-06 | `"N/A"` display when no user has visited                         |
| TC-07 | Username validation with empty fields                            |
| TC-08 | Password match validation                                        |
| TC-09 | Flavour slider renders correctly                                 |
| TC-10 | Price slider renders correctly                                   |
| TC-11 | Menu item selection expands score inputs                         |
| TC-12 | Biometric login button visible on Login screen                   |
| TC-13 | Logout button visible on every authenticated screen              |
| TC-14 | A recorded meal appears in Meal History                          |
| TC-15 | Recording a meal updates the restaurant's displayed average      |
| TC-16 | User data isolation across accounts                              |
| TC-17 | Edit Entry returns to Step 1 with selections intact              |
| TC-18 | Meal Detail links to Restaurant Detail                           |
| TC-19 | Full registration and login flow                                 |
| TC-20 | Full meal recording journey                                      |
| TC-21 | Meal History → Meal Detail → Restaurant Detail                   |
| TC-22 | Restaurant Detail backward navigation                            |
| TC-23 | Logout and re-login as a different user                          |

---

## Design System

Colours (defined in `ui/theme/Color.kt` — never hardcoded elsewhere):

| Name        | Hex       | Usage                                  |
|-------------|-----------|----------------------------------------|
| Amber       | `#C07A15` | Primary actions, FAB, badges           |
| AmberLight  | `#FAEEDA` | Primary surface / selected card        |
| Teal        | `#0F6E56` | Secondary actions, links               |
| TealLight   | `#E1F5EE` | Secondary surface                      |
| Background  | `#FAF8F3` | App background                         |
| TextPrimary | `#1C1A14` | Primary text                           |
| TextMuted   | `#6B6758` | Secondary text and hints               |

Component rules:

- Cards: white background, 0.5 dp border, 12 dp corner radius.
- Primary buttons: amber fill, pill shape, bold label.
- Secondary buttons: teal outline.
- Bottom navigation: two tabs only (Restaurants, My Meals).
- FAB: amber, always visible on Restaurant Detail.
- Score sliders: range 1–5 with descriptive endpoint labels
  (Flavour: Bland → Exceptional, Price: Overpriced → Steal).

---

## Troubleshooting

- **"google-services.json is missing" build error**: download the file from
  the Firebase Console and place it in `app/`.
- **Login or register fails immediately**: check that the device has internet
  access and that Email/Password sign-in is enabled in Firebase Auth.
- **Restaurant scores show "N/A" forever**: confirm that meals are actually
  being written to Firestore. The restaurant document's `avgFlavourScore` and
  `avgPriceScore` are updated on every save or edit by
  `MealRepository.recomputeRestaurantAverages()`.
- **Biometric button not visible**: it only appears after at least one
  successful password login on this device (credentials are then cached in
  EncryptedSharedPreferences). Logging out clears them.
- **`connectedDebugAndroidTest` fails with "No connected devices"**: start an
  emulator or plug in a debuggable device before running.
- **Instrumentation tests time out**: the emulator may be slow to start.
  Increase the test runner timeout in Android Studio's run configuration or
  use a faster system image (x86_64 with hardware acceleration).

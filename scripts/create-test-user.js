#!/usr/bin/env node
/**
 * York Lunch Club — Create / refresh a default test user
 *
 * Creates (or updates) a Firebase Auth user with known credentials AND writes
 * the matching Firestore profile doc at users/{uid}. Idempotent — re-running
 * resets the password and rewrites the profile fields.
 *
 * Usage:
 *   cd scripts
 *   npm install   # if not already
 *   node create-test-user.js
 *
 * Defaults (override via env vars):
 *   YLC_TEST_EMAIL    = tester@yorklunchclub.app
 *   YLC_TEST_PASSWORD = Test1234!
 *   YLC_TEST_FULLNAME = "Test User"
 *   YLC_TEST_USERNAME = tester
 *
 * NOTE: this script can create the Auth user even with the Email/Password
 * sign-in provider DISABLED in the console, but the Android app cannot sign
 * the user in until that provider is enabled. So if `npm run seed` works but
 * the app's Login screen still fails, the missing piece is almost always:
 *
 *   Firebase Console → Authentication → Sign-in method → Email/Password → Enable
 */

const path = require("path");
const admin = require("firebase-admin");

const PROJECT_ID = "yorklunchclub";
const SERVICE_ACCOUNT_PATH = path.resolve(__dirname, "service-account.json");

const EMAIL = process.env.YLC_TEST_EMAIL || "tester@yorklunchclub.app";
const PASSWORD = process.env.YLC_TEST_PASSWORD || "Test1234!";
const FULL_NAME = process.env.YLC_TEST_FULLNAME || "Test User";
const USERNAME = process.env.YLC_TEST_USERNAME || "tester";

let serviceAccount;
try {
  serviceAccount = require(SERVICE_ACCOUNT_PATH);
} catch (err) {
  console.error(
    `\nMissing service-account.json at ${SERVICE_ACCOUNT_PATH}\n\n` +
      "Generate one in Firebase Console → Project Settings → Service Accounts →\n" +
      `"Generate new private key" for project "${PROJECT_ID}", save as scripts/service-account.json.\n`
  );
  process.exit(1);
}

if (serviceAccount.project_id !== PROJECT_ID) {
  console.error(
    `service-account.json project_id mismatch: expected "${PROJECT_ID}", got "${serviceAccount.project_id}"`
  );
  process.exit(1);
}

admin.initializeApp({
  credential: admin.credential.cert(serviceAccount),
  projectId: PROJECT_ID,
});

const auth = admin.auth();
const db = admin.firestore();

async function upsertUser() {
  let user;
  try {
    user = await auth.getUserByEmail(EMAIL);
    await auth.updateUser(user.uid, {
      password: PASSWORD,
      displayName: FULL_NAME,
      emailVerified: true,
      disabled: false,
    });
    console.log(`  ↻ updated existing auth user  uid=${user.uid}  email=${EMAIL}`);
  } catch (err) {
    if (err.code !== "auth/user-not-found") throw err;
    user = await auth.createUser({
      email: EMAIL,
      password: PASSWORD,
      displayName: FULL_NAME,
      emailVerified: true,
    });
    console.log(`  + created auth user            uid=${user.uid}  email=${EMAIL}`);
  }

  await db.collection("users").doc(user.uid).set(
    {
      fullName: FULL_NAME,
      username: USERNAME,
    },
    { merge: true }
  );
  console.log(`  ✓ wrote users/${user.uid} { fullName, username }`);

  console.log("\nCredentials to use on the Login screen:");
  console.log(`  email:    ${EMAIL}`);
  console.log(`  password: ${PASSWORD}\n`);
  console.log(
    "If sign-in still fails in the app with \"sign-in provider is disabled\",\n" +
      "enable Email/Password in Firebase Console → Authentication → Sign-in method.\n"
  );
}

upsertUser()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("\nFailed:", err);
    process.exit(1);
  });

# York Lunch Club — Firestore seed

One-shot Node script that writes the 10 restaurants (r01..r10) + their menu
subcollections into the `yorklunchclub` Firebase project, using a service
account so it bypasses the read-only `restaurants` security rule.

## One-time setup

1. **Get a service account key.**
   Firebase Console → Project Settings → Service Accounts →
   *Generate new private key* → save the JSON file as:

   ```
   scripts/service-account.json
   ```

   This file grants admin access to your project. It's gitignored — never
   commit it.

2. **Install deps.**

   ```bash
   cd scripts
   npm install
   ```

## Run

```bash
cd scripts
npm run seed              # 10 restaurants + menus
npm run create-test-user  # default test user + Firestore profile
```

Default test user credentials (override with env vars — see
`create-test-user.js` header):

| Field    | Value                          |
|----------|--------------------------------|
| Email    | `tester@yorklunchclub.app`     |
| Password | `Test1234!`                    |
| Username | `tester`                       |

**Heads-up:** `create-test-user` succeeds even with the Email/Password
sign-in provider disabled in the console, but the Android app cannot
sign in until that provider is enabled. If the app shows "*The given
sign-in provider is disabled...*" or "*An internal error has occurred...*":

→ Firebase Console → **Authentication → Sign-in method → Email/Password → Enable**.

Re-running is safe — every doc uses a fixed ID (`r01`..`r10` for restaurants,
`m01`..`m06` for menu items), so a second run overwrites in place rather than
creating duplicates.

### Flags

| Flag           | Effect                                                      |
|----------------|-------------------------------------------------------------|
| `--wipe-menus` | Delete each restaurant's `menu` subcollection first.        |
| `--wipe-all`   | Delete each restaurant doc + its menu before re-writing.    |

Examples:

```bash
node seed-firestore.js --wipe-menus
node seed-firestore.js --wipe-all
```

## What gets written

```
restaurants/
  r01..r10/
    name, address, description, cuisine, imageUrl: "", galleryUrls: []
    menu/
      m01..m0N/
        name, price
```

`imageUrl` is left empty — the app's `RestaurantImage.resolve()` helper falls
back to `app/src/main/assets/<filename>` via a `restaurantId → filename` map.
Once you upload images to Firebase Storage, set `imageUrl` to the download
URL (or `gs://` URI) on each restaurant doc.

## Verifying

After running, in the Firebase Console → Firestore you should see:

- `restaurants` collection with 10 docs, IDs `r01`–`r10`
- Each doc with a `menu` subcollection containing 6 items

Then build & run the app — restaurants screen should populate.

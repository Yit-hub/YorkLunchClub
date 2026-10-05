#!/usr/bin/env node
/**
 * York Lunch Club — Firestore seed
 *
 * Seeds the 10 restaurants from CLAUDE.md plus a per-cuisine menu subcollection.
 *
 * Usage:
 *   1. From Firebase Console → Project Settings → Service Accounts →
 *      "Generate new private key" → save as scripts/service-account.json
 *   2. cd scripts && npm install
 *   3. npm run seed                 (idempotent — uses fixed doc IDs)
 *      npm run seed -- --wipe-menus (drops existing menu subcollection first)
 *
 * Restaurant.imageUrl is left empty; the app falls back to local assets via
 * RestaurantImage.resolve() (asset map keyed by restaurantId).
 */

const path = require("path");
const admin = require("firebase-admin");

const PROJECT_ID = "yorklunchclub";
const SERVICE_ACCOUNT_PATH = path.resolve(__dirname, "service-account.json");

const argv = process.argv.slice(2);
const WIPE_MENUS = argv.includes("--wipe-menus");
const WIPE_ALL = argv.includes("--wipe-all");

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

const db = admin.firestore();

const restaurants = [
  {
    id: "r01",
    name: "Bill's Restaurant",
    address: "12 Coney Street, York",
    cuisine: "British",
    description:
      "All-day British classics in a relaxed dining room — breakfast, brunch, burgers and Sunday roasts at the heart of York.",
    menu: [
      { name: "Bill's Big Breakfast", price: 14.95 },
      { name: "Eggs Benedict", price: 11.5 },
      { name: "Buttermilk Pancakes", price: 9.75 },
      { name: "Halloumi Burger", price: 13.95 },
      { name: "Sunday Roast Beef", price: 17.5 },
      { name: "Sticky Toffee Pudding", price: 6.75 },
    ],
  },
  {
    id: "r02",
    name: "YUZU Street Food",
    address: "Unit 6, Enterprise Complex, Walmgate, York",
    cuisine: "Japanese",
    description:
      "Bright Japanese street-food joint serving punchy ramen bowls, rice boxes and donburi by an open kitchen.",
    menu: [
      { name: "Tonkotsu Ramen", price: 13.5 },
      { name: "Miso Ramen", price: 12.95 },
      { name: "Katsu Chicken Curry", price: 12.5 },
      { name: "Gyoza (6)", price: 6.95 },
      { name: "Salmon Teriyaki Don", price: 14.95 },
      { name: "Yuzu Cheesecake", price: 5.95 },
    ],
  },
  {
    id: "r03",
    name: "Cosy Club",
    address: "19-22 Fossgate, York",
    cuisine: "Continental",
    description:
      "Eccentric, plant-filled dining rooms doing crowd-pleasing continental small plates, tapas and big brunch plates.",
    menu: [
      { name: "Halloumi Fries", price: 7.5 },
      { name: "Crispy Calamari", price: 8.25 },
      { name: "Smashed Avocado Brunch", price: 11.5 },
      { name: "Chorizo & Patatas Bravas", price: 8.95 },
      { name: "Cosy Burger", price: 14.5 },
      { name: "Lemon Posset", price: 6.5 },
    ],
  },
  {
    id: "r04",
    name: "Prezzo Italian",
    address: "1 Clifford Street, York",
    cuisine: "Italian",
    description:
      "Familiar Italian-chain favourites — stone-baked pizza, classic pasta and a long aperitivo list near Clifford's Tower.",
    menu: [
      { name: "Margherita Pizza", price: 11.95 },
      { name: "Pollo Pesto Pizza", price: 14.5 },
      { name: "Spaghetti Carbonara", price: 13.5 },
      { name: "Lasagne al Forno", price: 14.25 },
      { name: "Bruschetta Classica", price: 6.95 },
      { name: "Tiramisu", price: 6.75 },
    ],
  },
  {
    id: "r05",
    name: "L'Osteria Italiana",
    address: "31 Castlegate, York",
    cuisine: "Italian",
    description:
      "Small family-run Castlegate trattoria leaning on northern Italian regional dishes and a tight, hand-picked wine list.",
    menu: [
      { name: "Burrata e Prosciutto", price: 10.95 },
      { name: "Tagliatelle al Ragù", price: 14.5 },
      { name: "Risotto ai Funghi", price: 14.95 },
      { name: "Saltimbocca alla Romana", price: 18.5 },
      { name: "Pizza Diavola", price: 13.95 },
      { name: "Panna Cotta", price: 6.5 },
    ],
  },
  {
    id: "r06",
    name: "Il Paradiso Del Cibo",
    address: "40 Walmgate, York",
    cuisine: "Italian",
    description:
      "Long-running Walmgate Italian with wood-fired pizzas, blackboard pasta specials and a cult following.",
    menu: [
      { name: "Pizza Quattro Formaggi", price: 13.95 },
      { name: "Pizza Capricciosa", price: 14.5 },
      { name: "Spaghetti alle Vongole", price: 16.5 },
      { name: "Pollo Milanese", price: 17.95 },
      { name: "Antipasto Misto", price: 11.5 },
      { name: "Cannoli Siciliani", price: 6.95 },
    ],
  },
  {
    id: "r07",
    name: "Ambiente Tapas",
    address: "31 Fossgate, York",
    cuisine: "Spanish",
    description:
      "Lively Fossgate tapas room with patatas bravas, plancha seafood and a thoughtfully curated sherry and rioja list.",
    menu: [
      { name: "Patatas Bravas", price: 6.5 },
      { name: "Gambas al Ajillo", price: 9.95 },
      { name: "Albóndigas", price: 7.95 },
      { name: "Tortilla Española", price: 6.75 },
      { name: "Pulpo a la Gallega", price: 11.5 },
      { name: "Crema Catalana", price: 6.25 },
    ],
  },
  {
    id: "r08",
    name: "Byron",
    address: "11 High Ousegate, York",
    cuisine: "Burgers",
    description:
      "Properly hamburgers — dry-aged beef, brioche buns, milkshakes thick enough to chew, on High Ousegate.",
    menu: [
      { name: "Classic Byron Burger", price: 12.95 },
      { name: "Cheese Burger", price: 13.5 },
      { name: "Smokey Burger", price: 14.95 },
      { name: "Skin-on Fries", price: 4.5 },
      { name: "Onion Rings", price: 4.95 },
      { name: "Oreo Milkshake", price: 5.95 },
    ],
  },
  {
    id: "r09",
    name: "Burgsy's",
    address: "9 Walmgate, York",
    cuisine: "Burgers",
    description:
      "York indie burger spot doing smashed patties, loaded fries and rotating chef's specials chalked up daily.",
    menu: [
      { name: "Burgsy's Single Smash", price: 9.5 },
      { name: "Double Cheese Smash", price: 12.5 },
      { name: "Buttermilk Chicken Burger", price: 11.95 },
      { name: "Loaded Dirty Fries", price: 7.5 },
      { name: "Truffle Parmesan Fries", price: 6.5 },
      { name: "Salted Caramel Shake", price: 5.5 },
    ],
  },
  {
    id: "r10",
    name: "Rustique",
    address: "28 Castlegate, York",
    cuisine: "French",
    description:
      "Castlegate French bistro doing unfussy classics — moules-frites, steak haché, crème brûlée — at honest set-menu prices.",
    menu: [
      { name: "Soupe à l'Oignon", price: 6.95 },
      { name: "Moules Marinière & Frites", price: 16.5 },
      { name: "Steak Haché Frites", price: 15.95 },
      { name: "Coq au Vin", price: 17.5 },
      { name: "Salade Niçoise", price: 12.95 },
      { name: "Crème Brûlée", price: 6.75 },
    ],
  },
];

async function deleteCollection(ref) {
  const snap = await ref.get();
  const batch = db.batch();
  snap.docs.forEach((d) => batch.delete(d.ref));
  if (!snap.empty) await batch.commit();
}

async function seed() {
  console.log(
    `\nSeeding ${restaurants.length} restaurants into project "${PROJECT_ID}"` +
      (WIPE_MENUS ? " (wiping menus first)" : "") +
      (WIPE_ALL ? " (wiping each restaurant doc first)" : "") +
      "\n"
  );

  for (const r of restaurants) {
    const docRef = db.collection("restaurants").doc(r.id);

    if (WIPE_ALL) {
      await deleteCollection(docRef.collection("menu"));
      await docRef.delete().catch(() => {});
    } else if (WIPE_MENUS) {
      await deleteCollection(docRef.collection("menu"));
    }

    await docRef.set(
      {
        name: r.name,
        address: r.address,
        description: r.description,
        cuisine: r.cuisine,
        imageUrl: "",
        galleryUrls: [],
        avgFlavourScore: null,
        avgPriceScore: null,
      },
      { merge: true }
    );

    // Stable menu doc IDs so re-runs are idempotent
    let i = 1;
    for (const m of r.menu) {
      const menuId = `m${String(i).padStart(2, "0")}`;
      await docRef.collection("menu").doc(menuId).set({
        name: m.name,
        price: m.price,
      });
      i += 1;
    }

    console.log(`  ✓ ${r.id}  ${r.name}  (${r.menu.length} menu items)`);
  }

  console.log("\nDone.\n");
}

seed()
  .then(() => process.exit(0))
  .catch((err) => {
    console.error("\nSeed failed:", err);
    process.exit(1);
  });

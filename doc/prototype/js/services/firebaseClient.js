import { initializeApp } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-app.js";
import { getAuth } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";
import { getFirestore } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";

import { getRuntimeConfig } from "./runtimeConfig.js";

export function createFirebaseClient() {
  const { config, appId, initialAuthToken } = getRuntimeConfig();

  const app = initializeApp(config);
  const auth = getAuth(app);
  const db = getFirestore(app);

  return {
    appId,
    initialAuthToken,
    auth,
    db,
  };
}

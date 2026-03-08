import {
  onAuthStateChanged,
  signInAnonymously,
  signInWithCustomToken,
  signOut,
} from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";

export function createAuthService(auth, initialAuthToken) {
  async function initAuth() {
    if (initialAuthToken) {
      await signInWithCustomToken(auth, initialAuthToken);
      return;
    }
    await signInAnonymously(auth);
  }

  function observeAuth(callback) {
    return onAuthStateChanged(auth, callback);
  }

  async function logout() {
    await signOut(auth);
  }

  return {
    initAuth,
    observeAuth,
    logout,
  };
}

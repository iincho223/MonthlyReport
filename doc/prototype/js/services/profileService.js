import { doc, getDoc, setDoc } from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";

export function createProfileService(db, appId) {
  function profileRefByUid(uid) {
    return doc(db, "artifacts", appId, "users", uid, "profile", "data");
  }

  async function readProfile(uid) {
    const snapshot = await getDoc(profileRefByUid(uid));
    return snapshot.exists() ? snapshot.data() : null;
  }

  async function writeProfile(uid, profile) {
    await setDoc(profileRefByUid(uid), profile);
  }

  return {
    readProfile,
    writeProfile,
  };
}

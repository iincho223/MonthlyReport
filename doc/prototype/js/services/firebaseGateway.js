import { createAuthService } from "./authService.js";
import { createFirebaseClient } from "./firebaseClient.js";
import { createProfileService } from "./profileService.js";
import { createReportService } from "./reportService.js";

export function createFirebaseGateway() {
  const { appId, initialAuthToken, auth, db } = createFirebaseClient();

  const authService = createAuthService(auth, initialAuthToken);
  const profileService = createProfileService(db, appId);
  const reportService = createReportService(db, appId);

  return {
    auth,
    ...authService,
    ...profileService,
    ...reportService,
  };
}

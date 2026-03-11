import { createAuthService } from "./authService.js";
import { createFirebaseClient } from "./firebaseClient.js";
import { createProfileService } from "./profileService.js";
import { createReportService } from "./reportService.js";

/**
 * Firebase 関連機能を集約したゲートウェイを生成する。
 * インプット: なし。
 * アウトプット: auth/profile/report 操作を持つゲートウェイオブジェクト。
 *
 * @returns {object} Firebase ゲートウェイ
 */
export function createFirebaseGateway() {
  // ランタイム設定に基づいて Firebase クライアントを初期化する。
  const { appId, initialAuthToken, auth, db } = createFirebaseClient();

  // ドメインごとのサービスを生成して統合する。
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

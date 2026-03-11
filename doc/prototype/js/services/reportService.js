import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  onSnapshot,
  serverTimestamp,
  updateDoc,
} from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";

/**
 * 月報データアクセスサービスを生成する。
 * インプット: db Firestore インスタンス、appId アプリID。
 * アウトプット: 月報の監視・作成・更新・削除 API。
 *
 * @param {object} db Firestore インスタンス
 * @param {string} appId アプリID
 * @returns {object} 月報サービス
 */
export function createReportService(db, appId) {
  /**
   * 月報コレクション参照を返す。
   * インプット: なし。
   * アウトプット: reports コレクション参照。
   *
   * @returns {object} コレクション参照
   */
  function reportsCollectionRef() {
    return collection(db, "artifacts", appId, "public", "data", "reports");
  }

  /**
   * 月報IDからドキュメント参照を返す。
   * インプット: reportId 月報ID。
   * アウトプット: 月報ドキュメント参照。
   *
   * @param {string} reportId 月報ID
   * @returns {object} ドキュメント参照
   */
  function reportRefById(reportId) {
    return doc(db, "artifacts", appId, "public", "data", "reports", reportId);
  }

  /**
   * 月報一覧をリアルタイム監視する。
   * インプット: onData 正常時コールバック、onError 異常時コールバック。
   * アウトプット: 監視解除関数。
   *
   * @param {(reports: object[]) => void} onData 正常時コールバック
   * @param {(error: Error) => void} onError 異常時コールバック
   * @returns {() => void} 監視解除関数
   */
  function observeReports(onData, onError) {
    return onSnapshot(
      reportsCollectionRef(),
      (snapshot) => {
        // Firestore スナップショットを画面用配列へ変換する。
        const reports = snapshot.docs.map((reportDoc) => ({
          id: reportDoc.id,
          ...reportDoc.data(),
        }));
        onData(reports);
      },
      onError,
    );
  }

  /**
   * 月報を新規作成する。
   * インプット: payload 月報データ。
   * アウトプット: Firestore に新規月報が追加された状態。
   *
   * @param {object} payload 月報データ
   * @returns {Promise<void>} 実行完了
   */
  async function createReport(payload) {
    // 作成時に回答関連フィールドを初期化して保存する。
    await addDoc(reportsCollectionRef(), {
      ...payload,
      createdAt: serverTimestamp(),
      adminFeedback: "",
      adminFeedbackRole: "",
      adminFeedbackName: "",
    });
  }

  /**
   * 月報を更新する。
   * インプット: reportId 月報ID、payload 更新データ。
   * アウトプット: 指定月報が更新された状態。
   *
   * @param {string} reportId 月報ID
   * @param {object} payload 更新データ
   * @returns {Promise<void>} 実行完了
   */
  async function updateReport(reportId, payload) {
    await updateDoc(reportRefById(reportId), payload);
  }

  /**
   * 管理者回答を更新する。
   * インプット: reportId 月報ID、payload 回答データ。
   * アウトプット: 回答情報と回答時刻が更新された状態。
   *
   * @param {string} reportId 月報ID
   * @param {object} payload 回答データ
   * @returns {Promise<void>} 実行完了
   */
  async function updateFeedback(reportId, payload) {
    // 回答更新時に回答日時を自動付与する。
    await updateDoc(reportRefById(reportId), {
      ...payload,
      adminFeedbackAt: serverTimestamp(),
    });
  }

  /**
   * 月報を削除する。
   * インプット: reportId 月報ID。
   * アウトプット: 指定月報が削除された状態。
   *
   * @param {string} reportId 月報ID
   * @returns {Promise<void>} 実行完了
   */
  async function removeReport(reportId) {
    await deleteDoc(reportRefById(reportId));
  }

  return {
    observeReports,
    createReport,
    updateReport,
    updateFeedback,
    removeReport,
    serverTimestamp,
  };
}

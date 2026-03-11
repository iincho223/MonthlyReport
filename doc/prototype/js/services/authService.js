import {
  onAuthStateChanged,
  signInAnonymously,
  signInWithCustomToken,
  signOut,
} from "https://www.gstatic.com/firebasejs/11.6.1/firebase-auth.js";

/**
 * 認証関連サービスを生成する。
 * インプット: auth Firebase Auth インスタンス、initialAuthToken 初期トークン。
 * アウトプット: 認証初期化・状態監視・ログアウト API。
 *
 * @param {object} auth Firebase Auth インスタンス
 * @param {string | null} initialAuthToken 初期トークン
 * @returns {object} 認証サービス
 */
export function createAuthService(auth, initialAuthToken) {
  /**
   * 認証を初期化する。
   * インプット: なし。
   * アウトプット: 認証済み状態。
   *
   * @returns {Promise<void>} 実行完了
   */
  async function initAuth() {
    // 初期トークンがある場合はカスタムトークンでログインする。
    if (initialAuthToken) {
      await signInWithCustomToken(auth, initialAuthToken);
      return;
    }
    // 初期トークンがない場合は匿名認証で開始する。
    await signInAnonymously(auth);
  }

  /**
   * 認証状態変更を監視する。
   * インプット: callback 認証状態変更時コールバック。
   * アウトプット: 監視解除関数。
   *
   * @param {(user: object | null) => void} callback 認証状態変更時コールバック
   * @returns {() => void} 監視解除関数
   */
  function observeAuth(callback) {
    return onAuthStateChanged(auth, callback);
  }

  /**
   * ログアウトする。
   * インプット: なし。
   * アウトプット: 未認証状態。
   *
   * @returns {Promise<void>} 実行完了
   */
  async function logout() {
    await signOut(auth);
  }

  return {
    initAuth,
    observeAuth,
    logout,
  };
}

import { ROLES } from "../constants.js";
import { createFirebaseGateway, fetchUserProfileFromAPI } from "../services.js";
import { refreshIcons } from "../utils/icons.js";
import { applyScopeFilter } from "../utils/reportScope.js";

const { computed, onMounted, ref, watch } = Vue;

const gateway = createFirebaseGateway();

/**
 * 画面全体の状態管理とイベント処理を提供するコンポーザブル。
 */
export function useAppController() {
  const user = ref(null);
  const userProfile = ref(null);
  const reports = ref([]);
  const loading = ref(true);
  const view = ref("list");
  const currentReport = ref(null);
  const notification = ref(null);

  let unwatchReports = null;

  /**
   * 一時通知メッセージを表示する。
   */
  function showNotification(message) {
    notification.value = message;
    // 一定時間後に自動で通知を閉じる。
    setTimeout(() => {
      notification.value = null;
    }, 3000);
  }

  onMounted(async () => {
    try {
      // Firebase 認証の初期化を先に完了させる。
      await gateway.initAuth();
      gateway.observeAuth(async (authUser) => {
        user.value = authUser;

        // 未ログイン時は一覧購読を開始せず描画だけ更新する。
        if (!authUser) {
          loading.value = false;
          refreshIcons();
          return;
        }

        // ログインユーザーの業務プロフィールを取得する。
        userProfile.value = await gateway.readProfile(authUser.uid);
        loading.value = false;
        refreshIcons();
      });
    } catch (error) {
      console.error("Auth init failed:", error);
      loading.value = false;
    }
  });

  watch([user, userProfile], ([authUser, profile]) => {
    // 既存購読がある場合は二重購読を防ぐため解除する。
    if (unwatchReports) {
      unwatchReports();
      unwatchReports = null;
    }
    // ユーザー情報が揃うまでは購読を開始しない。
    if (!authUser || !profile) return;

    // 権限スコープでフィルタした月報一覧をリアルタイム購読する。
    unwatchReports = gateway.observeReports(
      (allReports) => {
        // 対象データを月の降順で表示する。
        reports.value = applyScopeFilter(allReports, profile, authUser)
          .sort((a, b) => b.month.localeCompare(a.month));
        refreshIcons();
      },
      (error) => console.error("Snapshot error:", error),
    );
  });

  watch(view, () => {
    refreshIcons();
  });

  const stats = computed(() => ({
    total: reports.value.length,
    pending: reports.value.filter(
      (report) => report.authorId !== user.value?.uid && !report.adminFeedback,
    ).length,
  }));

  const isHighRank = computed(
    () => userProfile.value?.role === "OM" || userProfile.value?.role === "GL",
  );

  /**
   * ログイン後にプロフィールを保存し、画面状態を更新する。
   */
  async function handleLogin(loginData) {
    // 必須情報が不足している場合は処理しない。
    if (!loginData.employeeId || !loginData.password || !user.value) return;

    loading.value = true;
    try {
      // API でプロフィールを取得し、認証ユーザーに紐づけて保存する。
      const profile = await fetchUserProfileFromAPI(loginData.employeeId, loginData.password);
      await gateway.writeProfile(user.value.uid, profile);
      userProfile.value = profile;
      showNotification(`ログインしました: ${profile.name}`);
    } catch (error) {
      console.error("Login failed:", error);
      showNotification(error.message || "ログインエラー");
    } finally {
      loading.value = false;
      refreshIcons();
    }
  }

  /**
   * ログアウトして画面を初期化する。
   */
  async function handleLogout() {
    await gateway.logout();
    window.location.reload();
  }

  /**
   * 月報フォーム画面を開く。
   */
  function openForm(report = null) {
    currentReport.value = report;
    view.value = "form";
  }

  /**
   * 月報詳細画面を開く。
   */
  function openDetail(report) {
    currentReport.value = report;
    view.value = "detail";
  }

  /**
   * 月報を新規作成または更新する。
   */
  async function saveReport(formData) {
    // ログインとプロフィールが未確定なら保存できない。
    if (!user.value || !userProfile.value) return;

    const payload = {
      ...formData,
      authorId: user.value.uid,
      authorRole: userProfile.value.role,
      reporterName: userProfile.value.name,
      reporterId: userProfile.value.employeeId,
      office: userProfile.value.office,
      team: userProfile.value.team,
      updatedAt: gateway.serverTimestamp(),
    };

    try {
      // 編集対象があれば更新、なければ新規作成する。
      if (currentReport.value?.id) {
        await gateway.updateReport(currentReport.value.id, payload);
        showNotification("更新しました");
      } else {
        await gateway.createReport(payload);
        showNotification("提出しました");
      }
      view.value = "list";
    } catch (error) {
      console.error("Save report failed:", error);
    }
  }

  /**
   * 管理者回答を保存する。
   */
  async function saveFeedback(feedbackComment) {
    // 対象月報とユーザー情報がない場合は処理しない。
    if (!currentReport.value || !userProfile.value) return;

    const roleLabel = ROLES[userProfile.value.role]
      ? ROLES[userProfile.value.role].label
      : "管理者";

    const updateData = {
      adminFeedback: feedbackComment,
      adminFeedbackRole: roleLabel,
      adminFeedbackName: userProfile.value.name,
    };

    try {
      // 回答を永続化し、ローカル表示にも即時反映する。
      await gateway.updateFeedback(currentReport.value.id, updateData);
      Object.assign(currentReport.value, updateData);
      showNotification("回答を保存しました");
    } catch (error) {
      console.error("Save feedback failed:", error);
    }
  }

  /**
   * 月報を削除する。
   */
  async function deleteReport() {
    // 対象が未選択の場合は削除処理を行わない。
    if (!currentReport.value?.id) return;
    // ユーザー確認でキャンセルされた場合は終了する。
    if (!confirm("削除しますか？")) return;

    try {
      await gateway.removeReport(currentReport.value.id);
      view.value = "list";
      showNotification("削除しました");
    } catch (error) {
      console.error("Delete report failed:", error);
    }
  }

  return {
    user,
    userProfile,
    reports,
    loading,
    view,
    currentReport,
    notification,
    stats,
    isHighRank,
    handleLogin,
    handleLogout,
    openForm,
    openDetail,
    saveReport,
    saveFeedback,
    deleteReport,
  };
}

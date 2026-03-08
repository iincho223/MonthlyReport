import { ROLES } from "../constants.js";
import { createFirebaseGateway, fetchUserProfileFromAPI } from "../services.js";
import { refreshIcons } from "../utils/icons.js";
import { applyScopeFilter } from "../utils/reportScope.js";

const { computed, onMounted, ref, watch } = Vue;

const gateway = createFirebaseGateway();

export function useAppController() {
  const user = ref(null);
  const userProfile = ref(null);
  const reports = ref([]);
  const loading = ref(true);
  const view = ref("list");
  const currentReport = ref(null);
  const notification = ref(null);

  let unwatchReports = null;

  function showNotification(message) {
    notification.value = message;
    setTimeout(() => {
      notification.value = null;
    }, 3000);
  }

  onMounted(async () => {
    try {
      await gateway.initAuth();
      gateway.observeAuth(async (authUser) => {
        user.value = authUser;

        if (!authUser) {
          loading.value = false;
          refreshIcons();
          return;
        }

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
    if (unwatchReports) {
      unwatchReports();
      unwatchReports = null;
    }
    if (!authUser || !profile) return;

    unwatchReports = gateway.observeReports(
      (allReports) => {
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

  async function handleLogin(loginData) {
    if (!loginData.employeeId || !loginData.password || !user.value) return;

    loading.value = true;
    try {
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

  async function handleLogout() {
    await gateway.logout();
    window.location.reload();
  }

  function openForm(report = null) {
    currentReport.value = report;
    view.value = "form";
  }

  function openDetail(report) {
    currentReport.value = report;
    view.value = "detail";
  }

  async function saveReport(formData) {
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

  async function saveFeedback(feedbackComment) {
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
      await gateway.updateFeedback(currentReport.value.id, updateData);
      Object.assign(currentReport.value, updateData);
      showNotification("回答を保存しました");
    } catch (error) {
      console.error("Save feedback failed:", error);
    }
  }

  async function deleteReport() {
    if (!currentReport.value?.id) return;
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

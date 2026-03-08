(function () {
  const { createApp, computed, onMounted, reactive, ref, watch, nextTick } = Vue;

  const ROLES = {
    REPORTER: { id: "REPORTER", label: "報告者", rank: 0 },
    TL: { id: "TL", label: "TL (Team Leader)", rank: 1 },
    GL: { id: "GL", label: "GL (Group Leader)", rank: 2 },
    OM: { id: "OM", label: "OM (Ops Manager)", rank: 3 },
  };

  const CONDITION_ITEMS = [
    { id: "physical", label: "体調" },
    { id: "stress", label: "ストレス" },
    { id: "relationships", label: "人間関係" },
    { id: "worries", label: "悩み" },
    { id: "fatigue", label: "疲れ" },
    { id: "sleep", label: "睡眠" },
    { id: "motivation", label: "やる気" },
  ];

  function defaultReportForm() {
    return {
      month: new Date().toISOString().slice(0, 7),
      title: "",
      salesInfo: "特になし",
      nextMonthOvertime: "",
      nextMonthOvertimeReason: "",
      thisMonthOvertime: "",
      thisMonthOvertimeReason: "",
      condition: Object.fromEntries(CONDITION_ITEMS.map((item) => [item.id, "○"])),
      comments: "",
    };
  }

  const MOCK_API_DATA = {
    EMP001: { name: "田中 太郎", employeeId: "EMP001", password: "pass", role: "OM", office: "東京本社", team: "経営企画" },
    EMP002: { name: "鈴木 一郎", employeeId: "EMP002", password: "pass", role: "GL", office: "大阪支社", team: "関西第一営業部" },
    EMP003: { name: "佐藤 花子", employeeId: "EMP003", password: "pass", role: "TL", office: "東京本社", team: "チームA" },
    EMP004: { name: "山田 健太", employeeId: "EMP004", password: "pass", role: "REPORTER", office: "東京本社", team: "チームA" },
  };

  function refreshIcons() {
    nextTick(function () {
      if (window.lucide) {
        window.lucide.createIcons();
      }
    });
  }

  function getFirebaseConfig() {
    try {
      const raw = typeof __firebase_config !== "undefined" ? __firebase_config : "{}";
      return JSON.parse(raw);
    } catch (_) {
      return {};
    }
  }

  function getAppId() {
    return typeof __app_id !== "undefined" ? __app_id : "monthly-report-vue-pro";
  }

  function getInitialAuthToken() {
    return typeof __initial_auth_token !== "undefined" ? __initial_auth_token : null;
  }

  function hasValidFirebaseConfig(config) {
    return Boolean(
      config &&
        typeof config.apiKey === "string" &&
        config.apiKey.trim().length > 20 &&
        typeof config.authDomain === "string" &&
        config.authDomain.trim().length > 0 &&
        typeof config.projectId === "string" &&
        config.projectId.trim().length > 0,
    );
  }

  const LOCAL_STORE_KEY = "monthly-report-prototype-local-store";
  const LOCAL_UID_KEY = "monthly-report-prototype-local-uid";

  function loadLocalStore() {
    try {
      const raw = window.localStorage.getItem(LOCAL_STORE_KEY);
      if (!raw) return { profilesByUid: {}, reports: [] };
      const parsed = JSON.parse(raw);
      return {
        profilesByUid: parsed && parsed.profilesByUid ? parsed.profilesByUid : {},
        reports: Array.isArray(parsed && parsed.reports) ? parsed.reports : [],
      };
    } catch (_) {
      return { profilesByUid: {}, reports: [] };
    }
  }

  function saveLocalStore(store) {
    window.localStorage.setItem(LOCAL_STORE_KEY, JSON.stringify(store));
  }

  function createLocalUid() {
    return "local_" + Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  function createLocalReportId() {
    return "report_" + Date.now().toString(36) + Math.random().toString(36).slice(2, 8);
  }

  async function fetchUserProfileFromAPI(employeeId, password) {
    return new Promise(function (resolve, reject) {
      setTimeout(function () {
        const data = MOCK_API_DATA[String(employeeId || "").toUpperCase()];
        if (!data) {
          reject(new Error("該当する社員番号が見つかりません"));
          return;
        }
        if (data.password !== password) {
          reject(new Error("パスワードが間違っています"));
          return;
        }
        const userProfile = {
          name: data.name,
          employeeId: data.employeeId,
          role: data.role,
          office: data.office,
          team: data.team,
        };
        resolve(userProfile);
      }, 800);
    });
  }

  function applyScopeFilter(allReports, profile, authUser) {
    if (profile.role === "REPORTER") {
      return allReports.filter((report) => report.authorId === authUser.uid);
    }
    if (profile.role === "TL") {
      return allReports.filter((report) => report.authorId === authUser.uid || report.team === profile.team);
    }
    if (profile.role === "GL") {
      return allReports.filter((report) => report.authorId === authUser.uid || report.office === profile.office);
    }
    return allReports;
  }

  const LoginView = {
    props: ["loading"],
    emits: ["login"],
    setup: function (_, ctx) {
      const loginData = reactive({ employeeId: "", password: "" });
      function submit() {
        ctx.emit("login", { employeeId: loginData.employeeId, password: loginData.password });
      }
      return { loginData, submit };
    },
    template: `
      <div class="login-screen">
        <div class="login-card">
          <div class="login-header">
            <div class="login-icon-box"><i data-lucide="key" class="icon-lg"></i></div>
            <h2 class="login-title">月次報告ポータル</h2>
            <p class="login-subtitle">Vue.js Digital Workflow</p>
          </div>
          <form @submit.prevent="submit" class="form-stack">
            <div>
              <label class="field-label">社員番号</label>
              <input v-model="loginData.employeeId" class="input-shell input-strong" required type="text" placeholder="例: EMP001" />
            </div>
            <div>
              <label class="field-label">パスワード</label>
              <input v-model="loginData.password" class="input-shell input-strong" required type="password" placeholder="パスワードを入力" />
            </div>
            <button :disabled="loading" type="submit" class="action-btn action-primary action-full">
              <i data-lucide="log-in" class="icon-sm"></i> ログイン
            </button>
          </form>
        </div>
      </div>
    `,
  };

  const HeaderBar = {
    props: ["userProfile", "isHighRank"],
    emits: ["logout"],
    template: `
      <header class="app-header">
        <div class="container header-inner">
          <div class="brand-area">
            <div class="brand-icon"><i data-lucide="briefcase" class="icon-sm"></i></div>
            <div class="brand-copy">
              <h1 class="brand-title">月次報告ポータル</h1>
              <span class="brand-subtitle">Vue.js Digital Flow</span>
            </div>
          </div>
          <div class="user-area">
            <div class="user-meta">
              <div class="user-name">{{ userProfile?.name }} <span class="user-id">({{ userProfile?.employeeId }})</span></div>
              <div class="user-scope">{{ isHighRank ? userProfile?.office : userProfile?.team }} | {{ userProfile?.role }}</div>
            </div>
            <button @click="$emit('logout')" class="icon-btn" aria-label="logout"><i data-lucide="log-out" class="icon-sm"></i></button>
          </div>
        </div>
      </header>
    `,
  };

  const DashboardView = {
    props: ["reports", "stats", "userProfile"],
    emits: ["open-form", "open-detail"],
    template: `
      <main class="container page-stack">
        <section class="page-head">
          <div>
            <h2 class="page-title"><i data-lucide="layout-dashboard" class="icon-sm icon-accent"></i> ダッシュボード</h2>
            <p class="page-subtitle">Status Monitoring</p>
          </div>
          <button @click="$emit('open-form')" class="action-btn action-primary"><i data-lucide="plus" class="icon-sm"></i> 新規提出</button>
        </section>

        <section v-if="ROLES[userProfile?.role]?.rank > 0" class="kpi-grid">
          <article class="section-card kpi-card">
            <div class="kpi-label">管理内 全提出</div>
            <div class="kpi-value">{{ stats.total }}</div>
          </article>
          <article class="section-card kpi-card kpi-card-alert">
            <div class="kpi-label kpi-label-alert">未回答の報告</div>
            <div class="kpi-value kpi-value-alert">{{ stats.pending }}</div>
          </article>
        </section>

        <section v-if="reports.length === 0" class="section-card empty-card">
          <i data-lucide="file-text" class="icon-xl icon-muted"></i>
          <h3 class="empty-title">報告書が見つかりません</h3>
        </section>

        <section v-else class="report-grid">
          <article v-for="report in reports" :key="report.id" @click="$emit('open-detail', report)" class="section-card report-card">
            <div class="report-card-head">
              <div class="report-month-chip">{{ report.month }}</div>
              <span :class="report.adminFeedback ? 'status-chip status-checked' : 'status-chip status-waiting'">{{ report.adminFeedback ? 'Checked' : 'Waiting' }}</span>
            </div>
            <h3 class="report-title">{{ report.title || '無題の報告' }}</h3>
            <div class="report-card-foot">
              <span class="report-author">{{ report.reporterName || '匿名' }}</span>
              <i data-lucide="chevron-right" class="icon-xs"></i>
            </div>
          </article>
        </section>
      </main>
    `,
    setup: function () {
      return { ROLES };
    },
  };

  const ReportFormView = {
    props: ["editingReport"],
    emits: ["save", "cancel"],
    setup: function (props, ctx) {
      const formData = reactive(defaultReportForm());

      watch(
        function () {
          return props.editingReport;
        },
        function (editing) {
          if (editing) {
            Object.assign(formData, JSON.parse(JSON.stringify(editing)));
            return;
          }
          Object.assign(formData, defaultReportForm());
        },
        { immediate: true },
      );

      function save() {
        ctx.emit("save", { ...formData });
      }

      return { formData, save, CONDITION_ITEMS };
    },
    template: `
      <section class="form-page container">
        <div class="form-head">
          <button @click="$emit('cancel')" class="icon-btn icon-btn-light"><i data-lucide="chevron-left" class="icon-md"></i></button>
          <h2 class="form-title">月次報告書の{{ editingReport ? '編集' : '作成' }}</h2>
        </div>

        <div class="form-stack-blocks">
          <article class="section-card form-block">
            <div class="two-col-grid">
              <div>
                <label class="field-label">報告月</label>
                <input v-model="formData.month" type="month" class="input-shell input-strong" />
              </div>
              <div>
                <label class="field-label">タイトル</label>
                <input v-model="formData.title" type="text" class="input-shell input-strong" placeholder="例: 今月の業務報告" />
              </div>
            </div>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">営業情報</h3>
            <textarea v-model="formData.salesInfo" rows="3" class="input-shell"></textarea>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">残業報告</h3>
            <div class="two-col-grid">
              <div class="inline-panel">
                <label class="field-label">来月残業見込み</label>
                <input v-model="formData.nextMonthOvertime" type="number" class="input-shell" placeholder="時間" />
                <input v-model="formData.nextMonthOvertimeReason" type="text" class="input-shell" placeholder="理由" />
              </div>
              <div class="inline-panel">
                <label class="field-label">今月残業見込み</label>
                <input v-model="formData.thisMonthOvertime" type="number" class="input-shell" placeholder="時間" />
                <input v-model="formData.thisMonthOvertimeReason" type="text" class="input-shell" placeholder="理由" />
              </div>
            </div>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">コンディション自己診断</h3>
            <div class="condition-grid">
              <div v-for="item in CONDITION_ITEMS" :key="item.id" class="condition-item">
                <span class="condition-label">{{ item.label }}</span>
                <div class="condition-switch">
                  <button v-for="opt in ['○', '△', '×']" :key="opt" type="button" @click="formData.condition[item.id] = opt" :class="formData.condition[item.id] === opt ? 'condition-btn is-active' : 'condition-btn'">{{ opt }}</button>
                </div>
              </div>
            </div>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">コメント（問題点・困っていること）</h3>
            <textarea v-model="formData.comments" rows="5" class="input-shell" placeholder="意見や悩みなどがあれば"></textarea>
          </article>
        </div>

        <div class="form-actions-fixed">
          <div class="form-actions-inner">
            <button @click="$emit('cancel')" class="action-btn action-secondary action-fill">キャンセル</button>
            <button @click="save" :disabled="!formData.title" class="action-btn action-primary action-fill">報告書を提出</button>
          </div>
        </div>
      </section>
    `,
  };

  const ReportDetailView = {
    props: ["user", "userProfile", "report"],
    emits: ["back", "edit", "delete", "save-feedback"],
    setup: function (props, ctx) {
      const feedbackText = ref("");

      watch(
        function () {
          return props.report;
        },
        function (nextReport) {
          feedbackText.value = (nextReport && nextReport.adminFeedback) || "";
        },
        { immediate: true },
      );

      const canFeedback = computed(function () {
        if (!props.userProfile || !props.report) return false;
        const rank = ROLES[props.userProfile.role] ? ROLES[props.userProfile.role].rank : 0;
        return rank > 0 && props.report.authorId !== (props.user && props.user.uid);
      });

      function saveFeedback() {
        ctx.emit("save-feedback", feedbackText.value);
      }

      return { feedbackText, canFeedback, saveFeedback, CONDITION_ITEMS };
    },
    template: `
      <section v-if="report" class="container detail-page">
        <div class="detail-head">
          <button @click="$emit('back')" class="icon-btn icon-btn-light"><i data-lucide="chevron-left" class="icon-md"></i></button>
          <div class="detail-actions">
            <button v-if="report.authorId === user?.uid" @click="$emit('edit')" class="action-btn action-soft-primary">編集</button>
            <button v-if="report.authorId === user?.uid || userProfile?.role === 'OM'" @click="$emit('delete')" class="action-btn action-soft-danger">削除</button>
          </div>
        </div>

        <div class="detail-grid">
          <article class="section-card detail-main">
            <h2 class="detail-title">{{ report.title || '報告書' }}</h2>
            <p class="detail-meta">{{ report.month }} / {{ report.office }} / {{ report.team }}</p>

            <section class="detail-section">
              <h3 class="detail-section-title">営業情報</h3>
              <p class="detail-paragraph">{{ report.salesInfo || '特になし' }}</p>
            </section>

            <section class="detail-overtime-grid">
              <div class="inline-panel">
                <div class="field-label">来月残業見込み</div>
                <div class="metric-value">{{ report.nextMonthOvertime || '0' }}h</div>
                <p class="panel-note">理由: {{ report.nextMonthOvertimeReason || '特になし' }}</p>
              </div>
              <div class="inline-panel">
                <div class="field-label">今月残業見込み</div>
                <div class="metric-value">{{ report.thisMonthOvertime || '0' }}h</div>
                <p class="panel-note">理由: {{ report.thisMonthOvertimeReason || '特になし' }}</p>
              </div>
            </section>

            <section class="detail-section">
              <h3 class="detail-section-title detail-section-title-muted">コンディション診断</h3>
              <div class="condition-mini-grid">
                <div v-for="item in CONDITION_ITEMS" :key="item.id" class="condition-mini-item">
                  <div class="condition-mini-label">{{ item.label }}</div>
                  <div class="condition-mini-value">{{ report.condition?.[item.id] || '○' }}</div>
                </div>
              </div>
            </section>

            <section class="detail-section">
              <h3 class="detail-section-title detail-section-title-muted">本人コメント</h3>
              <p class="detail-paragraph">{{ report.comments || '特になし' }}</p>
            </section>
          </article>

          <aside class="section-card detail-side">
            <h3 class="block-title">アドバイザー回答</h3>
            <div v-if="canFeedback" class="form-stack">
              <textarea v-model="feedbackText" rows="6" class="input-shell input-strong" placeholder="フィードバックを記入..."></textarea>
              <button @click="saveFeedback" class="action-btn action-warning action-full">回答を公開</button>
            </div>
            <div v-else class="detail-paragraph">{{ report.adminFeedback || '管理者の回答を待っています。' }}</div>
          </aside>
        </div>
      </section>
    `,
  };

  const NotificationToast = {
    props: ["notification"],
    template: `<div v-if="notification" class="notification-toast"><i data-lucide="check-circle-2" class="icon-md icon-success"></i><span class="notification-text">{{ notification }}</span></div>`,
  };

  const App = {
    components: {
      LoginView,
      HeaderBar,
      DashboardView,
      ReportFormView,
      ReportDetailView,
      NotificationToast,
    },
    setup: function () {
      const firebaseConfig = getFirebaseConfig();
      const appId = getAppId();
      const initialAuthToken = getInitialAuthToken();
      const useFirebase = typeof firebase !== "undefined" && hasValidFirebaseConfig(firebaseConfig);

      const firebaseApp = useFirebase ? firebase.initializeApp(firebaseConfig) : null;
      const auth = useFirebase ? firebase.auth(firebaseApp) : null;
      const db = useFirebase ? firebase.firestore(firebaseApp) : null;

      const localStore = loadLocalStore();
      const localAuthState = { currentUser: null };
      const localAuthListeners = [];
      const localReportListeners = [];

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
        setTimeout(function () {
          notification.value = null;
        }, 3000);
      }

      function profileRefByUid(uid) {
        if (!db) return null;
        return db
          .collection("artifacts")
          .doc(appId)
          .collection("users")
          .doc(uid)
          .collection("profile")
          .doc("data");
      }

      function reportsCollectionRef() {
        if (!db) return null;
        return db
          .collection("artifacts")
          .doc(appId)
          .collection("public")
          .doc("data")
          .collection("reports");
      }

      function emitLocalAuthChanged() {
        localAuthListeners.forEach(function (listener) {
          listener(localAuthState.currentUser);
        });
      }

      function onAuthChanged(listener) {
        if (auth) {
          return auth.onAuthStateChanged(listener);
        }
        localAuthListeners.push(listener);
        listener(localAuthState.currentUser);
        return function () {
          const idx = localAuthListeners.indexOf(listener);
          if (idx >= 0) localAuthListeners.splice(idx, 1);
        };
      }

      function emitLocalReports() {
        const cloned = localStore.reports.map(function (report) {
          return { ...report };
        });
        localReportListeners.forEach(function (listener) {
          listener(cloned);
        });
      }

      async function getProfile(uid) {
        if (db) {
          const snapshot = await profileRefByUid(uid).get();
          return snapshot.exists ? snapshot.data() : null;
        }
        return localStore.profilesByUid[uid] || null;
      }

      async function setProfile(uid, profile) {
        if (db) {
          await profileRefByUid(uid).set(profile);
          return;
        }
        localStore.profilesByUid[uid] = { ...profile };
        saveLocalStore(localStore);
      }

      function subscribeReports(listener, onError) {
        if (db) {
          return reportsCollectionRef().onSnapshot(
            function (snapshot) {
              const allReports = snapshot.docs.map(function (reportDoc) {
                return { id: reportDoc.id, ...reportDoc.data() };
              });
              listener(allReports);
            },
            onError,
          );
        }

        localReportListeners.push(listener);
        listener(localStore.reports.map(function (report) {
          return { ...report };
        }));
        return function () {
          const idx = localReportListeners.indexOf(listener);
          if (idx >= 0) localReportListeners.splice(idx, 1);
        };
      }

      async function addReport(payload) {
        if (db) {
          await reportsCollectionRef().add(payload);
          return;
        }
        localStore.reports.push({ id: createLocalReportId(), ...payload });
        saveLocalStore(localStore);
        emitLocalReports();
      }

      async function updateReport(id, payload) {
        if (db) {
          await reportsCollectionRef().doc(id).update(payload);
          return;
        }
        localStore.reports = localStore.reports.map(function (report) {
          return report.id === id ? { ...report, ...payload } : report;
        });
        saveLocalStore(localStore);
        emitLocalReports();
      }

      async function removeReport(id) {
        if (db) {
          await reportsCollectionRef().doc(id).delete();
          return;
        }
        localStore.reports = localStore.reports.filter(function (report) {
          return report.id !== id;
        });
        saveLocalStore(localStore);
        emitLocalReports();
      }

      async function initAuth() {
        if (auth) {
          if (initialAuthToken) {
            await auth.signInWithCustomToken(initialAuthToken);
            return;
          }
          await auth.signInAnonymously();
          return;
        }

        let uid = window.localStorage.getItem(LOCAL_UID_KEY);
        if (!uid) {
          uid = createLocalUid();
          window.localStorage.setItem(LOCAL_UID_KEY, uid);
        }
        localAuthState.currentUser = { uid: uid, isAnonymous: true };
        emitLocalAuthChanged();
      }

      async function handleLogin(loginData) {
        if (!loginData.employeeId || !loginData.password || !user.value) return;
        loading.value = true;
        try {
          const profile = await fetchUserProfileFromAPI(loginData.employeeId, loginData.password);
          await setProfile(user.value.uid, profile);
          userProfile.value = profile;
          showNotification("ログインしました: " + profile.name);
        } catch (error) {
          showNotification(error.message || "ログインエラー");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      async function handleLogout() {
        if (auth) {
          await auth.signOut();
        } else {
          localAuthState.currentUser = null;
          emitLocalAuthChanged();
        }
        window.location.reload();
      }

      function openForm(report) {
        currentReport.value = report || null;
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
          updatedAt: db ? firebase.firestore.FieldValue.serverTimestamp() : new Date().toISOString(),
        };

        if (currentReport.value && currentReport.value.id) {
          await updateReport(currentReport.value.id, payload);
          showNotification("更新しました");
        } else {
          await addReport({
            ...payload,
            createdAt: db ? firebase.firestore.FieldValue.serverTimestamp() : new Date().toISOString(),
            adminFeedback: "",
            adminFeedbackRole: "",
            adminFeedbackName: "",
          });
          showNotification("提出しました");
        }
        view.value = "list";
      }

      async function saveFeedback(feedbackComment) {
        if (!currentReport.value || !userProfile.value) return;
        const roleLabel = ROLES[userProfile.value.role] ? ROLES[userProfile.value.role].label : "管理者";
        const updateData = {
          adminFeedback: feedbackComment,
          adminFeedbackRole: roleLabel,
          adminFeedbackName: userProfile.value.name,
          adminFeedbackAt: db ? firebase.firestore.FieldValue.serverTimestamp() : new Date().toISOString(),
        };
        await updateReport(currentReport.value.id, updateData);
        Object.assign(currentReport.value, updateData);
        showNotification("回答を保存しました");
      }

      async function deleteReport() {
        if (!currentReport.value || !currentReport.value.id) return;
        if (!confirm("削除しますか？")) return;
        await removeReport(currentReport.value.id);
        showNotification("削除しました");
        view.value = "list";
      }

      onMounted(async function () {
        try {
          await initAuth();
          onAuthChanged(async function (authUser) {
            user.value = authUser;
            if (!authUser) {
              loading.value = false;
              refreshIcons();
              return;
            }

            userProfile.value = await getProfile(authUser.uid);
            loading.value = false;
            refreshIcons();
          });
        } catch (e) {
          loading.value = false;
          showNotification("認証初期化に失敗しました");
        }
      });

      watch([user, userProfile], function (values) {
        const authUser = values[0];
        const profile = values[1];

        if (unwatchReports) {
          unwatchReports();
          unwatchReports = null;
        }
        if (!authUser || !profile) return;

        unwatchReports = subscribeReports(
          function (allReports) {
            reports.value = applyScopeFilter(allReports, profile, authUser).sort(function (a, b) {
              return b.month.localeCompare(a.month);
            });
            refreshIcons();
          },
          function () {
            showNotification("一覧の取得に失敗しました");
          },
        );
      });

      watch(view, function () {
        refreshIcons();
      });

      const stats = computed(function () {
        return {
          total: reports.value.length,
          pending: reports.value.filter(function (report) {
            return report.authorId !== (user.value && user.value.uid) && !report.adminFeedback;
          }).length,
        };
      });

      const isHighRank = computed(function () {
        return userProfile.value && (userProfile.value.role === "OM" || userProfile.value.role === "GL");
      });

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
    },
    template: `
      <div class="screen-shell">
        <div v-if="loading" class="loading-screen">
          <div class="loading-spinner"><i data-lucide="loader-2" class="icon-xl"></i></div>
          <p class="loading-text">Connecting to Database</p>
        </div>

        <LoginView v-else-if="!userProfile" :loading="loading" @login="handleLogin" />

        <div v-else>
          <HeaderBar :user-profile="userProfile" :is-high-rank="isHighRank" @logout="handleLogout" />

          <DashboardView
            v-if="view === 'list'"
            :reports="reports"
            :stats="stats"
            :user-profile="userProfile"
            @open-form="openForm()"
            @open-detail="openDetail"
          />

          <ReportFormView
            v-if="view === 'form'"
            :editing-report="currentReport"
            @save="saveReport"
            @cancel="view = 'list'"
          />

          <ReportDetailView
            v-if="view === 'detail'"
            :user="user"
            :user-profile="userProfile"
            :report="currentReport"
            @back="view = 'list'"
            @edit="openForm(currentReport)"
            @delete="deleteReport"
            @save-feedback="saveFeedback"
          />

          <NotificationToast :notification="notification" />
        </div>
      </div>
    `,
  };

  createApp(App).mount("#app");
})();

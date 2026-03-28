(function () {
  const { createApp, computed, onMounted, reactive, ref, watch, nextTick } = Vue;

  const ROLES = {
    NG: { id: "NG", label: "新卒", rank: 0 },
    TM: { id: "TM", label: "メンバー", rank: 0 },
    TL: { id: "TL", label: "チームリーダー", rank: 1 },
    GL: { id: "GL", label: "グループリーダー", rank: 2 },
    OM: { id: "OM", label: "オフィスマネージャー", rank: 3 },
    SP: { id: "SP", label: "営業担当者", rank: 1 },
    SM: { id: "SM", label: "支店長", rank: 2 },
    SA: { id: "SA", label: "システム管理者", rank: 4 },
  };

  const OFFICE_LABELS = {
    TOKYO: "東京本社",
    OSAKA: "大阪支社",
  };

  const TEAM_LABELS = {
    HQ: "経営企画",
    SALES_WEST: "関西第一営業部",
    TEAM_A: "チームA",
  };

  const AUTH_TOKEN_KEY = "authToken";
  const REFRESH_TOKEN_KEY = "refreshToken";
  const API_BASE_URL = window.location.protocol === "file:"
    ? "http://localhost:8080/api/v1"
    : "/api/v1";

  let sessionExpiredHandler = null;

  const apiClient = window.axios.create({
    baseURL: API_BASE_URL,
    headers: {
      "Content-Type": "application/json",
    },
  });

  apiClient.interceptors.request.use(function (config) {
    const token = window.localStorage.getItem(AUTH_TOKEN_KEY);
    if (token) {
      config.headers.Authorization = "Bearer " + token;
    }
    return config;
  });

  apiClient.interceptors.response.use(
    function (response) {
      const data = response && response.data ? response.data : {};
      if (data.resultStatus !== "0") {
        return Promise.reject(new Error(data.resultMsg || "API エラーが発生しました"));
      }
      return data.params || {};
    },
    function (error) {
      if (error && error.response && error.response.status === 401 && typeof sessionExpiredHandler === "function") {
        sessionExpiredHandler();
      }
      if (error && error.response && error.response.data && error.response.data.resultMsg) {
        return Promise.reject(new Error(error.response.data.resultMsg));
      }
      return Promise.reject(error);
    }
  );

  function registerSessionExpiredHandler(handler) {
    sessionExpiredHandler = handler;
  }

  function roleLabel(role) {
    return ROLES[role] ? ROLES[role].label : role || "-";
  }

  function officeLabel(code) {
    return OFFICE_LABELS[code] || code || "-";
  }

  function teamLabel(code) {
    return TEAM_LABELS[code] || code || "-";
  }

  function isEscalationRole(role) {
    return role !== "NG" && role !== "TM";
  }

  function isFeedbackRole(role) {
    return role === "TL" || role === "GL" || role === "OM";
  }

  function isMaskedRole(role) {
    return role === "SA";
  }

  function maskText(value) {
    if (value == null || value === "") return value;
    return "マスク表示";
  }

  function formatDateTime(value) {
    if (!value) return "";
    return String(value).replace("T", " ").replace("Z", "").slice(0, 16);
  }

  function enrichProfile(profile) {
    if (!profile) return null;
    const enriched = {
      userId: profile.userId,
      employeeId: profile.employeeNo || profile.employeeId,
      name: profile.name,
      role: profile.role,
      roleLabel: roleLabel(profile.role),
      officeCode: profile.officeCode,
      office: officeLabel(profile.officeCode),
      teamCode: profile.teamCode,
      team: teamLabel(profile.teamCode),
    };
    enriched.scopeLabel = ["GL", "OM", "SM", "SA"].includes(enriched.role) ? enriched.office : enriched.team;
    return enriched;
  }

  function conditionLabelFromApi(apiValue) {
    const option = CONDITION_OPTIONS.find(function (item) {
      return item.api === apiValue;
    });
    return option ? option.label : "○";
  }

  function conditionApiFromLabel(label) {
    const option = CONDITION_OPTIONS.find(function (item) {
      return item.label === label;
    });
    return option ? option.api : "GOOD";
  }

  function toMaskedName(name, masked) {
    return masked ? maskText(name) : name;
  }

  function mapReportSummary(item, masked) {
    return {
      id: item.reportId,
      reportId: item.reportId,
      month: item.month,
      title: masked ? maskText(item.title) : item.title,
      reporterName: toMaskedName(item.reporterName, masked),
      reporterId: item.reporterId,
      authorRole: item.authorRole,
      officeCode: item.officeCode,
      office: officeLabel(item.officeCode),
      teamCode: item.teamCode,
      team: teamLabel(item.teamCode),
      feedbackRegistered: Boolean(item.feedbackRegistered),
      adminFeedback: Boolean(item.feedbackRegistered),
      updatedAt: formatDateTime(item.updatedAt),
    };
  }

  function mapReportDetail(item, masked) {
    const author = item.author || {};
    const feedback = item.feedback || {};
    const conditions = item.conditions || {};
    return {
      id: item.reportId,
      reportId: item.reportId,
      month: item.month,
      title: masked ? maskText(item.title) : item.title,
      salesInfo: masked ? maskText(item.salesInfo) : item.salesInfo,
      nextMonthOvertime: item.nextMonthOvertimeHours == null ? "" : String(item.nextMonthOvertimeHours),
      nextMonthOvertimeReason: masked ? maskText(item.nextMonthOvertimeReason) : item.nextMonthOvertimeReason,
      thisMonthOvertime: item.thisMonthOvertimeHours == null ? "" : String(item.thisMonthOvertimeHours),
      thisMonthOvertimeReason: masked ? maskText(item.thisMonthOvertimeReason) : item.thisMonthOvertimeReason,
      condition: Object.fromEntries(CONDITION_ITEMS.map(function (conditionItem) {
        return [conditionItem.id, conditionLabelFromApi(conditions[conditionItem.id])];
      })),
      comments: masked ? maskText(item.comments) : item.comments,
      reporterName: toMaskedName(author.name, masked),
      reporterId: author.employeeNo,
      authorRole: author.role,
      officeCode: author.officeCode,
      office: officeLabel(author.officeCode),
      teamCode: author.teamCode,
      team: teamLabel(author.teamCode),
      adminFeedback: masked ? maskText(feedback.feedbackComment) : feedback.feedbackComment,
      adminFeedbackRole: feedback.responderRole,
      adminFeedbackName: toMaskedName(feedback.responderName, masked),
      adminFeedbackAt: formatDateTime(feedback.respondedAt),
      updatedAt: formatDateTime(item.updatedAt),
    };
  }

  function toReportPayload(formData) {
    return {
      month: formData.month,
      title: formData.title,
      salesInfo: formData.salesInfo,
      nextMonthOvertimeHours: Number(formData.nextMonthOvertime || 0),
      nextMonthOvertimeReason: formData.nextMonthOvertimeReason,
      thisMonthOvertimeHours: Number(formData.thisMonthOvertime || 0),
      thisMonthOvertimeReason: formData.thisMonthOvertimeReason,
      conditions: Object.fromEntries(CONDITION_ITEMS.map(function (item) {
        return [item.id, conditionApiFromLabel(formData.condition[item.id])];
      })),
      comments: formData.comments,
    };
  }

  function mapEscalationSummary(item, masked) {
    return {
      id: item.escalationId,
      escalationId: item.escalationId,
      title: masked ? maskText(item.title) : item.title,
      targetEmployeeName: masked ? maskText(item.targetEmployeeName) : item.targetEmployeeName,
      targetTeam: masked ? maskText(item.targetTeam) : item.targetTeam,
      severity: item.severity,
      status: item.status,
      createdByName: toMaskedName(item.createdByName, masked),
      updatedAt: formatDateTime(item.updatedAt),
      logs: [],
    };
  }

  function mapEscalationDetail(item, base, masked) {
    return {
      id: item.escalationId,
      escalationId: item.escalationId,
      title: masked ? maskText(item.title) : item.title,
      targetEmployeeName: masked ? maskText(item.targetEmployeeName) : item.targetEmployeeName,
      targetTeam: masked ? maskText(item.targetTeam) : item.targetTeam,
      description: masked ? maskText(item.description) : item.description,
      severity: item.severity,
      status: item.status,
      createdByName: toMaskedName(item.createdByName, masked),
      createdByRole: item.createdByRole,
      createdAt: base && base.createdAt ? base.createdAt : formatDateTime(item.updatedAt),
      updatedAt: formatDateTime(item.updatedAt),
      logs: (item.history || []).map(function (log) {
        return {
          logId: log.logId,
          authorName: toMaskedName(log.author, masked),
          logText: masked ? maskText(log.text) : log.text,
          createdAt: formatDateTime(log.logDate),
        };
      }),
    };
  }

  const api = {
    login: function (payload) {
      return apiClient.post("/auth/login", {
        employeeNo: String(payload.employeeId || "").trim().toUpperCase(),
        password: payload.password,
      });
    },
    me: function () {
      return apiClient.post("/users/me", {});
    },
    logout: function () {
      return apiClient.post("/auth/logout", {});
    },
    dashboardSummary: function (month) {
      return apiClient.post("/dashboard/summary", { month: month });
    },
    searchReports: function () {
      return apiClient.post("/reports/search", { month: "", status: "ALL", page: 1, size: 100 });
    },
    reportDetail: function (reportId) {
      return apiClient.post("/reports/detail", { reportId: reportId });
    },
    createReport: function (payload) {
      return apiClient.post("/reports/create", payload);
    },
    updateReport: function (payload) {
      return apiClient.post("/reports/update", payload);
    },
    deleteReport: function (reportId) {
      return apiClient.post("/reports/delete", { reportId: reportId });
    },
    updateFeedback: function (reportId, feedbackComment) {
      return apiClient.post("/reports/feedback/update", { reportId: reportId, feedbackComment: feedbackComment });
    },
    searchEscalations: function () {
      return apiClient.post("/escalations/search", { status: "ALL", page: 1, size: 100 });
    },
    escalationDetail: function (escalationId) {
      return apiClient.post("/escalations/detail", { escalationId: escalationId });
    },
    createEscalation: function (payload) {
      return apiClient.post("/escalations/create", payload);
    },
    updateEscalation: function (payload) {
      return apiClient.post("/escalations/update", payload);
    },
    addEscalationLog: function (escalationId, logText) {
      return apiClient.post("/escalations/log/add", { escalationId: escalationId, logText: logText });
    },
  };

  // 提出期限: 毎月5日
  const DEADLINE_DAY = 5;

  const CONDITION_ITEMS = [
    { id: "physical", label: "体調" },
    { id: "stress", label: "ストレス" },
    { id: "relationships", label: "人間関係" },
    { id: "worries", label: "悩み" },
    { id: "fatigue", label: "疲れ" },
    { id: "sleep", label: "睡眠" },
    { id: "motivation", label: "やる気" },
  ];

  // コンディション選択肢（UI表示ラベル → API送信値マッピング）
  const CONDITION_OPTIONS = [
    { label: "◎", desc: "絶好調",     api: "BEST", colorClass: "is-active-best" },
    { label: "○", desc: "まぁまぁ普通", api: "GOOD", colorClass: "is-active-good" },
    { label: "▲", desc: "ちょっと不調", api: "WARN", colorClass: "is-active-warn" },
    { label: "×", desc: "もうだめ最悪", api: "NG",   colorClass: "is-active-ng"   },
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
      // デフォルトは「○（まぁまぁ普通）」
      condition: Object.fromEntries(CONDITION_ITEMS.map((item) => [item.id, "○"])),
      comments: "",
    };
  }

  function refreshIcons() {
    nextTick(function () {
      if (window.lucide) {
        window.lucide.createIcons();
      }
    });
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
    props: ["userProfile", "isHighRank", "canEscalate"],
    emits: ["logout", "open-escalation"],
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
              <div class="user-scope">{{ userProfile?.scopeLabel }} | {{ userProfile?.roleLabel }}</div>
            </div>
            <button v-if="canEscalate" @click="$emit('open-escalation')" class="icon-btn esc-nav-btn" title="エスカレーション" aria-label="エスカレーション一覧"><i data-lucide="alert-triangle" class="icon-sm"></i></button>
            <button @click="$emit('logout')" class="icon-btn" aria-label="logout"><i data-lucide="log-out" class="icon-sm"></i></button>
          </div>
        </div>
      </header>
    `,
  };

  const DashboardView = {
    props: ["reports", "stats", "userProfile", "unsubmittedMembers", "submissionRate", "currentMonthStr"],
    emits: ["open-form", "open-detail"],
    template: `
      <main class="container page-stack">
        <section class="page-head">
          <div>
            <h2 class="page-title"><i data-lucide="layout-dashboard" class="icon-sm icon-accent"></i> ダッシュボード</h2>
            <p class="page-subtitle">Status Monitoring</p>
          </div>
          <button v-if="userProfile?.role !== 'NG'" @click="$emit('open-form')" class="action-btn action-primary"><i data-lucide="plus" class="icon-sm"></i> 新規提出</button>
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
          <article class="section-card kpi-card">
            <div class="kpi-label">今月提出完了率</div>
            <div class="kpi-value" :class="submissionRate < 100 ? 'kpi-value-alert' : 'kpi-value-ok'">{{ submissionRate }}%</div>
            <div class="submission-bar-wrap"><div class="submission-bar" :style="{ width: submissionRate + '%' }"></div></div>
          </article>
        </section>

        <!-- 未提出者モニタリング -->
        <section v-if="ROLES[userProfile?.role]?.rank > 0 && unsubmittedMembers.length > 0" class="section-card unsubmitted-panel">
          <h3 class="block-title unsubmitted-title">
            <i data-lucide="users" class="icon-sm icon-accent"></i>
            未提出メンバー ({{ currentMonthStr }})
          </h3>
          <div class="unsubmitted-grid">
            <div v-for="m in unsubmittedMembers" :key="m.employeeNo" class="unsubmitted-chip">
              <div class="unsubmitted-avatar">{{ m.name.charAt(0) }}</div>
              <div>
                <div class="unsubmitted-name">{{ m.name }}</div>
                <div class="unsubmitted-id">{{ m.employeeNo }}</div>
              </div>
            </div>
          </div>
        </section>
        <section v-else-if="ROLES[userProfile?.role]?.rank > 0 && unsubmittedMembers.length === 0" class="section-card kpi-card" style="padding:0.8rem 1.2rem;">
          <span class="kpi-label" style="color:#10b981">✨ 全員提出済みです！</span>
        </section>

        <section v-if="reports.length === 0" class="section-card empty-card">
          <i data-lucide="file-text" class="icon-xl icon-muted"></i>
          <h3 class="empty-title">報告書が見つかりません</h3>
        </section>

        <section v-else class="report-grid">
          <article v-for="report in reports" :key="report.id" @click="$emit('open-detail', report)" class="section-card report-card">
            <div class="report-card-head">
              <div class="report-month-chip">{{ report.month }}</div>
              <span :class="report.feedbackRegistered ? 'status-chip status-checked' : 'status-chip status-waiting'">{{ report.feedbackRegistered ? 'Checked' : 'Waiting' }}</span>
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

      return { formData, save, CONDITION_ITEMS, CONDITION_OPTIONS };
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
                  <button v-for="opt in CONDITION_OPTIONS" :key="opt.label" type="button" @click="formData.condition[item.id] = opt.label" :class="formData.condition[item.id] === opt.label ? 'condition-btn is-active ' + opt.colorClass : 'condition-btn'" :title="opt.desc">{{ opt.label }}</button>
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
        return isFeedbackRole(props.userProfile.role) && props.report.reporterId !== props.userProfile.employeeId;
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
            <button v-if="report.reporterId === userProfile?.employeeId" @click="$emit('edit')" class="action-btn action-soft-primary">編集</button>
            <button v-if="report.reporterId === userProfile?.employeeId || userProfile?.role === 'OM'" @click="$emit('delete')" class="action-btn action-soft-danger">削除</button>
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

  // ===== エスカレーション: 一覧 =====
  const EscalationListView = {
    props: ["escalations", "userProfile"],
    emits: ["new-escalation", "open-escalation", "back"],
    setup: function () {
      const SEVERITY_LABELS = { HIGH: "高", MEDIUM: "中", LOW: "低" };
      const STATUS_LABELS = { PENDING: "未対応", ONGOING: "対応中", RESOLVED: "解決済み" };
      return { SEVERITY_LABELS, STATUS_LABELS };
    },
    template: `
      <main class="container page-stack">
        <section class="page-head">
          <div>
            <button @click="$emit('back')" class="icon-btn icon-btn-light" style="margin-bottom:0.4rem"><i data-lucide="chevron-left" class="icon-md"></i></button>
            <h2 class="page-title"><i data-lucide="alert-triangle" class="icon-sm icon-accent"></i> エスカレーション</h2>
            <p class="page-subtitle">Escalation Management</p>
          </div>
          <button @click="$emit('new-escalation')" class="action-btn action-primary">
            <i data-lucide="plus" class="icon-sm"></i> 新規起票
          </button>
        </section>

        <section v-if="escalations.length === 0" class="section-card empty-card">
          <i data-lucide="check-circle-2" class="icon-xl icon-muted"></i>
          <h3 class="empty-title">エスカレーションはありません</h3>
        </section>

        <section v-else class="esc-list">
          <article v-for="esc in escalations" :key="esc.id" @click="$emit('open-escalation', esc)" class="section-card esc-card">
            <div class="esc-card-header">
              <span :class="'esc-severity-badge severity-' + esc.severity.toLowerCase()">{{ SEVERITY_LABELS[esc.severity] }}</span>
              <span :class="'esc-status-badge status-' + esc.status.toLowerCase()">{{ STATUS_LABELS[esc.status] }}</span>
              <span class="esc-date">{{ esc.updatedAt }}</span>
            </div>
            <h3 class="esc-card-title">{{ esc.title }}</h3>
            <div class="esc-target">
              <i data-lucide="user" class="icon-xs"></i>
              {{ esc.targetEmployeeName }} ({{ esc.targetTeam }})
            </div>
            <div class="esc-footer">
              <span class="esc-created-by">起票: {{ esc.createdByName }}</span>
              <i data-lucide="chevron-right" class="icon-xs"></i>
            </div>
          </article>
        </section>
      </main>
    `,
  };

  // ===== エスカレーション: 起票・編集フォーム =====
  const EscalationFormView = {
    props: ["editingEscalation"],
    emits: ["save", "cancel"],
    setup: function (props, ctx) {
      const formData = reactive({
        title: "",
        targetEmployeeName: "",
        targetTeam: "",
        description: "",
        severity: "MEDIUM",
        status: "PENDING",
      });

      watch(
        function () { return props.editingEscalation; },
        function (esc) {
          if (esc) {
            Object.assign(formData, {
              title: esc.title || "",
              targetEmployeeName: esc.targetEmployeeName || "",
              targetTeam: esc.targetTeam || "",
              description: esc.description || "",
              severity: esc.severity || "MEDIUM",
              status: esc.status || "PENDING",
            });
          } else {
            Object.assign(formData, { title: "", targetEmployeeName: "", targetTeam: "", description: "", severity: "MEDIUM", status: "PENDING" });
          }
        },
        { immediate: true },
      );

      function save() { ctx.emit("save", { ...formData }); }
      return { formData, save };
    },
    template: `
      <section class="form-page container">
        <div class="form-head">
          <button @click="$emit('cancel')" class="icon-btn icon-btn-light"><i data-lucide="chevron-left" class="icon-md"></i></button>
          <h2 class="form-title">エスカレーションの{{ editingEscalation ? '編集' : '起票' }}</h2>
        </div>

        <div class="form-stack-blocks">
          <article class="section-card form-block">
            <div>
              <label class="field-label">タイトル <span style="color:var(--danger)">*</span></label>
              <input v-model="formData.title" type="text" class="input-shell input-strong" placeholder="例: 長期欠勤対応" />
            </div>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">対象情報</h3>
            <div class="two-col-grid">
              <div>
                <label class="field-label">対象者名</label>
                <input v-model="formData.targetEmployeeName" type="text" class="input-shell" placeholder="氏名または「(チーム全体)」" />
              </div>
              <div>
                <label class="field-label">対象チーム</label>
                <input v-model="formData.targetTeam" type="text" class="input-shell" placeholder="チーム名" />
              </div>
            </div>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">詳細説明</h3>
            <textarea v-model="formData.description" rows="6" class="input-shell" placeholder="状況の詳細を記入してください"></textarea>
          </article>

          <article class="section-card form-block">
            <h3 class="block-title">分類</h3>
            <div class="two-col-grid">
              <div>
                <label class="field-label">重要度</label>
                <select v-model="formData.severity" class="input-shell input-strong">
                  <option value="LOW">低</option>
                  <option value="MEDIUM">中</option>
                  <option value="HIGH">高</option>
                </select>
              </div>
              <div>
                <label class="field-label">ステータス</label>
                <select v-model="formData.status" class="input-shell input-strong">
                  <option value="PENDING">未対応</option>
                  <option value="ONGOING">対応中</option>
                  <option value="RESOLVED">解決済み</option>
                </select>
              </div>
            </div>
          </article>
        </div>

        <div class="form-actions-fixed">
          <div class="form-actions-inner">
            <button @click="$emit('cancel')" class="action-btn action-secondary action-fill">キャンセル</button>
            <button @click="save" :disabled="!formData.title.trim()" class="action-btn action-primary action-fill">
              {{ editingEscalation ? '更新する' : '起票する' }}
            </button>
          </div>
        </div>
      </section>
    `,
  };

  // ===== エスカレーション: 詳細 =====
  const EscalationDetailView = {
    props: ["escalation", "userProfile"],
    emits: ["back", "edit", "add-log"],
    setup: function (props, ctx) {
      const newLogText = ref("");
      const SEVERITY_LABELS = { HIGH: "高", MEDIUM: "中", LOW: "低" };
      const STATUS_LABELS = { PENDING: "未対応", ONGOING: "対応中", RESOLVED: "解決済み" };

      const canEdit = computed(function () {
        if (!props.userProfile) return false;
        return ROLES[props.userProfile.role] && ROLES[props.userProfile.role].rank >= 1;
      });

      function submitLog() {
        if (!newLogText.value.trim()) return;
        ctx.emit("add-log", newLogText.value.trim());
        newLogText.value = "";
      }

      return { newLogText, SEVERITY_LABELS, STATUS_LABELS, canEdit, submitLog };
    },
    template: `
      <section v-if="escalation" class="container detail-page">
        <div class="detail-head">
          <button @click="$emit('back')" class="icon-btn icon-btn-light"><i data-lucide="chevron-left" class="icon-md"></i></button>
          <div class="detail-actions">
            <button v-if="canEdit" @click="$emit('edit')" class="action-btn action-soft-primary">編集</button>
          </div>
        </div>

        <div class="detail-grid">
          <article class="section-card detail-main">
            <div class="esc-card-header" style="margin-bottom:0.8rem">
              <span :class="'esc-severity-badge severity-' + escalation.severity.toLowerCase()">{{ SEVERITY_LABELS[escalation.severity] }}</span>
              <span :class="'esc-status-badge status-' + escalation.status.toLowerCase()">{{ STATUS_LABELS[escalation.status] }}</span>
            </div>
            <h2 class="detail-title">{{ escalation.title }}</h2>
            <p class="detail-meta">起票: {{ escalation.createdByName }} ({{ escalation.createdByRole }}) / {{ escalation.createdAt }}</p>

            <section class="detail-section">
              <h3 class="detail-section-title">対象情報</h3>
              <p class="detail-paragraph">{{ escalation.targetEmployeeName }} — {{ escalation.targetTeam }}</p>
            </section>

            <section class="detail-section">
              <h3 class="detail-section-title">詳細説明</h3>
              <p class="detail-paragraph" style="white-space:pre-wrap">{{ escalation.description }}</p>
            </section>

            <section class="detail-section">
              <h3 class="detail-section-title">対応ログ</h3>
              <div v-if="escalation.logs && escalation.logs.length > 0" class="esc-log-timeline">
                <div v-for="log in escalation.logs" :key="log.logId" class="esc-log-entry">
                  <div class="esc-log-dot"></div>
                  <div class="esc-log-body">
                    <div class="esc-log-meta">
                      <span class="esc-log-author">{{ log.authorName }}</span>
                      <span class="esc-log-date">{{ log.createdAt }}</span>
                    </div>
                    <p class="esc-log-text">{{ log.logText }}</p>
                  </div>
                </div>
              </div>
              <p v-else class="detail-paragraph" style="color:var(--ink-500)">対応ログはまだありません</p>
            </section>

            <section v-if="canEdit" class="detail-section esc-log-form">
              <h3 class="detail-section-title">対応ログを追記</h3>
              <textarea v-model="newLogText" rows="4" class="input-shell" placeholder="対応内容を記入してください..."></textarea>
              <button @click="submitLog" :disabled="!newLogText.trim()" class="action-btn action-primary" style="margin-top:0.6rem">ログを追記</button>
            </section>
          </article>
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
      EscalationListView,
      EscalationFormView,
      EscalationDetailView,
      NotificationToast,
    },
    setup: function () {
      const user = ref(null);
      const userProfile = ref(null);
      const reports = ref([]);
      const loading = ref(true);
      const view = ref("list");
      const currentReport = ref(null);
      const notification = ref(null);
      const escalations = ref([]);
      const currentEscalation = ref(null);
      const dashboardSummary = ref({
        totalReports: 0,
        pendingFeedbackCount: 0,
        submissionRate: 0,
        unsubmittedMembers: [],
      });
      const currentMonthStr = new Date().toISOString().slice(0, 7);

      function showNotification(message) {
        notification.value = message;
        setTimeout(function () {
          notification.value = null;
        }, 3000);
      }

      function clearStoredTokens() {
        window.localStorage.removeItem(AUTH_TOKEN_KEY);
        window.localStorage.removeItem(REFRESH_TOKEN_KEY);
      }

      function resetState() {
        user.value = null;
        userProfile.value = null;
        reports.value = [];
        escalations.value = [];
        currentReport.value = null;
        currentEscalation.value = null;
        dashboardSummary.value = {
          totalReports: 0,
          pendingFeedbackCount: 0,
          submissionRate: 0,
          unsubmittedMembers: [],
        };
        view.value = "list";
      }

      function clearSession(message) {
        clearStoredTokens();
        resetState();
        if (message) {
          showNotification(message);
        }
      }

      function storeLoginSession(params) {
        window.localStorage.setItem(AUTH_TOKEN_KEY, params.accessToken);
        window.localStorage.setItem(REFRESH_TOKEN_KEY, params.refreshToken);
      }

      async function loadProfile() {
        const params = await api.me();
        const profile = enrichProfile(params);
        user.value = profile;
        userProfile.value = profile;
        return profile;
      }

      async function loadReports() {
        if (!userProfile.value) return [];
        const params = await api.searchReports();
        const masked = isMaskedRole(userProfile.value.role);
        reports.value = (params.items || []).map(function (item) {
          return mapReportSummary(item, masked);
        });
        return reports.value;
      }

      async function loadDashboard() {
        if (!userProfile.value) return;
        dashboardSummary.value = await api.dashboardSummary(currentMonthStr);
      }

      async function loadEscalations() {
        if (!userProfile.value || !isEscalationRole(userProfile.value.role)) {
          escalations.value = [];
          return [];
        }
        const params = await api.searchEscalations();
        const masked = isMaskedRole(userProfile.value.role);
        escalations.value = (params.items || []).map(function (item) {
          return mapEscalationSummary(item, masked);
        });
        return escalations.value;
      }

      async function reloadHomeData() {
        await Promise.all([loadReports(), loadDashboard(), loadEscalations()]);
      }

      async function restoreSession() {
        await loadProfile();
        await reloadHomeData();
      }

      async function handleLogin(loginData) {
        if (!loginData.employeeId || !loginData.password) return;
        loading.value = true;
        try {
          const params = await api.login(loginData);
          storeLoginSession(params);
          const profile = enrichProfile(params.userProfile);
          user.value = profile;
          userProfile.value = profile;
          await reloadHomeData();
          view.value = "list";
          showNotification("ログインしました: " + profile.name);
        } catch (error) {
          clearStoredTokens();
          showNotification(error.message || "ログインエラー");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      async function handleLogout(skipApi, message) {
        if (!skipApi) {
          try {
            await api.logout();
          } catch (_) {
            // トークン期限切れ時も画面状態だけは確実に初期化する。
          }
        }
        clearSession(message);
        refreshIcons();
      }

      function openForm(report) {
        currentReport.value = report || null;
        view.value = "form";
      }

      async function openDetail(report) {
        loading.value = true;
        try {
          const params = await api.reportDetail(report.reportId || report.id);
          currentReport.value = mapReportDetail(params, isMaskedRole(userProfile.value.role));
          view.value = "detail";
        } catch (error) {
          showNotification(error.message || "月報詳細の取得に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      async function saveReport(formData) {
        if (!userProfile.value) return;

        loading.value = true;
        try {
          const payload = toReportPayload(formData);
          if (currentReport.value && currentReport.value.reportId) {
            await api.updateReport({ ...payload, reportId: currentReport.value.reportId });
            showNotification("更新しました");
          } else {
            await api.createReport(payload);
            showNotification("提出しました");
          }
          await Promise.all([loadReports(), loadDashboard()]);
          currentReport.value = null;
          view.value = "list";
        } catch (error) {
          showNotification(error.message || "月報の保存に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      async function saveFeedback(feedbackComment) {
        if (!currentReport.value || !userProfile.value) return;
        loading.value = true;
        try {
          await api.updateFeedback(currentReport.value.reportId, feedbackComment);
          currentReport.value = mapReportDetail(
            await api.reportDetail(currentReport.value.reportId),
            isMaskedRole(userProfile.value.role)
          );
          await Promise.all([loadReports(), loadDashboard()]);
          showNotification("回答を保存しました");
        } catch (error) {
          showNotification(error.message || "回答の保存に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      async function deleteReport() {
        if (!currentReport.value || !currentReport.value.reportId) return;
        if (!confirm("削除しますか？")) return;
        loading.value = true;
        try {
          await api.deleteReport(currentReport.value.reportId);
          await Promise.all([loadReports(), loadDashboard()]);
          currentReport.value = null;
          view.value = "list";
          showNotification("削除しました");
        } catch (error) {
          showNotification(error.message || "削除に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      function openEscalationList() {
        view.value = "escalation-list";
      }

      async function openEscalationDetail(esc) {
        loading.value = true;
        try {
          const params = await api.escalationDetail(esc.escalationId || esc.id);
          currentEscalation.value = mapEscalationDetail(params, esc, isMaskedRole(userProfile.value.role));
          view.value = "escalation-detail";
        } catch (error) {
          showNotification(error.message || "エスカレーション詳細の取得に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      function openEscalationForm(esc) {
        currentEscalation.value = esc || null;
        view.value = "escalation-form";
      }

      async function saveEscalation(formData) {
        if (!userProfile.value) return;
        loading.value = true;
        try {
          if (currentEscalation.value && currentEscalation.value.escalationId) {
            await api.updateEscalation({ ...formData, escalationId: currentEscalation.value.escalationId });
            showNotification("エスカレーションを更新しました");
          } else {
            await api.createEscalation(formData);
            showNotification("エスカレーションを起票しました");
          }
          await loadEscalations();
          currentEscalation.value = null;
          view.value = "escalation-list";
        } catch (error) {
          showNotification(error.message || "エスカレーションの保存に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      async function addEscalationLog(logText) {
        if (!currentEscalation.value || !userProfile.value) return;
        loading.value = true;
        try {
          await api.addEscalationLog(currentEscalation.value.escalationId, logText);
          currentEscalation.value = mapEscalationDetail(
            await api.escalationDetail(currentEscalation.value.escalationId),
            currentEscalation.value,
            isMaskedRole(userProfile.value.role)
          );
          await loadEscalations();
          showNotification("対応ログを追記しました");
        } catch (error) {
          showNotification(error.message || "対応ログの追加に失敗しました");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      }

      onMounted(async function () {
        registerSessionExpiredHandler(function () {
          handleLogout(true, "セッションの有効期限が切れました。再度ログインしてください。");
        });

        if (!window.localStorage.getItem(AUTH_TOKEN_KEY)) {
          loading.value = false;
          refreshIcons();
          return;
        }

        try {
          await restoreSession();
        } catch (e) {
          clearSession(e.message || "ログイン状態を復元できませんでした");
        } finally {
          loading.value = false;
          refreshIcons();
        }
      });

      watch(view, function () {
        refreshIcons();
      });

      const stats = computed(function () {
        return {
          total: dashboardSummary.value.totalReports || 0,
          pending: dashboardSummary.value.pendingFeedbackCount || 0,
        };
      });

      const isHighRank = computed(function () {
        return userProfile.value && ["GL", "OM", "SM", "SA"].includes(userProfile.value.role);
      });

      const canEscalate = computed(function () {
        return userProfile.value && isEscalationRole(userProfile.value.role);
      });

      const daysUntilDeadline = computed(function () {
        const today = new Date();
        const deadline = new Date(today.getFullYear(), today.getMonth(), DEADLINE_DAY);
        if (today > deadline) return 0;
        return Math.ceil((deadline.getTime() - today.getTime()) / (1000 * 60 * 60 * 24));
      });

      const isSelfSubmitted = computed(function () {
        if (!userProfile.value) return false;
        return reports.value.some(function (r) {
          return r.month === currentMonthStr && r.reporterId === userProfile.value.employeeId;
        });
      });

      const unsubmittedMembers = computed(function () {
        return dashboardSummary.value.unsubmittedMembers || [];
      });

      const submissionRate = computed(function () {
        return dashboardSummary.value.submissionRate || 0;
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
        canEscalate,
        currentMonthStr,
        daysUntilDeadline,
        isSelfSubmitted,
        unsubmittedMembers,
        submissionRate,
        escalations,
        currentEscalation,
        DEADLINE_DAY,
        ROLES,
        handleLogin,
        handleLogout,
        openForm,
        openDetail,
        saveReport,
        saveFeedback,
        deleteReport,
        openEscalationList,
        openEscalationDetail,
        openEscalationForm,
        saveEscalation,
        addEscalationLog,
      };
    },
    template: `
      <div class="screen-shell">
        <div v-if="loading" class="loading-screen">
          <div class="loading-spinner"><i data-lucide="loader-2" class="icon-xl"></i></div>
          <p class="loading-text">Loading Application</p>
        </div>

        <LoginView v-else-if="!userProfile" :loading="loading" @login="handleLogin" />

        <div v-else>
          <HeaderBar :user-profile="userProfile" :is-high-rank="isHighRank" :can-escalate="canEscalate" @logout="handleLogout" @open-escalation="openEscalationList" />

          <!-- 提出期限バナー -->
          <div class="deadline-banner">
            <div class="container deadline-inner">
              <div class="deadline-info">
                <i data-lucide="calendar" class="icon-sm"></i>
                <span>今月 (<strong>{{ currentMonthStr }}</strong>) の提出期限まであと
                  <strong class="deadline-days">{{ daysUntilDeadline }}</strong> 日
                  &nbsp;(毎月 {{ DEADLINE_DAY }} 日〆切)
                </span>
              </div>
              <div v-if="!isSelfSubmitted" class="deadline-alert">
                <i data-lucide="alert-circle" class="icon-sm"></i>
                <span>未提出 — お早めに提出してください</span>
              </div>
              <div v-else class="deadline-done">
                <i data-lucide="check-circle" class="icon-sm"></i>
                <span>今月分は提出済みです</span>
              </div>
            </div>
          </div>

          <DashboardView
            v-if="view === 'list'"
            :reports="reports"
            :stats="stats"
            :user-profile="userProfile"
            :unsubmitted-members="unsubmittedMembers"
            :submission-rate="submissionRate"
            :current-month-str="currentMonthStr"
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

          <EscalationListView
            v-if="view === 'escalation-list'"
            :escalations="escalations"
            :user-profile="userProfile"
            @new-escalation="openEscalationForm(null)"
            @open-escalation="openEscalationDetail"
            @back="view = 'list'"
          />

          <EscalationFormView
            v-if="view === 'escalation-form'"
            :editing-escalation="currentEscalation"
            @save="saveEscalation"
            @cancel="view = 'escalation-list'"
          />

          <EscalationDetailView
            v-if="view === 'escalation-detail'"
            :escalation="currentEscalation"
            :user-profile="userProfile"
            @back="view = 'escalation-list'"
            @edit="openEscalationForm(currentEscalation)"
            @add-log="addEscalationLog"
          />

          <NotificationToast :notification="notification" />
        </div>
      </div>
    `,
  };

  createApp(App).mount("#app");
})();

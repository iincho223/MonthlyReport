import { ROLES } from "../constants.js";

export const DashboardView = {
  props: ["reports", "stats", "userProfile"],
  emits: ["open-form", "open-detail"],
  setup() {
    return { ROLES };
  },
  template: `
    <main class="container page-stack">
      <section class="page-head">
        <div>
          <h2 class="page-title"><i data-lucide="layout-dashboard" class="icon-sm icon-accent"></i> ダッシュボード</h2>
          <p class="page-subtitle">Status Monitoring</p>
        </div>

        <button @click="$emit('open-form')" class="action-btn action-primary">
          <i data-lucide="plus" class="icon-sm"></i> 新規提出
        </button>
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
        <article
          v-for="report in reports"
          :key="report.id"
          @click="$emit('open-detail', report)"
          class="section-card report-card"
        >
          <div class="report-card-head">
            <div class="report-month-chip">{{ report.month }}</div>
            <span :class="report.adminFeedback ? 'status-chip status-checked' : 'status-chip status-waiting'">
              {{ report.adminFeedback ? 'Checked' : 'Waiting' }}
            </span>
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
};

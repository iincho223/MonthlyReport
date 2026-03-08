import { CONDITION_ITEMS, ROLES } from "../constants.js";

const { computed, ref, watch } = Vue;

export const ReportDetailView = {
  props: ["user", "userProfile", "report"],
  emits: ["back", "edit", "delete", "save-feedback"],
  setup(props, { emit }) {
    const feedbackText = ref("");

    watch(
      () => props.report,
      (nextReport) => {
        feedbackText.value = nextReport?.adminFeedback || "";
      },
      { immediate: true },
    );

    const canFeedback = computed(() => {
      if (!props.userProfile || !props.report) return false;
      const rank = ROLES[props.userProfile.role]?.rank || 0;
      return rank > 0 && props.report.authorId !== props.user?.uid;
    });

    function saveFeedback() {
      emit("save-feedback", feedbackText.value);
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

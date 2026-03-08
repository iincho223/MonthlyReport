import { CONDITION_ITEMS, DEFAULT_REPORT_FORM } from "../constants.js";

const { reactive, watch } = Vue;

export const ReportFormView = {
  props: ["editingReport"],
  emits: ["save", "cancel"],
  setup(props, { emit }) {
    const formData = reactive(DEFAULT_REPORT_FORM());

    watch(
      () => props.editingReport,
      (editing) => {
        if (editing) {
          Object.assign(formData, JSON.parse(JSON.stringify(editing)));
          return;
        }
        Object.assign(formData, DEFAULT_REPORT_FORM());
      },
      { immediate: true },
    );

    function save() {
      emit("save", { ...formData });
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
                <button
                  v-for="opt in ['○', '△', '×']"
                  :key="opt"
                  type="button"
                  @click="formData.condition[item.id] = opt"
                  :class="formData.condition[item.id] === opt ? 'condition-btn is-active' : 'condition-btn'"
                >
                  {{ opt }}
                </button>
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

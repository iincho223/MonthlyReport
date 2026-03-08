const { reactive } = Vue;

export const LoginView = {
  props: ["loading"],
  emits: ["login"],
  setup(_, { emit }) {
    const loginData = reactive({ employeeId: "", password: "" });

    function submit() {
      emit("login", { ...loginData });
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
            <input
              v-model="loginData.employeeId"
              class="input-shell input-strong"
              required
              type="text"
              placeholder="例: EMP001"
            />
          </div>
          <div>
            <label class="field-label">パスワード</label>
            <input
              v-model="loginData.password"
              class="input-shell input-strong"
              required
              type="password"
              placeholder="パスワードを入力"
            />
          </div>

          <button :disabled="loading" type="submit" class="action-btn action-primary action-full">
            <i data-lucide="log-in" class="icon-sm"></i>
            ログイン
          </button>
        </form>
      </div>
    </div>
  `,
};

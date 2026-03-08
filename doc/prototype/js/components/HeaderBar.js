import { ROLES } from "../constants.js";

export const HeaderBar = {
  props: ["userProfile", "isHighRank"],
  emits: ["logout"],
  setup() {
    return { ROLES };
  },
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
            <div class="user-scope">
              {{ isHighRank ? userProfile?.office : userProfile?.team }} | {{ ROLES[userProfile?.role]?.label || '' }}
            </div>
          </div>

          <button @click="$emit('logout')" class="icon-btn" aria-label="logout">
            <i data-lucide="log-out" class="icon-sm"></i>
          </button>
        </div>
      </div>
    </header>
  `,
};

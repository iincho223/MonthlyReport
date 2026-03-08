import { LoginView } from "./components/LoginView.js";
import { HeaderBar } from "./components/HeaderBar.js";
import { DashboardView } from "./components/DashboardView.js";
import { ReportFormView } from "./components/ReportFormView.js";
import { ReportDetailView } from "./components/ReportDetailView.js";
import { NotificationToast } from "./components/NotificationToast.js";
import { useAppController } from "./composables/useAppController.js";

const { createApp } = Vue;

const App = {
  components: {
    LoginView,
    HeaderBar,
    DashboardView,
    ReportFormView,
    ReportDetailView,
    NotificationToast,
  },
  setup() {
    return useAppController();
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

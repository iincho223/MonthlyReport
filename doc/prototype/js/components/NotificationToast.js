export const NotificationToast = {
  props: ["notification"],
  template: `
    <div v-if="notification" class="notification-toast">
      <i data-lucide="check-circle-2" class="icon-md icon-success"></i>
      <span class="notification-text">{{ notification }}</span>
    </div>
  `,
};

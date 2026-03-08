export function getRuntimeConfig() {
  const config = JSON.parse(__firebase_config);
  const appId = typeof __app_id !== "undefined" ? __app_id : "monthly-report-vue-pro";
  const initialAuthToken =
    typeof __initial_auth_token !== "undefined" ? __initial_auth_token : null;

  return { config, appId, initialAuthToken };
}

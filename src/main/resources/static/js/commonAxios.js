/** 
 * axiosを利用したpost通信
 * 正常終了：レスポンスデータ
 * 異常終了：null
 */
/**
 * 共通 axios インスタンス。
 * 全 API 通信はこのインスタンス経由で行う。
 * リクエスト: localStorage の authToken を Authorization ヘッダに付与。
 * レスポンス: resultStatus !== "0" なら Error を throw、正常時は params を resolve。
 */
const apiClient = axios.create({
  baseURL: '/api/v1',
  headers: { 'Content-Type': 'application/json' },
});

apiClient.interceptors.request.use(function(config) {
  const token = window.localStorage.getItem('authToken');
  if (token) {
    config.headers.Authorization = 'Bearer ' + token;
  }
  return config;
});

apiClient.interceptors.response.use(
  function(response) {
    const data = response && response.data ? response.data : {};
    if (data.resultStatus !== '0') {
      return Promise.reject(new Error(data.resultMsg || 'API エラーが発生しました'));
    }
    return data.params || {};
  },
  function(error) {
    if (error && error.response && error.response.status === 401) {
      window.localStorage.removeItem('authToken');
      window.location.reload();
    }
    if (error && error.response && error.response.data && error.response.data.resultMsg) {
      return Promise.reject(new Error(error.response.data.resultMsg));
    }
    return Promise.reject(error);
  }
);

export { apiClient };

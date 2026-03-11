const MOCK_API_DATA = {
  EMP001: {
    name: "田中 太郎",
    employeeId: "EMP001",
    password: "pass",
    role: "OM",
    office: "東京本社",
    team: "経営企画",
  },
  EMP002: {
    name: "鈴木 一郎",
    employeeId: "EMP002",
    password: "pass",
    role: "GL",
    office: "大阪支社",
    team: "関西第一営業部",
  },
  EMP003: {
    name: "佐藤 花子",
    employeeId: "EMP003",
    password: "pass",
    role: "TL",
    office: "東京本社",
    team: "チームA",
  },
  EMP004: {
    name: "山田 健太",
    employeeId: "EMP004",
    password: "pass",
    role: "REPORTER",
    office: "東京本社",
    team: "チームA",
  },
};

/**
 * モック API からユーザープロフィールを取得する。
 * インプット: employeeId 社員番号、password パスワード。
 * アウトプット: 認証成功時はユーザープロフィール、失敗時は Error。
 *
 * @param {string} employeeId 社員番号
 * @param {string} password パスワード
 * @returns {Promise<object>} ユーザープロフィール
 */
export async function fetchUserProfileFromAPI(employeeId, password) {
  return new Promise((resolve, reject) => {
    // API 通信を模した遅延を入れる。
    setTimeout(() => {
      const data = MOCK_API_DATA[employeeId.toUpperCase()];
      // 対象ユーザーが存在しない場合は認証エラーを返す。
      if (!data) {
        reject(new Error("該当する社員番号が見つかりません"));
        return;
      }

      // パスワード不一致時は認証失敗とする。
      if (data.password !== password) {
        reject(new Error("パスワードが間違っています"));
        return;
      }

      // 応答から機微情報である password を除外する。
      const { password: _, ...userProfile } = data;
      resolve(userProfile);
    }, 800);
  });
}

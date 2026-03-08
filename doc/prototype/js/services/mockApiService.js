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

export async function fetchUserProfileFromAPI(employeeId, password) {
  return new Promise((resolve, reject) => {
    setTimeout(() => {
      const data = MOCK_API_DATA[employeeId.toUpperCase()];
      if (!data) {
        reject(new Error("該当する社員番号が見つかりません"));
        return;
      }

      if (data.password !== password) {
        reject(new Error("パスワードが間違っています"));
        return;
      }

      const { password: _, ...userProfile } = data;
      resolve(userProfile);
    }, 800);
  });
}

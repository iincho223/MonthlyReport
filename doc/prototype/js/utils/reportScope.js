/**
 * ロールに応じて表示可能な月報に絞り込む。
 * インプット: allReports 全月報、profile ユーザープロフィール、authUser 認証ユーザー。
 * アウトプット: 閲覧権限に合致した月報配列。
 *
 * @param {object[]} allReports 全月報
 * @param {object | null} profile ユーザープロフィール
 * @param {object | null} authUser 認証ユーザー
 * @returns {object[]} フィルタ済み月報
 */
export function applyScopeFilter(allReports, profile, authUser) {
  // 判定に必要な情報が欠けている場合は空配列を返す。
  if (!profile || !authUser) return [];

  // REPORTER は自分の投稿のみ参照可能。
  if (profile.role === "REPORTER") {
    return allReports.filter((report) => report.authorId === authUser.uid);
  }

  // TL は自分の投稿または同一チームを参照可能。
  if (profile.role === "TL") {
    return allReports.filter(
      (report) => report.authorId === authUser.uid || report.team === profile.team,
    );
  }

  // GL は自分の投稿または同一オフィスを参照可能。
  if (profile.role === "GL") {
    return allReports.filter(
      (report) => report.authorId === authUser.uid || report.office === profile.office,
    );
  }

  // OM は全件参照可能。
  return allReports;
}

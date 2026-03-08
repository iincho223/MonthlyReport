export function applyScopeFilter(allReports, profile, authUser) {
  if (!profile || !authUser) return [];

  if (profile.role === "REPORTER") {
    return allReports.filter((report) => report.authorId === authUser.uid);
  }

  if (profile.role === "TL") {
    return allReports.filter(
      (report) => report.authorId === authUser.uid || report.team === profile.team,
    );
  }

  if (profile.role === "GL") {
    return allReports.filter(
      (report) => report.authorId === authUser.uid || report.office === profile.office,
    );
  }

  return allReports;
}

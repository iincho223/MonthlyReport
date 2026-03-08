import {
  addDoc,
  collection,
  deleteDoc,
  doc,
  onSnapshot,
  serverTimestamp,
  updateDoc,
} from "https://www.gstatic.com/firebasejs/11.6.1/firebase-firestore.js";

export function createReportService(db, appId) {
  function reportsCollectionRef() {
    return collection(db, "artifacts", appId, "public", "data", "reports");
  }

  function reportRefById(reportId) {
    return doc(db, "artifacts", appId, "public", "data", "reports", reportId);
  }

  function observeReports(onData, onError) {
    return onSnapshot(
      reportsCollectionRef(),
      (snapshot) => {
        const reports = snapshot.docs.map((reportDoc) => ({
          id: reportDoc.id,
          ...reportDoc.data(),
        }));
        onData(reports);
      },
      onError,
    );
  }

  async function createReport(payload) {
    await addDoc(reportsCollectionRef(), {
      ...payload,
      createdAt: serverTimestamp(),
      adminFeedback: "",
      adminFeedbackRole: "",
      adminFeedbackName: "",
    });
  }

  async function updateReport(reportId, payload) {
    await updateDoc(reportRefById(reportId), payload);
  }

  async function updateFeedback(reportId, payload) {
    await updateDoc(reportRefById(reportId), {
      ...payload,
      adminFeedbackAt: serverTimestamp(),
    });
  }

  async function removeReport(reportId) {
    await deleteDoc(reportRefById(reportId));
  }

  return {
    observeReports,
    createReport,
    updateReport,
    updateFeedback,
    removeReport,
    serverTimestamp,
  };
}

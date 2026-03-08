export const ROLES = {
  REPORTER: {
    id: "REPORTER",
    label: "報告者",
    color: "bg-gray-100 text-gray-700",
    rank: 0,
  },
  TL: {
    id: "TL",
    label: "TL (Team Leader)",
    color: "bg-emerald-100 text-emerald-700",
    rank: 1,
  },
  GL: {
    id: "GL",
    label: "GL (Group Leader)",
    color: "bg-blue-100 text-blue-700",
    rank: 2,
  },
  OM: {
    id: "OM",
    label: "OM (Ops Manager)",
    color: "bg-purple-100 text-purple-700",
    rank: 3,
  },
};

export const CONDITION_ITEMS = [
  { id: "physical", label: "体調" },
  { id: "stress", label: "ストレス" },
  { id: "relationships", label: "人間関係" },
  { id: "worries", label: "悩み" },
  { id: "fatigue", label: "疲れ" },
  { id: "sleep", label: "睡眠" },
  { id: "motivation", label: "やる気" },
];

export const DEFAULT_REPORT_FORM = () => ({
  month: new Date().toISOString().slice(0, 7),
  title: "",
  salesInfo: "特になし",
  nextMonthOvertime: "",
  nextMonthOvertimeReason: "",
  thisMonthOvertime: "",
  thisMonthOvertimeReason: "",
  condition: Object.fromEntries(CONDITION_ITEMS.map((item) => [item.id, "○"])),
  comments: "",
});

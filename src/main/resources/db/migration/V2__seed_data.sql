-- InMemoryDataStore に登録されていた初期データ（検証用ユーザー・グループ・チーム・月報1件）を
-- そのまま DB へ移行したもの。role_rank は UserRole の宣言順（ordinal）に準拠する。
--
-- groups.gl_user_id / teams.tl_user_id -> users と users.team_code -> teams は相互参照のため、
-- 投入時のみ外部キー制約チェックを一時的に無効化する。
SET FOREIGN_KEY_CHECKS = 0;

insert into roles (role_code, role_name, role_rank, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  ('NG', '新卒', 0, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('TM', 'メンバー', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('TL', 'チームリーダー', 2, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('GL', 'グループリーダー', 3, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('OM', 'オフィスマネージャー', 4, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('SP', '営業担当者', 5, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('SM', '支店長', 6, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('SA', 'システム管理者', 7, 0, now(3), 'SYSTEM', now(3), 'SYSTEM');

insert into offices (office_code, office_name, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  ('TOKYO', '東京本社', 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('OSAKA', '大阪支社', 0, now(3), 'SYSTEM', now(3), 'SYSTEM');

-- グループ/チーム: InMemoryDataStore にはなかった "HQ" チームは、
-- 管理系ロール(OM/SP/SM/SA)のユーザーが所属していた team_code をそのまま移行するために追加した
-- (users.team_code は NOT NULL FK のため、参照先チーム行が必須)。
-- GROUP_A/GROUP_OSAKA_1 配下に置くと GL 検索時のチーム件数がテスト前提(各1件)と乖離するため、
-- 専用の GROUP_HQ を新設して所属させる。TL には所属営業所の OM を割り当てる。
insert into groups (group_code, group_name, office_code, gl_user_id, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  ('GROUP_A', '東京第1グループ', 'TOKYO', 1009, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('GROUP_OSAKA_1', '大阪第1グループ', 'OSAKA', 1002, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('GROUP_HQ', '本社グループ', 'TOKYO', 1001, 0, now(3), 'SYSTEM', now(3), 'SYSTEM');

insert into teams (team_code, team_name, group_code, office_code, tl_user_id, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  ('TEAM_A', 'Aチーム', 'GROUP_A', 'TOKYO', 1003, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('SALES_WEST', '西営業チーム', 'GROUP_OSAKA_1', 'OSAKA', 1010, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  ('HQ', '経営企画', 'GROUP_HQ', 'TOKYO', 1001, 0, now(3), 'SYSTEM', now(3), 'SYSTEM');

insert into users (user_id, employee_no, user_name, password_hash, role_code, office_code, team_code, is_active, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  (1001, 'EMP001', '田中 太郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'OM', 'TOKYO', 'HQ', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1002, 'EMP002', '鈴木 一郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'GL', 'OSAKA', 'SALES_WEST', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1003, 'EMP003', '佐藤 花子', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'TL', 'TOKYO', 'TEAM_A', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1004, 'EMP004', '山田 健太', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'TM', 'TOKYO', 'TEAM_A', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1005, 'EMP005', '新卒 一郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'NG', 'TOKYO', 'TEAM_A', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1006, 'EMP006', '営業 次郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'SP', 'TOKYO', 'HQ', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1007, 'EMP007', '支店長 三郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'SM', 'TOKYO', 'HQ', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1008, 'EMP008', '管理者 四郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'SA', 'TOKYO', 'HQ', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1009, 'EMP009', '髙橋 五郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'GL', 'TOKYO', 'TEAM_A', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM'),
  (1010, 'EMP010', '伊藤 六郎', '$2y$10$UwgLQifHOrDPw/5TO6LZN.7Km8rj7h4phpTJzzowR54Ve3AwoRste', 'TL', 'OSAKA', 'SALES_WEST', 1, 0, now(3), 'SYSTEM', now(3), 'SYSTEM');

insert into reports (report_id, report_month, sales_info, next_month_overtime_hours, next_month_overtime_reason, this_month_overtime_hours, this_month_overtime_reason, comments, status, author_user_id, office_code, team_code, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  ('01KWRVG1G1979FJXA8YCE85GWV', '2026-03', '特になし', 20, '案件リリース対応', 18, '障害調査', '相談事項あり', 'SUBMITTED', 1004, 'TOKYO', 'TEAM_A', 0, now(3), 'SYSTEM', date_sub(now(3), interval 1 day), 'SYSTEM');

insert into report_conditions (report_id, physical, stress, relationships, worries, fatigue, sleep, motivation, delete_flag, updated_at, updated_by, registered_at, registered_by) values
  ('01KWRVG1G1979FJXA8YCE85GWV', 'GOOD', 'WARN', 'BEST', 'WARN', 'NG', 'WARN', 'GOOD', 0, now(3), 'SYSTEM', date_sub(now(3), interval 1 day), 'SYSTEM');

SET FOREIGN_KEY_CHECKS = 1;

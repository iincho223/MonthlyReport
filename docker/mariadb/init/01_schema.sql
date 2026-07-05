-- doc/システム要件定義/DB設計.md「7. DDLサンプル(初版)」を転記したもの。
-- テーブル定義を変更する場合は DB設計.md を正として同時に更新すること。

create table roles (
  role_code varchar(20) not null,
  role_name varchar(100) not null,
  role_rank tinyint not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (role_code)
) engine=InnoDB default charset=utf8mb4;

create table offices (
  office_code varchar(20) not null,
  office_name varchar(100) not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (office_code)
) engine=InnoDB default charset=utf8mb4;

-- groups/teams はリーダー(GL/TL)を users.user_id で参照するが、
-- users.team_code が teams を参照するため相互参照となる。
-- gl_user_id / tl_user_id の FK 制約は users 作成後に ALTER TABLE で付与する。
create table groups (
  group_code varchar(20) not null,
  group_name varchar(100) not null,
  office_code varchar(20) not null,
  gl_user_id bigint not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (group_code),
  key idx_groups_office (office_code),
  constraint fk_groups_office foreign key (office_code) references offices (office_code)
) engine=InnoDB default charset=utf8mb4;

create table teams (
  team_code varchar(20) not null,
  team_name varchar(100) not null,
  group_code varchar(20) not null,
  office_code varchar(20) not null,
  tl_user_id bigint not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (team_code),
  key idx_teams_group (group_code),
  key idx_teams_office (office_code),
  constraint fk_teams_group foreign key (group_code) references groups (group_code),
  constraint fk_teams_office foreign key (office_code) references offices (office_code)
) engine=InnoDB default charset=utf8mb4;

create table users (
  user_id bigint not null auto_increment,
  employee_no varchar(20) not null,
  user_name varchar(100) not null,
  password_hash varchar(255) not null,
  role_code varchar(20) not null,
  office_code varchar(20) not null,
  team_code varchar(20) not null,
  is_active tinyint(1) not null default 1,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (user_id),
  unique key uk_users_employee_no (employee_no),
  key idx_users_role_office_team (role_code, office_code, team_code),
  constraint fk_users_role foreign key (role_code) references roles (role_code),
  constraint fk_users_office foreign key (office_code) references offices (office_code),
  constraint fk_users_team foreign key (team_code) references teams (team_code)
) engine=InnoDB default charset=utf8mb4;

alter table groups add constraint fk_groups_gl foreign key (gl_user_id) references users (user_id);
alter table teams add constraint fk_teams_tl foreign key (tl_user_id) references users (user_id);

create table reports (
  report_id char(26) not null,
  report_month char(7) not null,
  title varchar(100) not null,
  sales_info text null,
  next_month_overtime_hours smallint null,
  next_month_overtime_reason varchar(255) null,
  this_month_overtime_hours smallint null,
  this_month_overtime_reason varchar(255) null,
  comments text null,
  status varchar(30) not null,
  author_user_id bigint not null,
  office_code varchar(20) not null,
  team_code varchar(20) not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (report_id),
  unique key uk_reports_author_month_alive (author_user_id, report_month, delete_flag),
  key idx_reports_author_month (author_user_id, report_month desc),
  key idx_reports_scope_month (office_code, team_code, report_month desc),
  key idx_reports_status_month (status, report_month desc),
  key idx_reports_updated_at (updated_at desc),
  constraint fk_reports_author foreign key (author_user_id) references users (user_id)
) engine=InnoDB default charset=utf8mb4;

create table report_conditions (
  report_id char(26) not null,
  physical varchar(10) not null,
  stress varchar(10) not null,
  relationships varchar(10) not null,
  worries varchar(10) not null,
  fatigue varchar(10) not null,
  sleep varchar(10) not null,
  motivation varchar(10) not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (report_id),
  constraint fk_report_conditions_report foreign key (report_id) references reports (report_id)
) engine=InnoDB default charset=utf8mb4;

create table report_feedbacks (
  report_id char(26) not null,
  feedback_comment text null,
  responder_user_id bigint not null,
  responded_at datetime(3) not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (report_id),
  key idx_feedbacks_responder (responder_user_id, responded_at desc),
  constraint fk_report_feedbacks_report foreign key (report_id) references reports (report_id),
  constraint fk_report_feedbacks_responder foreign key (responder_user_id) references users (user_id)
) engine=InnoDB default charset=utf8mb4;

create table escalations (
  escalation_id varchar(20) not null,
  title varchar(200) not null,
  target_employee_name varchar(100) not null,
  target_team varchar(100) not null,
  description text null,
  severity varchar(10) not null,
  status varchar(10) not null,
  due_date date not null,
  resolved_date date null,
  created_by bigint not null,
  created_by_name varchar(100) not null,
  created_by_role varchar(20) not null,
  assignee_user_id bigint null,
  office_code varchar(20) not null,
  team_code varchar(20) not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (escalation_id),
  key idx_escalations_office_team (office_code, team_code, status),
  key idx_escalations_created_by (created_by, status),
  key idx_escalations_assignee (assignee_user_id, status),
  key idx_escalations_due_date (due_date),
  constraint fk_escalations_created_by foreign key (created_by) references users (user_id),
  constraint fk_escalations_assignee foreign key (assignee_user_id) references users (user_id)
) engine=InnoDB default charset=utf8mb4;

create table escalation_logs (
  log_id char(26) not null,
  escalation_id varchar(20) not null,
  log_text text not null,
  author_user_id bigint not null,
  author_name varchar(100) not null,
  created_at datetime(3) not null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (log_id),
  key idx_escalation_logs_escalation (escalation_id, created_at),
  constraint fk_escalation_logs_escalation foreign key (escalation_id) references escalations (escalation_id),
  constraint fk_escalation_logs_author foreign key (author_user_id) references users (user_id)
) engine=InnoDB default charset=utf8mb4;

create table refresh_tokens (
  token_id char(26) not null,
  user_id bigint not null,
  token_hash varchar(255) not null,
  expires_at datetime(3) not null,
  revoked_at datetime(3) null,
  delete_flag tinyint(1) not null default 0,
  updated_at datetime(3) not null,
  updated_by varchar(50) not null,
  registered_at datetime(3) not null,
  registered_by varchar(50) not null,
  primary key (token_id),
  constraint fk_refresh_tokens_user foreign key (user_id) references users (user_id)
) engine=InnoDB default charset=utf8mb4;

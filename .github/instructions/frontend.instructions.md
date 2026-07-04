---
description: "Use when implementing or reviewing frontend Vue.js screens, CSS, axios API calls, or prototype files. Covers file structure, component definition, state management, Vue API usage, view switching, Lucide icons, CSS conventions, mock data, security, and the axios HTTP client rules."
applyTo: ["src/main/resources/static/**", "doc/prototype/**"]
---

# フロントエンド製造ルール

## ファイル構成

- 全般: `src/main/resources/static/` 配下に配置する。
- JS: `src/main/resources/static/js/` 配下に配置する（Vue 3 Composition API、CDN 読み込み、単一ファイル構成）
- CSS: `src/main/resources/static/css/` 配下に配置する
- HTTP クライアント（モジュール版）: `src/main/resources/static/js/services/apiClient.js`（axios インスタンスを集約）
- `app.standalone.js` では CDN `<script>` タグで axios を読み込む（`https://cdn.jsdelivr.net/npm/axios@1.x/dist/axios.min.js`）。
- 新たなファイルを増やさず、既存2ファイルへの追記で完結させる（`apiClient.js` を除く）。

## コンポーネント定義

- コンポーネントは `{ template, props, emits, setup() }` のプレーンオブジェクト形式で定義する。
- Vue API は `const { createApp, computed, onMounted, reactive, ref, watch, nextTick } = Vue;` からの分割代入で使用する。
- 各画面は1コンポーネント（`XxxView`）として定義し、1コンポーネント・1責務（一覧 / フォーム / 詳細）を維持する。
- `template` は文字列リテラルで定義する。

## 状態管理

- 全画面共通の状態（ユーザー情報・表示ビュー・一覧データ・通知）は root `App` の `setup()` が `ref` / `reactive` で保持する。
- 子コンポーネントはローカル状態（フォーム入力・UI フラグ）のみを管理する。
- 親→子のデータ受け渡しは `props`、子→親のイベントは `emits` で一方向データフローを維持する。
- フォームコンポーネントは `watch(() => props.editingXxx, ...)` で props 変化をローカル `formData` へ同期する。

## Vue API の使い分け

- `ref`: プリミティブ値・単体オブジェクト（`loading`, `view`, `currentReport` など）
- `reactive`: フォームデータオブジェクト（`formData`）
- `computed`: 権限判定・表示制御などの派生値（`canFeedback`, `canEdit`, `canEscalate` など）
- `watch`: props 変化に伴うフォーム同期
- `onMounted`: 初回データ取得
- `nextTick`: DOM 更新後に `lucide.createIcons()` を呼び出す場合

## Props / Emits

- `props` は配列形式（`props: ["fieldA", "fieldB"]`）で宣言する。
- `emits` は配列形式（`emits: ["save", "cancel"]`）で宣言する。
- イベント名はケバブケース（`open-form`, `save-feedback`）を使用する。

## ビュー切り替え

- 表示画面は root の `view`（`ref<string>`）で管理し、値は `"list"` / `"form"` / `"detail"` / `"escalation-list"` / `"escalation-form"` / `"escalation-detail"` / `"admin-users"` / `"admin-user-form"` / `"admin-groups"` / `"admin-office-form"` / `"admin-team-form"` とする。
- テンプレートの `v-show="view === 'xxx'"` でビューを切り替える（`v-if` は状態消失を伴うため原則使用しない）。

## Lucide アイコン

- アイコンは `<i data-lucide="icon-name" class="icon-{size}"></i>` で埋め込む。
- サイズクラス: `icon-xs / icon-sm / icon-md / icon-lg / icon-xl`
- カラークラス: `icon-accent / icon-muted / icon-success`
- アイコンを含む DOM が描画された後に `nextTick(() => lucide.createIcons())` を呼び出す。

## CSS

- 色・スペーシングなどのデザイントークンは `:root` のカスタムプロパティ（`var(--primary)`, `var(--surface-bg)` 等）で参照し、カラーコードをインライン指定しない。
- 新規トークンを追加する場合は既存の命名規則 `--{category}-{variant}` に従い、`:root` ブロックへ追記する。
- クラス名は機能プレフィックス付きの BEM 的命名（例: `.esc-card`, `.esc-card-header`）とする。
- 新規コンポーネントのスタイルは `prototype.css` の末尾に追記する。

## モックデータ

- ローカル動作用モックデータ（`MOCK_REPORTS`, `MOCK_ESCALATIONS` 等）は `app.standalone.js` の先頭（Vue 分割代入の直後）にまとめて定義する。
- ID は機能ごとのプレフィックス付き文字列形式（月報: `Rxxxxxxx`、エスカレーション: `ESCxxx`）で統一する。

## セキュリティ

- ユーザー入力値を `v-html` で直接描画しない（XSS 防止）。
- テンプレート内でユーザー入力を表示する場合は `{{ value }}` テキスト補間を使用する。

## HTTP クライアント（axios）

- API 通信は必ず axios インスタンス経由で行う（`fetch` / `XMLHttpRequest` の直接使用を禁止）。
- axios インスタンスは `services/apiClient.js`（モジュール版）または `app.standalone.js` 内の即時関数スコープ（スタンドアロン版）に一元定義する。
- インスタンス共通設定:
  - `baseURL`: `/api/v1`
  - `headers`: `{ "Content-Type": "application/json" }`
- **リクエストインターセプター**: `localStorage` から `authToken` を取得し `Authorization: Bearer <token>` を付与する。
- **レスポンスインターセプター**:
  - `resultStatus !== "0"` の場合は `new Error(resultMsg)` を reject する。
  - 正常時は `response.data.params` を resolve する（`ApiResponse` アンラップ）。
  - HTTP 401 の場合はログアウト処理（`handleLogout`）を呼び出す。
- 各サービスは axios インスタンスが resolve した `params` を受け取るだけにとどめ、`ApiResponse` の構造に直接依存しない。

## コメント

- 意味の明確なコードを書くことを優先し、冗長なコメントは避ける。
- 分岐、ループ、例外処理、メソッドの呼び出しなどの複雑なロジックには、処理の目的や意図を説明するコメントを付与する。
- TODO コメントは、必要な修正や改善点を明確に記述する。
- 例外処理には、catch ブロック内で発生したエラーの内容や対処方法を説明するコメントを付与する。
- 設計書や仕様書との関連がある場合は、その関連性をコメントで明示する。
# MonthlyReport Copilot Instructions

## 目的

このリポジトリで GitHub Copilot が提案する設計・実装・単体テストを、現行アーキテクチャと仕様に一致させる。

## 前提アーキテクチャ

- Java 21 + Spring Boot。
- API は原則 POST、ベースパスは `/api/v1`。
- Controller -> Service -> Repository（InMemoryDataStore）の責務分離を維持する。
- 共通レスポンスは `ApiResponse` を使用する。
- エラーは `BusinessException + ErrorCodes + MessageKeys/messages.properties` を使用する。
- レスポンスの Map キーは `ResponseKeys` を使用し、文字列リテラルを新規追加しない。
- バリデーション定数は `ValidationConstants` を使用する。
- フロントエンド（プロトタイプ）の API 通信には **axios** を使用する（ネイティブ `fetch` / `XMLHttpRequest` は使用しない）。

## 仕様準拠ルール

### 仕様確認

- 仕様変更を伴う実装時は、次の文書との整合を必ず確認する。
  - `doc/システム要件定義/API設計方針.md`
  - `doc/システム要件定義/API仕様書.md`
  - `doc/システム要件定義/API詳細設計/*.md`
  - `doc/システム要件定義/UI設計書.md`
  - `doc/システム要件定義/UI設計(画面別)/*.md`
  - `doc/画面要件/画面要件.html`（UI外観・操作フローの一次基準）
- 現時点の実装は暫定版として扱い、仕様書と矛盾する場合は実装より仕様を正とする。
- 実装由来の制約を新仕様として逆輸入しない（仕様書に明記された内容のみを採用する）。

### ロールと可視範囲

#### グループ定義

- チーム: エンジニアのメンバーの集合。
- グループ: エンジニアのチームの集合。グループは営業所に対応する。
- 営業所: グループの集合。
- 営業： 営業所に所属する営業担当の集合。
- システム管理者: 全ての営業所・グループ・チームを横断的に管理する。

#### ロール定義

- NG: 月報の提出や閲覧は不可。エスカレーション機能へのアクセス不可。
- TM: 月報提出と自分の月報のみ。エスカレーション機能へのアクセス不可。
- TL: 自分の月報の提出、自チームの月報とエスカレーションを管理。
- GL: 自分の月報の提出、自グループの月報とエスカレーションを管理。
- OM: 営業所に所属する全件の月報とエスカレーションを管理。
- SP: 自身が担当となっているエスカレーションを管理。
- SM: 営業所に所属する全てのエスカレーションを管理。
- SA: 月報とエスカレーションの内容はマスクして表示。営業所の追加やユーザー管理など全てのロールを横断的に管理。
- ロールの上下関係: `NG < TM < TL < GL < OM`、`SP` と `SM` は営業担当者・支店長としてエスカレーション管理の特例ロール、`SA` は全てのロールを横断的に管理する特例ロール。

#### 月報

- NG: 月報の提出不可。月報の閲覧不可。
- TM: 自分の月報のみ。
- TL: 自分の月報 + 同一チームの月報。
- GL: 自分の月報 + 同一グループの月報。
- OM: 自分の月報 + 営業所に所属する全件の月報。
- SP: 自分が担当となっている月報。
- SM: 営業所に所属する全件の月報。
- SA: 月報自体へは画面からアクセス可能。月報の内容はマスクしたものを表示。

#### エスカレーション

- NG: エスカレーション機能へのアクセス不可。
- TM: エスカレーション機能へのアクセス不可。
- TL: 自チームに関するエスカレーション + 自分が起票したエスカレーション。
- GL: 自グループのエスカレーション全件。
- OM: 営業所に所属する全件のエスカレーション。
- SP: 自分が担当となっているエスカレーション全件。
- SM: 営業所に所属する全件のエスカレーション。
- SA: エスカレーション自体へは画面からアクセス可能。エスカレーションの内容はマスクしたものを表示。

### 業務制約

#### 月報

- 月報の重複禁止: 同一ユーザーによる同一月（YYYY-MM）の月報作成は 1 件。
- 編集権限: 月報は作成者本人のみ。
- 削除権限: 月報は作成者本人、または OM のみ。
- 回答（フィードバック）: TL 以上かつ他者の月報に対してのみ可能。

#### エスカレーション

- 起票権限: TL 以上のロールのみ（REPORTER は不可）。
- 参照・更新権限: ロール別可視範囲に準ずる。
- ステータス遷移: `PENDING（未対応）→ ONGOING（対応中）→ RESOLVED（解決済み）`（逆方向も可）。
- 重要度変更: `LOW（低）` / `MEDIUM（中）` / `HIGH（高）` 間の変更はいつでも可能。
- 対応担当者の変更: TL 以上のロールであればいつでも可能。ただし、担当者を未設定にすることはできない。
- 必ず対応期日を設定すること。
- ステータスを`RESOLVED（解決済み）`とする場合、完了期日を設定すること。
- 対応ログ（履歴）の追記は詳細画面から随時可能。削除は不可。

### 提出期限

- 締切日は毎月 5 日（`DEADLINE_DAY = 5`）とする。
- 提出のリマインドは提出するまで、報告月の前月25日、当月は1日以降毎日行う。
- 締切の判定・通知・リマインドの仕様を変更する場合は、API仕様書と UI 設計書の両方を更新する。
- 提出された月報へのTL以上のフィードバックは、報告月の翌月5日まで可能とする。

### UI・データ仕様

- 呼称: ロールの表示は以下のとおりとする。
  - NG: 新卒
  - TM: メンバー
  - TL: チームリーダー
  - GL: グループリーダー
  - OM: オフィスマネージャー
  - SP: 営業担当者
  - SM: 支店長
  - SA: システム管理者

#### コンディション定義（4段階）

| UI表示 | API値 | 説明 |
|--------|-------|------|
| ◎ | BEST | 絶好調 |
| ○ | GOOD | まぁまぁ普通 |
| ▲ | WARN | ちょっと不調 |
| × | NG   | もうだめ最悪 |

- UI ↔ API 間は上記マッピングで相互変換する。
- コンディション自己診断の項目は 体調・ストレス・人間関係・悩み・疲れ・睡眠・やる気 の 7 項目固定。

#### 月報ステータス（バッジ表示）

- `NotSubmitted`: 月報が未提出。
- `Checked`: `adminFeedback` が登録済み（回答あり）。
- `Reviewing`: `adminFeedback` が未登録（回答待ち）。

#### エスカレーション定義

- ステータス:
  - `PENDING（未対応）`
  - `ONGOING（対応中）`
  - `RESOLVED（解決済み）`
- 重要度:
  - `LOW（低）`
  - `MEDIUM（中）`
  - `HIGH（高）`

#### ダッシュボード仕様

- 提出期限バナーは全ロール共通でローダー直下（ヘッダー下）に固定表示する。
  - 今月（YYYY-MM）の残日数を表示。
  - 自分が今月分を提出済みか否かで表示を切り替える（未提出は警告、提出済みは完了表示）。
- TL 以上には以下を追加表示する。
  - 今月の提出完了率（%）と進捗バー。
  - 未提出メンバー一覧（チップ形式）。全員提出済みの場合は完了メッセージを表示。

#### 月報詳細・フォームの表示順（維持すること）

1. 営業情報
2. 来月残業見込み（数値 + 理由）
3. 今月残業見込み（数値 + 理由）
4. コンディション自己診断（7項目）
5. コメント（問題点・意見など）

## 設計ルール

- 新規機能は最初に API 契約を明確化する。
  - エンドポイント
  - リクエスト DTO
  - レスポンス params のキー
  - 業務エラーコード
- 1 メソッド 1 責務を維持し、複雑分岐は private メソッドへ抽出する。
- 外部公開メソッドには Javadoc を付与し、インプット/アウトプットを明記する。
- 詳細設計には次の見出しを含める。
  - タイトル（API-xx 詳細設計）
  - 目次
  - 概要
  - 前提条件
  - 入出力仕様
  - ロジックフロー
  - 例外ケース
  - CRUD
  - SQL（必要な場合）
- API詳細設計の見出しは必ず採番する。
  - 採番形式は整数のみの `x` / `x.x` / `x.x.x` / `x.x.x.x`（最大4階層）とする。
  - 階層の区切りは `.` を使用し、5階層以上の見出しは作成しない。
- 仕様の重要値は定数化する。
  - 正規表現・範囲値: `ValidationConstants`
  - メッセージキー: `MessageKeys`
  - レスポンスキー: `ResponseKeys`

## 製造ルール

### 全般

- コードは効率よりも可読性と保守性を優先して記述する。
- 命名は一貫性を保ち、プロジェクトの既存規約に従う。
- コメントは設計書との整合を保ち、コードの意図やロジックの説明に限定する。冗長なコメントは避ける。
- 例外は適切にキャッチし、ユーザーにわかりやすいエラーメッセージを提供する。ログには技術的な詳細を記録する。

### API(バックエンド)

#### 全般
- クラスには Javadoc を付与し、役割を明記する。
- メソッドには Javadoc を付与し、インプット/アウトプットを明記する。
- ロジックフローはシンプルに保ち、複雑な分岐は private メソッドへ抽出する。
- API のエンドポイントは RESTful な命名規則を使用する（例: `POST /api/v1/reports/search`）。
- マジックナンバーや文字列を直接コードに埋め込まず、定数クラス（`ValidationConstants`, `MessageKeys`, `ResponseKeys`）を使用する。ただし、その定数がプロジェクト全体で再利用されない場合は、同一クラス内の private static final 定数として定義することも許容する。

#### Controller

- `@PostMapping` を使用する。
- `@Valid DTO` を受け取り、サービス戻り値を `ApiResponse.ok(...)` で返す。
- 業務ロジックを書かない。

#### Service

- 業務判断・認可・整形を担当する。
- 例外文言は `messageSource + MessageKeys` で取得する。
- 文字列メッセージを直接 throw しない。

#### DTO

- request DTO は `dto/request` パッケージに配置する。
- request DTO は `record` を維持する。
- バリデーションは Bean Validation で定義する。
- `@Min/@Max/@Size/@Pattern` の値は `ValidationConstants` を参照する。
- Javadoc にインプット/アウトプットを記載する。

#### Constants / Utils / Model

- 定数クラスは `final class + private コンストラクタ` で定義する。
- 共通化できる処理は Utils に切り出す。
- モデルは用途に応じて Lombok（`@Data`, `@NoArgsConstructor`, `@AllArgsConstructor` など）を使用する。
- 既存の命名・パッケージ規約を崩さない。

### 画面

#### ファイル構成

- 全般: `src/main/resources/static/` 配下に配置する。
- JS: `src/main/resources/static/js/`配下に配置する（Vue 3 Composition API、CDN 読み込み、単一ファイル構成）
- CSS: `src/main/resources/static/css/`配下に配置する
- HTTP クライアント（モジュール版）: `src/main/resources/static/js/services/apiClient.js`（axios インスタンスを集約）
- `app.standalone.js` では CDN `<script>` タグで axios を読み込む（`https://cdn.jsdelivr.net/npm/axios@1.x/dist/axios.min.js`）。
- 新たなファイルを増やさず、既存2ファイルへの追記で完結させる（`apiClient.js` を除く）。

#### コンポーネント定義

- コンポーネントは `{ template, props, emits, setup() }` のプレーンオブジェクト形式で定義する。
- Vue API は `const { createApp, computed, onMounted, reactive, ref, watch, nextTick } = Vue;` からの分割代入で使用する。
- 各画面は1コンポーネント（`XxxView`）として定義し、1コンポーネント・1責務（一覧 / フォーム / 詳細）を維持する。
- `template` は文字列リテラルで定義する。

#### 状態管理

- 全画面共通の状態（ユーザー情報・表示ビュー・一覧データ・通知）は root `App` の `setup()` が `ref` / `reactive` で保持する。
- 子コンポーネントはローカル状態（フォーム入力・UI フラグ）のみを管理する。
- 親→子のデータ受け渡しは `props`、子→親のイベントは `emits` で一方向データフローを維持する。
- フォームコンポーネントは `watch(() => props.editingXxx, ...)` で props 変化をローカル `formData` へ同期する。

#### Vue API の使い分け

- `ref`: プリミティブ値・単体オブジェクト（`loading`, `view`, `currentReport` など）
- `reactive`: フォームデータオブジェクト（`formData`）
- `computed`: 権限判定・表示制御などの派生値（`canFeedback`, `canEdit`, `canEscalate` など）
- `watch`: props 変化に伴うフォーム同期
- `onMounted`: 初回データ取得
- `nextTick`: DOM 更新後に `lucide.createIcons()` を呼び出す場合

#### Props / Emits

- `props` は配列形式（`props: ["fieldA", "fieldB"]`）で宣言する。
- `emits` は配列形式（`emits: ["save", "cancel"]`）で宣言する。
- イベント名はケバブケース（`open-form`, `save-feedback`）を使用する。

#### ビュー切り替え

- 表示画面は root の `view`（`ref<string>`）で管理し、値は `"list"` / `"form"` / `"detail"` / `"escalation-list"` / `"escalation-form"` / `"escalation-detail"` とする。
- テンプレートの `v-show="view === 'xxx'"` でビューを切り替える（`v-if` は状態消失を伴うため原則使用しない）。

#### Lucide アイコン

- アイコンは `<i data-lucide="icon-name" class="icon-{size}"></i>` で埋め込む。
- サイズクラス: `icon-xs / icon-sm / icon-md / icon-lg / icon-xl`
- カラークラス: `icon-accent / icon-muted / icon-success`
- アイコンを含む DOM が描画された後に `nextTick(() => lucide.createIcons())` を呼び出す。

#### CSS

- 色・スペーシングなどのデザイントークンは `:root` のカスタムプロパティ（`var(--primary)`, `var(--surface-bg)` 等）で参照し、カラーコードをインライン指定しない。
- 新規トークンを追加する場合は既存の命名規則 `--{category}-{variant}` に従い、`:root` ブロックへ追記する。
- クラス名は機能プレフィックス付きの BEM 的命名（例: `.esc-card`, `.esc-card-header`）とする。
- 新規コンポーネントのスタイルは `prototype.css` の末尾に追記する。

#### モックデータ

- ローカル動作用モックデータ（`MOCK_REPORTS`, `MOCK_ESCALATIONS` 等）は `app.standalone.js` の先頭（Vue 分割代入の直後）にまとめて定義する。
- ID は機能ごとのプレフィックス付き文字列形式（月報: `Rxxxxxxx`、エスカレーション: `ESCxxx`）で統一する。

#### セキュリティ

- ユーザー入力値を `v-html` で直接描画しない（XSS 防止）。
- テンプレート内でユーザー入力を表示する場合は `{{ value }}` テキスト補間を使用する。

#### HTTP クライアント（axios）

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

## 単体テストルール

### テストレベル

- API 契約テスト: MockMvc で Controller 経由の E2E 風テストを優先。
- サービステスト: 権限分岐・境界値・重複チェックの失敗系を重点化。アウトプットはフル検証。
- SQL: 外部結合条件の抜け漏れや誤りを防止するため、必要に応じて Repository レベルで SQL の正当性を検証する。

### 最低カバレッジ（機能追加時）

- Controller: C0, C1ともに100%。
- Service: C0 100%、C1 100%。ただし、お作法的に到達不可能な例外ケースは C1 免除可（例: `IllegalArgumentException` の発生パターンなど）。
- Repository: C0 100%、C1 100%。ただし、InMemoryDataStore の単純な getter/setter は C1 免除可。
- DTO: C0 100%。
- Utils / Constants: C0 100%.

### 検証観点

- `resultStatus/resultCd/resultMsg/params` の整合。
- `ResponseKeys` のキー名が契約どおりであること。
- `MessageKeys` 由来の文言が返ること。

### テストデータ

- InMemoryDataStore の初期データ前提を崩さない。
- 追加データはテスト内で明示し、ケース間で状態依存を作らない。

## 変更時チェックリスト

- メッセージ・レスポンスキー・制約値の文字列リテラルを新規追加していないか。
- 例外が `BusinessException` と `ErrorCodes` で統一されているか。
- 仕様書・API 詳細設計との齟齬がないか。
- API詳細設計の見出し採番が `x` / `x.x` / `x.x.x` / `x.x.x.x`（最大4階層）になっているか。
- 新規/変更 API に対するテストを追加したか。
- `./mvnw compile` と関連テストが通るか。
- フロントエンドで `fetch` / `XMLHttpRequest` を直接使用していないか（axios インスタンス経由であること）。
- レスポンスインターセプターが `ApiResponse` をアンラップしているか（サービス層で `resultStatus` を直接参照していないか）。

## Copilot 依頼テンプレート

### 設計依頼

「API仕様書と API-xx 詳細設計に準拠して、XXX 機能の Controller/Service/DTO 変更方針を提案。ResponseKeys/MessageKeys/ValidationConstants を必ず利用すること。」

### 実装依頼

「既存の責務分離を維持して XXX を実装。Controller に業務ロジックを書かず、Service で認可と業務判定を行い、例外文言は messages.properties から取得すること。」

### テスト依頼

「XXX の MockMvc テストを追加。正常系・権限違反・バリデーション異常・業務制約違反を各 1 ケース以上作成し、resultStatus/resultCd/params を検証すること。」

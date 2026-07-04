---
description: "Use when asking Copilot to design an API, implement a feature, or write tests. Contains standard prompt templates for design requests, implementation requests, and test requests."
---

# Copilot 依頼テンプレート

## 設計依頼

「API仕様書と API-xx 詳細設計に準拠して、XXX 機能の Controller/Service/DTO 変更方針を提案。ResponseKeys/MessageKeys/ValidationConstants を必ず利用すること。」

## 実装依頼

「既存の責務分離を維持して XXX を実装。Controller に業務ロジックを書かず、Service で認可と業務判定を行い、例外文言は messages.properties から取得すること。」

## テスト依頼

「XXX の MockMvc テストを追加。正常系・権限違反・バリデーション異常・業務制約違反を各 1 ケース以上作成し、resultStatus/resultCd/params を検証すること。」

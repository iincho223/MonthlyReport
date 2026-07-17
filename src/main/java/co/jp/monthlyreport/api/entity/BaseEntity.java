package co.jp.monthlyreport.api.entity;

import jakarta.persistence.Column;
import jakarta.persistence.MappedSuperclass;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.Setter;

/**
 * 全テーブル共通の WHO カラム（DB設計.md 3章）を保持する基底クラス。
 * インプット: なし。
 * アウトプット: delete_flag / updated_at / updated_by / registered_at / registered_by を持つエンティティ。
 */
@Getter
@Setter
@MappedSuperclass
public abstract class BaseEntity {

  /** 削除フラグ（0:有効 / 1:削除） */
  @Column(name = "delete_flag", nullable = false)
  private boolean deleteFlag;

  /** 更新日時 */
  @Column(name = "updated_at", nullable = false)
  private OffsetDateTime updatedAt;

  /** 更新者（社員番号または "SYSTEM"） */
  @Column(name = "updated_by", nullable = false, length = 50)
  private String updatedBy;

  /** 登録日時 */
  @Column(name = "registered_at", nullable = false)
  private OffsetDateTime registeredAt;

  /** 登録者（社員番号または "SYSTEM"） */
  @Column(name = "registered_by", nullable = false, length = 50)
  private String registeredBy;
}

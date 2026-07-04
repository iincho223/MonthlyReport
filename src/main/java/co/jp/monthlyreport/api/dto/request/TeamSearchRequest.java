package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;

/**
 * チーム一覧検索リクエスト（API-23）。
 * インプット: 検索条件。
 * アウトプット: バリデーション済みの検索条件データ。
 *
 * @param groupCode グループコード絞り込み（任意）
 * @param teamName  チーム名の部分一致検索（任意）
 * @param page      ページ番号（任意、省略時 1）
 * @param size      1ページあたりの件数（任意、省略時 20、最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_PAGE_SIZE}）
 */
public record TeamSearchRequest(
    String groupCode,
    String teamName,
    @Min(ValidationConstants.MIN_PAGE) Integer page,
    @Min(ValidationConstants.MIN_PAGE_SIZE) @Max(ValidationConstants.MAX_PAGE_SIZE) Integer size) {}

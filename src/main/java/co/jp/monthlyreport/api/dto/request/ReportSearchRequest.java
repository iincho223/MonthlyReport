package co.jp.monthlyreport.api.dto.request;

import co.jp.monthlyreport.api.common.ValidationConstants;
import java.util.List;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Pattern;

/**
 * 月報一覧検索リクエスト（API-06）。
 * インプット: 絞り込み条件とページング条件。
 * アウトプット: バリデーション済みの検索条件。
 *
 * @param month  絞り込み年月（yyyy-MM 形式、省略可）
 * @param status ステータス絞り込み（ALL / SUBMITTED / PENDING_FEEDBACK / FEEDBACKED、省略可）
 * @param page   ページ番号（1始まり、省略時は 1）
 * @param size   1ページあたりの件数（最大 {@value co.jp.monthlyreport.api.common.ValidationConstants#MAX_PAGE_SIZE}、省略時は 20）
 * @param sort   ソート条件（省略可）
 */
public record ReportSearchRequest(
    @Pattern(regexp = ValidationConstants.REGEX_YEAR_MONTH_OPTIONAL, message = "{validation.year_month}") String month,
    String status,
    @Min(ValidationConstants.MIN_PAGE) Integer page,
    @Min(ValidationConstants.MIN_PAGE_SIZE) @Max(ValidationConstants.MAX_PAGE_SIZE) Integer size,
    List<String> sort) {}

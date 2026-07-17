package co.jp.monthlyreport.api.common;

import java.security.SecureRandom;
import java.time.Instant;

/**
 * ULID（char(26)）採番ユーティリティ。
 * インプット: なし。
 * アウトプット: 先頭10桁がミリ秒タイムスタンプ、後続16桁が乱数の Crockford Base32 文字列。
 */
public final class UlidGenerator {

  private static final char[] CROCKFORD_BASE32 = "0123456789ABCDEFGHJKMNPQRSTVWXYZ".toCharArray();
  private static final int TIMESTAMP_CHARS = 10;
  private static final int RANDOM_CHARS = 16;
  private static final SecureRandom RANDOM = new SecureRandom();

  private UlidGenerator() {
  }

  /**
   * ULID（26文字）を新規発番する。
   * インプット: なし。
   * アウトプット: report_id / log_id 等に使用する26文字のULID文字列。
   *
   * @return ULID文字列
   */
  public static String generate() {
    StringBuilder builder = new StringBuilder(TIMESTAMP_CHARS + RANDOM_CHARS);
    builder.append(encodeTimestamp(Instant.now().toEpochMilli()));
    builder.append(encodeRandom());
    return builder.toString();
  }

  /**
   * ミリ秒タイムスタンプを10桁の Crockford Base32 へ変換する。
   *
   * @param epochMilli エポックミリ秒
   * @return 10文字のタイムスタンプ部
   */
  private static String encodeTimestamp(long epochMilli) {
    char[] chars = new char[TIMESTAMP_CHARS];
    long value = epochMilli;
    for (int i = TIMESTAMP_CHARS - 1; i >= 0; i--) {
      chars[i] = CROCKFORD_BASE32[(int) (value % 32)];
      value /= 32;
    }
    return new String(chars);
  }

  /**
   * 乱数16桁の Crockford Base32 文字列を生成する。
   *
   * @return 16文字の乱数部
   */
  private static String encodeRandom() {
    byte[] bytes = new byte[RANDOM_CHARS];
    RANDOM.nextBytes(bytes);
    StringBuilder builder = new StringBuilder(RANDOM_CHARS);
    for (byte b : bytes) {
      builder.append(CROCKFORD_BASE32[(b & 0xFF) % 32]);
    }
    return builder.toString();
  }
}

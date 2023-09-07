package co.jp.monthlyreport.util;

import java.text.SimpleDateFormat;
import java.util.Calendar;

import org.springframework.stereotype.Component;

/** 日付関連ユーティリティクラス */
@Component
public class DateUtils {

    /** 現在年月日取得フォーマット */
    private static final String YYYYMMDD = "yyyyMMdd";
    /** 提出月切り替え日 */
    private static final int KIRIKAE_DD= 20;
    
    /**
     * 提出年月の取得.
     * @return 提出年月
     */
    public static String getTeisyutsuYm() {
        // 現在年月日の取得
        Calendar cal = Calendar.getInstance();
        SimpleDateFormat sf = new SimpleDateFormat(YYYYMMDD);
        String ymd = sf.format(cal.getTime());
        Integer dd = Integer.parseInt(ymd.substring(6, 8));

        String result;
        if (dd < KIRIKAE_DD) {
            // 現在の日が20日より前
            result = String.valueOf(Integer.parseInt(ymd.substring(0, 6)) - 1);
        } else {
            // 現在の日が20日以後
            result = ymd.substring(0, 6);
        }
        return result;

    }

}

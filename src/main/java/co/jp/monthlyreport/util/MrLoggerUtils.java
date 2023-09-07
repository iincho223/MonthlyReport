package co.jp.monthlyreport.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * ログ出力クラス.
 */
@Component
public class MrLoggerUtils {
    protected final static Logger logger = LoggerFactory.getLogger(MrLoggerUtils.class);

    public void info(String msg) {
        logger.info(msg);
    }

    public void warn(String msg) {
        logger.warn(msg);
    }

    public void error(String msg) {
        logger.error(msg);
    }

    public void error(String msg, Throwable t) {
        logger.error(msg, t);
    }

    public void debug(String msg) {
        logger.debug(msg);
    }

    public void trace(String msg) {
        logger.trace(msg);
    }
    
}

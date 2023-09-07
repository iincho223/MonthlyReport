package co.jp.monthlyreport.controller;

import org.slf4j.MDC;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;
import org.thymeleaf.util.StringUtils;

import co.jp.monthlyreport.constants.URI;
import co.jp.monthlyreport.dto.UserSession;
import co.jp.monthlyreport.util.MrLoggerUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * ハンドラーインターセプター.
 */
@Component
public class AppHandlerInterceptor implements HandlerInterceptor {
    @Autowired
    private MrLoggerUtils log;

    @Autowired
    private UserSession userInfo;

    /**
     * コントローラ処理前の共通処理
     */
    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
			throws Exception {
        MDC.put("syainNo", userInfo.getSyainNo());
        log.trace("Request prosess start.");
        if (request.getRequestURI().equals(URI.LOGIN.getUri())) {
            // ログイン系からのアクセスの場合、セッションをクリア
            userInfo.setSyainNo(null);
            userInfo.setKaisyaCd(null);
        } else if (!request.getRequestURI().equals("/favicon.ico")) {
            if (StringUtils.isEmpty(userInfo.getSyainNo())) {
                // セッションが無い場合
                log.error("E01-MRE99-9901 : セッション切れ");
                response.sendRedirect(URI.ERROR.getUri());
                return false;
            }
        }
        return true;
    }

    /**
     * コントローラ処理後の共通処理
     */
    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable ModelAndView modelAndView) throws Exception {
        log.trace("Request prosess end.");
        MDC.clear();
	}

    /**
     * リクエスト処理完了後の共通処理
     */
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
			@Nullable Exception ex) throws Exception {
	}

}

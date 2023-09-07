package co.jp.monthlyreport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import co.jp.monthlyreport.dto.response.TopInitResponse;
import co.jp.monthlyreport.service.TopService;

/**
 * Webコントローラ.
 */
@Controller
public class WebController {

    /** ログイン画面 */
    @GetMapping(path="/login")
    public String login(Model model) {
        return "login/login";
    }

    @Autowired
    private TopService topService;

    /** TOP画面 */
    @GetMapping(path="/top")
    public String top(Model model) throws Exception {
        TopInitResponse response = topService.init();
        model.addAttribute("msgList", response.getMsgList());
        model.addAttribute("isMemberSettingView", response.isMenberSettingViewFlg());
        return "top/top";
    }
}

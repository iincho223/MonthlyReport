package co.jp.monthlyreport.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import co.jp.monthlyreport.dto.request.LoginAuthRequest;
import co.jp.monthlyreport.dto.response.LoginAuthResponse;
import co.jp.monthlyreport.service.LoginAuthServise;
import jakarta.servlet.http.HttpServletRequest;

/**
 * Apiコントローラ.
 */
@RestController
public class ApiController {

    @Autowired
    private LoginAuthServise loginAuthServise;

    @PostMapping(path="/login/auth")
    public LoginAuthResponse auth(@RequestBody LoginAuthRequest request, HttpServletRequest httpReq) throws Exception {
        return loginAuthServise.auth(request, httpReq);
    }

}

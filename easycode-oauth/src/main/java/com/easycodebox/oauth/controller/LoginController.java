package com.easycodebox.oauth.controller;

import java.util.Objects;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.WebAttributes;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.context.support.WebApplicationObjectSupport;
import org.springframework.web.servlet.ModelAndView;

/**
 * @author WangXiaoJin
 * @date 2019-06-06 10:49
 */
@Controller
public class LoginController extends WebApplicationObjectSupport {

    @GetMapping("/login")
    public ModelAndView login(String error, HttpServletRequest request) {
        ModelAndView modelAndView = new ModelAndView("/login");
        boolean isError = error != null;
        if (isError) {
            String errorMsg = Objects.requireNonNull(getMessageSourceAccessor())
                .getMessage("AbstractUserDetailsAuthenticationProvider.badCredentials");
            HttpSession session = request.getSession(false);
            if (session != null) {
                AuthenticationException ex = (AuthenticationException) session
                    .getAttribute(WebAttributes.AUTHENTICATION_EXCEPTION);
                if (ex != null) {
                    errorMsg = ex.getMessage();
                }
            }
            modelAndView.addObject("errorMsg", errorMsg);
        }
        return modelAndView;
    }

}

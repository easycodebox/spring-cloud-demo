package com.easycodebox.oauth.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangXiaoJin
 * @date 2019-03-14 16:12
 */
@RestController
public class TestController {

    @GetMapping("/user")
    @Secured("ROLE_USER")
    public Principal user(Principal principal) {
        return principal;
    }

    @GetMapping("/permit")
    @PreAuthorize("permitAll()")
    public String permit() {
        return "SERVER - permit : " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

    @GetMapping("/deny")
    @PreAuthorize("denyAll()")
    public String deny() {
        return "SERVER - deny : " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

}

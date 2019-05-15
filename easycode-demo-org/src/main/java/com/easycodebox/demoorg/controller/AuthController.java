package com.easycodebox.demoorg.controller;

import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import org.springframework.security.access.annotation.Secured;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangXiaoJin
 * @date 2019-01-16 10:04
 */
@RestController
public class AuthController {

    @GetMapping("/user-info")
    @Secured("ROLE_USER")
    public Principal user(Principal principal) {
        return principal;
    }

    @GetMapping("/permit")
    @PreAuthorize("permitAll()")
    public String permit() {
        return "ORG - permit : " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

    @GetMapping("/deny")
    @PreAuthorize("denyAll()")
    public String deny() {
        return "ORG - deny : " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

}

package com.easycodebox.demouser.controller;

import com.easycodebox.demo.api.ShopApi;
import com.easycodebox.demo.model.Org;
import java.security.Principal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
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

    @Autowired
    private ShopApi shopApi;

    @GetMapping("/user-info")
    @Secured("ROLE_USER")
    public Principal user(Principal principal) {
        return principal;
    }

    @GetMapping("/permit")
    @PreAuthorize("permitAll()")
    public String permit() {
        return "USER - permit : " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

    @GetMapping("/deny")
    @PreAuthorize("denyAll()")
    public String deny() {
        return "USER - deny : " + DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(LocalDateTime.now());
    }

    @GetMapping("/org-deny")
    public List<Org> orgDeny() {
        return shopApi.orgDeny();
    }

}

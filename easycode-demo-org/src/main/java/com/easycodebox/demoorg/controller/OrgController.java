package com.easycodebox.demoorg.controller;

import com.easycodebox.demo.model.Org;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangXiaoJin
 * @date 2019-01-16 10:04
 */
@RestController
public class OrgController {

    @Autowired
    private ServerProperties serverProperties;

    @PostMapping("/org")
    public Org add(Org org) {
        org.setName(serverProperties.getPort() + " - " + org.getName());
        return org;
    }

    @GetMapping("/org/{id}")
    public Org load(@PathVariable Long id) {
        Org org = new Org();
        org.setId(id);
        org.setName(serverProperties.getPort() + " - Org name");
        return org;
    }

    @GetMapping("/org")
    public List<Org> list() {
        return createObjs("Org name");
    }

    @GetMapping("/org2")
    @PreAuthorize("denyAll()")
    public List<Org> list2() {
        return createObjs("Org name2");
    }

    @GetMapping("/test")
    public String test() {
        return "test : " + serverProperties.getPort();
    }

    private List<Org> createObjs(String baseName) {
        List<Org> objs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Org obj = new Org();
            obj.setId((long) i);
            obj.setName(serverProperties.getPort() + " - " + baseName + " - " + i);
            objs.add(obj);
        }
        return objs;
    }

}

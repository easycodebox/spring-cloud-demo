package com.easycodebox.demouser.controller;

import com.easycodebox.demo.api.OrgApi;
import com.easycodebox.demo.api.ShopApi;
import com.easycodebox.demo.model.Org;
import com.easycodebox.demo.model.User;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangXiaoJin
 * @date 2019-01-16 10:04
 */
@RestController
public class UserController {

    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private OrgApi orgApi;
    @Autowired
    private ShopApi shopApi;

    @PostMapping("/user")
    public User add(@RequestBody User user) {
        user.setName(serverProperties.getPort() + " - " + user.getName());
        if (!CollectionUtils.isEmpty(user.getOrgs())) {
            user.getOrgs().replaceAll(org -> orgApi.add(org));
        }
        return user;
    }

    @GetMapping("/user/{id}")
    public User load(@PathVariable Long id) {
        User user = new User();
        user.setId(id);
        user.setName(serverProperties.getPort() + " - User name");
        Org org = orgApi.load(id);
        user.setOrgs(Collections.singletonList(org));
        return user;
    }

    @GetMapping("/user")
    public List<User> list() {
        shopApi.list();
        //List<Org> orgs = orgApi.list();
        List<User> users = createObjs("User name");
        //users.forEach(user -> user.setOrgs(orgs));
        return users;
    }

    @GetMapping("/user2")
    public List<User> list2() {
        List<Org> orgs = orgApi.list();
        List<User> users = createObjs("User name2");
        users.forEach(user -> user.setOrgs(orgs));
        return users;
    }

    @GetMapping("/test")
    public String test() {
        return "test : " + serverProperties.getPort();
    }

    private List<User> createObjs(String baseName) {
        List<User> objs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            User obj = new User();
            obj.setId((long) i);
            obj.setName(serverProperties.getPort() + " - " + baseName + " - " + i);
            objs.add(obj);
        }
        return objs;
    }

}

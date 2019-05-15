package com.easycodebox.demoshop.controller;

import com.easycodebox.demo.api.OrgApi;
import com.easycodebox.demo.model.Shop;
import java.util.ArrayList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangXiaoJin
 * @date 2019-01-16 10:04
 */
@RestController
public class ShopController {

    @Autowired
    private ServerProperties serverProperties;
    @Autowired
    private OrgApi orgApi;

    @PostMapping("/shop")
    public Shop add(Shop shop) {
        shop.setName(serverProperties.getPort() + " - " + shop.getName());
        return shop;
    }

    @GetMapping("/shop/{id}")
    public Shop load(@PathVariable Long id) {
        Shop shop = new Shop();
        shop.setId(id);
        shop.setName(serverProperties.getPort() + " - Shop name");
        return shop;
    }

    @GetMapping("/shop")
    public List<Shop> list() {
        orgApi.load(1L);
        return createObjs("Shop name");
    }

    @GetMapping("/shop2")
    public List<Shop> list2() {
        return createObjs("Shop name2");
    }

    @GetMapping("/test")
    public String test() {
        return "test : " + serverProperties.getPort();
    }

    private List<Shop> createObjs(String baseName) {
        List<Shop> objs = new ArrayList<>();
        for (int i = 0; i < 2; i++) {
            Shop obj = new Shop();
            obj.setId((long) i);
            obj.setName(serverProperties.getPort() + " - " + baseName + " - " + i);
            objs.add(obj);
        }
        return objs;
    }

}

package com.easycodebox.demo.api;

import com.easycodebox.demo.model.Org;
import com.easycodebox.demo.model.Shop;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * {@code @FeignClient}值用大写、小写都可以
 *
 * @author WangXiaoJin
 * @date 2019-02-19 21:05
 */
@FeignClient("easycode-demo-shop")
public interface ShopApi {

    @PostMapping("/shop")
    Shop add(@SpringQueryMap Shop shop);

    @GetMapping("/shop/{id}")
    Shop load(@PathVariable Long id);

    @GetMapping("/shop")
    List<Shop> list();

    @GetMapping("/shop2")
    List<Shop> list2();

    @GetMapping("/test")
    String test();

    @GetMapping("/org-deny")
    List<Org> orgDeny();
}

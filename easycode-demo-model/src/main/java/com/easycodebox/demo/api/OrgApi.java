package com.easycodebox.demo.api;

import com.easycodebox.demo.model.Org;
import java.util.List;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.cloud.openfeign.SpringQueryMap;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;

/**
 * {@code @FeignClient}值用大写、小写都可以
 *
 * @author WangXiaoJin
 * @date 2019-02-19 21:05
 */
@FeignClient("easycode-demo-org")
public interface OrgApi {

    @PostMapping("/org")
    Org add(@SpringQueryMap Org org);

    @GetMapping("/org/{id}")
    Org load(@PathVariable Long id);

    @GetMapping("/org")
    List<Org> list();

    @GetMapping("/org2")
    List<Org> list2();

    @GetMapping("/test")
    String test();

}

package com.easycodebox.turbine.controller;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author WangXiaoJin
 * @date 2019-01-16 10:04
 */
@RestController
public class GlobalController {

    @Autowired
    private DiscoveryClient discoveryClient;

    /**
     * 返回指定Service的所有实例信息
     *
     * @param svc ServiceId，默认为${spring.application.name}
     * @return 返回实例列表
     */
    @GetMapping("/svc-instance/{svc}")
    public List<ServiceInstance> svcInstance(@PathVariable String svc) {
        return discoveryClient.getInstances(svc);
    }

    /**
     * 返回所有的服务
     *
     * @return 返回服务列表
     */
    @GetMapping("/svc")
    public List<String> svc() {
        return discoveryClient.getServices();
    }

}

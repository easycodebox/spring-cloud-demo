package com.easycodebox.zuul.config;

import com.easycodebox.spring.cloud.ribbon.DefaultRibbonConfiguration;
import com.easycodebox.spring.cloud.ribbon.DefaultRibbonLoadBalancedRetryFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryFactory;
import org.springframework.cloud.netflix.ribbon.RibbonClients;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author WangXiaoJin
 * @date 2019-02-25 13:47
 */
@Configuration
@RibbonClients(defaultConfiguration = DefaultRibbonConfiguration.class)
public class RibbonConfig {

    /**
     * 提供{@link org.springframework.retry.RetryListener}功能
     * @param clientFactory clientFactory
     * @return LoadBalancedRetryFactory
     */
    @Bean
    public LoadBalancedRetryFactory retryFactory(SpringClientFactory clientFactory) {
        return new DefaultRibbonLoadBalancedRetryFactory(clientFactory);
    }

}

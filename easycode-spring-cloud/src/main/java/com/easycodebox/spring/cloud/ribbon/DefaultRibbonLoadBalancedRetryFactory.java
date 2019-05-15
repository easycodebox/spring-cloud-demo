package com.easycodebox.spring.cloud.ribbon;

import java.util.Map;
import org.springframework.cloud.netflix.ribbon.RibbonLoadBalancedRetryFactory;
import org.springframework.cloud.netflix.ribbon.SpringClientFactory;
import org.springframework.retry.RetryListener;
import org.springframework.retry.backoff.BackOffPolicy;
import org.springframework.util.CollectionUtils;

/**
 * 扩展{@link RibbonLoadBalancedRetryFactory}类的{@link RetryListener}及{@link BackOffPolicy}功能。
 * <p>
 * 只要Ribbon Client context中提供{@link RetryListener}、{@link BackOffPolicy}，就会被使用。
 *
 * @author WangXiaoJin
 * @date 2019-02-25 12:07
 */
public class DefaultRibbonLoadBalancedRetryFactory extends RibbonLoadBalancedRetryFactory {

    private SpringClientFactory clientFactory;

    public DefaultRibbonLoadBalancedRetryFactory(
        SpringClientFactory clientFactory) {
        super(clientFactory);
        this.clientFactory = clientFactory;
    }

    @Override
    public RetryListener[] createRetryListeners(String service) {
        // 在指定service的spring context中搜索所有RetryListener实例
        Map<String, RetryListener> instances = clientFactory.getInstances(service, RetryListener.class);
        RetryListener[] listeners = new RetryListener[0];
        return CollectionUtils.isEmpty(instances) ? listeners : instances.values().toArray(listeners);
    }

    @Override
    public BackOffPolicy createBackOffPolicy(String service) {
        Map<String, BackOffPolicy> instances = clientFactory.getInstances(service, BackOffPolicy.class);
        return CollectionUtils.isEmpty(instances) ? null : instances.values().iterator().next();
    }
}

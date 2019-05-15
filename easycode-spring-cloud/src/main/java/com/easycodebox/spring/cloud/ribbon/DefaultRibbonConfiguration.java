package com.easycodebox.spring.cloud.ribbon;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.client.loadbalancer.LoadBalancedRetryContext;
import org.springframework.cloud.netflix.ribbon.support.ContextAwareRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.retry.RetryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.retry.RetryListener;
import org.springframework.retry.listener.RetryListenerSupport;

/**
 * 官方文档提到@code @RibbonClients}的defaultConfiguration属性配置的类需要增加{@code @Configuration}注解。
 * <p>
 * 测试后发现不增加{@code @Configuration}注解也可以的。当出现内部类配置了{@code @Configuration}时此类
 * 必须得加上{@code @Configuration}注解，否则无效。
 *
 * @author WangXiaoJin
 * @date 2019-02-25 13:49
 */
public class DefaultRibbonConfiguration {

    @Bean
    public RetryListener logRibbonRetryListener() {
        return new LogRibbonRetryListener();
    }

    /**
     * 打印Ribbon重试相关的日志。因为SpringCloud在实例化AbstractLoadBalancerAwareClient实例时
     * 并没有提供配置LoadBalancerCommand的ExecutionListener属性入口，所以这里使用了Spring Retry Listener
     * 来实现类似功能。
     * <p>
     * <b>注：当重试次数配置成0时，还是会输出<code>retryCount:1</code>相关日志，但实际上没有发送请求。</b>
     */
    static class LogRibbonRetryListener extends RetryListenerSupport {

        private static final Logger log = LoggerFactory.getLogger(LogRibbonRetryListener.class);

        @Override
        public <T, E extends Throwable> void close(RetryContext context, RetryCallback<T, E> callback,
            Throwable throwable) {
            if (log.isInfoEnabled()) {
                log.info("Close - {} - retryCount:{} - throwable:{}", assembleUri(context), context.getRetryCount(),
                    throwable == null ? null : throwable.getMessage());
            }
        }

        @Override
        public <T, E extends Throwable> void onError(RetryContext context, RetryCallback<T, E> callback,
            Throwable throwable) {
            if (log.isInfoEnabled()) {
                log.info("Error - {} - retryCount:{} - throwable:{}", assembleUri(context), context.getRetryCount(),
                    throwable == null ? null : throwable.getMessage());
            }
        }

        @Override
        public <T, E extends Throwable> boolean open(RetryContext context,
            RetryCallback<T, E> callback) {
            if (log.isInfoEnabled()) {
                log.info("Open - {} - retryCount:{}", assembleUri(context), context.getRetryCount());
            }
            return super.open(context, callback);
        }

        private String assembleUri(RetryContext context) {
            StringBuilder sb = new StringBuilder();
            if (context instanceof LoadBalancedRetryContext) {
                LoadBalancedRetryContext con = (LoadBalancedRetryContext) context;
                String method = con.getRequest().getMethodValue();
                String serviceId = null;
                String instanceId = null;
                String uri = con.getRequest().getURI().toString();
                if (con.getRequest() instanceof ContextAwareRequest) {
                    ContextAwareRequest req = (ContextAwareRequest) con.getRequest();
                    serviceId = req.getContext().getServiceId();
                }
                if (con.getServiceInstance() != null) {
                    instanceId = con.getServiceInstance().getInstanceId();
                    serviceId = serviceId == null ? con.getServiceInstance().getServiceId() : serviceId;
                }
                sb.append(method).append(" ").append(serviceId).append(" ").append(uri);
                if (StringUtils.isNotEmpty(instanceId)) {
                    sb.append("[").append(instanceId).append("]");
                }
            }
            return sb.toString();
        }
    }

}

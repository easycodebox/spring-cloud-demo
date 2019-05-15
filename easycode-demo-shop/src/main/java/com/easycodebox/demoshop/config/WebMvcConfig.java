package com.easycodebox.demoshop.config;

import com.easycodebox.spring.cloud.feign.FeignExceptionResolver;
import com.easycodebox.spring.cloud.oauth2.OAuth2ExceptionResolver;
import java.util.List;
import org.springframework.context.MessageSource;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * @author WangXiaoJin
 * @date 2019-04-13 13:09
 */
@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private MessageSource messageSource;

    public WebMvcConfig(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        // 获取 OAuth2Exception 的 HttpErrorCode，并设置到 Response 中
        OAuth2ExceptionResolver auth2ExceptionResolver = new OAuth2ExceptionResolver();
        auth2ExceptionResolver.setMessageSource(messageSource);
        resolvers.add(auth2ExceptionResolver);

        // 获取FeignException的HttpCode，设置到Response中
        FeignExceptionResolver feignExceptionResolver = new FeignExceptionResolver();
        resolvers.add(feignExceptionResolver);
    }

}

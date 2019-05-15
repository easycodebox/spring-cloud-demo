package com.easycodebox.oauth.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableResourceServer;
import org.springframework.security.oauth2.config.annotation.web.configuration.ResourceServerConfigurerAdapter;

/**
 * 想自定义 ResourceServer HttpSecurity，请实现{@link ResourceServerConfigurerAdapter}接口，没必要继承{@link WebSecurityConfigurerAdapter}。
 * <p>
 * 如果想访问{@link WebSecurityConfig}定义的requestMatchers之外的资源，需要提供Token。
 *
 * @author WangXiaoJin
 * @date 2019-04-16 16:09
 */
@Configuration
@EnableResourceServer
public class ResourceServerConfig {

}

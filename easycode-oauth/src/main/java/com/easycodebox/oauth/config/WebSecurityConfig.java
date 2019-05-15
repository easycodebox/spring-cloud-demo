package com.easycodebox.oauth.config;

import com.easycodebox.oauth.security.DefaultJdbcUserDetailsManager;
import javax.sql.DataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.boot.autoconfigure.web.ErrorProperties;
import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyAuthoritiesMapper;
import org.springframework.security.access.hierarchicalroles.RoleHierarchyImpl;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.ObjectPostProcessor;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.DelegatingPasswordEncoder;
import org.springframework.security.crypto.password.MessageDigestPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

/**
 * {@code @EnableWebSecurity(debug = true)} - 打印请求及跳转相关的信息。如果你不想打印信息可省略此注解，因为SpringBoot默认提供。
 * <p>
 * 此{@link Order}值必须小于 3(ResourceServer Order)，否则请求会被 ResourceServer 的 WebSecurityConfigurer拦截
 *
 * @author WangXiaoJin
 * @date 2019-03-27 15:35
 */
@Configuration
@Order(2)
@EnableWebSecurity(debug = true)
@EnableGlobalMethodSecurity(securedEnabled = true, prePostEnabled = true)
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    private DataSource dataSource;
    private ErrorProperties errorProperties;

    @Value("${security.role-hierarchy}")
    private String roleHierarchy;

    public WebSecurityConfig(DataSource dataSource, ServerProperties serverProperties) {
        this.dataSource = dataSource;
        this.errorProperties = serverProperties.getError();
    }

    @Override
    public void configure(WebSecurity web) {
        web.ignoring().requestMatchers(PathRequest.toStaticResources().atCommonLocations());
    }

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.requestMatchers()
            .antMatchers("/login", "/logout", "/oauth/authorize", errorProperties.getPath())
            .and().authorizeRequests()
            .antMatchers(errorProperties.getPath()).permitAll()
            .anyRequest().authenticated()
            .and().formLogin().permitAll()
            .and().logout().permitAll();
    }

    @Override
    protected void configure(AuthenticationManagerBuilder auth) throws Exception {
        auth.userDetailsService(userDetailsService())
            .passwordEncoder(passwordEncoder())
            .withObjectPostProcessor(new ObjectPostProcessor<DaoAuthenticationProvider>() {
                @Override
                public <O extends DaoAuthenticationProvider> O postProcess(O object) {
                    // 定义权限继承的映射关系
                    RoleHierarchyImpl hierarchy = new RoleHierarchyImpl();
                    // 角色继承可以扩展成从数据库中获取
                    hierarchy.setHierarchy(roleHierarchy);
                    object.setAuthoritiesMapper(new RoleHierarchyAuthoritiesMapper(hierarchy));
                    return object;
                }
            });
    }

    /**
     * 此Bean会被{@code InitializeUserDetailsManagerConfigurer}用于全局默认的AuthenticationManagerBuilder中
     *
     * @return UserDetailsService
     */
    @Bean
    @Override
    public UserDetailsService userDetailsService() {
        DefaultJdbcUserDetailsManager manager = new DefaultJdbcUserDetailsManager(dataSource);
        manager.setUserExistsSql("SELECT username FROM u_user WHERE username = ? AND deleted = 0");
        manager.setUsersByUsernameQuery(
            "SELECT id, userNo, username, nickname, password, realname, status, portrait, gender, email, mobile "
                + "FROM u_user WHERE username = ? AND deleted = 0");
        manager.setAuthoritiesByUsernameQuery("SELECT r.id, r.name FROM "
            + "(SELECT ur.roleId FROM u_user_role ur WHERE EXISTS (SELECT u.id FROM u_user u WHERE u.deleted = 0 AND u.username = ? AND u.id = ur.userId)) tmp "
            + "LEFT JOIN u_role r ON tmp.roleId = r.id WHERE r.deleted = 0 AND r.status = 0");
        return manager;
    }

    @SuppressWarnings("deprecation")
    private PasswordEncoder passwordEncoder() {
        DelegatingPasswordEncoder encoder = (DelegatingPasswordEncoder) PasswordEncoderFactories
            .createDelegatingPasswordEncoder();
        // 兼容之前的MD5密码
        encoder.setDefaultPasswordEncoderForMatches(new MessageDigestPasswordEncoder("MD5"));
        return encoder;
    }

}

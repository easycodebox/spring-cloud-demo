package com.easycodebox.oauth.config;

import com.easycodebox.spring.cloud.oauth2.OAuth2AuthenticationMixin;
import com.easycodebox.spring.cloud.oauth2.OAuth2RequestMixin;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.CoreJackson2Module;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * 配置Spring Session
 *
 * @author WangXiaoJin
 * @date 2019-05-15 21:22
 */
@Configuration
public class SessionConfig implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    /**
     * 定义SpringSession默认的Serializer
     *
     * @return RedisSerializer
     */
    @Bean
    public RedisSerializer<Object> springSessionDefaultRedisSerializer() {
        return new GenericJackson2JsonRedisSerializer(objectMapper());
    }

    /**
     * 创建ObjectMapper对象，添加SpringSecurity相关的MixIn类，用于反序列化没有构造器的类。
     * <p>
     * 在最后一步开启了默认的DefaultTyping（{@code mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY)}），
     * 如果不开启此功能，{@link SecurityJackson2Modules#enableDefaultTyping(com.fasterxml.jackson.databind.ObjectMapper)}
     * 会创建{@code WhitelistTypeResolverBuilder}，只有在此白名单、配置了MixIn或明确的映射关系的类才会反序列化成功。
     * <p>
     * 如果你非常重视安全性，强烈建议你不开启默认的DefaultTyping（删除{@code mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY)}），
     * 如果有额外的POJO类需要写入Session，则自定义MixIn或映射关系（反序列规则）。这样会比较繁琐，但更安全。
     * 因为Redis的数据可能会被人恶意篡改成其他敏感类。
     *
     * @return the {@link ObjectMapper} to use
     * @see CoreJackson2Module#setupModule(com.fasterxml.jackson.databind.Module.SetupContext)
     * @see SecurityJackson2Modules#enableDefaultTyping(com.fasterxml.jackson.databind.ObjectMapper)
     */
    private ObjectMapper objectMapper() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        //下面两个MixIn用于RedisAuthorizationCodeServices存储AuthCode
        mapper.addMixIn(OAuth2Authentication.class, OAuth2AuthenticationMixin.class);
        mapper.addMixIn(OAuth2Request.class, OAuth2RequestMixin.class);
        // 开启默认的DefaultTyping
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
        return mapper;
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }
}

package com.easycodebox.oauth.config;

import com.easycodebox.spring.cloud.oauth2.OAuth2AuthenticationMixin;
import com.easycodebox.spring.cloud.oauth2.OAuth2RequestMixin;
import com.fasterxml.jackson.annotation.JsonTypeInfo.As;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectMapper.DefaultTyping;
import org.springframework.beans.factory.BeanClassLoaderAware;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.security.jackson2.SecurityJackson2Modules;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * 配置Redis相关配置
 *
 * @author WangXiaoJin
 * @date 2019-05-30 20:13
 */
@Configuration
public class RedisConfig implements BeanClassLoaderAware {

    private ClassLoader classLoader;

    @Bean
    public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
        RedisTemplate<Object, Object> template = new RedisTemplate<>();
        template.setConnectionFactory(redisConnectionFactory);
        template.setDefaultSerializer(jsonRedisSerializer());
        template.setKeySerializer(RedisSerializer.string());
        template.setHashKeySerializer(RedisSerializer.string());
        return template;
    }

    private GenericJackson2JsonRedisSerializer jsonRedisSerializer() {
        ObjectMapper mapper = new ObjectMapper();
        // 注册SpringSecurity相关的Module
        mapper.registerModules(SecurityJackson2Modules.getModules(classLoader));
        //下面两个MixIn用于RedisAuthorizationCodeServices存储AuthCode
        mapper.addMixIn(OAuth2Authentication.class, OAuth2AuthenticationMixin.class);
        mapper.addMixIn(OAuth2Request.class, OAuth2RequestMixin.class);
        // 开启默认的DefaultTyping
        mapper.enableDefaultTyping(DefaultTyping.NON_FINAL, As.PROPERTY);
        return new GenericJackson2JsonRedisSerializer(mapper);
    }

    @Override
    public void setBeanClassLoader(ClassLoader classLoader) {
        this.classLoader = classLoader;
    }

}

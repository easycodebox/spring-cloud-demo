package com.easycodebox.spring.cloud.oauth2;

import org.springframework.data.redis.core.BoundHashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.code.RandomValueAuthorizationCodeServices;
import org.springframework.util.Assert;

/**
 * 用于把OAuthServer分配给Client的AuthorizationCode存储至Redis
 *
 * @author WangXiaoJin
 * @date 2019-05-30 13:09
 */
public class RedisAuthorizationCodeServices extends RandomValueAuthorizationCodeServices {

    private static final String AUTH_CODE_KEY = "auth_code";

    /**
     * AuthCode存储至Redis的Key前缀
     */
    private String prefix = "";

    private final RedisTemplate<Object, Object> redisTemplate;

    public RedisAuthorizationCodeServices(RedisTemplate<Object, Object> redisTemplate) {
        Assert.notNull(redisTemplate, "RedisTemplate is required.");
        this.redisTemplate = redisTemplate;
    }

    @Override
    protected void store(String code, OAuth2Authentication authentication) {
        redisTemplate.boundHashOps(getKey()).put(code, authentication);
    }

    @Override
    protected OAuth2Authentication remove(String code) {
        BoundHashOperations<Object, Object, Object> ops = redisTemplate.boundHashOps(getKey());
        Object value = ops.get(code);
        if (value != null) {
            ops.delete(code);
        }
        return (OAuth2Authentication) value;
    }

    private String getKey() {
        return prefix + AUTH_CODE_KEY;
    }

    public String getPrefix() {
        return prefix;
    }

    public void setPrefix(String prefix) {
        this.prefix = prefix;
    }

}

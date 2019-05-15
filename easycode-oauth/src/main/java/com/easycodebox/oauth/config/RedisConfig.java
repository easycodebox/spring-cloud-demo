package com.easycodebox.oauth.config;

import io.lettuce.core.ReadFrom;
import org.springframework.boot.autoconfigure.data.redis.LettuceClientConfigurationBuilderCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Redis配置
 *
 * @author WangXiaoJin
 * @date 2019-03-27 15:35
 */
@Configuration
public class RedisConfig {

    /**
     * {@link ReadFrom#MASTER_PREFERRED}配置LettuceClient优先从Master读数据，没有可用的Master时再从Slave读数据。
     * <p>
     * 这样既可以确保Master无效时还能从Slave读数据（相对于{@link ReadFrom#MASTER}来说），
     * 又可最大程度避免读取脏数据（相对于{@link ReadFrom#SLAVE_PREFERRED}/{@link ReadFrom#SLAVE}来说）。
     *
     * @return LettuceClientConfigurationBuilderCustomizer
     */
    @Bean
    public LettuceClientConfigurationBuilderCustomizer lettuceClientCustomizer() {
        return clientConfigurationBuilder -> clientConfigurationBuilder.readFrom(ReadFrom.MASTER_PREFERRED);
    }

}

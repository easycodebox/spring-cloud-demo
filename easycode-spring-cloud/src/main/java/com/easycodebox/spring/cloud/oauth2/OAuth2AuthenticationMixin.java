package com.easycodebox.spring.cloud.oauth2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.provider.OAuth2Request;

/**
 * 定义{@link org.springframework.security.oauth2.provider.OAuth2Authentication}类的序列化/反序列化规则
 *
 * @author WangXiaoJin
 * @date 2019-05-30 20:48
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class OAuth2AuthenticationMixin {

    /**
     * 构造{@link org.springframework.security.oauth2.provider.OAuth2Authentication}对象
     */
    @JsonCreator
    public OAuth2AuthenticationMixin(@JsonProperty("storedRequest") OAuth2Request storedRequest,
        @JsonProperty("userAuthentication") Authentication userAuthentication) {
    }
}

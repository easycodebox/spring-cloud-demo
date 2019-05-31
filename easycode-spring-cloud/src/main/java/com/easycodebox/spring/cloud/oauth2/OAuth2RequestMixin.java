package com.easycodebox.spring.cloud.oauth2;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonTypeInfo;

/**
 * 定义{@link org.springframework.security.oauth2.provider.OAuth2Request}类的序列化/反序列化规则
 *
 * @author WangXiaoJin
 * @date 2019-05-30 20:48
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.CLASS)
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY, getterVisibility = JsonAutoDetect.Visibility.NONE,
    isGetterVisibility = JsonAutoDetect.Visibility.NONE)
public class OAuth2RequestMixin {

}

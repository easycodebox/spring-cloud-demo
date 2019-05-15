package com.easycodebox.spring.cloud.security;

import java.io.Serializable;
import java.security.Principal;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * 实现 {@link Principal} 接口，用于刷新Token时获取准确的 name，否则name值为此类的 toString() 方法返回值，
 * 这会导致AuthServer找不到对应的用户，报用户不存在异常。参考 {@link org.springframework.security.authentication.AbstractAuthenticationToken#getName()}
 *
 * @author WangXiaoJin
 * @date 2019-04-15 17:14
 */
@Data
@EqualsAndHashCode(onlyExplicitlyIncluded = true)
public class AuthUser implements Principal, Serializable {

    private static final long serialVersionUID = -8128594962418360918L;

    /**
     * 用户ID
     */
    @EqualsAndHashCode.Include
    private String userId;

    /**
     * 用户名
     */
    @EqualsAndHashCode.Include
    private String username;

    /**
     * 员工编号
     */
    private String userNo;

    /**
     * 昵称
     */
    private String nickname;

    /**
     * 密码
     */
    private String password;

    /**
     * 真实姓名
     */
    private String realname;

    /**
     * 头像
     */
    private String portrait;

    /**
     * 性别
     */
    private Integer gender;

    /**
     * 邮箱
     */
    private String email;

    /**
     * 手机号
     */
    private String mobile;

    @Override
    public String getName() {
        return username;
    }
}

package com.easycodebox.oauth.security;

import com.easycodebox.spring.cloud.security.SecurityUser;
import java.util.List;
import java.util.Objects;
import javax.sql.DataSource;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.provisioning.JdbcUserDetailsManager;

/**
 * 自定义{@link JdbcUserDetailsManager}功能
 *
 * @author WangXiaoJin
 * @date 2019-04-10 13:23
 */
public class DefaultJdbcUserDetailsManager extends JdbcUserDetailsManager {

    public DefaultJdbcUserDetailsManager() {
        super();
    }

    public DefaultJdbcUserDetailsManager(DataSource dataSource) {
        super(dataSource);
    }

    @Override
    protected List<UserDetails> loadUsersByUsername(String username) {
        return Objects.requireNonNull(getJdbcTemplate()).query(getUsersByUsernameQuery(),
            new String[]{username}, (rs, rowNum) -> {
                int status = rs.getInt("status");
                SecurityUser user = new SecurityUser();
                user.setUserId(rs.getString("id"));
                user.setUsername(rs.getString("username"));
                user.setUserNo(rs.getString("userNo"));
                user.setNickname(rs.getString("nickname"));
                user.setPassword(rs.getString("password"));
                user.setRealname(rs.getString("realname"));
                user.setPortrait(rs.getString("portrait"));
                Object gender = rs.getObject("gender");
                if (gender != null) {
                    user.setGender((Integer) gender);
                }
                user.setEmail(rs.getString("email"));
                user.setMobile(rs.getString("mobile"));
                user.setEnabled(status != 2);
                user.setAccountNonExpired(true);
                user.setAccountNonLocked(status != 1);
                user.setCredentialsNonExpired(true);
                return user;
            });
    }

    @Override
    protected UserDetails createUserDetails(String username, UserDetails userFromUserQuery,
        List<GrantedAuthority> combinedAuthorities) {

        SecurityUser user = (SecurityUser) userFromUserQuery;
        if (!isUsernameBasedPrimaryKey()) {
            user.setUsername(username);
        }
        user.setAuthorities(combinedAuthorities);
        return user;
    }
}

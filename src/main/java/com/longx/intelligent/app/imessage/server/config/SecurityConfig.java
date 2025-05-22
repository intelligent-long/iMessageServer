package com.longx.intelligent.app.imessage.server.config;

import com.longx.intelligent.app.imessage.server.security.AccessEvaluator;
import com.longx.intelligent.app.imessage.server.security.AccessPolicyMapper;
import com.longx.intelligent.app.imessage.server.security.SecurityFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Created by LONG on 2024/3/27 at 9:53 PM.
 */
@Configuration
public class SecurityConfig {

    @Bean
    public AccessEvaluator configureSecurity(AccessPolicyMapper accessPolicyMapper){
        accessPolicyMapper
                .antMatch("/ws")
                .permitAll()
                .antMatch("/auth/register")
                .permitAll()
                .antMatch("/auth/verify_code/send")
                .permitAll()
                .antMatch("/auth/login/imessage_id_user")
                .permitAll()
                .antMatch("/auth/login/email")
                .permitAll()
                .antMatch("/auth/login/verify_code")
                .permitAll()
                .antMatch("/auth/password/reset")
                .permitAll()
                .antMatch("/auth/offline_detail")
                .permitAll()
                .antMatch("/broadcast/media/data/{mediaId}")
                .permitAll()
                .antMatch("/url/**")
                .permitAll()
                .antMatch("/region/**")
                .permitAll()
                .antMatch("/**")
                .authed();
        return new AccessEvaluator(accessPolicyMapper);
    }


    @Bean
    public FilterRegistrationBean<SecurityFilter> filterRegistrationBean() {
        FilterRegistrationBean<SecurityFilter> filterRegistrationBean = new FilterRegistrationBean<>();
        filterRegistrationBean.setFilter(new SecurityFilter());
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }
}

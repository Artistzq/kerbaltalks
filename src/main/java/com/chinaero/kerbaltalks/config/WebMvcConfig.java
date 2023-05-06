package com.chinaero.kerbaltalks.config;

import com.chinaero.kerbaltalks.contorller.interceptor.*;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginTicketInterceptor loginTicketInterceptor;
    private final LoginRequiredInterceptor loginRequiredInterceptor;
    private final AccessLimitInterceptor accessLimitInterceptor;
    private final CSRFDefenceInterceptor csrfDefenceInterceptor;
    private final MessageInterceptor messageInterceptor;

    public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor,
                        LoginRequiredInterceptor loginRequiredInterceptor,
                        AccessLimitInterceptor accessLimitInterceptor,
                        CSRFDefenceInterceptor csrfDefenceInterceptor,
                        MessageInterceptor messageInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
        this.loginRequiredInterceptor = loginRequiredInterceptor;
        this.accessLimitInterceptor = accessLimitInterceptor;
        this.csrfDefenceInterceptor = csrfDefenceInterceptor;
        this.messageInterceptor = messageInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(accessLimitInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(messageInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(csrfDefenceInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}

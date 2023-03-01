package com.chinaero.kerbaltalks.config;

import com.chinaero.kerbaltalks.contorller.interceptor.LoginRequiredInterceptor;
import com.chinaero.kerbaltalks.contorller.interceptor.LoginTicketInterceptor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final LoginTicketInterceptor loginTicketInterceptor;
    private final LoginRequiredInterceptor loginRequiredInterceptor;

    public WebMvcConfig(LoginTicketInterceptor loginTicketInterceptor, LoginRequiredInterceptor loginRequiredInterceptor) {
        this.loginTicketInterceptor = loginTicketInterceptor;
        this.loginRequiredInterceptor = loginRequiredInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(loginTicketInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
        registry.addInterceptor(loginRequiredInterceptor)
                .excludePathPatterns("/**/*.css", "/**/*.js", "/**/*.png", "/**/*.jpg", "/**/*.jpeg");
    }
}

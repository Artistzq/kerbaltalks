package com.chinaero.kerbaltalks.controller.interceptor;

import com.chinaero.kerbaltalks.annotation.AntiCSRF;
import com.chinaero.kerbaltalks.util.CookieUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.lang.reflect.Method;

/**
 * @Author : Artis Yao
 */
@Component
public class CSRFDefenceInterceptor implements HandlerInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(CSRFDefenceInterceptor.class);

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 检查拦截的目标是不是方法
        if (handler instanceof HandlerMethod handlerMethod) {
            Method method = handlerMethod.getMethod();
            AntiCSRF antiCSRF = method.getAnnotation(AntiCSRF.class);

            // 判定有没有注解
            if (antiCSRF != null) {

//                logger.info("need csrf check.");
                // 这里需要从Request Form中取出Token，对比Cookie中的Token，是否一样
                // 从Cookie中获取凭证，登陆后才取得到，且一定取得到，由其他interceptor保证
                String token = CookieUtil.getValue(request, "_csrf");
//                System.out.println(token);
                if (! request.getParameter("_csrf").equals(token)) {
                    return false;
                }
            }
        }
        return true;
    }
}

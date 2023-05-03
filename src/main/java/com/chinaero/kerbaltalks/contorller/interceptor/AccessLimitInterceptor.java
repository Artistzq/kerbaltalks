package com.chinaero.kerbaltalks.contorller.interceptor;

import com.chinaero.kerbaltalks.util.HostHolder;
import com.chinaero.kerbaltalks.util.KerbaltalksUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.concurrent.TimeUnit;

/**
 * 根据IP+用户ID限制访问
 * 访问次数放在Redis里面
 * 过期时间设置很快
 * @Author : Artis Yao
 */
@Component
public class AccessLimitInterceptor implements HandlerInterceptor {

    private final HostHolder hostHolder;
    private final RedisTemplate<String, Integer> redisTemplate;
    private final static Logger logger = LoggerFactory.getLogger(AccessLimitInterceptor.class);

    public AccessLimitInterceptor(HostHolder hostHolder, RedisTemplate redisTemplate) {
        this.hostHolder = hostHolder;
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        int id = hostHolder.getUser().getId();
        String ip = request.getRemoteHost();
        String key = "ACCESS_BAN" + ":" + ip + ":" + id;

        int seconds = 10;
        int times = 2;

        // 检查key是否过期，过期则能访问，否则不允许访问

        Integer value = redisTemplate.opsForValue().get(key);
        if (value == null) {
            redisTemplate.opsForValue().set(key, 1, seconds, TimeUnit.SECONDS);
        } else if (value <= times) {
            redisTemplate.opsForValue().increment(key);
        } else {
            String msg = String.format("IP[%s]的用户[%s]: [%d]秒只能访问[%d]次.", ip, id, seconds, times);
            logger.warn(msg);

            response.setCharacterEncoding("UTF-8");
            response.setContentType("text/html; charset=utf-8");
            try (PrintWriter writer = response.getWriter()) {
                writer.print(msg);
            } catch (IOException e) {
                e.printStackTrace();
            }

            return false;
        }

        return true;
    }
}

package com.chinaero.kerbaltalks.contorller.interceptor;

import com.chinaero.kerbaltalks.entity.User;
import com.chinaero.kerbaltalks.service.MessageService;
import com.chinaero.kerbaltalks.util.HostHolder;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

/**
 * @Author : Artis Yao
 */
@Component
public class MessageInterceptor implements HandlerInterceptor {

    private final HostHolder hostHolder;
    private final MessageService messageService;

    public MessageInterceptor(HostHolder hostHolder, MessageService messageService) {
        this.hostHolder = hostHolder;
        this.messageService = messageService;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        User user = hostHolder.getUser();
        if (user != null && modelAndView != null) {
            int unreadLetterCount = messageService.findUnreadLetterCount(user.getId(), null);
            int unreadNoticeCount = messageService.findUnreadNoticeCount(user.getId(), null);
            modelAndView.addObject("allUnreadCount", unreadLetterCount + unreadNoticeCount);
        }
    }
}

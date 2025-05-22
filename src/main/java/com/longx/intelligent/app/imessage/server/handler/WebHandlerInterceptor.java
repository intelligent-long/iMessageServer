package com.longx.intelligent.app.imessage.server.handler;

import com.longx.intelligent.app.imessage.server.service.SessionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

/**
 * Created by LONG on 2024/3/31 at 9:51 PM.
 */
@Component
public class WebHandlerInterceptor implements HandlerInterceptor {
    @Autowired
    private SessionService sessionService;

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        //让未登陆的Http连接的Session失效
        try {
            HttpSession session = request.getSession();
            if (sessionService.getUserOfSession(session) == null) {
                sessionService.invalidateSession(session);
            }
        }catch (IllegalStateException ignore){}
    }
}
package com.longx.intelligent.app.imessage.server.handler;

import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.value.WebsocketValues;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Created by LONG on 2024/3/31 at 7:58 PM.
 */
@Component
public class WebsocketHandshakeInterceptor implements HandshakeInterceptor {
    @Autowired
    private SessionService sessionService;

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletRequest) {
            HttpSession session = servletRequest.getServletRequest().getSession();
            attributes.put(WebsocketValues.HTTP_SESSION_WEBSOCKET_SESSION_ATTRIBUTE_KEY, session);
        }
        return true;
    }

    @Override
    public void afterHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler wsHandler, Exception exception) {
        //未登陆的Http连接，一律让Session失效
        if (request instanceof ServletServerHttpRequest servletServerRequest) {
            HttpServletRequest servletRequest = servletServerRequest.getServletRequest();
            HttpSession session = servletRequest.getSession();
            if(sessionService.getUserOfSession(session) == null) {
                sessionService.invalidateSession(session);
            }
        }
    }
}

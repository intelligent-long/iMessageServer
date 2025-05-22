package com.longx.intelligent.app.imessage.server.handler;

import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.service.LoggedInWebsocketSessionOperationService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import com.longx.intelligent.app.imessage.server.value.WebsocketValues;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServletServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.support.DefaultHandshakeHandler;

import java.security.Principal;
import java.util.Map;

/**
 * Created by LONG on 2024/3/31 at 7:59 PM.
 */
@Component
public class WebsocketHandshakeHandler extends DefaultHandshakeHandler {
    @Autowired
    private SessionService sessionService;
    @Autowired
    @Lazy
    private LoggedInWebsocketSessionOperationService loggedInWebsocketSessionOperationService;

    @Override
    protected Principal determineUser(ServerHttpRequest request, WebSocketHandler wsHandler, Map<String, Object> attributes) {
        if (request instanceof ServletServerHttpRequest servletServerRequest) {
            String requestURI = ((ServletServerHttpRequest) request).getServletRequest().getRequestURI();
            if (requestURI.equals(WebsocketValues.WEBSOCKET_ENDPOINT)) {
                HttpServletRequest servletRequest = servletServerRequest.getServletRequest();
                HttpSession session = servletRequest.getSession();
                User user = sessionService.getUserOfSession(session);
                if (user != null) {
                    //不论 Websocket Session 能不能关闭成功，都将当前用户的 simpSessions 清空，这样新的关于用户的消息就不会再发给他
                    loggedInWebsocketSessionOperationService.closeAllSimpSessionOfUser(user.getImessageId());
                    //关联 iMessage ID 和 Websocket Session
                    return user::getImessageId;
                }
            }
        }
        return null;
    }
}

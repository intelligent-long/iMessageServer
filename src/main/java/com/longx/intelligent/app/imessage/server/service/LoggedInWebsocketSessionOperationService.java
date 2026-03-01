package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.util.Logger;
import com.longx.intelligent.app.imessage.server.value.WebsocketValues;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketSession;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by LONG on 2024/3/31 at 8:21 PM.
 */
@Service
public class LoggedInWebsocketSessionOperationService {
    @Autowired
    private SimpUserRegistry simpUserRegistry;

    private static final Map<String, List<WebSocketSession>> WEBSOCKET_SESSIONS = new HashMap<>();

    public void holdWebsocketSession(String imessageId, WebSocketSession webSocketSession) {
        synchronized (WEBSOCKET_SESSIONS) {
            List<WebSocketSession> allWebsocketSessionOfUser = WEBSOCKET_SESSIONS.get(imessageId);
            if (allWebsocketSessionOfUser == null)
                allWebsocketSessionOfUser = new ArrayList<>();

            allWebsocketSessionOfUser.add(webSocketSession);
            WEBSOCKET_SESSIONS.put(imessageId, allWebsocketSessionOfUser);
        }
    }

    public void closeAllWebsocketSessionOfUser(String imessageId) throws IOException {
        synchronized (WEBSOCKET_SESSIONS) {
            List<WebSocketSession> allWebsocketSessionOfUser = WEBSOCKET_SESSIONS.get(imessageId);
            if (allWebsocketSessionOfUser != null) {
                for(WebSocketSession webSocketSession : allWebsocketSessionOfUser) {
                    serverActiveClose(webSocketSession);
                }
                WEBSOCKET_SESSIONS.remove(imessageId);
            }
        }
    }

    public void closeOneWebsocketSessionOfUser(String imessageId, String httpSessionId) throws IOException {
        synchronized (WEBSOCKET_SESSIONS) {
            List<WebSocketSession> allWebsocketSessionOfUser = WEBSOCKET_SESSIONS.get(imessageId);
            if (allWebsocketSessionOfUser != null) {
                for(WebSocketSession webSocketSession : allWebsocketSessionOfUser) {
                    HttpSession httpSession = (HttpSession) webSocketSession.getAttributes().get(WebsocketValues.HTTP_SESSION_WEBSOCKET_SESSION_ATTRIBUTE_KEY);
                    if(httpSession.getId().equals(httpSessionId)) serverActiveClose(webSocketSession);
                }
                WEBSOCKET_SESSIONS.remove(imessageId);
            }
        }
    }

    public void serverActiveClose(WebSocketSession webSocketSession) throws IOException {
        Logger.info("服务器主动关闭 Websocket Session > Code: " + WebsocketValues.WEBSOCKET_CLOSE_CODE_SERVER_ACTIVE_CLOSE + ", Reason: " + WebsocketValues.WEBSOCKET_CLOSE_REASON_SERVER_ACTIVE_CLOSE);
        webSocketSession.close(new CloseStatus(WebsocketValues.WEBSOCKET_CLOSE_CODE_SERVER_ACTIVE_CLOSE, WebsocketValues.WEBSOCKET_CLOSE_REASON_SERVER_ACTIVE_CLOSE));
    }

    public void closeForForceClientUpdate(WebSocketSession webSocketSession) throws IOException {
        Logger.info("服务器主动关闭 Websocket Session > Code: " + WebsocketValues.WEBSOCKET_CLOSE_CODE_CLOSE_FOR_CLIENT_NEED_UPDATE + ", Reason: " + WebsocketValues.WEBSOCKET_CLOSE_REASON_CLOSE_FOR_CLIENT_NEED_UPDATE);
        webSocketSession.close(new CloseStatus(WebsocketValues.WEBSOCKET_CLOSE_CODE_CLOSE_FOR_CLIENT_NEED_UPDATE, WebsocketValues.WEBSOCKET_CLOSE_REASON_CLOSE_FOR_CLIENT_NEED_UPDATE));
    }

    public void closeForForceClientUpdateHigher(WebSocketSession webSocketSession) throws IOException {
        Logger.info("服务器主动关闭 Websocket Session > Code: " + WebsocketValues.WEBSOCKET_CLOSE_CODE_CLOSE_FOR_CLIENT_VERSION_HIGHER + ", Reason: " + WebsocketValues.WEBSOCKET_CLOSE_REASON_CLOSE_FOR_CLIENT_VERSION_HIGHER);
        webSocketSession.close(new CloseStatus(WebsocketValues.WEBSOCKET_CLOSE_CODE_CLOSE_FOR_CLIENT_VERSION_HIGHER, WebsocketValues.WEBSOCKET_CLOSE_REASON_CLOSE_FOR_CLIENT_VERSION_HIGHER));
    }

    public void closeAllSimpSessionOfUser(String imessageId){
        //将当前用户的 simpSessions 清空，这样新的关于用户的消息就不会再发给他
        SimpUser simpUser = simpUserRegistry.getUser(imessageId);
        if(simpUser != null) simpUser.getSessions().clear();
    }
}

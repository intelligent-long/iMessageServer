package com.longx.intelligent.app.imessage.server.handler;

import com.longx.intelligent.app.imessage.server.config.ImessageConfig;
import com.longx.intelligent.app.imessage.server.service.LoggedInWebsocketSessionOperationService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.WebSocketHandlerDecorator;
import org.springframework.web.socket.handler.WebSocketHandlerDecoratorFactory;

import java.security.Principal;
import java.util.List;

/**
 * Created by LONG on 2024/3/31 at 8:13 PM.
 */
@Component
public class WebsocketHandlerDecoratorFactory implements WebSocketHandlerDecoratorFactory {
    @Autowired
    private LoggedInWebsocketSessionOperationService loggedInWebsocketSessionOperationService;
    @Autowired
    private ImessageConfig imessageConfig;

    @Override
    public WebSocketHandler decorate(WebSocketHandler handler) {
        return new WebSocketHandlerDecorator(handler){
            @Override
            public void afterConnectionEstablished(WebSocketSession webSocketSession) throws Exception {
                HttpHeaders handshakeHeaders = webSocketSession.getHandshakeHeaders();
                List<String> clientVersions = handshakeHeaders.get("Client-Version");
                if(clientVersions == null){
                    loggedInWebsocketSessionOperationService.closeForForceClientUpdate(webSocketSession);
                    System.out.println("关闭了一个客户端需要升级的 Websocket 新连接");
                    return;
                }else {
                    try {
                        int clientVersion = Integer.parseInt(clientVersions.getFirst());
                        int lowestAllowClientVersion = imessageConfig.getLowestAllowClientVersion();
                        int currentClientVersion = imessageConfig.getCurrentClientVersion();
                        if(clientVersion < lowestAllowClientVersion){
                            loggedInWebsocketSessionOperationService.closeForForceClientUpdate(webSocketSession);
                            System.out.println("关闭了一个客户端需要升级的 Websocket 新连接");
                            return;
                        }
                        if(clientVersion > currentClientVersion){
                            loggedInWebsocketSessionOperationService.closeForForceClientUpdateHigher(webSocketSession);
                            System.out.println("关闭了一个客户端版本过高的 Websocket 新连接");
                            return;
                        }
                    }catch (Exception e){
                        loggedInWebsocketSessionOperationService.closeForForceClientUpdate(webSocketSession);
                        System.out.println("关闭了一个客户端需要升级的 Websocket 新连接");
                        return;
                    }
                }
                Principal principal = webSocketSession.getPrincipal();
                if (principal != null) {
                    String imessageId = principal.getName();
                    //先关闭其他此用户的Websocket连接, 如果有的话
                    loggedInWebsocketSessionOperationService.closeAllWebsocketSessionOfUser(imessageId);
                    //登陆后的websocket连接，hold websocket session
                    loggedInWebsocketSessionOperationService.holdWebsocketSession(imessageId, webSocketSession);
                    System.out.println("持有了一个新的 Websocket 连接会话");
                }else {
                    //未登陆的websocket连接一律关掉
                    loggedInWebsocketSessionOperationService.serverActiveClose(webSocketSession);
                    System.out.println("关闭了一个未登陆的 Websocket 新连接");
                }
                super.afterConnectionEstablished(webSocketSession);
            }
        };
    }
}

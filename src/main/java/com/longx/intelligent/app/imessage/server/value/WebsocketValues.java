package com.longx.intelligent.app.imessage.server.value;

/**
 * Created by LONG on 2024/3/31 at 8:24 PM.
 */
public class WebsocketValues {
    public static final String WEBSOCKET_ENDPOINT = "/ws";
    public static final int WEBSOCKET_CLOSE_CODE_SERVER_ACTIVE_CLOSE = 4000;
    public static final String WEBSOCKET_CLOSE_REASON_SERVER_ACTIVE_CLOSE = "服务器主动下线";
    public static final int WEBSOCKET_CLOSE_CODE_CLOSE_FOR_CLIENT_NEED_UPDATE = 4001;
    public static final String WEBSOCKET_CLOSE_REASON_CLOSE_FOR_CLIENT_NEED_UPDATE = "客户端必须更新新版本";
    public static final int WEBSOCKET_CLOSE_CODE_CLOSE_FOR_CLIENT_VERSION_HIGHER = 4002;
    public static final String WEBSOCKET_CLOSE_REASON_CLOSE_FOR_CLIENT_VERSION_HIGHER = "客户端版本过高";
    public static final String HTTP_SESSION_WEBSOCKET_SESSION_ATTRIBUTE_KEY = "HTTP_SESSION";
}

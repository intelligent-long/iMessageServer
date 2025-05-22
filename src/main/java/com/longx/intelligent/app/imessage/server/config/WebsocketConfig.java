package com.longx.intelligent.app.imessage.server.config;

import com.longx.intelligent.app.imessage.server.handler.WebsocketHandlerDecoratorFactory;
import com.longx.intelligent.app.imessage.server.handler.WebsocketHandshakeHandler;
import com.longx.intelligent.app.imessage.server.handler.WebsocketHandshakeInterceptor;
import com.longx.intelligent.app.imessage.server.value.StompDestinations;
import com.longx.intelligent.app.imessage.server.value.WebsocketValues;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketTransportRegistration;
import org.springframework.web.socket.server.support.HttpSessionHandshakeInterceptor;

/**
 * Created by LONG on 2024/3/31 at 7:54 PM.
 */
@Configuration
public class WebsocketConfig implements WebSocketMessageBrokerConfigurer {
    @Autowired
    private WebsocketHandshakeInterceptor websocketHandshakeInterceptor;
    @Autowired
    private WebsocketHandshakeHandler websocketHandshakeHandler;
    @Autowired
    @Lazy
    private WebsocketHandlerDecoratorFactory websocketHandlerDecoratorFactory;

    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {
        registry.addEndpoint(WebsocketValues.WEBSOCKET_ENDPOINT)
                .addInterceptors(websocketHandshakeInterceptor, new HttpSessionHandshakeInterceptor())
                .setHandshakeHandler(websocketHandshakeHandler);
    }

    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {
        registry.enableSimpleBroker(StompDestinations.PREFIX_TOPIC, StompDestinations.PREFIX_QUEUE);
        registry.setApplicationDestinationPrefixes(StompDestinations.PREFIX_APP, StompDestinations.PREFIX_USER);
    }

    @Override
    public void configureWebSocketTransport(WebSocketTransportRegistration registry) {
        registry.addDecoratorFactory(websocketHandlerDecoratorFactory);
    }
}

package com.longx.intelligent.app.imessage.server.yier;

import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.service.LoggedInWebsocketSessionOperationService;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.event.EventListener;
import org.springframework.session.Session;
import org.springframework.session.events.AbstractSessionEvent;
import org.springframework.session.events.SessionDeletedEvent;
import org.springframework.session.events.SessionDestroyedEvent;
import org.springframework.session.events.SessionExpiredEvent;
import org.springframework.stereotype.Component;

import java.io.IOException;

/**
 * Created by LONG on 2024/3/31 at 8:56 PM.
 */

/*
 当 HTTP Session 关闭的时候，同时关闭该用户对应 HTTP Session 的 Websocket Session
 */
@Component
public class HttpSessionEventYier {
    @Autowired
    private LoggedInWebsocketSessionOperationService loggedInWebsocketSessionOperationService;
    @Autowired
    private SessionService sessionService;

    private void closeWebsocketSession(AbstractSessionEvent event) {
        try {
            Session session = event.getSession();
            User userOfSession = sessionService.getUserOfSession(session);
            if(userOfSession != null) {
                loggedInWebsocketSessionOperationService.closeOneWebsocketSessionOfUser(userOfSession.getImessageId(), session.getId());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @EventListener
    public void afterSessionDeleted(SessionDeletedEvent event) {
        closeWebsocketSession(event);
    }

    @EventListener
    public void afterSessionExpired(SessionExpiredEvent event) {
        closeWebsocketSession(event);
    }

    @EventListener
    public void afterSessionDestroyed(SessionDestroyedEvent event) {
        closeWebsocketSession(event);
    }
}

package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.OfflineDetail;
import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.util.Logger;
import com.longx.intelligent.app.imessage.server.value.Constants;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.Session;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Created by LONG on 2024/3/30 at 11:50 AM.
 */
@Service
public class AuthService {
    @Autowired
    private UserService userService;
    @Autowired
    private RedisOperationService redisOperationService;
    @Autowired
    private SessionService sessionService;
    @Autowired
    private LoggedInWebsocketSessionOperationService loggedInWebsocketSessionOperationService;

    public String createNewUser(String email, String password, String username){
        String imessageId = userService.generateImessageId();
        User user = new User(imessageId, imessageId, email, password, new Date(), username, null, null, null, null, null);
        boolean inserted = userService.insertUser(user);
        if(!inserted) return null;
        Logger.info("创建了一个新用户 > " + "email: " + email + ", imessageId: " + imessageId);
        return imessageId;
    }

    public boolean isLoginAttemptsLimitExceeded(String userId){
        return redisOperationService.AUTH.getLoginFailureTimes(userId) > Constants.LOGIN_MAX_FAILURE_TIMES;
    }

    public boolean passLogin(User user, String password){
//        return user != null && PasswordCrypto.checkPassword(password, user.getPasswordHash());
        return user != null && Objects.equals(password, user.getPasswordHash());
    }

    public void incrementLoginFailureTimes(String imessageId){
        redisOperationService.AUTH.incrementLoginFailureTimes(imessageId);
    }

    public void login(HttpSession session, User user, OfflineDetail offlineDetails){
        forceMakeOfflineForLoggedIn(user, offlineDetails);
        redisOperationService.AUTH.removeLoginFailureTimes(user.getImessageId());
        //将用户放入Session，登陆成功
        sessionService.setUserToSession(session, user);
        Logger.info("登陆成功 > " + "iMessage ID: " + user.getImessageId() + ", Email: " + user.getEmail());
    }

    private void forceMakeOfflineForLoggedIn(User user, OfflineDetail offlineDetail){
        Set<String> allSessionIdOfUser = new HashSet<>();
        Map<String, ? extends Session> allSession = sessionService.findAllSessionByImessageId(user.getImessageId());
        allSession.values().forEach((Session session) -> {
            allSessionIdOfUser.add(session.getId());
        });
        //删除该用户的 HTTP Session，对应的 Websocket Session 会通过 EventListener 自动关掉
        sessionService.deleteAllSessionOfUser(user);
        //不论 Websocket Session 能不能关闭成功，都将当前用户的 simpSessions 清空，这样新的关于用户的消息就不会再发给他
        loggedInWebsocketSessionOperationService.closeAllSimpSessionOfUser(user.getImessageId());
        //将SessionID和OfflineDetails关联起来放到Redis
        allSessionIdOfUser.forEach(sessionId -> {
            redisOperationService.AUTH.recordOfflineDetail(sessionId, offlineDetail);
        });
        allSessionIdOfUser.forEach(sessionId -> {
            Logger.info("主动下线了一个会话 > " +
                    "Session ID: " + sessionId + ", " +
                    "iMessage ID: " + user.getImessageId() + ", " +
                    "iMessage ID User: " + user.getImessageIdUser() + ", " +
                    "Email: " + user.getEmail() + ", " +
                    "Username: " + user.getUsername());
        });
    }

    public OfflineDetail getOfflineDetail(String sessionId){
        return redisOperationService.AUTH.getOfflineDetail(sessionId);
    }

    public void removeOfflineDetail(String sessionId){
        redisOperationService.AUTH.removeOfflineDetail(sessionId);
    }

}

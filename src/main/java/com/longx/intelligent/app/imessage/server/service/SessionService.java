package com.longx.intelligent.app.imessage.server.service;

import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.util.Base64Util;
import com.longx.intelligent.app.imessage.server.value.Constants;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.session.FindByIndexNameSessionRepository;
import org.springframework.session.Session;
import org.springframework.session.data.redis.RedisIndexedSessionRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by LONG on 2024/1/13 at 5:29 PM.
 */
@Service
public class SessionService {
    @Autowired
    private RedisIndexedSessionRepository redisIndexedSessionRepository;
    @Autowired
    private UserService userService;

    public void setUserToSession(HttpSession session, User user){
        session.setAttribute(Constants.SESSION_ATTRIBUTE_KEY_USER, user);
        session.setAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME, user.getImessageId());
    }

    public void findAndSetUserToSession(HttpSession session, String imessageId){
        User user = userService.findUserByImessageId(imessageId);
        if(user != null) {
            setUserToSession(session, user);
        }
    }

    public void removeUserOfSession(HttpSession session){
        session.removeAttribute(Constants.SESSION_ATTRIBUTE_KEY_USER);
        session.removeAttribute(FindByIndexNameSessionRepository.PRINCIPAL_NAME_INDEX_NAME);
    }

    public User getUserOfSession(HttpSession session){
        return (User) session.getAttribute(Constants.SESSION_ATTRIBUTE_KEY_USER);
    }

    public User getUserOfSession(Session session){
        return session.getAttribute(Constants.SESSION_ATTRIBUTE_KEY_USER);
    }

    public Map<String, ? extends Session> findAllSessionByImessageId(String imessageId){
        return redisIndexedSessionRepository.findByPrincipalName(imessageId);
    }

    public void deleteAllSessionOfUser(User user){
        redisIndexedSessionRepository.findByPrincipalName(user.getImessageId()).keySet().forEach(redisIndexedSessionRepository::deleteById);
        redisIndexedSessionRepository.cleanUpExpiredSessions();
    }

    public void invalidateSession(HttpSession session){
        session.invalidate();
    }

    public String getSessionIdFromCookie(HttpServletRequest request){
        String cookie = request.getHeader("Cookie");
        if(cookie != null) {
            Pattern pattern = Pattern.compile("(?<=SESSION\\=).[^;]*");
            Matcher matcher = pattern.matcher(cookie);
            if (matcher.find()) {
                String sessionId = matcher.group();
                byte[] bytes = Base64Util.decodeFromString(sessionId);
                return new String(bytes);
            }
        }
        return null;
    }
}

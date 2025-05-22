package com.longx.intelligent.app.imessage.server.aspect;

import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import jakarta.servlet.http.HttpSession;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Before;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Created by LONG on 2024/4/28 at 12:27 AM.
 */
@Aspect
@Component
public class SessionCheckAspect {
    @Autowired
    private SessionService sessionService;

    @Pointcut("within(@org.springframework.stereotype.Controller *)")
    public void controller() {}

    @Pointcut("execution(* *(.., jakarta.servlet.http.HttpSession, ..))")
    public void methodWithSession() {}

    @Before("controller() && methodWithSession()")
    public void checkSessionBeforeMethod(JoinPoint joinPoint) {
        Object[] args = joinPoint.getArgs();
        for (Object arg : args) {
            if (arg instanceof HttpSession session) {
                User user = sessionService.getUserOfSession(session);
                if (user == null) {
                    throw new UserNotLoggedInException("User not logged in");
                }
            }
        }
    }

    public static class UserNotLoggedInException extends IllegalStateException{
        public UserNotLoggedInException(String userNotLoggedIn) {
            super(userNotLoggedIn);
        }
    }
}

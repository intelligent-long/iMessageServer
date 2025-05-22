package com.longx.intelligent.app.imessage.server.security;

import com.longx.intelligent.app.imessage.server.data.User;
import com.longx.intelligent.app.imessage.server.service.SessionService;
import jakarta.servlet.*;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;
import org.springframework.web.context.support.WebApplicationContextUtils;

import java.io.IOException;

/**
 * Created by LONG on 2024/1/13 at 12:40 AM.
 */
@WebFilter
@Component
public class SecurityFilter implements Filter {
    private AccessEvaluator accessEvaluator;
    private SessionService sessionService;

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        Filter.super.init(filterConfig);
        ServletContext context = filterConfig.getServletContext();
        ApplicationContext applicationContext = WebApplicationContextUtils.getWebApplicationContext(context);
        accessEvaluator = applicationContext.getBean(AccessEvaluator.class);
        sessionService = applicationContext.getBean(SessionService.class);
    }

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws ServletException, IOException {
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        HttpServletResponse httpServletResponse = (HttpServletResponse) servletResponse;
        String servletPath = httpServletRequest.getServletPath();
        HttpSession session = httpServletRequest.getSession();
        User currentUser = sessionService.getUserOfSession(session);
        boolean access = accessEvaluator.access(servletPath, currentUser);
        if(access){
            filterChain.doFilter(servletRequest, servletResponse);
        }else {
            session.invalidate();
            sendAccessDenied(httpServletRequest, httpServletResponse);
        }
    }

    @Override
    public void destroy() {
        Filter.super.destroy();
    }

    private void sendAccessDenied(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String url = request.getRequestURL().toString();
        String sessionId = request.getSession().getId();
        System.err.println("Access Denied > URL: " + url + ", Session ID: " + sessionId);
        response.sendError(HttpServletResponse.SC_FORBIDDEN, "Access Denied");
    }
}

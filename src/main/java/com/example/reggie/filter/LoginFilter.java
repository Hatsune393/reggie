package com.example.reggie.filter;


import com.example.reggie.common.R;
import com.example.reggie.common.util.BaseContext;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.PathMatcher;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;


@Slf4j
@Component
@WebFilter(value = "loginFilter", urlPatterns = {"/"})
public class LoginFilter implements Filter {
    private final String NOT_LOGIN_MSG = "NOTLOGIN";
    private final String EMPLOYEE_ID = "employee_id";
    private String[] STATIC_PATHS = new String[]{
            "/employee/login",
            "/employee/logout",
            "/backend/**",
            "/front/**",
            "/user/sendMsg",
            "/user/login"
    };

    private PathMatcher pathMatcher = new AntPathMatcher();

    @Override
    public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain filterChain) throws IOException, ServletException {
        // 1. 获取请求URI
        HttpServletRequest httpServletRequest = (HttpServletRequest) servletRequest;
        String uri = httpServletRequest.getRequestURI();
        log.info("请求被拦截：{}", uri);

        // 2. 设置未登录可以被访问的默认URL

        // 3. 使用路径匹配器判断当前路径是否可以直接访问
        if (shouldPass(uri)) {
            log.info("请求路径为静态资源，放行");
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 4. 若当前路径无法直接被放行，则判断用户登陆状态
        Object employeeId = httpServletRequest.getSession().getAttribute(EMPLOYEE_ID);
        if (employeeId != null) {
            log.info("用户处于登陆状态，放行");
            // 添加用户id信息到上下文中
            BaseContext.setSessionUserId((Long) employeeId);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 5. 判断User登陆状态
        Long userId = (Long) httpServletRequest.getSession().getAttribute("user_id");
        if (userId != null) {
            log.info("用户处于登陆状态，放行");
            // 添加用户id信息到上下文中
            BaseContext.setSessionUserId(userId);
            filterChain.doFilter(servletRequest, servletResponse);
            return;
        }

        // 6. 若用户未处于登陆状态，则返回错误码
        log.info("用户未处于登陆状态，返还错误码");
        servletResponse.getWriter().write(
                new ObjectMapper().writeValueAsString(
                        R.error(NOT_LOGIN_MSG)));
    }

    public boolean shouldPass(String uri) {
        for (String path : STATIC_PATHS) {
            if (pathMatcher.match(path, uri)) {
                return true;
            }
        }

        return false;
    }

}

package com.brad.skyserver.security;

import context.BaseContext;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.filter.OncePerRequestFilter;
import java.io.IOException;

/** 在 JWT 认证后运行，只在当前请求期间保存员工 ID。 */
public class BaseContextFilter extends OncePerRequestFilter {
    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        BaseContext.removeUserId();
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            if (authentication != null && authentication.isAuthenticated()
                    && authentication.getPrincipal() instanceof Jwt jwt) {
                long employeeId;
                try {
                    employeeId = Long.parseLong(jwt.getSubject());
                    if (employeeId <= 0) throw new NumberFormatException("Invalid employee ID");
                } catch (NumberFormatException exception) {
                    response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                    response.setContentType("application/json;charset=UTF-8");
                    response.getWriter().write("{\"code\":0,\"msg\":\"登录身份无效\",\"data\":\"\"}");
                    return;
                }
                BaseContext.setUserId(employeeId);//将jwt中获取到的登录信息保存在当前线程中
            }
            filterChain.doFilter(request, response);
        } finally {
            BaseContext.removeUserId();
        }
    }
}

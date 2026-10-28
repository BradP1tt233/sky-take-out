package com.brad.skyserver;

import com.brad.skyserver.security.BaseContextFilter;
import context.BaseContext;
import jakarta.servlet.ServletException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpServletResponse;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class BaseContextFilterTests {
    @AfterEach void cleanup() {
        BaseContext.removeUserId();
        SecurityContextHolder.clearContext();
    }
    private void authenticate(String id) {
        Jwt jwt = Jwt.withTokenValue("test").header("alg", "HS256").subject(id).build();
        SecurityContextHolder.getContext().setAuthentication(new JwtAuthenticationToken(jwt,
                List.of(new SimpleGrantedAuthority("SCOPE_employee"))));
    }
    @Test void differentRequestsCannotReuseEmployeeIdentity() throws Exception {
        BaseContextFilter filter = new BaseContextFilter();
        for (long id : List.of(2L, 3L)) {
            authenticate(Long.toString(id));
            filter.doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                    (request, response) -> assertEquals(id, BaseContext.getUserId()));
            assertNull(BaseContext.getUserId());
        }
    }
    @Test void downstreamExceptionStillClearsIdentity() {
        authenticate("2");
        assertThrows(ServletException.class, () -> new BaseContextFilter().doFilter(
                new MockHttpServletRequest(), new MockHttpServletResponse(), (request, response) -> {
                    assertEquals(2L, BaseContext.getUserId());
                    throw new ServletException("test failure");
                }));
        assertNull(BaseContext.getUserId());
    }
    @Test void unauthenticatedRequestHasNoIdentityAndClearsStaleState() throws Exception {
        SecurityContextHolder.clearContext();
        BaseContext.setUserId(99L);
        new BaseContextFilter().doFilter(new MockHttpServletRequest(), new MockHttpServletResponse(),
                (request, response) -> assertNull(BaseContext.getUserId()));
        assertNull(BaseContext.getUserId());
    }
    @Test void malformedEmployeeIdIsRejectedAndCleared() throws Exception {
        authenticate("invalid");
        MockHttpServletResponse response = new MockHttpServletResponse();
        new BaseContextFilter().doFilter(new MockHttpServletRequest(), response,
                (request, result) -> fail("不应处理无效的员工身份"));
        assertEquals(401, response.getStatus());
        assertNull(BaseContext.getUserId());
    }
}

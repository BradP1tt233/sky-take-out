package config;

import com.brad.skyserver.security.EmployeePasswordEncoder;
import com.brad.skyserver.security.BaseContextFilter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import constant.MessageConstant;
import constant.StatusConstant;
import entity.Employee;
import mapper.EmployeeMapper;
import java.nio.charset.StandardCharsets;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.*;
import org.springframework.security.oauth2.server.resource.web.DefaultBearerTokenResolver;
import org.springframework.security.web.SecurityFilterChain;
import com.nimbusds.jose.jwk.source.ImmutableSecret;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    @Bean
    PasswordEncoder passwordEncoder() { return new EmployeePasswordEncoder(); }

    @Bean
    AuthenticationManager authenticationManager(EmployeeMapper mapper, PasswordEncoder encoder) {
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(username -> {
            Employee employee = mapper.getByUsername(username);
            if (employee == null) throw new UsernameNotFoundException(MessageConstant.ACCOUNT_NOT_FOUND);
            return User.withUsername(employee.getUsername())
                    .password(employee.getPassword() == null ? "" : employee.getPassword())
                    .authorities("ROLE_EMPLOYEE")
                    .disabled(!StatusConstant.ENABLE.equals(employee.getStatus())).build();
        });
        provider.setPasswordEncoder(encoder);
        provider.setUserDetailsPasswordService((user, password) -> {
            mapper.updatePassword(user.getUsername(), password, user.getPassword());
            return User.withUserDetails(user).password(password).build();
        });
        return new ProviderManager(provider);
    }

    @Bean
    SecretKey jwtSecretKey(@Value("${sky.jwt.admin-secret-key}") String secret) {
        byte[] bytes = secret.getBytes(StandardCharsets.UTF_8);
        if (bytes.length < 32) throw new IllegalArgumentException("JWT 密钥至少需要 32 字节");
        return new SecretKeySpec(bytes, "HmacSHA256");
    }
    @Bean
    JwtEncoder jwtEncoder(SecretKey key) { return new NimbusJwtEncoder(new ImmutableSecret<>(key)); }
    @Bean
    JwtDecoder jwtDecoder(SecretKey key) {
        NimbusJwtDecoder decoder = NimbusJwtDecoder.withSecretKey(key).macAlgorithm(MacAlgorithm.HS256).build();
        decoder.setJwtValidator(JwtValidators.createDefaultWithIssuer("sky-admin"));
        return decoder;
    }
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http,
            @Value("${sky.jwt.admin-token-name:token}") String tokenName) throws Exception {
        DefaultBearerTokenResolver resolver = new DefaultBearerTokenResolver();
        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(HttpMethod.POST, "/admin/employee/login").permitAll()
                        .requestMatchers("/admin/**").hasAuthority("SCOPE_employee")
                        .anyRequest().denyAll())
                .exceptionHandling(errors -> errors
                        .authenticationEntryPoint((request, response, exception) -> unauthorized(response))
                        .accessDeniedHandler((request, response, exception) -> {
                            response.setStatus(403);
                            response.setContentType("application/json;charset=UTF-8");
                            response.getWriter().write("{\"code\":0,\"msg\":\"无权访问\",\"data\":\"\"}");
                        }))
                .oauth2ResourceServer(oauth -> oauth
                        .bearerTokenResolver(request -> {
                            if (HttpMethod.POST.matches(request.getMethod())
                                    && (request.getContextPath() + "/admin/employee/login").equals(request.getRequestURI())) return null;
                            String bearer = resolver.resolve(request);
                            return bearer != null ? bearer : request.getHeader(tokenName);
                        })
                        .authenticationEntryPoint((request, response, exception) -> unauthorized(response))
                        .jwt(jwt -> {}))
                .addFilterAfter(new BaseContextFilter(), BearerTokenAuthenticationFilter.class)
                .build();
    }
    private static void unauthorized(jakarta.servlet.http.HttpServletResponse response) throws java.io.IOException {
        response.setStatus(401);
        response.setContentType("application/json;charset=UTF-8");
        response.getWriter().write("{\"code\":0,\"msg\":\"用户未登录或令牌已失效\",\"data\":\"\"}");
    }
}

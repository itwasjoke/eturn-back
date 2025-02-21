package com.eturn.eturn.security.jwt;

import com.eturn.eturn.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    public static final String BEARER_PREFIX = "Bearer ";
    public static final String HEADER_NAME = "Authorization";
    private final JwtService jwtService;
    private final UserService userService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            UserService userService
    ) {
        this.jwtService = jwtService;
        this.userService = userService;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {
        // Проверяем наличие и корректность заголовка авторизации
        String authHeader = request.getHeader(HEADER_NAME);
        if (!isAuthHeaderValid(authHeader)) {
            filterChain.doFilter(request, response);
            return;
        }

        // Извлекаем JWT из заголовка
        String jwt = extractJwtFromHeader(authHeader);

        // Получаем имя пользователя из токена
        String username = jwtService.extractUserName(jwt);

        // Если пользователь не аутентифицирован, проверяем токен и аутентифицируем
        if (!username.isEmpty() && isUserNotAuthenticated()) {
            authenticateUserIfTokenValid(jwt, username, request);
        }

        // Продолжаем цепочку фильтров
        filterChain.doFilter(request, response);
    }

    /**
     * Проверяет, является ли заголовок авторизации корректным
     * @param authHeader Заголовок авторизации.
     * @return true, если заголовок корректен, иначе false.
     */
    private boolean isAuthHeaderValid(String authHeader) {
        return authHeader != null && authHeader.startsWith(BEARER_PREFIX);
    }

    /**
     * Извлекает JWT из заголовка авторизации
     * @param authHeader Заголовок авторизации
     * @return JWT токен.
     */
    private String extractJwtFromHeader(String authHeader) {
        return authHeader.substring(BEARER_PREFIX.length());
    }

    /**
     * Проверяет, аутентифицирован ли пользователь
     * @return true, если пользователь не аутентифицирован, иначе false.
     */
    private boolean isUserNotAuthenticated() {
        return SecurityContextHolder.getContext().getAuthentication() == null;
    }

    /**
     * Аутентифицирует пользователя, если токен валиден
     * @param jwt      JWT токен.
     * @param username Имя пользователя.
     * @param request  HTTP-запрос.
     */
    private void authenticateUserIfTokenValid(
            String jwt,
            String username,
            HttpServletRequest request
    ) {
        UserDetails userDetails = userService
                .userDetailsService()
                .loadUserByUsername(username);

        if (jwtService.isTokenValid(jwt, userDetails)) {
            SecurityContext context =
                    SecurityContextHolder.createEmptyContext();

            UsernamePasswordAuthenticationToken authToken =
                    createAuthToken(userDetails, request);

            context.setAuthentication(authToken);
            SecurityContextHolder.setContext(context);
        }
    }

    /**
     * Создает токен аутентификации для пользователя
     * @param userDetails Данные пользователя.
     * @param request    HTTP-запрос.
     * @return Токен аутентификации.
     */
    private UsernamePasswordAuthenticationToken createAuthToken(
            UserDetails userDetails,
            HttpServletRequest request
    ) {
        UsernamePasswordAuthenticationToken authToken =
                new UsernamePasswordAuthenticationToken(
                    userDetails,
                    null,
                    userDetails.getAuthorities()
        );
        authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
        return authToken;
    }
}

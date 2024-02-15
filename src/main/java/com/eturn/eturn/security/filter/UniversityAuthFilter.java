//package com.eturn.eturn.security.filter;
//
//import jakarta.servlet.FilterChain;
//import jakarta.servlet.ServletException;
//import jakarta.servlet.http.HttpServletRequest;
//import jakarta.servlet.http.HttpServletResponse;
//import org.springframework.security.core.context.SecurityContext;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.filter.OncePerRequestFilter;
//
//import java.io.IOException;
//import java.net.http.HttpClient;
//import java.net.http.HttpRequest;
//
//public class UniversityAuthFilter extends OncePerRequestFilter {
//    private final HttpClient client = HttpClient.newHttpClient();
//
//    // feign client
//
//    @Override
//    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
//        throws ServletException, IOException {
//
//        Long userId = Long.valueOf(request.getHeader("user_id"));
//        String username = request.getHeader("username");
//        String password = request.getHeader("password");
//
////        if (client.send(HttpRequest.newBuilder().GET().build(), new )) {
////
//////            SecurityContextHolder.getContext().setAuthentication(
//////                new Authentication(GRANT.MEMBER, userId)
//////            );
////
////            filterChain.doFilter(request, response);
////        } else {
////            SecurityContextHolder.clearContext();
////            filterChain.doFilter(request, response);
////        }
//    }
//
//    public enum GRANT {
//        MEMBER, ADMIN
//    }
//}

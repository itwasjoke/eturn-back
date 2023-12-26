package com.eturn.eturn.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.CsrfFilter;
import org.springframework.security.web.csrf.CsrfTokenRepository;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;
import org.springframework.web.servlet.HandlerExceptionResolver;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final CustomUserDetailsService customUserDetailsService;
    private final JwtTokenRepository jwtTokenRepository;

    public SecurityConfig(CustomUserDetailsService customUserDetailsService, JwtTokenRepository jwtTokenRepository) {
        this.customUserDetailsService = customUserDetailsService;
        this.jwtTokenRepository = jwtTokenRepository;
    }

//    @Bean
//    public BCryptPasswordEncoder passwordEncoder() {
//        return new BCryptPasswordEncoder();
//    }
//
//    @Bean
//    public DaoAuthenticationProvider authenticationProvider() {
//        DaoAuthenticationProvider daoAuthenticationProvider = new DaoAuthenticationProvider();
//        daoAuthenticationProvider.setUserDetailsService(customUserDetailsService);
//        daoAuthenticationProvider.setPasswordEncoder(passwordEncoder());
//        return daoAuthenticationProvider;
//    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http.cors(Customizer.withDefaults())
            .csrf(AbstractHttpConfigurer::disable)
            .authorizeHttpRequests(auth -> auth.requestMatchers("/user/register").permitAll())
            .userDetailsService(customUserDetailsService)
                .sessionManagement(s ->
                        s.sessionCreationPolicy(SessionCreationPolicy.NEVER))
                .addFilterAt(new JwtCsrfFilter(jwtTokenRepository), CsrfFilter.class)
                .csrf(csrf ->
                        csrf.ignoringRequestMatchers("/**"))
            .formLogin(formLogin -> formLogin
                .loginPage("/user/login")
                .loginProcessingUrl("/user/login")
                .defaultSuccessUrl("/user/turn")
                .usernameParameter("username")
                .passwordParameter("password")
                .loginProcessingUrl("/user/login")
            ).logout(logout -> logout
                .invalidateHttpSession(true)
                .clearAuthentication(true)
                .logoutRequestMatcher(new AntPathRequestMatcher("/user/logout"))
                .logoutSuccessUrl("/user/login?logout").permitAll()
            )
            .httpBasic(Customizer.withDefaults())
            .build();
    }
}

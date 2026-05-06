package com.mall.common.base;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.mall.common.response.R;
import com.mall.common.response.ResultCode;
import com.mall.common.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.List;

/**
 * Spring Security configuration supporting three authentication systems:
 * 1. Platform admin (role: ROLE_PLATFORM)
 * 2. Merchant admin (role: ROLE_MERCHANT)
 * 3. Member/Consumer (role: ROLE_MEMBER)
 */
@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
public class SecurityConfig {

    private final JwtUtil jwtUtil;
    private final ObjectMapper objectMapper;
    private final StringRedisTemplate stringRedisTemplate;

    @Value("${mall.cors.allowed-origins:http://localhost:5173,http://localhost:5174,http://localhost:3000,http://localhost:3002}")
    private String allowedOrigins;

    public SecurityConfig(JwtUtil jwtUtil, ObjectMapper objectMapper, StringRedisTemplate stringRedisTemplate) {
        this.jwtUtil = jwtUtil;
        this.objectMapper = objectMapper;
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtUtil, stringRedisTemplate);
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Enable CORS
            .cors()
            .and()
            // Disable CSRF (stateless JWT)
            .csrf().disable()
            // Disable session
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            // Configure authorization
            .authorizeRequests()
                // Public auth endpoints (all three auth systems)
                .antMatchers(
                    "/api/v1/member/auth/**",
                    "/api/v1/platform/auth/**",
                    "/api/v1/merchant/auth/**"
                ).permitAll()
                // Payment notify callbacks (third-party, no JWT)
                .antMatchers("/api/v1/notify/**").permitAll()
                // Knife4j API doc
                .antMatchers(
                    "/doc.html",
                    "/webjars/**",
                    "/swagger-resources/**",
                    "/v3/api-docs/**",
                    "/v2/api-docs/**",
                    "/v2/api-docs",
                    "/favicon.ico"
                ).permitAll()
                // Druid monitoring - platform admin only (was permitAll, security fix S-07)
                .antMatchers("/druid/**").hasRole("PLATFORM")
                // Public read-only product endpoints for C-end
                .antMatchers(HttpMethod.GET,
                    "/api/v1/product/**",
                    "/api/v1/category/**",
                    "/api/v1/brand/**"
                ).permitAll()
                // Public read-only member endpoints (home page, product browsing, category tree)
                .antMatchers(HttpMethod.GET,
                    "/api/v1/member/home",
                    "/api/v1/member/product/**",
                    "/api/v1/member/category/**",
                    "/api/v1/member/brand/**"
                ).permitAll()
                // Role-based URL access control (S-01)
                .antMatchers("/api/v1/platform/**").hasRole("PLATFORM")
                .antMatchers("/api/v1/merchant/**").hasRole("MERCHANT")
                .antMatchers("/api/v1/member/**").hasRole("MEMBER")
                // All other requests require authentication
                .anyRequest().authenticated()
            .and()
            // Exception handling
            .exceptionHandling()
                .authenticationEntryPoint(authenticationEntryPoint())
                .accessDeniedHandler(accessDeniedHandler())
            .and()
            // Add JWT filter
            .addFilterBefore(jwtAuthenticationFilter(), UsernamePasswordAuthenticationFilter.class)
            // Disable default headers for API
            .headers().frameOptions().disable();

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        List<String> origins = Arrays.asList(allowedOrigins.split(","));
        configuration.setAllowedOrigins(origins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    @Bean
    public AuthenticationEntryPoint authenticationEntryPoint() {
        return (request, response, authException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            R<?> result = R.fail(ResultCode.UNAUTHORIZED);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }

    @Bean
    public AccessDeniedHandler accessDeniedHandler() {
        return (request, response, accessDeniedException) -> {
            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
            response.setCharacterEncoding("UTF-8");
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            R<?> result = R.fail(ResultCode.FORBIDDEN);
            response.getWriter().write(objectMapper.writeValueAsString(result));
        };
    }
}

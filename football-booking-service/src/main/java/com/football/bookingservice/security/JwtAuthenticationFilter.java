package com.football.bookingservice.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider tokenProvider;
    private final CustomUserDetailsService userDetailsService;
    private final String internalServiceKey;

    public JwtAuthenticationFilter(
            JwtTokenProvider tokenProvider,
            CustomUserDetailsService userDetailsService,
            @org.springframework.beans.factory.annotation.Value("${internal.service.key:FootballInternalSecretServiceKey2026}") String internalServiceKey) {
        this.tokenProvider = tokenProvider;
        this.userDetailsService = userDetailsService;
        this.internalServiceKey = internalServiceKey;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // 1. Kiểm tra xác thực liên dịch vụ qua Service Credential (X-Internal-Service-Key)
        String requestInternalKey = request.getHeader("X-Internal-Service-Key");
        if (StringUtils.hasText(requestInternalKey) && requestInternalKey.equals(internalServiceKey)) {
            UserDetails internalServiceUser = org.springframework.security.core.userdetails.User
                    .withUsername("internal-system-service")
                    .password("")
                    .roles("ADMIN", "INTERNAL_SERVICE")
                    .build();

            UsernamePasswordAuthenticationToken internalAuth =
                    new UsernamePasswordAuthenticationToken(internalServiceUser, null, internalServiceUser.getAuthorities());
            internalAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
            SecurityContextHolder.getContext().setAuthentication(internalAuth);

            filterChain.doFilter(request, response);
            return;
        }

        // 2. Kiểm tra xác thực người dùng qua JWT Bearer Token
        String token = resolveToken(request);

        if (StringUtils.hasText(token) && tokenProvider.validateToken(token)) {
            String username = tokenProvider.getUsernameFromToken(token);
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
            authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

            SecurityContextHolder.getContext().setAuthentication(authentication);
        }

        filterChain.doFilter(request, response);
    }

    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}

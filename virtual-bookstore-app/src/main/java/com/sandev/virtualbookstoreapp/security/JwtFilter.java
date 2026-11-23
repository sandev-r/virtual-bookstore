package com.sandev.virtualbookstoreapp.security;


import io.jsonwebtoken.JwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.util.AntPathMatcher;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Arrays;

import static org.springframework.http.HttpHeaders.AUTHORIZATION;

@Component
@RequiredArgsConstructor
public class JwtFilter extends OncePerRequestFilter {
    private final UserDetailsService  userDetailsService;
    private final JwtService jwtService;
    private final @NonNull AntPathMatcher pathMatcher;
    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain) throws ServletException, IOException {
        if(!shouldNotFilter(request)) {
            filterChain.doFilter(request, response);
        }
        final String authHeader = request.getHeader(AUTHORIZATION);
        if(authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
        }
        final String token =  authHeader.substring(7);
        try {
            final String username = jwtService.extractUsername(token);

            if(username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                final UserDetails userDetails = userDetailsService.loadUserByUsername(username);
                if(this.jwtService.isTokenValid(token, userDetails.getUsername())){
                    final UsernamePasswordAuthenticationToken authentication = new UsernamePasswordAuthenticationToken(userDetails,
                            null, userDetails.getAuthorities());
                    authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(authentication);
                }
            }
        } catch (JwtException | org.springframework.security.core.userdetails.UsernameNotFoundException ignored) {
            // Leave the request unauthenticated; protected routes will be rejected by Spring Security.
        }
        filterChain.doFilter(request,response);
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        final String path =  request.getServletPath();
        return Arrays.stream(SecurityConfig.PUBLIC_URLS)
                .anyMatch(pattern -> pathMatcher.match(pattern ,path));
    }
}

package com.eFarm.backend.security;

import com.eFarm.backend.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String username;

        // Kontrollo nëse Authorization header ekziston dhe fillon me "Bearer "
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // Ekstrakto JWT token
        jwt = authHeader.substring(7);

        try {
            // Ekstrakto username nga JWT token
            username = jwtService.extractUsername(jwt);

            // Kontrollo nëse username ekziston dhe nuk ka autentikim në SecurityContext
            if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

                // Ngarko detajet e user-it
                UserDetails userDetails = this.userDetailsService.loadUserByUsername(username);

                // Valido token
                if (jwtService.isTokenValid(jwt, userDetails)) {

                    // Krijo authentication token
                    UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                            userDetails,
                            null,
                            userDetails.getAuthorities()
                    );

                    // Vendos detajet e autentikimit
                    authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                    // Vendos autentikimin në SecurityContext
                    SecurityContextHolder.getContext().setAuthentication(authToken);
                }
            }
        } catch (Exception e) {
            // Log gabimin por mos e ndërprit filter chain
            logger.error("Error processing JWT token: " + e.getMessage());
            SecurityContextHolder.clearContext();
        }

        // Vazhdo me filter chain
        filterChain.doFilter(request, response);
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) throws ServletException {
        String path = request.getRequestURI();

        // Mos përdor JWT filter për endpoint-et publike
        return path.startsWith("/api/auth/") ||
                path.startsWith("/api/public/") ||
                path.equals("/") ||
                path.startsWith("/favicon.ico") ||
                path.startsWith("/error");
    }
}
package com.funpearl.funpearl.security.jwt;

import com.funpearl.funpearl.security.CustomUserDetailsService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final CustomUserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        // 1. 从请求头中获取 Authorization
        final String authHeader = request.getHeader("Authorization");

        // 2. 检查是否有 token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // 3. 提取 token（去掉 "Bearer " 前缀）
        final String token = authHeader.substring(7);

        // 4. 从 token 中提取用户名
        final String username = jwtService.extractUsername(token);

        // 5. 如果用户名存在且当前没有认证信息
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            // 6. 从数据库加载用户信息
            UserDetails userDetails = userDetailsService.loadUserByUsername(username);

            // 7. 验证 token 是否有效
            if (jwtService.validateToken(token)) {
                // 8. 创建认证对象
                UsernamePasswordAuthenticationToken authToken =
                        new UsernamePasswordAuthenticationToken(
                                userDetails,
                                null,
                                userDetails.getAuthorities()
                        );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // 9. 将认证信息存入 SecurityContext
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }

        // 10. 继续执行下一个过滤器
        filterChain.doFilter(request, response);
    }
}
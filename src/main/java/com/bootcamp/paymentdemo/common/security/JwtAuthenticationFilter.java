package com.bootcamp.paymentdemo.common.security;

import com.bootcamp.paymentdemo.common.dto.BaseResponse;
import com.bootcamp.paymentdemo.common.token.repository.BlackAccessTokenRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jspecify.annotations.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.PathContainer;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.pattern.PathPattern;
import org.springframework.web.util.pattern.PathPatternParser;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static com.bootcamp.paymentdemo.common.Constants.*;
import static org.springframework.boot.security.autoconfigure.web.servlet.PathRequest.toStaticResources;

/**
 * JWT 토큰 인증 필터
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtTokenProvider jwtTokenProvider;
    private final ObjectMapper objectMapper;
    private final UserDetailsServiceImpl userDetailsServiceImpl;
    private final BlackAccessTokenRepository blackAccessTokenRepository;

    // PathPatternParser 사용하여 인증 제외 URI 검별
    private static final PathPatternParser patternParser = new PathPatternParser();

    // 인증 제외 패턴 선언
    private static final List<PathPattern> EXCLUDE_PATTERNS = List.of(
            // region 정적 리소스
            patternParser.parse("/css/**")
            , patternParser.parse("/js/**")
            , patternParser.parse("/images/**")
            , patternParser.parse("/webjars/**")
            , patternParser.parse("/bootstrap/**")
            , patternParser.parse("/.well-known/**")
            , patternParser.parse("/favicon.ico")
            , patternParser.parse("/static/**")
            , patternParser.parse("/api/public/**")
            , patternParser.parse("/") // 루트 페이지
            , patternParser.parse("/pages/**") // 페이지 구성 통과
            // endregion
            , patternParser.parse("/api/signup") // 회원 가입
            , patternParser.parse("/api/auth/login") // 로그인
            , patternParser.parse("/api/auth/refresh") // 토큰 재발급
            , patternParser.parse("/api/webhooks/**") // 웹훅
    );

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        log.info("===JWT FILTER IN===");
        try {
            // Request Header에서 JWT 토큰 추출
            String token = getJwtFromRequest(request);

            if(token == null) {
                log.error("액세스 토큰 없음 : {}", request.getRequestURI());
                BaseResponse<Void> baseResponse = BaseResponse.fail(HttpStatus.UNAUTHORIZED.name(), MSG_TOKEN_EMPTY, null);
                response.setContentType("application/json; charset=UTF-8");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
                return;
            }

            // AccessToken 블랙리스트 조회
            if(blackAccessTokenRepository.existsByAccessToken(token)) {
                log.error("블랙 리스트 등록 토큰 사용 감지 : {}", request.getRequestURI());
                BaseResponse<Void> baseResponse = BaseResponse.fail(HttpStatus.UNAUTHORIZED.name(), MSG_AUTH_WRONG, null);
                response.setContentType("application/json; charset=UTF-8");
                response.setStatus(HttpStatus.UNAUTHORIZED.value());
                response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
                return;
            }

            // 토큰 유효성 검증
            if (jwtTokenProvider.validateToken(token)) {
                // 토큰에서 사용자 정보 추출
                String email = jwtTokenProvider.getEmail(token);

                // 인증 객체 생성
                UserDetails userDetails = userDetailsServiceImpl.loadUserByUsername(email);

                UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(
                           userDetails, null, userDetails.getAuthorities()
                    );

                // TODO 무슨 기능을 하는지 잘 모르겠음
                authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

                // SecurityContext에 인증 정보 설정
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        } catch (Exception e) {
            logger.error("JWT 인증 필터 오류 발생", e);

            BaseResponse<Void> baseResponse = BaseResponse.fail(HttpStatus.UNAUTHORIZED.name(), MSG_AUTH_FAIL, null);
            response.setContentType("application/json; charset=UTF-8");
            response.setStatus(HttpStatus.UNAUTHORIZED.value());
            response.getWriter().write(objectMapper.writeValueAsString(baseResponse));
            return;
        }

        filterChain.doFilter(request, response);

        log.info("===JWT FILTER OUT===");
    }

    /**
     * Request Header에서 JWT 토큰 추출
     * Authorization: Bearer {token}
     */
    private String getJwtFromRequest(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");

        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }

        return null;
    }

    @Override
    protected boolean shouldNotFilter(@NonNull HttpServletRequest request) {
        PathContainer path = PathContainer.parsePath(request.getRequestURI());
        return EXCLUDE_PATTERNS.stream().anyMatch(pattern -> pattern.matches(path));
    }
}

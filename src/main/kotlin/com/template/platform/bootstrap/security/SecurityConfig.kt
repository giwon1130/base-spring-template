package com.template.platform.bootstrap.security

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.http.HttpMethod
import org.springframework.security.authentication.AuthenticationManager
import org.springframework.security.config.Customizer
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter

/**
 * Spring Security 설정.
 *
 * BMOA에서 사용했던 접근 제어 정책을 유지하면서 필요한 엔드포인트만 조정했다.
 */
@Configuration
@EnableWebSecurity
class SecurityConfig(
    private val jwtAuthenticationFilter: JwtAuthenticationFilter,
    private val customUserDetailsService: CustomUserDetailsService
) {

    @Bean
    fun securityFilterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors(Customizer.withDefaults())
            .csrf { it.disable() }
            .sessionManagement { it.sessionCreationPolicy(SessionCreationPolicy.STATELESS) }
            .authorizeHttpRequests { auth ->
                auth
                    .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                    // Swagger/OpenAPI endpoints (must be before /api/** matcher)
                    .requestMatchers("/v3/api-docs/**", "/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()
                    .requestMatchers("/api/v1/auth/**").permitAll()
                    .requestMatchers("/api/v1/errors").permitAll()
                    .requestMatchers("/api/v1/label-render/**").permitAll()
                    .requestMatchers("/api/v1/notifications/**").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/scenes/change-detections/*/download").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/scenes/change-detections/*/labels/download").permitAll()
                    .requestMatchers(HttpMethod.GET, "/api/v1/reports/download-secure").permitAll()
                    // Actuator endpoints for monitoring and health checks
                    .requestMatchers("/actuator/**").permitAll()
                    // Test endpoints for development
                    .requestMatchers("/sse/**").permitAll()
                    .requestMatchers("/api/changes/**").permitAll()
                    .requestMatchers("/api/v1/admin/**").hasRole("ADMIN")
                    .requestMatchers("/api/**").authenticated()
                    .anyRequest().permitAll()
            }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .userDetailsService(customUserDetailsService)
            .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)

        return http.build()
    }

    @Bean
    fun authenticationManager(authConfig: AuthenticationConfiguration): AuthenticationManager {
        return authConfig.authenticationManager
    }
}

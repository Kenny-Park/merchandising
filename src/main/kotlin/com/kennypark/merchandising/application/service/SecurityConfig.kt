package com.kennypark.merchandising.application.service

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.autoconfigure.security.servlet.PathRequest
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.security.config.annotation.web.builders.HttpSecurity
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity
import org.springframework.security.config.annotation.web.configurers.*
import org.springframework.security.config.http.SessionCreationPolicy
import org.springframework.security.web.SecurityFilterChain
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter
import org.springframework.web.cors.CorsConfiguration
import org.springframework.web.cors.CorsConfigurationSource
import org.springframework.web.cors.UrlBasedCorsConfigurationSource


@Configuration
@EnableWebSecurity
class SecurityConfig(
    @Autowired
    private val jwtAccessDeniedHandler: JwtAccessDeniedHandler,
) {

    @Bean
    fun corsConfigurationSource(): CorsConfigurationSource {
        val configuration = CorsConfiguration()

        // 1. 허용할 프론트엔드 주소
        configuration.allowedOrigins = listOf("http://localhost:3000")

        // 2. 허용할 HTTP 메서드
        configuration.allowedMethods = listOf("GET", "POST", "PUT", "DELETE", "OPTIONS")

        // 3. 허용할 헤더 (프론트에서 보낼 헤더)
        configuration.allowedHeaders = listOf("*")

        // 4. 서버가 응답 시 프론트에 노출할 헤더 (이게 있어야 x-idempotency-key가 보임)
        configuration.exposedHeaders = listOf("x-idempotency-key")

        // 5. 쿠키나 인증 헤더를 허용할지 여부
        configuration.allowCredentials = true

        val source = UrlBasedCorsConfigurationSource()
        source.registerCorsConfiguration("/**", configuration)
        return source
    }

    @Bean
    @Throws(Exception::class)
    fun filterChain(http: HttpSecurity): SecurityFilterChain {
        http
            .cors { it.configurationSource(corsConfigurationSource()) }
            .formLogin { it.disable() }
            .httpBasic { it.disable() }
            .csrf { it.disable() }
            .sessionManagement {
                it.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            }
           .headers { headerConfig ->
                headerConfig.frameOptions({ frameOptionsConfig ->
                    frameOptionsConfig.disable()
                }
                )
            }
            .authorizeHttpRequests { authorizeRequests ->
                authorizeRequests
                    .requestMatchers( "login", "swagger-ui/**", "/api/v1/merchandise/**", "/api/v1/coupon/**", "login/api/v1/**", "api/v1/**", "v3/**", "v3/api-docs/**",
                        "swagger-ui.html").permitAll()
                    .requestMatchers(PathRequest.toH2Console()).permitAll()
                    .requestMatchers("/order/api/v1/**").hasRole("USER")
                    .anyRequest().authenticated()
            }
            //.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter::class.java)
            /*.exceptionHandling { exceptionConfig ->
                exceptionConfig.authenticationEntryPoint(
                    MyAuthorizedEntryPoint()
                ).accessDeniedHandler(jwtAccessDeniedHandler)
            }*/
        return http.build()
    }

}
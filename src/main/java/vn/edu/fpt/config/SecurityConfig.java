package vn.edu.fpt.config;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import vn.edu.fpt.service.CustomOAuth2UserService;
import vn.edu.fpt.service.CustomUserDetailsService;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private CustomAuthenticationFailureHandler failureHandler;

    @Autowired
    private CustomUserDetailsService userDetailsService;

    @Autowired
    private CustomOAuth2UserService oauth2UserService;

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authenticationProvider(authenticationProvider())
                .authorizeHttpRequests(auth -> auth
                        // 1. TÀI NGUYÊN TĨNH
                        .requestMatchers(
                                "/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html",
                                "/webjars/**", "/css/**", "/js/**", "/images/**", "/favicon.ico", "/error"
                        ).permitAll()

                        // 2. API HỆ THỐNG
                        .requestMatchers("/api/warehouses/**", "/api/inventory/**").permitAll()

                        // 3. CHỈ ADMIN MỚI ĐƯỢC VÀO
                        .requestMatchers(
                                "/admin/dashboard/**",
                                "/admin/vouchers/**",
                                "/admin/accounts/**",
                                "/admin/staff/**"
                        ).hasAuthority("ADMIN")

                        // 4. CẢ ADMIN & STAFF ĐỀU VÀO ĐƯỢC
                        .requestMatchers(
                                "/admin/orders/**",
                                "/admin/products/**",
                                "/admin/inventory/**",
                                "/admin/map/**",
                                "/admin/reviews/**",
                                "/admin/chat/**",
                                "/admin"
                        ).hasAnyAuthority("ADMIN", "STAFF")

                        .requestMatchers(
                                "/", "/login", "/register/**", "/verify/**", "/resend-otp",
                                "/forgot-password/**", "/verify-reset/**", "/reset-password/**",
                                "/products/**",
                                "/ws-smarttech/**",
                                "/api/chat/history/**",  // <-- THÊM DẤU /** VÀO ĐÂY
                                "/api/chat/rooms"        // <-- THÊM DÒNG NÀY (Để khách vãng lai không bị lỗi)
                        ).permitAll()

                        .requestMatchers("/cart/**", "/checkout/**", "/profile/**", "/orders/**").authenticated()
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")
                        .defaultSuccessUrl("/", true)
                        .failureHandler(failureHandler)
                        .permitAll()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .userInfoEndpoint(userInfo -> userInfo.userService(oauth2UserService))
                        .defaultSuccessUrl("/", true)
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}
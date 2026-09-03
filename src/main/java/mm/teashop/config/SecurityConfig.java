package mm.teashop.config;

import lombok.RequiredArgsConstructor;
import mm.teashop.repo.OwnerRepository;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

@Configuration
@RequiredArgsConstructor
public class SecurityConfig {
    private final OwnerRepository owners;

    @Bean
    PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    UserDetailsService userDetailsService() {
        return username -> owners.findByUsername(username)
            .map(owner -> User.withUsername(owner.getUsername())
                .password(owner.getPassword())
                .roles("OWNER")
                .disabled(!owner.isEnabled())
                .build())
            .orElseThrow(() -> new org.springframework.security.core.userdetails.UsernameNotFoundException("Owner not found"));
    }

    @Bean
    AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, SecurityContextRepository repository) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .securityContext(context -> context
                .requireExplicitSave(true)
                .securityContextRepository(repository))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/manage/waiters/login").permitAll()
                .requestMatchers("/api/manage/waiters/**", "/api/manage/reports").hasRole("OWNER")
                .anyRequest().permitAll())
            .exceptionHandling(errors -> errors
                .authenticationEntryPoint((request, response, ex) -> response.sendError(HttpStatus.UNAUTHORIZED.value(), "Owner login required"))
                .accessDeniedHandler((request, response, ex) -> response.sendError(HttpStatus.FORBIDDEN.value(), "Owner access required")))
            .logout(logout -> logout
                .logoutUrl("/api/auth/logout")
                .logoutSuccessHandler((request, response, authentication) -> response.setStatus(HttpStatus.NO_CONTENT.value())));
        return http.build();
    }
}

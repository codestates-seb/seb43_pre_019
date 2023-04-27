package backend.com.backend.config;

import backend.com.backend.auth.filter.JwtAuthenticationFilter;
import backend.com.backend.auth.filter.JwtVerificationFilter;
import backend.com.backend.auth.jwt.JwtTokenizer;
import backend.com.backend.auth.utils.CustomAuthorityUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

import static org.springframework.security.config.Customizer.withDefaults;

@Configuration
public class SecurityConfiguration {
    private final JwtTokenizer jwtTokenizer;
    private final CustomAuthorityUtils authorityUtils;

    public SecurityConfiguration(JwtTokenizer jwtTokenizer, CustomAuthorityUtils authorityUtils) {
        this.jwtTokenizer = jwtTokenizer;
        this.authorityUtils = authorityUtils;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .csrf().disable()        // (2)
                .cors(withDefaults())    // (3)
                .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)  // (1) 추가
                .and()
                .formLogin().disable()   // (4)
                .httpBasic().disable()   // (5)
                .apply(new CustomFilterConfigurer())
                .and()
                .authorizeHttpRequests(authorize -> authorize
                        .antMatchers(HttpMethod.POST, "/api/members/sign").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/members/login").permitAll()
                        .antMatchers(HttpMethod.POST, "/api/questions").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.POST, "/api/questions/*/answers").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.POST, "/api/answers/*/comments").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/api/questions/*").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/api/questions/*/answers/*").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.PATCH, "/api/answers/*/comments/*").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/questions/*").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/questions/*/answers/*").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.DELETE, "/api/answers/*/comments/*").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "api/members/info").hasAnyRole("USER", "ADMIN")
                        .antMatchers(HttpMethod.GET, "/api/questions/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/answers/{answer-id}/comments/**").permitAll()
                        .antMatchers(HttpMethod.GET, "/api/member/**").permitAll()
                        .anyRequest().permitAll()
                );
        return http.build();
    }

    // (7)
    @Bean
    public PasswordEncoder passwordEncoder() {
        return PasswordEncoderFactories.createDelegatingPasswordEncoder();
    }

    // (8)
    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("*"));   // (8-1)
        configuration.setAllowedMethods(Arrays.asList("GET","POST", "PATCH", "DELETE", "OPTIONS"));  // (8-2)

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();   // (8-3)
        source.registerCorsConfiguration("/**", configuration);      // (8-4)
        return source;
    }
    public class CustomFilterConfigurer extends AbstractHttpConfigurer<CustomFilterConfigurer, HttpSecurity> {  // (2-1)
        @Override
        public void configure(HttpSecurity builder) throws Exception {  // (2-2)
            AuthenticationManager authenticationManager = builder.getSharedObject(AuthenticationManager.class);  // (2-3)

            JwtAuthenticationFilter jwtAuthenticationFilter = new JwtAuthenticationFilter(authenticationManager, jwtTokenizer);  // (2-4)
            jwtAuthenticationFilter.setFilterProcessesUrl("/api/members/login");          // (2-5)

            JwtVerificationFilter jwtVerificationFilter = new JwtVerificationFilter(jwtTokenizer, authorityUtils); //(2) 추가

            builder
                    .addFilter(jwtAuthenticationFilter)
                    .addFilterAfter(jwtVerificationFilter, JwtAuthenticationFilter.class); // (3)
        }
    }
}

package org.example.messageservice.config;

import java.net.PasswordAuthentication;
import java.security.KeyFactory;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.X509EncodedKeySpec;
import java.util.Base64;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtDecoders;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.BearerTokenAuthentication;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.oauth2.server.resource.web.authentication.BearerTokenAuthenticationFilter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

        @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
        private String issuerUri;


        private static final String PUBLIC_KEY = """
                        -----BEGIN PUBLIC KEY-----
                        MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnGRR5EjI6sK7mNTodRpvt/tlMbDnopEIPKBJcGYVqh4f+S1K9l3GOnRTaXSt0GoaCmC+xbA6148Q2vAoZEQAyHXEhAnp9AXFcNe2Or3Ti31o44iF/fJ+wCOOM7eVvrX866+vRRZ61+8dz4L9FTCpzv3YU9g/2bGh/QMytTXfuSjdoXb3rBVIAJW1KzRB5ts89DW6BS5rWAkXTdoxwzcX2ogK6cHKx+upxlnAdmsBCFdSvzm34ZYSKHfkUf0UuhCKLnMEkwk6irqCcsmwk90Vp9IltDv2GPBDPvr7qDNBQh5+4U1A2dT5YDHyerc2ggnvE4iTlVJfzOXz9oQUggKy4wIDAQAB
                        -----END PUBLIC KEY-----
                        """;

        @Bean
        public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
                // Disable CSRF & Enable Stateless Session Management
                http
                                .csrf(csrf -> csrf.disable())
                                .sessionManagement(session -> session
                                                .sessionCreationPolicy(SessionCreationPolicy.STATELESS));

                // Configure OAuth2 resource server to validate tokens
                http.oauth2ResourceServer(oauth2 -> oauth2
                                .jwt(Customizer.withDefaults()));

                http
                                .authorizeHttpRequests(authorize -> authorize
                                                .requestMatchers("/login/", "/register/").permitAll() // Public
                                                                                                      // endpoints
                                                .anyRequest().authenticated() // Secure everything else
                                );

                return http.build();
        }

        @Bean
        public JwtDecoder jwtDecoder() {
                try {
                        RSAPublicKey rsaPublicKey = getPublicKey(PUBLIC_KEY);
                        return NimbusJwtDecoder.withPublicKey(rsaPublicKey).build();
                } catch (Exception e) {
                        throw new IllegalArgumentException("Failed to create JwtDecoder", e);
                }
        }

        private RSAPublicKey getPublicKey(String publicKey) throws Exception {
                String cleanKey = publicKey
                                .replaceAll("-----BEGIN PUBLIC KEY-----", "")
                                .replaceAll("-----END PUBLIC KEY-----", "")
                                .replaceAll("\\s+", "");
                byte[] decodedKey = Base64.getDecoder().decode(cleanKey);

                KeyFactory keyFactory = KeyFactory.getInstance("RSA");
                X509EncodedKeySpec keySpec = new X509EncodedKeySpec(decodedKey);
                return (RSAPublicKey) keyFactory.generatePublic(keySpec);
        }

        @Bean
        public JwtAuthenticationConverter jwtAuthenticationConverter() {
                System.out.println("JWT Authentication Converter");
                JwtGrantedAuthoritiesConverter grantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
                grantedAuthoritiesConverter.setAuthoritiesClaimName("role");
                grantedAuthoritiesConverter.setAuthorityPrefix("ROLE_");

                JwtAuthenticationConverter authenticationConverter = new JwtAuthenticationConverter();
                authenticationConverter.setJwtGrantedAuthoritiesConverter(grantedAuthoritiesConverter);
                return authenticationConverter;
        }
}

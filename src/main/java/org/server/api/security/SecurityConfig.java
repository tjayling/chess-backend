//package org.server.api.security;// src/main/java/com/auth0/example/security/SecurityConfig.java
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.security.config.Customizer;
//import org.springframework.security.config.annotation.web.builders.HttpSecurity;
//import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
//import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
//import org.springframework.security.oauth2.core.OAuth2TokenValidator;
//import org.springframework.security.oauth2.jwt.*;
//import org.springframework.security.web.SecurityFilterChain;
//
//@EnableWebSecurity
//public class SecurityConfig {
//
//    @Value("${auth0.audience}")
//    private String audience;
//
//    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
//    private String issuer;
//
//    @Bean
//    JwtDecoder jwtDecoder() {
//        NimbusJwtDecoder jwtDecoder = JwtDecoders.fromOidcIssuerLocation(issuer);
//
//        OAuth2TokenValidator<Jwt> audienceValidator = new AudienceValidator(audience);
//        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuer);
//        OAuth2TokenValidator<Jwt> withAudience = new DelegatingOAuth2TokenValidator<>(withIssuer, audienceValidator);
//
//        jwtDecoder.setJwtValidator(withAudience);
//
//        return jwtDecoder;
//    }
//    @Bean
//    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
////        http.authorizeRequests()
////                .mvcMatchers("/api/public").permitAll()
////                .mvcMatchers("/api/private").authenticated()
////                .mvcMatchers("/api/private-scoped").hasAuthority("SCOPE_read:messages")
////                .and().cors()
////                .and().oauth2ResourceServer().jwt();
//        http.authorizeRequests(authorize -> authorize
//                        .antMatchers("/api/public").permitAll()
//                        .antMatchers("/api/private").authenticated()
//                        .antMatchers("/api/private-scoped").hasAuthority("SCOPE_read:messages")
//                        .anyRequest().authenticated()
//                )
//                .cors(Customizer.withDefaults())
//                .oauth2ResourceServer().jwt();
//        return http.build();
//    }
//}
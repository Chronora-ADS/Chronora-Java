package com.example.client_server.util;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

@Configuration
public class CorsConfig {

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration config = new CorsConfiguration();

        config.addAllowedOrigin("http://127.0.0.1:5501"); // URL do seu frontend
        config.addAllowedOrigin("http://127.0.0.1:5502"); // URL do seu frontend
        config.addAllowedOrigin("http://localhost:8000"); // URL do seu frontend
        config.addAllowedHeader("*");
        config.addAllowedMethod("GET");
        config.addAllowedMethod("POST");
        config.addAllowedMethod("PUT");
        config.addAllowedMethod("DELETE");
        config.addAllowedMethod("OPTIONS");
        config.setAllowCredentials(true);

        source.registerCorsConfiguration("/auth/**", config);
        source.registerCorsConfiguration("/user/**", config);
        source.registerCorsConfiguration("/service/**", config);

        return new CorsFilter(source);
    }
}
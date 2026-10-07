package com.football.bookingservice.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI bookingOpenAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title("Football Booking Service API")
                        .description("Tài liệu RESTful API nâng cao cho Hệ thống quản lý đặt lịch sân bóng đá (Buổi thực hành 3)")
                        .version("1.0.0")
                        .contact(new Contact().name("SOA Football System").email("admin@football.com")));
    }
}

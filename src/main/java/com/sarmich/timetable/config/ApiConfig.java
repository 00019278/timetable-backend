package com.sarmich.timetable.config;

import java.util.Collections;
import java.util.List;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApiConfig {

    @Bean
    public OpenAPI myOpenAPI() {
        Server devServer = new Server();
        Contact contact = new Contact();
        contact.setEmail("kun.uz");
        contact.setName("BezKoder");
        contact.setUrl("https://www.bezkoder.com");

        Info info = new Info()
                .title("Kun uz Management API")
                .version("1.0")
                .contact(contact)
                .description("This API exposes endpoints to manage tutorials.")
                .termsOfService("https://www.bezkoder.com/terms")
                .license(null);

        return new OpenAPI().info(info).servers(Collections.singletonList(devServer));
    }
}

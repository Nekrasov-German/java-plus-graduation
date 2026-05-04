package ru.practicum.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
@ComponentScan({
        "ru.practicum.service",
        "ru.practicum.client",
        "ru.practicum.dto"
})
@EnableFeignClients(basePackages = {
        "ru.practicum.client",
        "ru.practicum.interaction.user_client"
})
public class ServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(ServiceApp.class, args);
    }
}

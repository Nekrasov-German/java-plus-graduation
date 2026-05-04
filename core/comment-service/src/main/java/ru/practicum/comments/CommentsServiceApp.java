package ru.practicum.comments;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients(basePackages = {
        "ru.practicum.client",
        "ru.practicum.interaction"
})
public class CommentsServiceApp {
    public static void main(String[] args) {
        SpringApplication.run(CommentsServiceApp.class, args);
    }
}

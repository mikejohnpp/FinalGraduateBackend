package org.social.notificationservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@EntityScan(basePackages = { "org.social.common.entities"})
@EnableJpaRepositories(basePackages = {"org.social.common.repositories"})
@ComponentScan(basePackages = {
        "org.social.notificationservice",
        "org.social.common.exceptions",
        "org.social.common.kafka"
})
public class NotificationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(NotificationServiceApplication.class, args);
    }

}

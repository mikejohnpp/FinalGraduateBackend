package org.social.servicetemplete;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(exclude = {})
@EntityScan(basePackages = { "org.social.entities"})
@EnableJpaRepositories(basePackages = {"org.social.repositories"})
public class ServiceTempleteApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceTempleteApplication.class, args);
    }
}

package org.social.chatservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.persistence.autoconfigure.EntityScan;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;

@EnableCaching
@SpringBootApplication
@EnableScheduling
@EnableFeignClients
@EnableDiscoveryClient
@EntityScan(basePackages = { "org.social.common.entities" })
@EnableJpaRepositories(basePackages = { "org.social.common.repositories" })
@ComponentScan(basePackages = {
        "org.social.chatservice",
        "org.social.common.exceptions",
        "org.social.common.kafka",
        "org.social.common"
})
public class ChatServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ChatServiceApplication.class, args);
    }

}

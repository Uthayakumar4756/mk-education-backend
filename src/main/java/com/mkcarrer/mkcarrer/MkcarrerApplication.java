package com.mkcarrer.mkcarrer;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication(scanBasePackages = "com.mkcareer")
@EntityScan(basePackages = "com.mkcareer.entity")
@EnableJpaRepositories(basePackages = "com.mkcareer.repository")
public class MkcarrerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MkcarrerApplication.class, args);
    }
}

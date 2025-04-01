package com.wovengold.pdi;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.domain.EntityScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;

@SpringBootApplication
@ComponentScan(basePackages = "com.wovengold.pdi")
@EntityScan(basePackages = "com.wovengold.pdi.model")
@EnableJpaRepositories(basePackages = "com.wovengold.pdi.repository")
public class WovenGoldPdiApplication {
    public static void main(String[] args) {
        SpringApplication.run(WovenGoldPdiApplication.class, args);
    }
    
    @Bean
    public CommandLineRunner testRunner() {
        return args -> {
            System.out.println("\n\n====================================");
            System.out.println("WovenGold PDI Application is running!");
            System.out.println("Access the API at: http://localhost:9090");
            System.out.println("Health check at: http://localhost:9090/health");
            System.out.println("H2 Console at: http://localhost:9090/h2-console");
            System.out.println("====================================\n\n");
        };
    }
} 
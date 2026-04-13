package com.deploylens;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class DeployLensApplication {
    public static void main(String[] args) {
        SpringApplication.run(DeployLensApplication.class, args);
    }
}

package com.mall.job;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Scheduled tasks application.
 */
@SpringBootApplication
@ComponentScan(basePackages = "com.mall")
@EnableScheduling
public class MallJobApplication {

    public static void main(String[] args) {
        SpringApplication.run(MallJobApplication.class, args);
    }
}

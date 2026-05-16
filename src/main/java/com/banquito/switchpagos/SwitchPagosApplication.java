package com.banquito.switchpagos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class SwitchPagosApplication {

    public static void main(String[] args) {
        SpringApplication.run(SwitchPagosApplication.class, args);
    }

}

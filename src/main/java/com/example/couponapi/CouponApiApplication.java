package com.example.couponapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Ponto de entrada da aplicação Spring Boot.
 *
 * @SpringBootApplication combina:
 *  - @Configuration       → classe de configuração Spring
 *  - @EnableAutoConfiguration → habilita a auto-configuração do Spring Boot
 *  - @ComponentScan       → escaneia os pacotes a partir deste
 */
@SpringBootApplication
public class CouponApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(CouponApiApplication.class, args);
    }
}

package br.com.ofisy;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class OfisyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfisyApplication.class, args);
    }

}

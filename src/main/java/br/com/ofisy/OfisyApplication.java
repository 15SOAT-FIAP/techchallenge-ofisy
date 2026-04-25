package br.com.ofisy;

import br.com.ofisy.infrastructure.config.auth.JwtProperties;
import br.com.ofisy.infrastructure.config.security.SecurityProperties;
import br.com.ofisy.infrastructure.config.seed.DataSeederProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class, DataSeederProperties.class})
public class OfisyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfisyApplication.class, args);
    }
}
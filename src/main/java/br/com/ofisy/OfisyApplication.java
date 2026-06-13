package br.com.ofisy;

import br.com.ofisy.shared.jwt.JwtProperties;
import br.com.ofisy.config.security.SecurityProperties;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties({SecurityProperties.class, JwtProperties.class})
public class OfisyApplication {

    public static void main(String[] args) {
        SpringApplication.run(OfisyApplication.class, args);
    }
}
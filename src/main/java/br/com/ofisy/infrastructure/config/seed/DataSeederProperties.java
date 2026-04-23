package br.com.ofisy.infrastructure.config.seed;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "data-seeder")
public class DataSeederProperties {
    private String adminPassword;
    private String adminLogin;
}

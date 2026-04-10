package br.com.ofisy.infrastructure.config.seed;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
public class DataSeeder {

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = "admin@ofisy.com";

            if (userRepository.findByEmailEmailAddress(adminEmail).isEmpty()) {
                User admin = User.create(adminEmail, passwordEncoder.encode("Admin@1234"), "Administrator", Role.ADMIN);
                userRepository.save(admin);
            }
        };
    }
}
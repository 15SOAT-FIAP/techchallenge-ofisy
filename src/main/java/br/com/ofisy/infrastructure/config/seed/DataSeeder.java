package br.com.ofisy.infrastructure.config.seed;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Configuration
@RequiredArgsConstructor
public class DataSeeder {

    private final DataSeederProperties dataSeederProperties;

    @Bean
    CommandLineRunner initDatabase(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            String adminEmail = dataSeederProperties.getAdminLogin();

            if (userRepository.findByEmailAddress(adminEmail).isEmpty()) {
                User admin = User.create(adminEmail, passwordEncoder.encode(dataSeederProperties.getAdminPassword()), "Administrator", Role.ADMIN);
                userRepository.save(admin);
            }
        };
    }
}
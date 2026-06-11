package br.com.ofisy.application.user.createuser;

import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
public class CreateUserService implements CreateUserUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public CreateUserService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(CreateUserCommand command) {
        User.validateExistingEmail(
                repository.existsByEmailAddress(command.email()),
                command.email()
        );

        User newUser = User.create(
                command.email(),
                passwordEncoder.encode(command.password()),
                command.name(),
                command.role()
        );

        return repository.save(newUser);
    }
}

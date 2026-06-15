package br.com.ofisy.application.user.login;

import br.com.ofisy.application.user.exceptions.EmailNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class LoginService implements LoginUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;
    private final TokenGenerator tokenGenerator;

    public LoginService(UserRepository repository, PasswordEncoder passwordEncoder, TokenGenerator tokenGenerator) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
        this.tokenGenerator = tokenGenerator;
    }

    @Override
    public String execute(LoginCommand command) {
        User user = repository.findByEmailAddress(command.email())
                .orElseThrow(() -> new EmailNotFoundException(command.email()));

        user.validateIsActive();

        user.validateCurrentPassword(
                passwordEncoder.matches(command.password(), user.getPassword())
        );

        return tokenGenerator.generateToken(user.getEmail().emailAddress());
    }
}

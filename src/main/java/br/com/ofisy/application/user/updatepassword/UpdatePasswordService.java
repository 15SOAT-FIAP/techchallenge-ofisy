package br.com.ofisy.application.user.updatepassword;

import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class UpdatePasswordService implements UpdatePasswordUseCase {

    private final UserRepository repository;
    private final PasswordEncoder passwordEncoder;

    public UpdatePasswordService(UserRepository repository, PasswordEncoder passwordEncoder) {
        this.repository = repository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public User execute(UUID id, UpdatePasswordCommand command) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.validateCurrentPassword(
                passwordEncoder.matches(command.currentPassword(), user.getPassword())
        );
        user.updatePassword(passwordEncoder.encode(command.newPassword()));
        return repository.save(user);
    }
}

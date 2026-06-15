package br.com.ofisy.application.user.deactivateuser;

import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class DeactivateUserService implements DeactivateUserUseCase {

    private final UserRepository repository;

    public DeactivateUserService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(UUID id) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.deactivate();
        return repository.save(user);
    }
}

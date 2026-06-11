package br.com.ofisy.application.user.findbyid;

import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class FindUserByIdService implements FindUserByIdUseCase {

    private final UserRepository repository;

    public FindUserByIdService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
    }
}

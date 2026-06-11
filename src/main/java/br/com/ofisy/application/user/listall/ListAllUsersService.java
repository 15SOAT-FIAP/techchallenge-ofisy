package br.com.ofisy.application.user.listall;

import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional(readOnly = true)
public class ListAllUsersService implements ListAllUsersUseCase {

    private final UserRepository repository;

    public ListAllUsersService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public Page<User> execute(Pageable pageable) {
        return repository.findAll(pageable);
    }
}

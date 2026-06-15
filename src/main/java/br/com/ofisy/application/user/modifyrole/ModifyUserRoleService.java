package br.com.ofisy.application.user.modifyrole;

import br.com.ofisy.application.user.exceptions.UserNotFoundException;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class ModifyUserRoleService implements ModifyUserRoleUseCase {

    private final UserRepository repository;

    public ModifyUserRoleService(UserRepository repository) {
        this.repository = repository;
    }

    @Override
    public User execute(UUID id, ModifyUserRoleCommand command) {
        User user = repository.findById(id)
                .orElseThrow(() -> new UserNotFoundException(id));
        user.modifyRole(command.role());
        return repository.save(user);
    }
}

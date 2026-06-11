package br.com.ofisy.application.user.createuser;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;

public interface CreateUserUseCase {

    User execute(CreateUserCommand command);

    record CreateUserCommand(String name, String email, String password, Role role) {}
}

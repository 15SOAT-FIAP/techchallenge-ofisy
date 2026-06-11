package br.com.ofisy.application.user.modifyrole;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;

import java.util.UUID;

public interface ModifyUserRoleUseCase {

    User execute(UUID id, ModifyUserRoleCommand command);

    record ModifyUserRoleCommand(Role role) {}
}

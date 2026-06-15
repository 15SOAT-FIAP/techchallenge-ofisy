package br.com.ofisy.application.user.deactivateuser;

import br.com.ofisy.domain.user.User;

import java.util.UUID;

public interface DeactivateUserUseCase {
    User execute(UUID id);
}

package br.com.ofisy.application.user.activateuser;

import br.com.ofisy.domain.user.User;

import java.util.UUID;

public interface ActivateUserUseCase {
    User execute(UUID id);
}

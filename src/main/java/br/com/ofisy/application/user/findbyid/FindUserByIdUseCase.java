package br.com.ofisy.application.user.findbyid;

import br.com.ofisy.domain.user.User;

import java.util.UUID;

public interface FindUserByIdUseCase {
    User execute(UUID id);
}

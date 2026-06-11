package br.com.ofisy.application.user.updatepassword;

import br.com.ofisy.domain.user.User;

import java.util.UUID;

public interface UpdatePasswordUseCase {

    User execute(UUID id, UpdatePasswordCommand command);

    record UpdatePasswordCommand(String currentPassword, String newPassword) {}
}

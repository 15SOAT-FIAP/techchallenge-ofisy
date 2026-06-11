package br.com.ofisy.application.user.getidbyemail;

import java.util.UUID;

public interface GetIdByEmailUseCase {
    UUID execute(String email);
}

package br.com.ofisy.application.user.login;

public interface LoginUseCase {

    LoginResult execute(LoginCommand command);

    record LoginCommand(String email, String password) {}
    record LoginResult(String token) {}
}

package br.com.ofisy.application.user.login;

public interface LoginUseCase {

    String execute(LoginCommand command);

    record LoginCommand(String email, String password) {}
}

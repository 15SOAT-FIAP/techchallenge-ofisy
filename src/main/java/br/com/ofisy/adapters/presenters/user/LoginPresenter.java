package br.com.ofisy.adapters.presenters.user;

import br.com.ofisy.adapters.controllers.user.dto.LoginResponseDTO;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class LoginPresenter {

    public static LoginResponseDTO present(String token) {
        return new LoginResponseDTO(token);
    }

}

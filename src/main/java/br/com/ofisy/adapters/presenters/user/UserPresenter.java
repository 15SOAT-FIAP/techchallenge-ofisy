package br.com.ofisy.adapters.presenters.user;

import br.com.ofisy.adapters.controllers.user.dto.UserResponseDTO;
import br.com.ofisy.domain.user.User;

import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserPresenter {

    public static UserResponseDTO present(User user) {
        return new UserResponseDTO(
                user.getId(),
                user.getName(),
                user.getEmail().emailAddress(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

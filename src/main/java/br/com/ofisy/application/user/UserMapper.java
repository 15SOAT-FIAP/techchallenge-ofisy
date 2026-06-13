package br.com.ofisy.application.user;

import br.com.ofisy.application.user.dto.UserResponseDTO;
import br.com.ofisy.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserResponseDTO toResponse(User user) {
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

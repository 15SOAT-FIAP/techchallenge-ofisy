package br.com.ofisy.application.user.mapper;

import br.com.ofisy.application.user.dto.UserDTO;
import br.com.ofisy.domain.user.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

    public UserDTO.UserResponse toResponse(User user) {
        return new UserDTO.UserResponse(
                user.getId(),
                user.getName(),
                user.getEmail(),
                user.getRole(),
                user.isActive(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}

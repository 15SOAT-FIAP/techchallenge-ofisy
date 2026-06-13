package br.com.ofisy.adapters.gateways.user;

import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.User;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class UserMapper {

    public static User toDomain(UserEntity entity) {
        return User.reconstruct(
                entity.getId(),
                new Email(entity.getEmail()),
                entity.getPassword(),
                entity.getName(),
                entity.getRole(),
                entity.isActive(),
                entity.getCreatedAt(),
                entity.getUpdatedAt()
        );
    }

    public static UserEntity toEntity(User user) {
        return UserEntity.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail().emailAddress())
                .password(user.getPassword())
                .role(user.getRole())
                .active(user.isActive())
                .createdAt(user.getCreatedAt())
                .updatedAt(user.getUpdatedAt())
                .build();
    }
}

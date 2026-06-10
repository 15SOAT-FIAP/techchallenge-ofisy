package br.com.ofisy.adapters.gateways.user;

import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserGatewayMapperTest {

    private static final String VALID_EMAIL = "joao@ofisy.com";
    private static final String VALID_NAME = "João Silva";
    private static final String VALID_PASSWORD = "hashed-password";
    private static final Role VALID_ROLE = Role.ATTENDANT;

    @Nested
    class ToEntity {

        @Test
        void shouldMapAllFieldsFromDomainToEntity() {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, VALID_ROLE);

            var entity = UserEntityMapper.toEntity(user);

            assertThat(entity).isNotNull();
            assertThat(entity.getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(entity.getName()).isEqualTo(VALID_NAME);
            assertThat(entity.getPassword()).isEqualTo(VALID_PASSWORD);
            assertThat(entity.getRole()).isEqualTo(VALID_ROLE);
            assertThat(entity.isActive()).isTrue();
        }

        @Test
        void shouldPreserveNullIdForNewUser() {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, VALID_ROLE);

            var entity = UserEntityMapper.toEntity(user);

            assertThat(entity.getId()).isNull();
        }

        @Test
        void shouldPreserveIdForReconstructedUser() {
            var id = UUID.randomUUID();
            var user = userDomain(id, true, LocalDateTime.now(), null);

            var entity = UserEntityMapper.toEntity(user);

            assertThat(entity.getId()).isEqualTo(id);
        }

        @Test
        void shouldPreserveTimestampsFromDomain() {
            var createdAt = LocalDateTime.of(2024, 1, 10, 10, 0);
            var updatedAt = LocalDateTime.of(2024, 1, 15, 12, 0);
            var user = userDomain(UUID.randomUUID(), true, createdAt, updatedAt);

            var entity = UserEntityMapper.toEntity(user);

            assertThat(entity.getCreatedAt()).isEqualTo(createdAt);
            assertThat(entity.getUpdatedAt()).isEqualTo(updatedAt);
        }

        @ParameterizedTest
        @EnumSource(Role.class)
        void shouldMapAllRolesCorrectly(Role role) {
            var user = User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, role);

            var entity = UserEntityMapper.toEntity(user);

            assertThat(entity.getRole()).isEqualTo(role);
        }

        @Test
        void shouldMapInactiveUserCorrectly() {
            var user = userDomain(UUID.randomUUID(), false, LocalDateTime.now(), LocalDateTime.now());

            var entity = UserEntityMapper.toEntity(user);

            assertThat(entity.isActive()).isFalse();
        }
    }

    @Nested
    class ToDomain {

        @Test
        void shouldMapAllFieldsFromEntityToDomain() {
            var entity = validEntity();

            var user = UserEntityMapper.toDomain(entity);

            assertThat(user).isNotNull();
            assertThat(user.getId()).isEqualTo(entity.getId());
            assertThat(user.getName()).isEqualTo(entity.getName());
            assertThat(user.getEmail().emailAddress()).isEqualTo(entity.getEmail());
            assertThat(user.getPassword()).isEqualTo(entity.getPassword());
            assertThat(user.getRole()).isEqualTo(entity.getRole());
            assertThat(user.isActive()).isEqualTo(entity.isActive());
        }

        @Test
        void shouldPreserveTimestampsFromEntity() {
            var entity = validEntity();

            var user = UserEntityMapper.toDomain(entity);

            assertThat(user.getCreatedAt()).isEqualTo(entity.getCreatedAt());
            assertThat(user.getUpdatedAt()).isEqualTo(entity.getUpdatedAt());
        }

        @Test
        void shouldReconstructEmailValueObjectFromString() {
            var entity = validEntity();

            var user = UserEntityMapper.toDomain(entity);

            assertThat(user.getEmail()).isNotNull();
            assertThat(user.getEmail().emailAddress()).isEqualTo(VALID_EMAIL);
        }

        @Test
        void shouldPreserveIdFromEntity() {
            var entity = validEntity();

            var user = UserEntityMapper.toDomain(entity);

            assertThat(user.getId()).isEqualTo(entity.getId());
        }
    }

    private User userDomain(UUID id, boolean active, LocalDateTime createdAt, LocalDateTime updatedAt) {
        return User.reconstruct(id, new Email(VALID_EMAIL), VALID_PASSWORD, VALID_NAME, VALID_ROLE,
                active, createdAt, updatedAt);
    }

    private UserEntity validEntity() {
        return UserEntity.builder()
                .id(UUID.randomUUID())
                .email(VALID_EMAIL)
                .name(VALID_NAME)
                .password(VALID_PASSWORD)
                .role(VALID_ROLE)
                .active(true)
                .createdAt(LocalDateTime.of(2024, 1, 10, 10, 0))
                .updatedAt(LocalDateTime.of(2024, 1, 15, 12, 0))
                .build();
    }
}

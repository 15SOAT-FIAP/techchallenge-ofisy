package br.com.ofisy.adapters.gateways.user;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    private static final String VALID_EMAIL = "joao@ofisy.com";
    private static final String VALID_NAME = "João Silva";
    private static final String VALID_PASSWORD = "hashed-password";
    private static final Role VALID_ROLE = Role.ATTENDANT;

    @Mock
    private JpaUserRepository jpa;

    @InjectMocks
    private UserRepositoryImpl repository;

    @Nested
    class Save {

        @Test
        void shouldConvertToEntityBeforeSaving() {
            var user = validUser();
            var savedEntity = validEntity();
            when(jpa.save(any(UserEntity.class))).thenReturn(savedEntity);

            repository.save(user);

            var captor = ArgumentCaptor.forClass(UserEntity.class);
            verify(jpa).save(captor.capture());
            assertThat(captor.getValue().getEmail()).isEqualTo(VALID_EMAIL);
            assertThat(captor.getValue().getName()).isEqualTo(VALID_NAME);
            assertThat(captor.getValue().getRole()).isEqualTo(VALID_ROLE);
            assertThat(captor.getValue().isActive()).isTrue();
        }

        @Test
        void shouldReturnDomainUserWithIdAfterSave() {
            var user = validUser();
            var savedEntity = validEntity();
            when(jpa.save(any(UserEntity.class))).thenReturn(savedEntity);

            var result = repository.save(user);

            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(savedEntity.getId());
            assertThat(result.getName()).isEqualTo(savedEntity.getName());
            assertThat(result.getEmail().emailAddress()).isEqualTo(savedEntity.getEmail());
            assertThat(result.getRole()).isEqualTo(savedEntity.getRole());
        }

        @Test
        void shouldReturnUserWithTimestampsFromPersistedEntity() {
            var user = validUser();
            var savedEntity = validEntity();
            when(jpa.save(any(UserEntity.class))).thenReturn(savedEntity);

            var result = repository.save(user);

            assertThat(result.getCreatedAt()).isEqualTo(savedEntity.getCreatedAt());
            assertThat(result.getUpdatedAt()).isEqualTo(savedEntity.getUpdatedAt());
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturnPageOfDomainUsersMappedFromEntities() {
            var pageable = PageRequest.of(0, 10);
            var entity = validEntity();
            var page = new PageImpl<>(List.of(entity), pageable, 1);
            when(jpa.findAll(pageable)).thenReturn(page);

            var result = repository.findAll(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().getFirst().getId()).isEqualTo(entity.getId());
            assertThat(result.getContent().getFirst().getName()).isEqualTo(entity.getName());
            verify(jpa).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<UserEntity> emptyPage = new PageImpl<>(List.of(), pageable, 0);
            when(jpa.findAll(pageable)).thenReturn(emptyPage);

            var result = repository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    class FindById {

        @Test
        void shouldReturnDomainUserWhenEntityFound() {
            var id = UUID.randomUUID();
            var entity = validEntity();
            when(jpa.findById(id)).thenReturn(Optional.of(entity));

            var result = repository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get().getId()).isEqualTo(entity.getId());
            assertThat(result.get().getName()).isEqualTo(entity.getName());
            assertThat(result.get().getEmail().emailAddress()).isEqualTo(entity.getEmail());
        }

        @Test
        void shouldReturnEmptyWhenNotFound() {
            var id = UUID.randomUUID();
            when(jpa.findById(id)).thenReturn(Optional.empty());

            var result = repository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class FindByEmailAddress {

        @Test
        void shouldReturnDomainUserWhenEmailFound() {
            var entity = validEntity();
            when(jpa.findByEmail(VALID_EMAIL)).thenReturn(Optional.of(entity));

            var result = repository.findByEmailAddress(VALID_EMAIL);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail().emailAddress()).isEqualTo(VALID_EMAIL);
            verify(jpa).findByEmail(VALID_EMAIL);
        }

        @Test
        void shouldReturnEmptyWhenEmailNotFound() {
            when(jpa.findByEmail(VALID_EMAIL)).thenReturn(Optional.empty());

            var result = repository.findByEmailAddress(VALID_EMAIL);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    class ExistsByEmailAddress {

        @Test
        void shouldReturnTrueWhenEmailExists() {
            when(jpa.existsByEmail(VALID_EMAIL)).thenReturn(true);

            assertThat(repository.existsByEmailAddress(VALID_EMAIL)).isTrue();
            verify(jpa).existsByEmail(VALID_EMAIL);
        }

        @Test
        void shouldReturnFalseWhenEmailDoesNotExist() {
            when(jpa.existsByEmail(VALID_EMAIL)).thenReturn(false);

            assertThat(repository.existsByEmailAddress(VALID_EMAIL)).isFalse();
        }
    }

    private User validUser() {
        return User.create(VALID_EMAIL, VALID_PASSWORD, VALID_NAME, VALID_ROLE);
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

package br.com.ofisy.infrastructure.persistence.user;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserRepositoryImplTest {

    @Mock
    private JpaUserRepository jpaUserRepository;

    @InjectMocks
    private UserRepositoryImpl userRepository;

    @Nested
    @DisplayName("findById")
    class FindById {

        @Test
        @DisplayName("Deve retornar usuário quando encontrado")
        void shouldReturnUserWhenFound() {
            UUID id = UUID.randomUUID();
            User user = mockUser();

            when(jpaUserRepository.findById(id)).thenReturn(Optional.of(user));

            Optional<User> result = userRepository.findById(id);

            assertThat(result).isPresent();
            assertThat(result.get()).isEqualTo(user);
            verify(jpaUserRepository).findById(id);
        }

        @Test
        @DisplayName("Deve retornar vazio quando não encontrado")
        void shouldReturnEmptyWhenNotFound() {
            UUID id = UUID.randomUUID();

            when(jpaUserRepository.findById(id)).thenReturn(Optional.empty());

            Optional<User> result = userRepository.findById(id);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("findAll")
    class FindAll {

        @Test
        @DisplayName("Deve retornar página com usuários")
        void shouldReturnPageWithUsers() {
            Pageable pageable = PageRequest.of(0, 10);
            User user = mockUser();
            Page<User> page = new PageImpl<>(List.of(user), pageable, 1);

            when(jpaUserRepository.findAll(pageable)).thenReturn(page);

            Page<User> result = userRepository.findAll(pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getTotalElements()).isEqualTo(1);
            verify(jpaUserRepository).findAll(pageable);
        }

        @Test
        @DisplayName("Deve retornar página vazia quando não há usuários")
        void shouldReturnEmptyPage() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<User> emptyPage = new PageImpl<>(List.of(), pageable, 0);

            when(jpaUserRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<User> result = userRepository.findAll(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }
    }

    @Nested
    @DisplayName("save")
    class Save {

        @Test
        @DisplayName("Deve salvar e retornar usuário")
        void shouldSaveAndReturnUser() {
            User user = mockUser();

            when(jpaUserRepository.save(user)).thenReturn(user);

            User result = userRepository.save(user);

            assertThat(result).isEqualTo(user);
            verify(jpaUserRepository).save(user);
        }
    }

    @Nested
    @DisplayName("findByEmailAddress")
    class FindByEmailAddress {

        @Test
        @DisplayName("Deve retornar usuário quando email encontrado")
        void shouldReturnUserWhenEmailFound() {
            String email = "joao@ofisy.com";
            User user = mockUser();

            when(jpaUserRepository.findByEmailEmailAddress(email)).thenReturn(Optional.of(user));

            Optional<User> result = userRepository.findByEmailAddress(email);

            assertThat(result).isPresent();
            assertThat(result.get().getEmail().emailAddress()).isEqualTo(email);
            verify(jpaUserRepository).findByEmailEmailAddress(email);
        }

        @Test
        @DisplayName("Deve retornar vazio quando email não encontrado")
        void shouldReturnEmptyWhenEmailNotFound() {
            String email = "naoexiste@ofisy.com";

            when(jpaUserRepository.findByEmailEmailAddress(email)).thenReturn(Optional.empty());

            Optional<User> result = userRepository.findByEmailAddress(email);

            assertThat(result).isEmpty();
        }
    }

    @Nested
    @DisplayName("existsByEmailAddress")
    class ExistsByEmailAddress {

        @Test
        @DisplayName("Deve retornar true quando email já existe")
        void shouldReturnTrueWhenEmailExists() {
            String email = "joao@ofisy.com";

            when(jpaUserRepository.existsByEmailEmailAddress(email)).thenReturn(true);

            assertThat(userRepository.existsByEmailAddress(email)).isTrue();
            verify(jpaUserRepository).existsByEmailEmailAddress(email);
        }

        @Test
        @DisplayName("Deve retornar false quando email não existe")
        void shouldReturnFalseWhenEmailDoesNotExist() {
            String email = "naoexiste@ofisy.com";

            when(jpaUserRepository.existsByEmailEmailAddress(email)).thenReturn(false);

            assertThat(userRepository.existsByEmailAddress(email)).isFalse();
        }
    }

    @Nested
    @DisplayName("deleteById")
    class DeleteById {

        @Test
        @DisplayName("Deve deletar usuário por ID")
        void shouldDeleteUserById() {
            UUID id = UUID.randomUUID();

            userRepository.deleteById(id);

            verify(jpaUserRepository).deleteById(id);
        }
    }

    private User mockUser() {
        return User.create("joao@ofisy.com", "hashed-password", "João Silva", Role.ATTENDANT);
    }

}
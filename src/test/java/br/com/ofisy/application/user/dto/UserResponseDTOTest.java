package br.com.ofisy.application.user.dto;

import br.com.ofisy.domain.user.Email;
import br.com.ofisy.domain.user.Role;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class UserResponseDTOTest {

    public static final String TEST_USER_PRINCIPAL_NAME = "João Silva";
    public static final String TEST_USER_PRINCIPAL_EMAIL = "joao@ofisy.com";

    @Test
    @DisplayName("Deve criar response com todos os campos")
    void shouldCreateResponseWithAllFields() {
        UUID id = UUID.randomUUID();
        Email email = new Email(TEST_USER_PRINCIPAL_EMAIL);
        LocalDateTime now = LocalDateTime.now();

        UserResponseDTO dto = new UserResponseDTO(
                id, TEST_USER_PRINCIPAL_NAME, email, Role.ATTENDANT, true, now, null
        );

        assertThat(dto.id()).isEqualTo(id);
        assertThat(dto.name()).isEqualTo(TEST_USER_PRINCIPAL_NAME);
        assertThat(dto.email().emailAddress()).isEqualTo(TEST_USER_PRINCIPAL_EMAIL);
        assertThat(dto.role()).isEqualTo(Role.ATTENDANT);
        assertThat(dto.active()).isTrue();
        assertThat(dto.creationDate()).isEqualTo(now);
        assertThat(dto.modifiedDate()).isNull();
    }

    @Test
    @DisplayName("Deve criar response com data de modificação preenchida")
    void shouldCreateResponseWithModifiedDate() {
        UUID id = UUID.randomUUID();
        Email email = new Email(TEST_USER_PRINCIPAL_EMAIL);
        LocalDateTime createdAt = LocalDateTime.now().minusDays(1);
        LocalDateTime updatedAt = LocalDateTime.now();

        UserResponseDTO dto = new UserResponseDTO(
                id, TEST_USER_PRINCIPAL_NAME, email, Role.ADMIN, false, createdAt, updatedAt
        );

        assertThat(dto.active()).isFalse();
        assertThat(dto.modifiedDate()).isEqualTo(updatedAt);
        assertThat(dto.role()).isEqualTo(Role.ADMIN);
    }
}
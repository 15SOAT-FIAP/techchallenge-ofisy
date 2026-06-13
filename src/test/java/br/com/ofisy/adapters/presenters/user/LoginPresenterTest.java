package br.com.ofisy.adapters.presenters.user;

import br.com.ofisy.adapters.controllers.user.dto.LoginResponseDTO;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LoginPresenterTest {

    @Nested
    class Present {

        @Test
        void shouldMapTokenToLoginResponseDTO() {
            var token = "mocked-jwt-token";

            LoginResponseDTO dto = LoginPresenter.present(token);

            assertThat(dto).isNotNull();
            assertThat(dto.token()).isEqualTo(token);
        }

        @Test
        void shouldPreserveTokenValueExactly() {
            var token = "eyJhbGciOiJIUzI1NiJ9.eyJzdWIiOiJhZG1pbkBvZmlzeS5jb20ifQ.abc123";

            var dto = LoginPresenter.present(token);

            assertThat(dto.token()).isEqualTo(token);
        }

        @Test
        void shouldHandleMinimalToken() {
            var token = "t";

            var dto = LoginPresenter.present(token);

            assertThat(dto.token()).isEqualTo("t");
        }
    }
}
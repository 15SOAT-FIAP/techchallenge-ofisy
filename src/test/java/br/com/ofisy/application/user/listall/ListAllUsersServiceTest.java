package br.com.ofisy.application.user.listall;

import br.com.ofisy.domain.user.Role;
import br.com.ofisy.domain.user.User;
import br.com.ofisy.domain.user.UserRepository;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListAllUsersServiceTest {

    @Mock
    private UserRepository repository;

    @InjectMocks
    private ListAllUsersService service;

    @Nested
    class Execute {

        @Test
        void shouldReturnPageOfUsers() {
            var pageable = PageRequest.of(0, 10);
            var user = User.create("joao@ofisy.com", "hashed", "João Silva", Role.ATTENDANT);
            var page = new PageImpl<>(List.of(user), pageable, 1);
            when(repository.findAll(pageable)).thenReturn(page);

            var result = service.execute(pageable);

            assertThat(result.getTotalElements()).isEqualTo(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("João Silva");
            verify(repository).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoUsers() {
            var pageable = PageRequest.of(0, 10);
            var emptyPage = new PageImpl<User>(Collections.emptyList(), pageable, 0);
            when(repository.findAll(pageable)).thenReturn(emptyPage);

            var result = service.execute(pageable);

            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isZero();
        }

        @Test
        void shouldRespectPageableParameters() {
            var pageable = PageRequest.of(2, 5);
            var emptyPage = new PageImpl<User>(Collections.emptyList(), pageable, 0);
            when(repository.findAll(pageable)).thenReturn(emptyPage);

            service.execute(pageable);

            verify(repository).findAll(pageable);
        }
    }
}

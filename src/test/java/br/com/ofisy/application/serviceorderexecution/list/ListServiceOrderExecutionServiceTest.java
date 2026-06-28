package br.com.ofisy.application.serviceorderexecution.list;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListServiceOrderExecutionServiceTest {

    @Mock
    private ServiceOrderExecutionRepository repository;

    @InjectMocks
    private ListServiceOrderExecutionService listService;

    @Nested
    class Execute {

        @Test
        void shouldReturnPageOfServiceOrderExecutionsSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);
            var execution1 = createServiceOrderExecution();
            var execution2 = createServiceOrderExecution();
            Page<ServiceOrderExecution> page = new PageImpl<>(Arrays.asList(execution1, execution2), pageable, 2);

            when(repository.findAll(pageable)).thenReturn(page);

            Page<ServiceOrderExecution> result = listService.execute(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            verify(repository).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoExecutionsExist() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ServiceOrderExecution> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(repository.findAll(pageable)).thenReturn(emptyPage);

            Page<ServiceOrderExecution> result = listService.execute(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(repository).findAll(pageable);
        }

        @Test
        void shouldReturnCorrectPaginationInfo() {
            Pageable pageable = PageRequest.of(1, 5);
            var execution = createServiceOrderExecution();
            Page<ServiceOrderExecution> page = new PageImpl<>(Collections.singletonList(execution), pageable, 15);

            when(repository.findAll(pageable)).thenReturn(page);

            Page<ServiceOrderExecution> result = listService.execute(pageable);

            assertThat(result.getNumber()).isEqualTo(1);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
        }
    }

    private ServiceOrderExecution createServiceOrderExecution() {
        return ServiceOrderExecution.reconstruct(
                UUID.randomUUID(),
                UUID.randomUUID(),
                UUID.randomUUID(),
                ServiceOrderExecutionStatus.PENDING,
                LocalDateTime.now(),
                LocalDateTime.now(),
                null,
                null
        );
    }
}


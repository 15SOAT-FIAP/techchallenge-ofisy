package br.com.ofisy.application.serviceorderexecution.getaverageexecutiontime;

import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecution;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionRepository;
import br.com.ofisy.domain.serviceorderexecution.ServiceOrderExecutionStatus;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class GetAverageExecutionTimeServiceTest {

    private static final UUID SERVICE_CATALOG_ID = UUID.randomUUID();
    private static final UUID SERVICE_ORDER_ID = UUID.randomUUID();

    @Mock private ServiceOrderExecutionRepository repository;

    @InjectMocks
    private GetAverageExecutionTimeService service;

    @Nested
    @DisplayName("execute")
    class Execute {

        @Test
        @DisplayName("Deve retornar zero quando não há execuções para o serviço")
        void shouldReturnZeroWhenNoExecutionsFound() {
            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(emptyPage());

            var result = service.execute(SERVICE_CATALOG_ID);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Deve calcular a média em minutos das execuções concluídas")
        void shouldCalculateAverageInMinutesForCompletedExecutions() {
            var exec1 = executionWithTimes(
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 10, 30));
            var exec2 = executionWithTimes(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 10, 0));

            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(pageOf(exec1, exec2));

            var result = service.execute(SERVICE_CATALOG_ID);

            assertThat(result).isEqualTo(45.0);
        }

        @Test
        @DisplayName("Deve ignorar execuções sem data de início")
        void shouldIgnoreExecutionsWithoutStartedAt() {
            var withoutStart = executionWithTimes(null, LocalDateTime.of(2026, 1, 1, 10, 0));
            var complete = executionWithTimes(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 9, 20));

            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(pageOf(withoutStart, complete));

            var result = service.execute(SERVICE_CATALOG_ID);

            assertThat(result).isEqualTo(20.0);
        }

        @Test
        @DisplayName("Deve ignorar execuções sem data de término")
        void shouldIgnoreExecutionsWithoutFinishedAt() {
            var withoutFinish = executionWithTimes(LocalDateTime.of(2026, 1, 1, 9, 0), null);
            var complete = executionWithTimes(
                    LocalDateTime.of(2026, 1, 1, 9, 0),
                    LocalDateTime.of(2026, 1, 1, 9, 40));

            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(pageOf(withoutFinish, complete));

            var result = service.execute(SERVICE_CATALOG_ID);

            assertThat(result).isEqualTo(40.0);
        }

        @Test
        @DisplayName("Deve retornar zero quando nenhuma execução possui as duas datas preenchidas")
        void shouldReturnZeroWhenNoExecutionHasBothTimestamps() {
            var onlyStarted = executionWithTimes(LocalDateTime.of(2026, 1, 1, 9, 0), null);
            var onlyFinished = executionWithTimes(null, LocalDateTime.of(2026, 1, 1, 9, 0));

            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(pageOf(onlyStarted, onlyFinished));

            var result = service.execute(SERVICE_CATALOG_ID);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Deve considerar zero minutos quando o término é anterior ao início")
        void shouldClampNegativeMinutesToZero() {
            var invertedTimes = executionWithTimes(
                    LocalDateTime.of(2026, 1, 1, 10, 0),
                    LocalDateTime.of(2026, 1, 1, 9, 0));

            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(pageOf(invertedTimes));

            var result = service.execute(SERVICE_CATALOG_ID);

            assertThat(result).isZero();
        }

        @Test
        @DisplayName("Deve consultar o repositório sem paginação")
        void shouldQueryRepositoryWithUnpagedPageable() {
            when(repository.findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged()))
                    .thenReturn(emptyPage());

            service.execute(SERVICE_CATALOG_ID);

            verify(repository).findByServiceCatalogId(SERVICE_CATALOG_ID, Pageable.unpaged());
        }
    }

    private Page<ServiceOrderExecution> emptyPage() {
        return new PageImpl<>(List.of());
    }

    private Page<ServiceOrderExecution> pageOf(ServiceOrderExecution... executions) {
        return new PageImpl<>(List.of(executions));
    }

    private ServiceOrderExecution executionWithTimes(LocalDateTime startedAt, LocalDateTime finishedAt) {
        return ServiceOrderExecution.reconstruct(
                UUID.randomUUID(), SERVICE_CATALOG_ID, SERVICE_ORDER_ID,
                ServiceOrderExecutionStatus.COMPLETED, LocalDateTime.now(), LocalDateTime.now(),
                startedAt, finishedAt);
    }
}
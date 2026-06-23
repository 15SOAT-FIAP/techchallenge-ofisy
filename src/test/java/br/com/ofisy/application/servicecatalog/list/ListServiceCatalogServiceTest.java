package br.com.ofisy.application.servicecatalog.list;

import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import br.com.ofisy.domain.servicecatalog.ServiceCatalogRepository;
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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ListServiceCatalogServiceTest {

    @Mock
    private ServiceCatalogRepository serviceCatalogRepository;

    @InjectMocks
    private ListServiceCatalogService listServiceCatalogService;

    @Nested
    class Execute {

        @Test
        void shouldReturnPageOfServiceCatalogsSuccessfully() {
            Pageable pageable = PageRequest.of(0, 10);
            var serviceCatalog1 = createServiceCatalog("Troca de óleo", "Troca de óleo e filtro", new BigDecimal("150.00"));
            var serviceCatalog2 = createServiceCatalog("Revisão", "Revisão completa", new BigDecimal("500.00"));
            Page<ServiceCatalog> page = new PageImpl<>(Arrays.asList(serviceCatalog1, serviceCatalog2), pageable, 2);

            when(serviceCatalogRepository.findAll(pageable)).thenReturn(page);

            Page<ServiceCatalog> result = listServiceCatalogService.execute(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getNumber()).isEqualTo(0);
            assertThat(result.getSize()).isEqualTo(10);
            verify(serviceCatalogRepository).findAll(pageable);
        }

        @Test
        void shouldReturnEmptyPageWhenNoServiceCatalogsExist() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<ServiceCatalog> emptyPage = new PageImpl<>(Collections.emptyList(), pageable, 0);

            when(serviceCatalogRepository.findAll(pageable)).thenReturn(emptyPage);

            Page<ServiceCatalog> result = listServiceCatalogService.execute(pageable);

            assertThat(result).isNotNull();
            assertThat(result.getContent()).isEmpty();
            assertThat(result.getTotalElements()).isEqualTo(0);
            verify(serviceCatalogRepository).findAll(pageable);
        }

        @Test
        void shouldReturnPageWithDifferentSizes() {
            var serviceCatalog1 = createServiceCatalog("Serviço 1", "Descrição 1", new BigDecimal("100.00"));
            var serviceCatalog2 = createServiceCatalog("Serviço 2", "Descrição 2", new BigDecimal("200.00"));
            var serviceCatalog3 = createServiceCatalog("Serviço 3", "Descrição 3", new BigDecimal("300.00"));

            // First page with 2 elements
            Pageable pageable1 = PageRequest.of(0, 2);
            Page<ServiceCatalog> page1 = new PageImpl<>(Arrays.asList(serviceCatalog1, serviceCatalog2), pageable1, 3);
            when(serviceCatalogRepository.findAll(pageable1)).thenReturn(page1);

            Page<ServiceCatalog> result1 = listServiceCatalogService.execute(pageable1);
            assertThat(result1.getContent()).hasSize(2);
            assertThat(result1.getTotalElements()).isEqualTo(3);

            // Second page with 1 element
            Pageable pageable2 = PageRequest.of(1, 2);
            Page<ServiceCatalog> page2 = new PageImpl<>(Collections.singletonList(serviceCatalog3), pageable2, 3);
            when(serviceCatalogRepository.findAll(pageable2)).thenReturn(page2);

            Page<ServiceCatalog> result2 = listServiceCatalogService.execute(pageable2);
            assertThat(result2.getContent()).hasSize(1);
            assertThat(result2.getTotalElements()).isEqualTo(3);
        }

        @Test
        void shouldReturnPageWithCorrectPaginationInfo() {
            Pageable pageable = PageRequest.of(2, 5);
            var serviceCatalog = createServiceCatalog("Serviço", "Descrição", new BigDecimal("150.00"));
            Page<ServiceCatalog> page = new PageImpl<>(Collections.singletonList(serviceCatalog), pageable, 15);

            when(serviceCatalogRepository.findAll(pageable)).thenReturn(page);

            Page<ServiceCatalog> result = listServiceCatalogService.execute(pageable);

            assertThat(result.getNumber()).isEqualTo(2);
            assertThat(result.getSize()).isEqualTo(5);
            assertThat(result.getTotalElements()).isEqualTo(15);
            assertThat(result.getTotalPages()).isEqualTo(3);
            verify(serviceCatalogRepository).findAll(any(Pageable.class));
        }
    }

    private ServiceCatalog createServiceCatalog(String name, String description, BigDecimal price) {
        return ServiceCatalog.reconstruct(
                UUID.randomUUID(),
                name,
                description,
                price,
                LocalDateTime.now(),
                LocalDateTime.now()
        );
    }
}


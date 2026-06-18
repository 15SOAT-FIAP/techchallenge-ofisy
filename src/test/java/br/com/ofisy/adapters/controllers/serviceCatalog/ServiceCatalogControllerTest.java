package br.com.ofisy.adapters.controllers.serviceCatalog;

import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogRequestDTO;
import br.com.ofisy.adapters.controllers.servicecatalog.dto.ServiceCatalogResponseDTO;
import br.com.ofisy.adapters.controllers.servicecatalog.ServiceCatalogController;
import br.com.ofisy.application.servicecatalog.create.CreateServiceCatalogUseCase;
import br.com.ofisy.application.servicecatalog.identifybyid.IdentifyByIdServiceCatalogUseCase;
import br.com.ofisy.application.servicecatalog.identifybyname.IdentifyByNameServiceCatalogUseCase;
import br.com.ofisy.application.servicecatalog.list.ListServiceCatalogUseCase;
import br.com.ofisy.application.serviceorderexecution.getaverageexecutiontime.GetAverageExecutionTimeServiceOrderExecutionUseCase;
import br.com.ofisy.domain.servicecatalog.ServiceCatalog;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ServiceCatalogControllerTest {

	@Mock
	private GetAverageExecutionTimeServiceOrderExecutionUseCase getAverageExecutionTimeUseCase;

	@Mock
	private CreateServiceCatalogUseCase createServiceCatalogUseCase;

	@Mock
	private ListServiceCatalogUseCase listServiceCatalogUseCase;

	@Mock
	private IdentifyByIdServiceCatalogUseCase identifyByIdServiceCatalogUseCase;

	@Mock
	private IdentifyByNameServiceCatalogUseCase identifyByNameServiceCatalogUseCase;

	@InjectMocks
	private ServiceCatalogController controller;

	private ServiceCatalog serviceCatalog;

	@BeforeEach
	void setUp() {
		serviceCatalog = ServiceCatalog.create("Oil Change", "Change engine oil", new BigDecimal("50.00"));
	}

	@Test
	void getAll_shouldReturnPagedResponse() {
		Pageable pageable = PageRequest.of(0, 10);
		Page<ServiceCatalog> page = new PageImpl<>(List.of(serviceCatalog), pageable, 1);

		when(listServiceCatalogUseCase.execute(pageable)).thenReturn(page);

		var response = controller.getAll(pageable);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		Page<ServiceCatalogResponseDTO> body = response.getBody();
		assertNotNull(body);
		assertEquals(1, body.getTotalElements());
		assertEquals(serviceCatalog.getName(), body.getContent().get(0).name());
	}

	@Test
	void getById_shouldReturnService() {
		UUID id = UUID.randomUUID();
		ServiceCatalog svc = ServiceCatalog.reconstruct(id, serviceCatalog.getName(), serviceCatalog.getDescription(), serviceCatalog.getPrice(), serviceCatalog.getCreatedAt(), serviceCatalog.getUpdatedAt());

		when(identifyByIdServiceCatalogUseCase.execute(id)).thenReturn(svc);

		var response = controller.getById(id);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ServiceCatalogResponseDTO body = response.getBody();
		assertNotNull(body);
		assertEquals(id, body.id());
		assertEquals(svc.getName(), body.name());
	}

	@Test
	void getByName_shouldReturnService() {
		String name = "Oil Change";
		when(identifyByNameServiceCatalogUseCase.execute(name)).thenReturn(serviceCatalog);

		var response = controller.getByName(name);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		ServiceCatalogResponseDTO body = response.getBody();
		assertNotNull(body);
		assertEquals(serviceCatalog.getName(), body.name());
	}

	@Test
	void getExecutionTimeAverage_shouldReturnDouble() {
		UUID id = UUID.randomUUID();
		when(getAverageExecutionTimeUseCase.execute(id)).thenReturn(4.5);

		var response = controller.getExecutionTimeAverage(id);

		assertEquals(HttpStatus.OK, response.getStatusCode());
		assertEquals(4.5, response.getBody());
	}

	@Test
	void create_shouldReturnCreatedService() {
		ServiceCatalogRequestDTO dto = new ServiceCatalogRequestDTO(new BigDecimal("50.00"), "Oil Change", "Change engine oil");
		when(createServiceCatalogUseCase.execute(any(CreateServiceCatalogUseCase.CreateServiceCatalogCommand.class))).thenReturn(serviceCatalog);

		var response = controller.create(dto);

		assertEquals(HttpStatus.CREATED, response.getStatusCode());
		ServiceCatalogResponseDTO body = response.getBody();
		assertNotNull(body);
		assertEquals(serviceCatalog.getName(), body.name());
	}
}

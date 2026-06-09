package br.com.ofisy.adapters.controllers.vehicle;

import br.com.ofisy.application.vehicle.exceptions.VehicleAlreadyExistsException;
import br.com.ofisy.application.vehicle.exceptions.VehicleLicensePlateNotFoundException;
import br.com.ofisy.application.vehicle.exceptions.VehicleNotFoundException;
import br.com.ofisy.application.vehicle.identifybyid.IdentifyVehicleByIdUseCase;
import br.com.ofisy.application.vehicle.identifybylicenseplate.IdentifyVehicleByLicensePlateUseCase;
import br.com.ofisy.application.vehicle.listall.ListRegisteredVehiclesUseCase;
import br.com.ofisy.application.vehicle.listbycustomer.ListVehiclesByCustomerUseCase;
import br.com.ofisy.application.vehicle.register.RegisterVehicleUseCase;
import br.com.ofisy.domain.vehicle.LicensePlate;
import br.com.ofisy.domain.vehicle.Vehicle;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(VehicleController.class)
@WithMockUser
class VehicleControllerTest extends ControllerTestBase {

    private static final String BASE_URL = "/api/v1/vehicles";
    private static final String VALID_PLATE = "ABC1234";
    private static final UUID VALID_ID = UUID.randomUUID();
    private static final UUID VALID_CUSTOMER_ID = UUID.randomUUID();
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private RegisterVehicleUseCase registerVehicleUseCase;
    @MockitoBean
    private ListRegisteredVehiclesUseCase listRegisteredVehiclesUseCase;
    @MockitoBean
    private ListVehiclesByCustomerUseCase listVehiclesByCustomerUseCase;
    @MockitoBean
    private IdentifyVehicleByIdUseCase identifyVehicleByIdUseCase;
    @MockitoBean
    private IdentifyVehicleByLicensePlateUseCase identifyVehicleByLicensePlateUseCase;

    @Nested
    class GetAllVehicles {

        @Test
        void shouldReturn200WithPageOfVehicles() throws Exception {
            Vehicle vehicle = validVehicle();
            PageRequest pageable = PageRequest.of(0, 10);
            PageImpl<Vehicle> page = new PageImpl<>(List.of(vehicle), pageable, 1);
            when(listRegisteredVehiclesUseCase.execute(any())).thenReturn(page);

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content[0].licensePlate").value(VALID_PLATE))
                    .andExpect(jsonPath("$.totalElements").value(1));
        }

        @Test
        void shouldReturn200WithEmptyPage() throws Exception {
            PageRequest pageable = PageRequest.of(0, 10);
            when(listRegisteredVehiclesUseCase.execute(any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.content").isArray())
                    .andExpect(jsonPath("$.content").isEmpty())
                    .andExpect(jsonPath("$.totalElements").value(0));
        }

        @Test
        void shouldRespectPageAndSizeQueryParams() throws Exception {
            PageRequest pageable = PageRequest.of(1, 5);
            when(listRegisteredVehiclesUseCase.execute(any()))
                    .thenReturn(new PageImpl<>(Collections.emptyList(), pageable, 0));

            mockMvc.perform(get(BASE_URL).param("page", "1").param("size", "5"))
                    .andExpect(status().isOk());

            verify(listRegisteredVehiclesUseCase).execute(any());
        }
    }

    @Nested
    class GetVehiclesByCustomerId {

        @Test
        void shouldReturn200WithListOfVehicles() throws Exception {
            when(listVehiclesByCustomerUseCase.execute(VALID_CUSTOMER_ID)).thenReturn(List.of(validVehicle()));

            mockMvc.perform(get(BASE_URL + "/customers/{customerId}", VALID_CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].licensePlate").value(VALID_PLATE));
        }

        @Test
        void shouldReturn200WithEmptyListWhenNoneFound() throws Exception {
            when(listVehiclesByCustomerUseCase.execute(VALID_CUSTOMER_ID)).thenReturn(List.of());

            mockMvc.perform(get(BASE_URL + "/customers/{customerId}", VALID_CUSTOMER_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class GetVehicleByLicensePlate {

        @Test
        void shouldReturn200WithVehicleWhenFound() throws Exception {
            when(identifyVehicleByLicensePlateUseCase.execute(VALID_PLATE)).thenReturn(validVehicle());

            mockMvc.perform(get(BASE_URL).param("licensePlate", VALID_PLATE))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.licensePlate").value(VALID_PLATE))
                    .andExpect(jsonPath("$.model").value("Civic"));
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            when(identifyVehicleByLicensePlateUseCase.execute(VALID_PLATE))
                    .thenThrow(new VehicleLicensePlateNotFoundException(VALID_PLATE));

            mockMvc.perform(get(BASE_URL).param("licensePlate", VALID_PLATE))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Veículo não encontrado pela placa"));
        }
    }

    @Nested
    class GetVehicleById {

        @Test
        void shouldReturn200WithVehicleWhenFound() throws Exception {
            when(identifyVehicleByIdUseCase.execute(VALID_ID)).thenReturn(validVehicle());

            mockMvc.perform(get(BASE_URL + "/{id}", VALID_ID))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.licensePlate").value(VALID_PLATE))
                    .andExpect(jsonPath("$.model").value("Civic"))
                    .andExpect(jsonPath("$.brand").value("Honda"))
                    .andExpect(jsonPath("$.color").value("Preto"))
                    .andExpect(jsonPath("$.year").value(2022))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            when(identifyVehicleByIdUseCase.execute(VALID_ID))
                    .thenThrow(new VehicleNotFoundException(VALID_ID));

            mockMvc.perform(get(BASE_URL + "/{id}", VALID_ID))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Veículo não encontrado"));
        }

        @Test
        void shouldReturn400WhenIdIsNotValidUUID() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class RegisterVehicle {

        private String validBody() {
            return """
                    {
                        "customerId": "%s",
                        "licensePlate": "ABC1234",
                        "model": "Civic",
                        "brand": "Honda",
                        "color": "Preto",
                        "year": 2022
                    }
                    """.formatted(VALID_CUSTOMER_ID);
        }

        @Test
        void shouldReturn201WithCreatedVehicle() throws Exception {
            when(registerVehicleUseCase.execute(any())).thenReturn(validVehicle());

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.licensePlate").value(VALID_PLATE))
                    .andExpect(jsonPath("$.model").value("Civic"))
                    .andExpect(jsonPath("$.brand").value("Honda"))
                    .andExpect(jsonPath("$.createdAt").exists())
                    .andExpect(jsonPath("$.updatedAt").exists());
        }

        @Test
        void shouldReturn400WhenBodyIsEmpty() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content("{}"))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.customerId").exists())
                    .andExpect(jsonPath("$.errors.licensePlate").exists())
                    .andExpect(jsonPath("$.errors.model").exists())
                    .andExpect(jsonPath("$.errors.brand").exists())
                    .andExpect(jsonPath("$.errors.color").exists())
                    .andExpect(jsonPath("$.errors.year").exists());
        }

        @Test
        void shouldReturn400WhenBodyIsMissing() throws Exception {
            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn409WhenVehicleAlreadyExists() throws Exception {
            when(registerVehicleUseCase.execute(any()))
                    .thenThrow(new VehicleAlreadyExistsException(VALID_PLATE));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody()))
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.title").value("Veículo já existe"))
                    .andExpect(jsonPath("$.detail").value("Veículo com placa " + VALID_PLATE + " já está registrado."));
        }
    }

    private Vehicle validVehicle() {
        return Vehicle.reconstruct(VALID_ID, VALID_CUSTOMER_ID, new LicensePlate(VALID_PLATE),
                "Civic", "Honda", "Preto", 2022, null, NOW, NOW);
    }
}
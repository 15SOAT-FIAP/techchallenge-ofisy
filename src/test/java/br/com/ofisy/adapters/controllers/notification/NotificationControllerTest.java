package br.com.ofisy.adapters.controllers.notification;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.findallserviceorder.FindAllServiceOrderNotificationsUseCase;
import br.com.ofisy.application.notification.findallstock.FindAllStockNotificationsUseCase;
import br.com.ofisy.application.notification.findserviceorderbyid.FindServiceOrderNotificationByIdUseCase;
import br.com.ofisy.application.notification.findstockbyid.FindStockNotificationByIdUseCase;
import br.com.ofisy.application.notification.findunreadserviceorder.FindUnreadServiceOrderNotificationsUseCase;
import br.com.ofisy.application.notification.findunreadstock.FindUnreadStockNotificationsUseCase;
import br.com.ofisy.application.notification.markserviceorderasread.MarkServiceOrderNotificationAsReadUseCase;
import br.com.ofisy.application.notification.markstockasread.MarkStockNotificationAsReadUseCase;
import br.com.ofisy.domain.notification.Notification;
import br.com.ofisy.domain.notification.NotificationMessage;
import br.com.ofisy.domain.notification.NotificationType;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@WithMockUser
class NotificationControllerTest extends ControllerTestBase {

    private static final String BASE_URL = "/api/v1/notifications";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FindStockNotificationByIdUseCase findStockNotificationByIdUseCase;
    @MockitoBean
    private FindServiceOrderNotificationByIdUseCase findServiceOrderNotificationByIdUseCase;
    @MockitoBean
    private FindAllStockNotificationsUseCase findAllStockNotificationsUseCase;
    @MockitoBean
    private FindUnreadStockNotificationsUseCase findUnreadStockNotificationsUseCase;
    @MockitoBean
    private FindAllServiceOrderNotificationsUseCase findAllServiceOrderNotificationsUseCase;
    @MockitoBean
    private FindUnreadServiceOrderNotificationsUseCase findUnreadServiceOrderNotificationsUseCase;
    @MockitoBean
    private MarkStockNotificationAsReadUseCase markStockNotificationAsReadUseCase;
    @MockitoBean
    private MarkServiceOrderNotificationAsReadUseCase markServiceOrderNotificationAsReadUseCase;

    @Nested
    class FindStockById {

        @Test
        @WithMockUser(roles = "STOCKMAN")
        void shouldReturn200WhenFoundAndStockman() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            when(findStockNotificationByIdUseCase.execute(id)).thenReturn(notification);

            mockMvc.perform(get(BASE_URL + "/stock/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.message").value("Estoque baixo"));
        }

        @Test
        @WithMockUser(roles = "STOCKMAN")
        void shouldReturn404WhenNotFoundOrWrongType() throws Exception {
            UUID id = UUID.randomUUID();
            when(findStockNotificationByIdUseCase.execute(id)).thenThrow(new NotificationNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/stock/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Notificação não encontrada"));
        }
    }

    @Nested
    class FindServiceOrderById {

        @Test
        void shouldReturn200WhenFound() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "QUOTE_GENERATED", "Orçamento gerado");
            when(findServiceOrderNotificationByIdUseCase.execute(id)).thenReturn(notification);

            mockMvc.perform(get(BASE_URL + "/service-orders/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.message").value("Orçamento gerado"));
        }

        @Test
        void shouldReturn404WhenNotFoundOrWrongType() throws Exception {
            UUID id = UUID.randomUUID();
            when(findServiceOrderNotificationByIdUseCase.execute(id)).thenThrow(new NotificationNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/service-orders/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Notificação não encontrada"));
        }
    }

    @Nested
    class FindAllStock {

        @Test
        @WithMockUser(roles = "STOCKMAN")
        void shouldReturn200WithListWhenStockman() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            when(findAllStockNotificationsUseCase.execute()).thenReturn(List.of(notification));

            mockMvc.perform(get(BASE_URL + "/stock"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()));
        }
    }

    @Nested
    class FindUnreadStock {

        @Test
        @WithMockUser(roles = "STOCKMAN")
        void shouldReturn200WithUnreadListWhenStockman() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            when(findUnreadStockNotificationsUseCase.execute()).thenReturn(List.of(notification));

            mockMvc.perform(get(BASE_URL + "/stock/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].read").value(false));
        }
    }

    @Nested
    class FindAllServiceOrders {

        @Test
        void shouldReturn200WithList() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "QUOTE_GENERATED", "Orçamento gerado");
            when(findAllServiceOrderNotificationsUseCase.execute()).thenReturn(List.of(notification));

            mockMvc.perform(get(BASE_URL + "/service-orders"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()));
        }
    }

    @Nested
    class FindUnreadServiceOrders {

        @Test
        void shouldReturn200WithUnreadList() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "QUOTE_GENERATED", "Orçamento gerado");
            when(findUnreadServiceOrderNotificationsUseCase.execute()).thenReturn(List.of(notification));

            mockMvc.perform(get(BASE_URL + "/service-orders/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].read").value(false));
        }
    }

    @Nested
    class MarkStockAsRead {

        @Test
        @WithMockUser(roles = "STOCKMAN")
        void shouldReturn200WhenMarkedAsReadAndStockman() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            notification.markAsRead();
            when(markStockNotificationAsReadUseCase.execute(any())).thenReturn(notification);

            mockMvc.perform(patch(BASE_URL + "/stock/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.read").value(true));
        }
    }

    @Nested
    class MarkServiceOrderAsRead {

        @Test
        void shouldReturn200WhenMarkedAsRead() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "QUOTE_GENERATED", "Orçamento gerado");
            notification.markAsRead();
            when(markServiceOrderNotificationAsReadUseCase.execute(any())).thenReturn(notification);

            mockMvc.perform(patch(BASE_URL + "/service-orders/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.read").value(true));
        }
    }

    private Notification notificationDomain(UUID id, String type, String message) {
        return Notification.builder()
                .id(id)
                .type(NotificationType.valueOf(type))
                .message(NotificationMessage.fromString(message))
                .read(false)
                .createdAt(NOW)
                .updatedAt(NOW)
                .build();
    }
}
package br.com.ofisy.infrastructure.notification.web;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.ports.in.NotificationResponse;
import br.com.ofisy.application.notification.usecases.FindAllNotificationsUseCase;
import br.com.ofisy.application.notification.usecases.FindNotificationByIdUseCase;
import br.com.ofisy.application.notification.usecases.FindUnreadNotificationsUseCase;
import br.com.ofisy.application.notification.usecases.MarkNotificationAsReadUseCase;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
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
    private FindNotificationByIdUseCase findByIdUseCase;

    @MockitoBean
    private FindAllNotificationsUseCase findAllUseCase;

    @MockitoBean
    private FindUnreadNotificationsUseCase findUnreadUseCase;

    @MockitoBean
    private MarkNotificationAsReadUseCase markAsReadUseCase;

    @Nested
    class GetNotificationById {

        @Test
        void shouldReturn200WithNotificationWhenFound() throws Exception {
            var id = UUID.randomUUID();
            var response = notificationResponse(id, "LOW_STOCK", null, null, "Estoque baixo para Radiador", false);
            when(findByIdUseCase.execute(id)).thenReturn(response);

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.type").value("LOW_STOCK"))
                    .andExpect(jsonPath("$.message").value("Estoque baixo para Radiador"))
                    .andExpect(jsonPath("$.read").value(false));

            verify(findByIdUseCase).execute(id);
        }

        @Test
        void shouldReturn404WhenNotificationNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(findByIdUseCase.execute(id)).thenThrow(new NotificationNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Notificação não encontrada"));
        }

        @Test
        void shouldReturn400WhenIdIsNotAValidUUID() throws Exception {
            mockMvc.perform(get(BASE_URL + "/{id}", "not-a-uuid"))
                    .andExpect(status().isBadRequest());
        }
    }

    @Nested
    class GetAllNotifications {

        @Test
        void shouldReturn200WithListOfNotifications() throws Exception {
            var r1 = notificationResponse(UUID.randomUUID(), "LOW_STOCK", null, null, "Estoque baixo para Radiador", false);
            var r2 = notificationResponse(UUID.randomUUID(), "QUOTE_GENERATED", null, UUID.randomUUID(), "Orçamento #123 gerado", false);
            when(findAllUseCase.execute()).thenReturn(List.of(r1, r2));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].type").value("LOW_STOCK"))
                    .andExpect(jsonPath("$[1].type").value("QUOTE_GENERATED"))
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldReturn200WithEmptyListWhenNoNotifications() throws Exception {
            when(findAllUseCase.execute()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class GetUnreadNotifications {

        @Test
        void shouldReturn200WithUnreadNotifications() throws Exception {
            var r = notificationResponse(UUID.randomUUID(), "LOW_STOCK", UUID.randomUUID(), null, "Estoque baixo para Radiador", false);
            when(findUnreadUseCase.findUnread()).thenReturn(List.of(r));

            mockMvc.perform(get(BASE_URL + "/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].type").value("LOW_STOCK"))
                    .andExpect(jsonPath("$[0].read").value(false))
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void shouldReturn200WithEmptyListWhenNoUnreadNotifications() throws Exception {
            when(findUnreadUseCase.findUnread()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class MarkAsRead {

        @Test
        void shouldReturn200WhenMarkAsReadSuccessfully() throws Exception {
            var id = UUID.randomUUID();
            var response = notificationResponse(id, "LOW_STOCK", null, null, "Estoque baixo", true);
            when(markAsReadUseCase.execute(any())).thenReturn(response);

            mockMvc.perform(patch(BASE_URL + "/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.read").value(true));
        }

        @Test
        void shouldReturn404WhenNotificationNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(markAsReadUseCase.execute(any()))
                    .thenThrow(new NotificationNotFoundException(id));

            mockMvc.perform(patch(BASE_URL + "/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Notificação não encontrada"));
        }

        @Test
        void shouldReturn400WhenIdIsNotAValidUUID() throws Exception {
            mockMvc.perform(patch(BASE_URL + "/{id}/read", "not-a-uuid")
                            .with(csrf()))
                    .andExpect(status().isBadRequest());
        }
    }

    private NotificationResponse notificationResponse(UUID id, String type, UUID stockId, UUID quoteId, String message, boolean read) {
        return new NotificationResponse(id, type, stockId, quoteId, message, read, NOW, NOW);
    }
}

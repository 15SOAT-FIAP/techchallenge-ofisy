package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.interfaces.api.ControllerTestBase;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.UUID;

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
    private NotificationService notificationService;

    @Nested
    class GetAllNotifications {

        @Test
        void shouldReturn200WithListOfNotifications() throws Exception {
            var dto1 = responseDTO("LOW_STOCK", "Estoque baixo para Radiador");
            var dto2 = responseDTO("QUOTE_GENERATED", "Orçamento #123 gerado");
            when(notificationService.findAll()).thenReturn(List.of(dto1, dto2));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].type").value("LOW_STOCK"))
                    .andExpect(jsonPath("$[1].type").value("QUOTE_GENERATED"))
                    .andExpect(jsonPath("$.length()").value(2));
        }

        @Test
        void shouldReturn200WithEmptyListWhenNoNotifications() throws Exception {
            when(notificationService.findAll()).thenReturn(Collections.emptyList());

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
            var dto = responseDTO("LOW_STOCK", "Estoque baixo para Radiador");
            when(notificationService.findUnread()).thenReturn(List.of(dto));

            mockMvc.perform(get(BASE_URL + "/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].type").value("LOW_STOCK"))
                    .andExpect(jsonPath("$[0].read").value(false))
                    .andExpect(jsonPath("$.length()").value(1));
        }

        @Test
        void shouldReturn200WithEmptyListWhenNoUnreadNotifications() throws Exception {
            when(notificationService.findUnread()).thenReturn(Collections.emptyList());

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
            var dto = new NotificationResponseDTO(id, "LOW_STOCK", null, "Estoque baixo", true, NOW, NOW);
            when(notificationService.markAsRead(id)).thenReturn(dto);

            mockMvc.perform(patch(BASE_URL + "/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.read").value(true));

            verify(notificationService).markAsRead(id);
        }

        @Test
        void shouldReturn404WhenNotificationNotFound() throws Exception {
            var id = UUID.randomUUID();
            when(notificationService.markAsRead(id))
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

    private NotificationResponseDTO responseDTO(String type, String message) {
        return new NotificationResponseDTO(UUID.randomUUID(), type, null, message, false, NOW, NOW);
    }
}

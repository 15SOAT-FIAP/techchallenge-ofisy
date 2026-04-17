package br.com.ofisy.interfaces.api.notification;

import br.com.ofisy.application.notification.NotificationService;
import br.com.ofisy.application.notification.dto.NotificationRequestDTO;
import br.com.ofisy.application.notification.dto.NotificationResponseDTO;
import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.stock.exceptions.StockNotFoundException;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
@WithMockUser
class NotificationControllerTest {

    private static final String BASE_URL = "/api/v1/notifications";
    private static final LocalDateTime NOW = LocalDateTime.now();

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    private NotificationResponseDTO responseDTO(UUID id, UUID stockId, String message, boolean read) {
        return new NotificationResponseDTO(id, stockId, message, read, NOW);
    }

    @Nested
    class ListAll {

        @Test
        void shouldReturn200WithListOfNotifications() throws Exception {
            var id = UUID.randomUUID();
            var stockId = UUID.randomUUID();
            var dto = responseDTO(id, stockId, "Alerta de estoque", false);
            when(notificationService.listAll()).thenReturn(List.of(dto));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].stockId").value(stockId.toString()))
                    .andExpect(jsonPath("$[0].message").value("Alerta de estoque"))
                    .andExpect(jsonPath("$[0].read").value(false))
                    .andExpect(jsonPath("$[0].createdAt").exists());
        }

        @Test
        void shouldReturn200WithEmptyListWhenNoNotifications() throws Exception {
            when(notificationService.listAll()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class ListUnread {

        @Test
        void shouldReturn200WithFilteredUnreadNotifications() throws Exception {
            var id = UUID.randomUUID();
            var stockId = UUID.randomUUID();
            var dto = responseDTO(id, stockId, "Não lida", false);
            when(notificationService.listUnread()).thenReturn(List.of(dto));

            mockMvc.perform(get(BASE_URL + "/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].read").value(false));
        }

        @Test
        void shouldReturn200WithEmptyListWhenAllRead() throws Exception {
            when(notificationService.listUnread()).thenReturn(Collections.emptyList());

            mockMvc.perform(get(BASE_URL + "/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$").isEmpty());
        }
    }

    @Nested
    class SendNotification {

        private String validBody(UUID stockId) {
            return """
                    {
                        "stockId": "%s",
                        "message": "Alerta de estoque baixo"
                    }
                    """.formatted(stockId);
        }

        @Test
        void shouldReturn201WithCreatedNotification() throws Exception {
            var stockId = UUID.randomUUID();
            var id = UUID.randomUUID();
            var dto = responseDTO(id, stockId, "Alerta de estoque baixo", false);
            when(notificationService.send(any(NotificationRequestDTO.class))).thenReturn(dto);

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody(stockId)))
                    .andExpect(status().isCreated())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.stockId").value(stockId.toString()))
                    .andExpect(jsonPath("$.message").value("Alerta de estoque baixo"))
                    .andExpect(jsonPath("$.read").value(false))
                    .andExpect(jsonPath("$.createdAt").exists());
        }

        @Test
        void shouldReturn400WhenMessageIsBlank() throws Exception {
            var body = """
                    {
                        "stockId": "%s",
                        "message": ""
                    }
                    """.formatted(UUID.randomUUID());

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.message").exists());
        }

        @Test
        void shouldReturn400WhenStockIdIsInvalidUUID() throws Exception {
            var body = """
                    {
                        "stockId": "not-a-uuid",
                        "message": "Alerta"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest());
        }

        @Test
        void shouldReturn400WhenStockIdIsNull() throws Exception {
            var body = """
                    {
                        "message": "Alerta"
                    }
                    """;

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(body))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.title").value("Erro de validação"))
                    .andExpect(jsonPath("$.errors.stockId").exists());
        }

        @Test
        void shouldReturn404WhenStockDoesNotExist() throws Exception {
            var stockId = UUID.randomUUID();
            when(notificationService.send(any(NotificationRequestDTO.class)))
                    .thenThrow(new StockNotFoundException(stockId));

            mockMvc.perform(post(BASE_URL)
                            .with(csrf())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(validBody(stockId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Estoque não encontrado"));
        }
    }

    @Nested
    class MarkAsRead {

        @Test
        void shouldReturn200WithUpdatedNotification() throws Exception {
            var id = UUID.randomUUID();
            var stockId = UUID.randomUUID();
            var dto = responseDTO(id, stockId, "Alerta", true);
            when(notificationService.markAsRead(id)).thenReturn(dto);

            mockMvc.perform(patch(BASE_URL + "/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.read").value(true));
        }

        @Test
        void shouldReturn404WhenNotificationDoesNotExist() throws Exception {
            var id = UUID.randomUUID();
            when(notificationService.markAsRead(id))
                    .thenThrow(new NotificationNotFoundException(id));

            mockMvc.perform(patch(BASE_URL + "/{id}/read", id)
                            .with(csrf()))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Notificação não encontrada"));
        }
    }
}

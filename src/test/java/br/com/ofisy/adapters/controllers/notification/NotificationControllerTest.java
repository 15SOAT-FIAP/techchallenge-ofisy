package br.com.ofisy.adapters.controllers.notification;

import br.com.ofisy.application.notification.exceptions.NotificationNotFoundException;
import br.com.ofisy.application.notification.findall.FindAllNotificationsUseCase;
import br.com.ofisy.application.notification.findbyid.FindNotificationByIdUseCase;
import br.com.ofisy.application.notification.findunread.FindUnreadNotificationsUseCase;
import br.com.ofisy.application.notification.markasread.MarkNotificationAsReadUseCase;
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
    private FindNotificationByIdUseCase findNotificationByIdUseCase;
    @MockitoBean
    private FindAllNotificationsUseCase findAllNotificationsUseCase;
    @MockitoBean
    private FindUnreadNotificationsUseCase findUnreadNotificationsUseCase;
    @MockitoBean
    private MarkNotificationAsReadUseCase markNotificationAsReadUseCase;

    @Nested
    class FindById {

        @Test
        void shouldReturn200WithNotificationWhenFound() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            when(findNotificationByIdUseCase.execute(id)).thenReturn(notification);

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.id").value(id.toString()))
                    .andExpect(jsonPath("$.message").value("Estoque baixo"));
        }

        @Test
        void shouldReturn404WhenNotFound() throws Exception {
            UUID id = UUID.randomUUID();
            when(findNotificationByIdUseCase.execute(id)).thenThrow(new NotificationNotFoundException(id));

            mockMvc.perform(get(BASE_URL + "/{id}", id))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.title").value("Notificação não encontrada"));
        }
    }

    @Nested
    class FindAll {

        @Test
        void shouldReturn200WithList() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            when(findAllNotificationsUseCase.execute()).thenReturn(List.of(notification));

            mockMvc.perform(get(BASE_URL))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()));
        }
    }

    @Nested
    class FindUnread {

        @Test
        void shouldReturn200WithUnreadList() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            when(findUnreadNotificationsUseCase.execute()).thenReturn(List.of(notification));

            mockMvc.perform(get(BASE_URL + "/unread"))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$").isArray())
                    .andExpect(jsonPath("$[0].id").value(id.toString()))
                    .andExpect(jsonPath("$[0].read").value(false));
        }
    }

    @Nested
    class MarkAsRead {

        @Test
        void shouldReturn200WhenMarkedAsRead() throws Exception {
            UUID id = UUID.randomUUID();
            Notification notification = notificationDomain(id, "LOW_STOCK", "Estoque baixo");
            notification.markAsRead();
            when(markNotificationAsReadUseCase.execute(any())).thenReturn(notification);

            mockMvc.perform(patch(BASE_URL + "/{id}/read", id)
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

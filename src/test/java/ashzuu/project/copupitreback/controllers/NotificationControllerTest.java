package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.notification.NotificationDTO;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.NotificationService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(NotificationController.class)
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private NotificationService notificationService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("testuser").role(Role.USER).build();
        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void testGetMyNotifications() throws Exception {
        NotificationDTO dto = NotificationDTO.builder()
                .id(10L)
                .type("NEW_APPLICATION")
                .title("Nouvelle candidature")
                .content("Jean a postulé")
                .link("/dashboard")
                .isRead(false)
                .createdAt(Instant.now())
                .build();

        when(notificationService.getUserNotifications(eq(userDetails))).thenReturn(List.of(dto));

        mockMvc.perform(get("/notifications/me")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(10))
                .andExpect(jsonPath("$[0].title").value("Nouvelle candidature"))
                .andExpect(jsonPath("$[0].isRead").value(false));
    }

    @Test
    void testGetUnreadCount() throws Exception {
        when(notificationService.getUnreadCount(eq(userDetails))).thenReturn(4L);

        mockMvc.perform(get("/notifications/unread-count")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.unreadCount").value(4));
    }

    @Test
    void testMarkAsRead() throws Exception {
        mockMvc.perform(patch("/notifications/10/read")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAsRead(eq(10L), eq(userDetails));
    }

    @Test
    void testMarkAllAsRead() throws Exception {
        mockMvc.perform(post("/notifications/mark-all-read")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isNoContent());

        verify(notificationService).markAllAsRead(eq(userDetails));
    }
}

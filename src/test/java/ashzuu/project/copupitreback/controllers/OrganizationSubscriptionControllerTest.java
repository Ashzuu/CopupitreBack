package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.organization.SubscriptionStatusDTO;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.SubscriptionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(OrganizationSubscriptionController.class)
class OrganizationSubscriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private SubscriptionService subscriptionService;

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
    void testSubscribe() throws Exception {
        SubscriptionStatusDTO dto = SubscriptionStatusDTO.builder()
                .organizationId(5L)
                .organizationName("Harmonie de Lyon")
                .isSubscribed(true)
                .build();

        when(subscriptionService.subscribe(eq(5L), eq(userDetails))).thenReturn(dto);

        mockMvc.perform(post("/organizations/5/subscribe")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.organizationId").value(5))
                .andExpect(jsonPath("$.subscribed").value(true));
    }

    @Test
    void testUnsubscribe() throws Exception {
        SubscriptionStatusDTO dto = SubscriptionStatusDTO.builder()
                .organizationId(5L)
                .organizationName("Harmonie de Lyon")
                .isSubscribed(false)
                .build();

        when(subscriptionService.unsubscribe(eq(5L), eq(userDetails))).thenReturn(dto);

        mockMvc.perform(delete("/organizations/5/subscribe")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(false));
    }

    @Test
    void testIsSubscribed() throws Exception {
        SubscriptionStatusDTO dto = SubscriptionStatusDTO.builder()
                .organizationId(5L)
                .organizationName("Harmonie de Lyon")
                .isSubscribed(true)
                .build();

        when(subscriptionService.isSubscribed(eq(5L), eq(userDetails))).thenReturn(dto);

        mockMvc.perform(get("/organizations/5/is-subscribed")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subscribed").value(true));
    }
}

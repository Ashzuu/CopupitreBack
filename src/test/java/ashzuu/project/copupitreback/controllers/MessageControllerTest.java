package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.message.MessageDTO;
import ashzuu.project.copupitreback.dto.message.SendMessageRequest;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.MessagingService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(MessageController.class)
class MessageControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private MessagingService messagingService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Autowired
    private ObjectMapper objectMapper;

    private UserDetailsImpl userDetails;

    @BeforeEach
    void setUp() {
        User user = User.builder().id(1L).username("testuser").role(Role.USER).build();
        userDetails = new UserDetailsImpl(user);
    }

    @Test
    void testGetConversations() throws Exception {
        ashzuu.project.copupitreback.dto.message.ConversationDTO conv = ashzuu.project.copupitreback.dto.message.ConversationDTO.builder()
                .id("APPLICATION_15")
                .contextType("APPLICATION")
                .contextId(15L)
                .title("Candidature Trompette")
                .otherPartyName("Orchestre Philharmonique")
                .otherPartyRole("Organisation")
                .lastMessage("Bonjour !")
                .status("PENDING")
                .build();

        when(messagingService.getUserConversations(eq(userDetails))).thenReturn(List.of(conv));

        mockMvc.perform(get("/messages/conversations")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value("APPLICATION_15"))
                .andExpect(jsonPath("$[0].title").value("Candidature Trompette"))
                .andExpect(jsonPath("$[0].otherPartyName").value("Orchestre Philharmonique"));
    }

    @Test
    void testGetMessages() throws Exception {
        MessageDTO dto = MessageDTO.builder()
                .id(1L)
                .contextType("APPLICATION")
                .contextId(15L)
                .senderUsername("testuser")
                .senderFullName("Test User")
                .senderRole("MUSICIAN")
                .isMe(true)
                .content("Bonjour !")
                .createdAt(Instant.now())
                .build();

        when(messagingService.getMessages(eq("APPLICATION"), eq(15L), eq(userDetails))).thenReturn(List.of(dto));

        mockMvc.perform(get("/messages/APPLICATION/15")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(1))
                .andExpect(jsonPath("$[0].content").value("Bonjour !"))
                .andExpect(jsonPath("$[0].isMe").value(true));
    }

    @Test
    void testSendMessage() throws Exception {
        SendMessageRequest request = SendMessageRequest.builder()
                .content("Est-ce que les répétitions ont lieu le mardi ?")
                .build();

        MessageDTO dto = MessageDTO.builder()
                .id(2L)
                .contextType("APPLICATION")
                .contextId(15L)
                .senderUsername("testuser")
                .senderFullName("Test User")
                .senderRole("MUSICIAN")
                .isMe(true)
                .content("Est-ce que les répétitions ont lieu le mardi ?")
                .createdAt(Instant.now())
                .build();

        when(messagingService.sendMessage(eq("APPLICATION"), eq(15L), eq("Est-ce que les répétitions ont lieu le mardi ?"), eq(userDetails)))
                .thenReturn(dto);

        mockMvc.perform(post("/messages/APPLICATION/15")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(2))
                .andExpect(jsonPath("$.content").value("Est-ce que les répétitions ont lieu le mardi ?"));
    }

    @Test
    void testSendMessageInvalidContent() throws Exception {
        SendMessageRequest request = SendMessageRequest.builder().content("").build();

        mockMvc.perform(post("/messages/APPLICATION/15")
                        .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                        .with(SecurityMockMvcRequestPostProcessors.csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

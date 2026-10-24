package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.user.UpdateProfileRequest;
import ashzuu.project.copupitreback.dto.user.UserDTO;
import ashzuu.project.copupitreback.jpa.entities.user.Role;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.security.CustomUserDetailsService;
import ashzuu.project.copupitreback.security.JwtUtil;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.UserService;
import tools.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors;
import org.springframework.test.web.servlet.MockMvc;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(UserController.class)
class UserControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private UserService userService;

    @MockitoBean
    private JwtUtil jwtUtil;

    @MockitoBean
    private CustomUserDetailsService customUserDetailsService;

    @Test
    void testGetUserInfoUnauthenticated() throws Exception {
        mockMvc.perform(get("/users/me"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testGetUserInfoAuthenticated() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        UserDTO userDto = UserDTO.builder().id(1L).username("testuser").build();
        when(userService.getByUsername("testuser")).thenReturn(userDto);

        mockMvc.perform(get("/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"));
    }

    @Test
    void testUpdateProfileUnauthenticated() throws Exception {
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Jean")
                .lastName("Valjean")
                .mainInstrument("Piano")
                .build();

        mockMvc.perform(put("/users/me")
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testUpdateProfileAuthenticated() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("Jean")
                .lastName("Valjean")
                .mainInstrument("Piano")
                .secondaryInstruments(List.of("Orgue"))
                .musicalDescription("Pianiste classique")
                .yearsOfPractice(15)
                .build();

        UserDTO updatedDto = UserDTO.builder()
                .id(1L)
                .username("testuser")
                .firstName("Jean")
                .lastName("Valjean")
                .mainInstrument("Piano")
                .secondaryInstruments(List.of("Orgue"))
                .musicalDescription("Pianiste classique")
                .yearsOfPractice(15)
                .build();

        when(userService.updateProfile(eq("testuser"), any(UpdateProfileRequest.class))).thenReturn(updatedDto);

        mockMvc.perform(put("/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.username").value("testuser"))
                .andExpect(jsonPath("$.firstName").value("Jean"))
                .andExpect(jsonPath("$.lastName").value("Valjean"))
                .andExpect(jsonPath("$.mainInstrument").value("Piano"))
                .andExpect(jsonPath("$.secondaryInstruments[0]").value("Orgue"))
                .andExpect(jsonPath("$.yearsOfPractice").value(15));
    }

    @Test
    void testUpdateProfileInvalidBody() throws Exception {
        User userEntity = User.builder().id(1L).username("testuser").role(Role.USER).build();
        UserDetailsImpl userDetails = new UserDetailsImpl(userEntity);

        // Missing required fields (blank firstName and lastName)
        UpdateProfileRequest request = UpdateProfileRequest.builder()
                .firstName("")
                .lastName("")
                .mainInstrument("")
                .build();

        mockMvc.perform(put("/users/me")
                .with(SecurityMockMvcRequestPostProcessors.user(userDetails))
                .with(SecurityMockMvcRequestPostProcessors.csrf())
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }
}

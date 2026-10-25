package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.organization.SubscriptionStatusDTO;
import ashzuu.project.copupitreback.exceptions.UnknownID;
import ashzuu.project.copupitreback.jpa.entities.organization.Organization;
import ashzuu.project.copupitreback.jpa.entities.organization.OrganizationSubscription;
import ashzuu.project.copupitreback.jpa.entities.user.User;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationRepository;
import ashzuu.project.copupitreback.jpa.repositories.OrganizationSubscriptionRepository;
import ashzuu.project.copupitreback.jpa.repositories.UserRepository;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private OrganizationSubscriptionRepository subscriptionRepo;

    @Mock
    private OrganizationRepository organizationRepo;

    @Mock
    private UserRepository userRepo;

    @InjectMocks
    private SubscriptionService service;

    private User user;
    private UserDetailsImpl userDetails;
    private Organization org;

    @BeforeEach
    void setUp() {
        user = User.builder().id(1L).username("musician1").firstName("Jean").lastName("Dupont").build();
        userDetails = new UserDetailsImpl(user);
        org = Organization.builder().id(10L).name("Orchestre Symphonique").build();
    }

    @Test
    void testSubscribeSuccess() {
        when(organizationRepo.findById(10L)).thenReturn(Optional.of(org));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepo.existsByUserIdAndOrganizationId(1L, 10L)).thenReturn(false);

        SubscriptionStatusDTO result = service.subscribe(10L, userDetails);

        assertNotNull(result);
        assertEquals(10L, result.getOrganizationId());
        assertEquals("Orchestre Symphonique", result.getOrganizationName());
        assertTrue(result.isSubscribed());
        verify(subscriptionRepo).save(any(OrganizationSubscription.class));
    }

    @Test
    void testSubscribeAlreadySubscribed() {
        when(organizationRepo.findById(10L)).thenReturn(Optional.of(org));
        when(userRepo.findById(1L)).thenReturn(Optional.of(user));
        when(subscriptionRepo.existsByUserIdAndOrganizationId(1L, 10L)).thenReturn(true);

        SubscriptionStatusDTO result = service.subscribe(10L, userDetails);

        assertTrue(result.isSubscribed());
        verify(subscriptionRepo, never()).save(any());
    }

    @Test
    void testSubscribeUnauthenticated() {
        assertThrows(AccessDeniedException.class, () -> service.subscribe(10L, null));
    }

    @Test
    void testSubscribeUnknownOrg() {
        when(organizationRepo.findById(99L)).thenReturn(Optional.empty());
        assertThrows(UnknownID.class, () -> service.subscribe(99L, userDetails));
    }

    @Test
    void testUnsubscribeSuccess() {
        OrganizationSubscription sub = OrganizationSubscription.builder().user(user).organization(org).build();
        when(organizationRepo.findById(10L)).thenReturn(Optional.of(org));
        when(subscriptionRepo.findByUserIdAndOrganizationId(1L, 10L)).thenReturn(Optional.of(sub));

        SubscriptionStatusDTO result = service.unsubscribe(10L, userDetails);

        assertNotNull(result);
        assertFalse(result.isSubscribed());
        verify(subscriptionRepo).delete(sub);
    }

    @Test
    void testIsSubscribedTrue() {
        when(subscriptionRepo.existsByUserIdAndOrganizationId(1L, 10L)).thenReturn(true);

        SubscriptionStatusDTO result = service.isSubscribed(10L, userDetails);

        assertTrue(result.isSubscribed());
    }

    @Test
    void testIsSubscribedFalse() {
        when(subscriptionRepo.existsByUserIdAndOrganizationId(1L, 10L)).thenReturn(false);

        SubscriptionStatusDTO result = service.isSubscribed(10L, userDetails);

        assertFalse(result.isSubscribed());
    }

    @Test
    void testIsSubscribedUnauthenticated() {
        SubscriptionStatusDTO result = service.isSubscribed(10L, null);
        assertFalse(result.isSubscribed());
    }

    @Test
    void testGetSubscribedUsers() {
        when(subscriptionRepo.findSubscribedUsersByOrganizationId(10L)).thenReturn(List.of(user));

        List<User> result = service.getSubscribedUsers(10L);

        assertEquals(1, result.size());
        assertEquals("musician1", result.get(0).getUsername());
    }
}

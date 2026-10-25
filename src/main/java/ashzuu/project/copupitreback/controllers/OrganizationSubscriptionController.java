package ashzuu.project.copupitreback.controllers;

import ashzuu.project.copupitreback.dto.organization.SubscriptionStatusDTO;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import ashzuu.project.copupitreback.services.SubscriptionService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * Contrôleur REST gérant les abonnements des musiciens aux organisations (US-COM-01).
 */
@RestController
@RequestMapping("/organizations")
public class OrganizationSubscriptionController {

    private final SubscriptionService subscriptionService;

    public OrganizationSubscriptionController(SubscriptionService subscriptionService) {
        this.subscriptionService = subscriptionService;
    }

    @PostMapping("/{id}/subscribe")
    public ResponseEntity<SubscriptionStatusDTO> subscribe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(subscriptionService.subscribe(id, userDetails));
    }

    @DeleteMapping("/{id}/subscribe")
    public ResponseEntity<SubscriptionStatusDTO> unsubscribe(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(subscriptionService.unsubscribe(id, userDetails));
    }

    @GetMapping("/{id}/is-subscribed")
    public ResponseEntity<SubscriptionStatusDTO> isSubscribed(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetailsImpl userDetails) {
        return ResponseEntity.ok(subscriptionService.isSubscribed(id, userDetails));
    }
}

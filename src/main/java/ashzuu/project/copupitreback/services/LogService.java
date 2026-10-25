package ashzuu.project.copupitreback.services;

import ashzuu.project.copupitreback.dto.log.ClientLogRequest;
import ashzuu.project.copupitreback.security.UserDetailsImpl;
import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestBody;

@Service
public class LogService {
    private static final Logger clientLogger = LoggerFactory.getLogger("ashzuu.project.copupitreback.client");

    /**
     * Reaction when a log is received by the client.
     * @param request The request
     * @param userDetails The user details
     */
    public void receiveLogClient(
            @Valid @RequestBody ClientLogRequest request,
            @AuthenticationPrincipal UserDetailsImpl userDetails){
        String username = (userDetails != null && userDetails.getUsername() != null)
                ? userDetails.getUsername()
                : "anonymous";

        String context = (request.getContext() != null && !request.getContext().isBlank())
                ? request.getContext().trim()
                : "ClientApp";

        // Assainissement préventif contre les injections de saut de ligne (CRLF log injection)
        String sanitizedMessage = (request.getMessage() != null)
                ? request.getMessage().replace('\r', ' ').replace('\n', ' ').trim()
                : "";

        String logPrefix = String.format("[Frontend:%s] [User:%s] [URL:%s] - ",
                context, username, request.getUrl() != null ? request.getUrl() : "n/a");

        String fullMessage = logPrefix + sanitizedMessage;
        if (request.getStack() != null && !request.getStack().isBlank()) {
            fullMessage += " | Stack: " + request.getStack().replace('\r', ' ').replace('\n', ' ').trim();
        }

        String level = (request.getLevel() != null) ? request.getLevel().toUpperCase().trim() : "INFO";
        switch (level) {
            case "ERROR" -> clientLogger.error(fullMessage);
            case "WARN" -> clientLogger.warn(fullMessage);
            default -> clientLogger.info(fullMessage);
        }
    }

}

package ashzuu.project.copupitreback.exceptions;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

/**
 * Gestionnaire global des exceptions pour harmoniser les codes de retours HTTP de l'API.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(UnknownID.class)
    public ResponseEntity<Map<String, String>> handleUnknownID(UnknownID ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Ressource introuvable";
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(Map.of(
                "error", msg,
                "message", msg
        ));
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<Map<String, String>> handleAccessDenied(AccessDeniedException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Accès refusé";
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(Map.of(
                "error", msg,
                "message", msg
        ));
    }

    @ExceptionHandler({IllegalArgumentException.class, IllegalStateException.class})
    public ResponseEntity<Map<String, String>> handleBadRequest(RuntimeException ex) {
        String msg = ex.getMessage() != null ? ex.getMessage() : "Requête invalide";
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Map.of(
                "error", msg,
                "message", msg
        ));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, String>> handleValidationExceptions(MethodArgumentNotValidException ex) {
        Map<String, String> errors = new HashMap<>();
        StringBuilder combinedMessage = new StringBuilder();
        ex.getBindingResult().getFieldErrors().forEach(error -> {
            errors.put(error.getField(), error.getDefaultMessage());
            if (!combinedMessage.isEmpty()) {
                combinedMessage.append(", ");
            }
            combinedMessage.append(error.getField()).append(": ").append(error.getDefaultMessage());
        });
        String msg = combinedMessage.toString();
        errors.put("error", msg);
        errors.put("message", msg);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errors);
    }
}

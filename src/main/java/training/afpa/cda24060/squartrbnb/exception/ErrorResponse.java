package training.afpa.cda24060.squartrbnb.exception;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.*;
import org.springframework.http.HttpStatus;

import java.time.LocalDateTime;
import java.util.Map;

/**
 * Structure de réponse d'erreur retournée par l'API.
 *
 * Exemple de réponse JSON :
 * {
 *   "timestamp":        "2026-02-26T14:30:00",
 *   "httpStatus":       "BAD_REQUEST",
 *   "httpStatusCode":   400,
 *   "errorCode":        "ERR_VALIDATION",
 *   "error":            "Erreur de validation",
 *   "message":          "Les données fournies sont invalides",
 *   "path":             "/api/users",
 *   "validationErrors": {
 *       "password": "Le mot de passe doit contenir au moins 8 caractères",
 *       "email":    "L'email doit être valide"
 *   }
 * }
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorResponse {

    /** Horodatage de l'erreur */
    private LocalDateTime timestamp;

    /** Libellé HTTP (ex : "BAD_REQUEST", "NOT_FOUND") */
    private String httpStatus;

    /** Code numérique HTTP (ex : 400, 404, 409, 500) */
    private int httpStatusCode;

    /** Code d'erreur métier (ex : "ERR_VALIDATION", "ERR_EMAIL_EXISTS") */
    private String errorCode;

    /** Titre court de l'erreur */
    private String error;

    /** Message descriptif de l'erreur */
    private String message;

    /** Chemin de la requête qui a provoqué l'erreur */
    private String path;

    /**
     * Détails de validation champ par champ.
     * Présent uniquement pour les erreurs 400 de validation de formulaire.
     * Exemple : { "email": "L'email doit être valide", "password": "Min 8 caractères" }
     */
    private Map<String, String> validationErrors;

    // ── Factory methods ───────────────────────────────────────────────────

    /**
     * Crée un ErrorResponse complet (cas général).
     */
    public static ErrorResponse of(HttpStatus status, ErrorCode errorCode,
                                   String message, String path) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status.name())
                .httpStatusCode(status.value())
                .errorCode(errorCode.getCode())
                .error(errorCode.getDefaultMessage())
                .message(message)
                .path(path)
                .build();
    }

    /**
     * Crée un ErrorResponse avec les erreurs de validation champ par champ.
     * Utilisé pour les erreurs de formulaire (400).
     */
    public static ErrorResponse ofValidation(HttpStatus status, ErrorCode errorCode,
                                             String message, String path,
                                             Map<String, String> validationErrors) {
        return ErrorResponse.builder()
                .timestamp(LocalDateTime.now())
                .httpStatus(status.name())
                .httpStatusCode(status.value())
                .errorCode(errorCode.getCode())
                .error(errorCode.getDefaultMessage())
                .message(message)
                .path(path)
                .validationErrors(validationErrors)
                .build();
    }
}

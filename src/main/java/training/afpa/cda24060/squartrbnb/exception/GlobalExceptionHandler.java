package training.afpa.cda24060.squartrbnb.exception;

import lombok.extern.log4j.Log4j2;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.HttpRequestMethodNotSupportedException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.context.request.WebRequest;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.servlet.NoHandlerFoundException;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Gestionnaire centralisé de toutes les exceptions de l'API.
 *
 * Chaque handler retourne un {@link ErrorResponse} structuré avec :
 *  - httpStatus     : libellé HTTP (BAD_REQUEST, NOT_FOUND, …)
 *  - httpStatusCode : code numérique (400, 404, …)
 *  - errorCode      : code métier (ERR_VALIDATION, ERR_EMAIL_EXISTS, …)
 *  - message        : description lisible
 *  - validationErrors : détails champ par champ (formulaires uniquement)
 */
@RestControllerAdvice
@Log4j2
public class GlobalExceptionHandler {

    // ════════════════════════════════════════════════════════════════════════
    // 400 – BAD REQUEST
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Erreurs de validation de formulaire (@Valid / @Validated).
     * Retourne le détail de chaque champ invalide dans "validationErrors".
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException ex,
                                                          WebRequest request) {
        // Collecte des erreurs champ par champ (ordre conservé)
        Map<String, String> fieldErrors = new LinkedHashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String field   = ((FieldError) error).getField();
            String message = error.getDefaultMessage();
            fieldErrors.put(field, message);
        });

        log.warn("Erreur de validation: {}", fieldErrors);

        ErrorResponse body = ErrorResponse.ofValidation(
                HttpStatus.BAD_REQUEST,
                ErrorCode.VALIDATION_ERROR,
                "Les données du formulaire sont invalides. Veuillez corriger les champs indiqués.",
                extractPath(request),
                fieldErrors
        );
        return ResponseEntity.badRequest().body(body);
    }

    /**
     * Corps de requête illisible (JSON malformé, type incompatible, etc.).
     */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ErrorResponse> handleNotReadable(HttpMessageNotReadableException ex,
                                                           WebRequest request) {
        log.warn("Corps de requête illisible: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT,
                "Le corps de la requête est invalide ou mal formaté.", request);
    }

    /**
     * Paramètre de requête manquant (@RequestParam obligatoire absent).
     */
    @ExceptionHandler(MissingServletRequestParameterException.class)
    public ResponseEntity<ErrorResponse> handleMissingParam(MissingServletRequestParameterException ex,
                                                            WebRequest request) {
        String message = String.format("Le paramètre '%s' est obligatoire.", ex.getParameterName());
        log.warn("Paramètre manquant: {}", ex.getParameterName());
        return build(HttpStatus.BAD_REQUEST, ErrorCode.MISSING_REQUIRED_FIELD, message, request);
    }

    /**
     * Type de paramètre incorrect (ex : texte reçu à la place d'un entier dans l'URL).
     */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<ErrorResponse> handleTypeMismatch(MethodArgumentTypeMismatchException ex,
                                                            WebRequest request) {
        String expected = ex.getRequiredType() != null ? ex.getRequiredType().getSimpleName() : "inconnu";
        String message  = String.format(
                "Le paramètre '%s' doit être de type %s. Valeur reçue : '%s'.",
                ex.getName(), expected, ex.getValue());
        log.warn("Type de paramètre incorrect: {}", message);
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, message, request);
    }

    /**
     * Argument invalide levé manuellement depuis le service.
     */
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<ErrorResponse> handleIllegalArgument(IllegalArgumentException ex,
                                                               WebRequest request) {
        log.warn("Argument invalide: {}", ex.getMessage());
        return build(HttpStatus.BAD_REQUEST, ErrorCode.INVALID_ARGUMENT, ex.getMessage(), request);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 404 – NOT FOUND
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Ressource introuvable (utilisateur, rôle, etc.).
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleNotFound(ResourceNotFoundException ex,
                                                        WebRequest request) {
        log.warn("Ressource non trouvée: {}", ex.getMessage());

        // Détermination du code métier selon le message (optionnel, peut être affiné)
        ErrorCode code = ex.getMessage().contains("Utilisateur")
                ? ErrorCode.USER_NOT_FOUND
                : ex.getMessage().contains("Rôle")
                ? ErrorCode.ROLE_NOT_FOUND
                : ErrorCode.RESOURCE_NOT_FOUND;

        return build(HttpStatus.NOT_FOUND, code, ex.getMessage(), request);
    }

    /**
     * Route inexistante (endpoint non défini).
     */
    @ExceptionHandler(NoHandlerFoundException.class)
    public ResponseEntity<ErrorResponse> handleNoHandler(NoHandlerFoundException ex,
                                                         WebRequest request) {
        String message = String.format("L'endpoint '%s %s' n'existe pas.",
                ex.getHttpMethod(), ex.getRequestURL());
        log.warn(message);
        return build(HttpStatus.NOT_FOUND, ErrorCode.RESOURCE_NOT_FOUND, message, request);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 405 – METHOD NOT ALLOWED
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Méthode HTTP non supportée sur cet endpoint (ex : DELETE sur un endpoint GET-only).
     */
    @ExceptionHandler(HttpRequestMethodNotSupportedException.class)
    public ResponseEntity<ErrorResponse> handleMethodNotSupported(HttpRequestMethodNotSupportedException ex,
                                                                  WebRequest request) {
        String message = String.format("La méthode HTTP '%s' n'est pas supportée sur cette route.",
                ex.getMethod());
        log.warn(message);
        return build(HttpStatus.METHOD_NOT_ALLOWED, ErrorCode.INVALID_ARGUMENT, message, request);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 409 – CONFLICT
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Conflit métier levé manuellement (email/username déjà utilisé, etc.).
     */
    @ExceptionHandler(DataConflictException.class)
    public ResponseEntity<ErrorResponse> handleConflict(DataConflictException ex,
                                                        WebRequest request) {
        log.warn("Conflit de données: {}", ex.getMessage());

        ErrorCode code = ex.getMessage().contains("Email") || ex.getMessage().contains("email")
                ? ErrorCode.EMAIL_ALREADY_EXISTS
                : ex.getMessage().contains("Username") || ex.getMessage().contains("username")
                ? ErrorCode.USERNAME_ALREADY_EXISTS
                : ErrorCode.DATA_CONFLICT;

        return build(HttpStatus.CONFLICT, code, ex.getMessage(), request);
    }

    /**
     * Violation de contrainte d'intégrité en base (unicité, clé étrangère, etc.).
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ErrorResponse> handleDataIntegrity(DataIntegrityViolationException ex,
                                                             WebRequest request) {
        log.error("Violation d'intégrité de données: {}", ex.getMessage());
        return build(HttpStatus.CONFLICT, ErrorCode.DATA_CONFLICT,
                "Une contrainte d'intégrité a été violée (doublon ou référence invalide).", request);
    }

    // ════════════════════════════════════════════════════════════════════════
    // 500 – INTERNAL SERVER ERROR
    // ════════════════════════════════════════════════════════════════════════

    /**
     * Filet de sécurité : toute exception non gérée explicitement.
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGlobal(Exception ex, WebRequest request) {
        log.error("Erreur inattendue [{}]: {}", ex.getClass().getSimpleName(), ex.getMessage(), ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, ErrorCode.INTERNAL_ERROR,
                "Une erreur inattendue s'est produite. Veuillez réessayer ou contacter le support.", request);
    }

    // ════════════════════════════════════════════════════════════════════════
    // Méthode utilitaire
    // ════════════════════════════════════════════════════════════════════════

    private ResponseEntity<ErrorResponse> build(HttpStatus status, ErrorCode errorCode,
                                                String message, WebRequest request) {
        return ResponseEntity.status(status)
                .body(ErrorResponse.of(status, errorCode, message, extractPath(request)));
    }

    private String extractPath(WebRequest request) {
        return request.getDescription(false).replace("uri=", "");
    }
}

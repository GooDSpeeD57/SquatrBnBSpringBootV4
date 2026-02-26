package training.afpa.cda24060.squartrbnb.exception;

/**
 * Codes d'erreur métier pour faciliter la gestion côté frontend.
 * Chaque code correspond à un cas d'erreur précis.
 */
public enum ErrorCode {

    // ── Validation (400) ──────────────────────────────────────────────────
    VALIDATION_ERROR("ERR_VALIDATION", "Données invalides"),
    INVALID_PASSWORD_FORMAT("ERR_PASSWORD_FORMAT", "Format du mot de passe invalide"),
    INVALID_EMAIL_FORMAT("ERR_EMAIL_FORMAT", "Format de l'email invalide"),
    INVALID_DATE("ERR_DATE", "Date invalide"),
    MISSING_REQUIRED_FIELD("ERR_MISSING_FIELD", "Champ obligatoire manquant"),
    INVALID_ARGUMENT("ERR_INVALID_ARGUMENT", "Argument invalide"),

    // ── Conflit (409) ─────────────────────────────────────────────────────
    EMAIL_ALREADY_EXISTS("ERR_EMAIL_EXISTS", "Email déjà utilisé"),
    USERNAME_ALREADY_EXISTS("ERR_USERNAME_EXISTS", "Nom d'utilisateur déjà utilisé"),
    DATA_CONFLICT("ERR_CONFLICT", "Conflit de données"),

    // ── Non trouvé (404) ──────────────────────────────────────────────────
    USER_NOT_FOUND("ERR_USER_NOT_FOUND", "Utilisateur non trouvé"),
    ROLE_NOT_FOUND("ERR_ROLE_NOT_FOUND", "Rôle non trouvé"),
    RESOURCE_NOT_FOUND("ERR_NOT_FOUND", "Ressource non trouvée"),

    // ── Serveur (500) ─────────────────────────────────────────────────────
    INTERNAL_ERROR("ERR_INTERNAL", "Erreur interne du serveur"),
    DATABASE_ERROR("ERR_DATABASE", "Erreur de base de données");

    private final String code;
    private final String defaultMessage;

    ErrorCode(String code, String defaultMessage) {
        this.code  = code;
        this.defaultMessage = defaultMessage;
    }

    public String getCode()           { return code; }
    public String getDefaultMessage() { return defaultMessage; }
}

package training.afpa.cda24060.squartrbnb.exception;

/**
 * Exception levée en cas de conflit de données (ex: email ou username déjà utilisé)
 */
public class DataConflictException extends RuntimeException {
    
    public DataConflictException(String message) {
        super(message);
    }
}

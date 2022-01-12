package psneo.exceptions;

/**
 * PS exceptions related to objects initializations
 * 
 * @author schastel
 *
 */
public class NeoInitializationException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoInitializationException() {
		super();
	}

	public NeoInitializationException(String message) {
		super(message);
	}

	public NeoInitializationException(Throwable cause) {
		super(cause);
	}

	public NeoInitializationException(String message, Throwable cause) {
		super(message, cause);
	}

}

package psneo.exceptions;

/**
 * PS exceptions related to arguments of methods
 * 
 * @author schastel
 *
 */
public class NeoTimeoutException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoTimeoutException() {
		super();
	}

	public NeoTimeoutException(String message) {
		super(message);
	}

	public NeoTimeoutException(Throwable cause) {
		super(cause);
	}

	public NeoTimeoutException(String message, Throwable cause) {
		super(message, cause);
	}

}

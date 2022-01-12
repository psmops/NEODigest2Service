package psneo.exceptions;

/**
 * PS exceptions related to objects initializations
 * 
 * @author schastel
 *
 */
public class NeoAssertionException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoAssertionException() {
		super();
	}

	public NeoAssertionException(String message) {
		super(message);
	}

	public NeoAssertionException(Throwable cause) {
		super(cause);
	}

	public NeoAssertionException(String message, Throwable cause) {
		super(message, cause);
	}

}

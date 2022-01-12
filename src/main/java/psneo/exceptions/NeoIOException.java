package psneo.exceptions;

/**
 * PS exceptions related to objects initializations
 * 
 * @author schastel
 *
 */
public class NeoIOException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoIOException() {
		super();
	}

	public NeoIOException(String message) {
		super(message);
	}

	public NeoIOException(Throwable cause) {
		super(cause);
	}

	public NeoIOException(String message, Throwable cause) {
		super(message, cause);
	}

}

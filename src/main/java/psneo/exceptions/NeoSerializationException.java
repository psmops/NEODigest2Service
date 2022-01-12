package psneo.exceptions;

/**
 * PS exceptions related to arguments of methods
 * 
 * @author schastel
 *
 */
public class NeoSerializationException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoSerializationException() {
		super();
	}

	public NeoSerializationException(String message) {
		super(message);
	}

	public NeoSerializationException(Throwable cause) {
		super(cause);
	}

	public NeoSerializationException(String message, Throwable cause) {
		super(message, cause);
	}

}

package psneo.exceptions;

/**
 * PS exceptions related to objects initializations
 * 
 * @author schastel
 *
 */
public class NeoInstantiationException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoInstantiationException() {
		super();
	}

	public NeoInstantiationException(String message) {
		super(message);
	}

	public NeoInstantiationException(Throwable cause) {
		super(cause);
	}

	public NeoInstantiationException(String message, Throwable cause) {
		super(message, cause);
	}

}

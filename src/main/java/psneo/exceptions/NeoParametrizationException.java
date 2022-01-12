package psneo.exceptions;

/**
 * PS exceptions related to arguments of methods
 * 
 * @author schastel
 *
 */
public class NeoParametrizationException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoParametrizationException() {
		super();
	}

	public NeoParametrizationException(String message) {
		super(message);
	}

	public NeoParametrizationException(Throwable cause) {
		super(cause);
	}

	public NeoParametrizationException(String message, Throwable cause) {
		super(message, cause);
	}

}

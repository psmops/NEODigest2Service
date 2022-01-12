package psneo.exceptions;

/**
 * PS exceptions related to arguments of methods
 * 
 * @author schastel
 *
 */
public class NeoProcessingException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoProcessingException() {
		super();
	}

	public NeoProcessingException(String message) {
		super(message);
	}

	public NeoProcessingException(Throwable cause) {
		super(cause);
	}

	public NeoProcessingException(String message, Throwable cause) {
		super(message, cause);
	}

}

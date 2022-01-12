package psneo.exceptions;

/**
 * PS exceptions related to objects initializations
 * 
 * @author schastel
 *
 */
public class NeoExecutionException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoExecutionException() {
		super();
	}

	public NeoExecutionException(String message) {
		super(message);
	}

	public NeoExecutionException(Throwable cause) {
		super(cause);
	}

	public NeoExecutionException(String message, Throwable cause) {
		super(message, cause);
	}

}

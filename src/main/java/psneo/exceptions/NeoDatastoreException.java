package psneo.exceptions;

/**
 * PS exceptions related to objects initializations
 * 
 * @author schastel
 *
 */
public class NeoDatastoreException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	public NeoDatastoreException() {
		super();
	}

	public NeoDatastoreException(String message) {
		super(message);
	}

	public NeoDatastoreException(Throwable cause) {
		super(cause);
	}

	public NeoDatastoreException(String message, Throwable cause) {
		super(message, cause);
	}

}

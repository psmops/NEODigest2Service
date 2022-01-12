package psneo.exceptions;

import java.util.ArrayList;
import java.util.List;

/**
 * 
 * The base class for all PS exceptions 
 * 
 * @author schastel
 *
 */
public class NeoException extends Exception {
	/** */
	private static final long serialVersionUID = 1L;
	/** A generic data container */
	private Object data;
	
	public NeoException() {
		super();
		this.data = null;
	}

	public NeoException(String message) {
		super(message);
		this.data = null;
	}

	public NeoException(Throwable cause) {
		super(cause);
		this.data = null;
	}

	public NeoException(String message, Throwable cause) {
		super(message, cause);
		this.data = null;
	}
	
	public List<String> stackTrace() {
		List<String> stack = new ArrayList<String>();
		for (StackTraceElement e : this.getStackTrace()) {
			stack.add(e.toString());
		}
		return stack;
	}
	
	public void setData(Object data) {
		this.data = data;
	}
	
	public <T extends NeoException> T setData(Object data, Class<T> clazz) {
		this.data = data;
		return clazz.cast(this);
	}
	
	public Object getData() {
		return this.data;
	}
}

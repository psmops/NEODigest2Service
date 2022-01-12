package psneo.exceptions;

import psneo.os.ProcessExecutionResult;

/**
 * PS exceptions related to arguments of methods
 * 
 * @author schastel
 *
 */
public class NeoProcessException extends NeoException {
	/** */
	private static final long serialVersionUID = 1L;

	private ProcessExecutionResult processExecutionResult;
	public ProcessExecutionResult getProcessExecutionResult() {
		return this.processExecutionResult;
	}
	public NeoProcessException setProcessExecutionResult(ProcessExecutionResult processExecutionResult) {
		this.processExecutionResult = processExecutionResult;
		return this;
	}
	
	public NeoProcessException() {
		super();
	}

	public NeoProcessException(String message) {
		super(message);
	}

	public NeoProcessException(Throwable cause) {
		super(cause);
	}

	public NeoProcessException(String message, Throwable cause) {
		super(message, cause);
	}

}

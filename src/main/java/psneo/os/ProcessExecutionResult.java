package psneo.os;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Path;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psneo.exceptions.NeoProcessException;
import psneo.exceptions.NeoTimeoutException;
import psneo.logging.NeoLogging;

public class ProcessExecutionResult {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(ProcessExecutionResult.class);

	List<String> command;
	public List<String> getCommand() {
		return this.command;
	}
	public String getCommandAsString() {
		return String.join(" ", this.command);
	}

	Path cwd;
	public Path getCwd() {
		return this.cwd;
	}

	Integer exitStatus;
	public Integer getExitStatus() {
		return this.exitStatus;
	}
	public boolean isSuccess(boolean throwExceptionIfFailed) throws NeoProcessException {
		if ( (this.exitStatus != null) && (this.exitStatus.intValue() == 0) ) {
			return true;
		} else {
			if (throwExceptionIfFailed) {
				throw new NeoProcessException("Execution of [" + getCommandAsString() + "] failed")
				.setProcessExecutionResult(this);
			}
			return false;
		}
	}
	
	String stdout;
	public String getStdout() {
		return this.stdout;
	}
	public List<String> getStdoutLines() {
		return List.of(this.stdout.split("\n"));
	}
	String stderr;
	public String getStderr() {
		return this.stderr;
	}
	public List<String> getStderrLines() {
		return List.of(this.stderr.split("\n"));
	}

	Throwable exception;
	public Throwable getException() {
		return this.exception;
	}

	ProcessExecutionResult(List<String> command, Path cwd) {
		this.command = command;
		this.cwd = cwd;
		this.stdout = "";
		this.stderr = "";
	}

	/**
	 * See {@link #runCommand(List, Path, int, Map)} when cwd is null and env is null (=empty)
	 */
	public static ProcessExecutionResult runCommand(List<String> command, long timeoutSeconds) 
			throws NeoProcessException {
		return runCommand(command, null, timeoutSeconds, null);
	}
	/**
	 * See {@link #runCommand(List, Path, int, Map)} when cwd is null
	 */
	public static ProcessExecutionResult runCommand(List<String> command, long timeoutSeconds, Map<String, String> env) 
			throws NeoProcessException {
		return runCommand(command, null, timeoutSeconds, env);
	}
	/**
	 * See{@link #runCommand(List, Path, int, Map)} when env is null
	 */
	public static ProcessExecutionResult runCommand(List<String> command, Path path, long timeoutSeconds) 
			throws NeoProcessException {
		return runCommand(command, path, timeoutSeconds, null);
	}
	
	/**
	 * Run the command (i.e. an executable and its parameters) 
	 * from the cwd working directory, timing out after timeoutSeconds seconds.
	 * env can be used to provide environment parameters that can be used by the command executable
	 * 
	 * @param command the command to execute
	 * @param cwd the working directory (or where the JVM is executed from if null)
	 * @param timeoutSeconds (the timeout duration. <=0 means wait indefinitely)
	 * @param env if not null, key-value that are provided as environment parameters
	 * @return a representation of the execution of the command
	 */
	public static ProcessExecutionResult runCommand(List<String> command, Path cwd, long timeoutSeconds, 
			Map<String, String> env) {
		return runCommand(command, cwd, 
				timeoutSeconds <= 0 ? null : Duration.ofSeconds(timeoutSeconds), 
				env);
	}

	/**
	 * Run the command (i.e. an executable and its parameters) 
	 * from the cwd working directory, timing out after the timeout duration.
	 * env can be used to provide environment parameters that can be used by the command executable
	 * 
	 * @param command the command to execute
	 * @param cwd the working directory (or where the JVM is executed from if null)
	 * @param timeout if null, wait indefinitely; if not, timeout after this duration
	 * @param env if not null, key-value that are provided as environment parameters
	 * @return a representation of the execution of the command
	 */
	public static ProcessExecutionResult runCommand(List<String> command, Path cwd, Duration timeout, 
			Map<String, String> env) {
		ProcessExecutionResult processResult = new ProcessExecutionResult(command, cwd);
		ProcessBuilder processBuilder = new ProcessBuilder(command);
		if (cwd != null) {
			processBuilder.directory(cwd.toFile());
		}
		if (env != null) {
			Map<String, String> pbEnvironment = processBuilder.environment();
			pbEnvironment.putAll(env);
		}
		try {
			logger.debug("Starting process [{}]", String.join(" ", command));
			Process process = processBuilder.start();
			if (timeout == null) {
				processResult.exitStatus = process.waitFor();
			} else {
				if (process.waitFor(timeout.toMillis(), TimeUnit.MILLISECONDS)) {
					processResult.exitStatus = process.exitValue();
				} else {
					String message = NeoLogging.format("Command [{}] has timed out after {}", 
							processResult.getCommandAsString(), timeout);
					logger.debug("{}", message);
					processResult.exception = new NeoTimeoutException(message);
				}
			}
			// Grab the output streams
			ExecutorService es = Executors.newFixedThreadPool(2);
			try (InputStream is = process.getInputStream();
					InputStream ers = process.getErrorStream()) {
				StreamGobbler stdout = new StreamGobbler(is);
				StreamGobbler stderr = new StreamGobbler(ers);
				es.submit(stdout);
				es.submit(stderr);
				es.shutdown();
				es.awaitTermination(timeout.toMillis(), TimeUnit.MILLISECONDS);
				if (stdout.getOutput() == null) {
					processResult.stdout = "";
				} else {
					processResult.stdout = new String(stdout.getOutput());
				}
				if (stderr.getOutput() == null) {
					processResult.stderr = "";
				} else {
					processResult.stderr = new String(stderr.getOutput());
				}
			} 
			logger.debug("Exit status of [{}] is {}",
					processResult.getCommandAsString(), processResult.exitStatus);
		} catch (InterruptedException | IOException e) {
			processResult.exception = e;
		}
		return processResult;
	}
	
}

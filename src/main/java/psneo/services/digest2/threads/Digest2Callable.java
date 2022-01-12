package psneo.services.digest2.threads;

import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.Callable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psneo.os.ProcessExecutionResult;

public class Digest2Callable implements Callable<String> {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(Digest2Callable.class);

	String id;
	Path digest2Executable;
	Path digest2directory;
	Path observationsFile;
	
	public Digest2Callable(String id, 
			Path digest2Executable,
			Path digest2directory,
			Path observationsFile) {
		this.id = id;
		this.digest2Executable = digest2Executable;
		this.digest2directory = digest2directory;
		this.observationsFile = observationsFile;
	}

	@Override
	public String call() throws Exception {
		logger.info("Starting {}", this.id);
		List<String> command = List.of(
				this.digest2Executable.toString(),
				"-p",
				this.digest2directory.toString(),
				this.observationsFile.toString()
				);
		ProcessExecutionResult processExecutionResult = ProcessExecutionResult.runCommand(command, 1200);
		if (processExecutionResult.isSuccess(false)) {
			logger.info("{} output:\n{}", this.id, processExecutionResult.getStdout());
			return processExecutionResult.getStdout();
		} else {
			logger.info("{} failed", this.id);
			logger.info("{} stdout: {}", this.id, processExecutionResult.getStdout());
			logger.info("{} stderr: {}", this.id, processExecutionResult.getStderr());
			logger.info("{} stderr: {}", this.id, processExecutionResult.getException());
			return null;
		}
	}

}

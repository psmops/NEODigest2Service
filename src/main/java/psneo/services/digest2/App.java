package psneo.services.digest2;

import java.nio.file.Path;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import psneo.exceptions.NeoInitializationException;
import psneo.logging.NeoLogging;
import psneo.services.digest2.threads.HttpServer;

/**
 * Because digest2 single measurement of the score is somewhat unreliable
 *
 */
@Command(name = App.COMMAND_NAME)
public class App {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(App.class);

	static final String COMMAND_NAME = "ServiceDigest2";

	@Option(names = { "-debug", "-d"},
			description = "Debug information")
	boolean debug;

	@Option(names = { "-port", "-p", },
			description = "Port to listen to",
			defaultValue = "9290" )
	int port;

	@Option(names = { "-digest2directory", "-D", },
			description = "Directory containing *all* digest2 files expected to be named digest2.config, digest2.model,"
					+ " and digest2.obscodes",
					defaultValue = "/data/shared/digest2/current" )
	Path digest2directory;

	@Option(names = { "-instances", "-i", },
			description = "Number of digest2 instances to run concurrently",
			defaultValue = "5" )
	int instancesCount;
	
	@Option(names = { "-digest2executable", "-e"}, 
			description = "Path to the digest2 executable",
			defaultValue = "/digest2/digest2")
	Path digest2Executable;
	public Path getDigest2Executable() {
		return this.digest2Executable;
	}

	HttpServer httpServer;
	
	static App initialize(String[] args) throws NeoInitializationException {
		App app = new App();
		CommandLine commandLine = new CommandLine(app);
		commandLine.parseArgs(args);
		return app;
	}

	void run() throws Exception {
		// Create and submit the http server thread
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		this.httpServer = new HttpServer(this.port, this.digest2Executable, this.instancesCount, this.digest2directory);
		executorService.submit(this.httpServer);
		executorService.shutdown();
		executorService.awaitTermination(10, TimeUnit.MINUTES); //FIXME
	}
	
	public static void main(String[] args) throws Exception {
		//NeoLogging.initializeDebug();
		NeoLogging.silence();
		App app = initialize(args);
		app.run();
		logger.info("Terminating");
	}
}

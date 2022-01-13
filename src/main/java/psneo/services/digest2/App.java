package psneo.services.digest2;

import java.nio.file.Path;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import picocli.CommandLine;
import picocli.CommandLine.ArgGroup;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.ParseResult;
import psneo.exceptions.NeoInitializationException;
import psneo.logging.NeoLogging;
import psneo.services.digest2.threads.HttpServer;

/**
 * Because digest2 single measurement of the score is somewhat unreliable
 *
 */
@Command(name = App.COMMAND_NAME,
		mixinStandardHelpOptions = true,
		description = "Digest2 service")
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

	@ArgGroup(exclusive = true, multiplicity = "0..1", 
			heading = "Termination time control%n")
	Lifespan lifespan;
	static class Lifespan {
		static final String DEFAULT_STOP_AT = "17:00:00";
		@Option(names = {"-stopAt", "-s"},
				description = "Time when the server will stop. Default: " + DEFAULT_STOP_AT,
				required = true)
		LocalTime stopAt;
		@Option(names = { "-lifespan", "-l"},
				description = "Duration after which the server will stop",
				required = true)
		Duration lifespan;
	}
	Duration waitTimeBeforeTermination;
	
	HttpServer httpServer;

	static App initialize(String[] args) throws NeoInitializationException {
		App app = new App();
		CommandLine commandLine = new CommandLine(app);
		ParseResult parseResult = commandLine.parseArgs(args);
		if (parseResult.isUsageHelpRequested()) {
			CommandLine.usage(new App(), System.out);
			return null;
		}
		NeoLogging.log2file("/dev/stdout", app.debug ? Level.DEBUG : Level.INFO);
		NeoLogging.silenceJetty();
		return app;
	}

	void run() throws Exception {
		// Create and submit the http server thread
		ExecutorService executorService = Executors.newFixedThreadPool(1);
		this.httpServer = new HttpServer(this.port, this.digest2Executable, this.instancesCount, this.digest2directory);
		executorService.submit(this.httpServer);
		executorService.shutdown();
		initializeWaitTime();
		logger.info("Will terminate in {} at {}", 
				this.waitTimeBeforeTermination, LocalDateTime.now().plus(this.waitTimeBeforeTermination));
		executorService.awaitTermination(this.waitTimeBeforeTermination.toSeconds(), TimeUnit.SECONDS);
		this.httpServer.close();
	}

	private void initializeWaitTime() {
		// Calculate waitTimeBeforeTermination after all instantiations
		if (this.lifespan == null) {
			logger.debug("lifespan is null");
			this.lifespan = new Lifespan();
			this.lifespan.stopAt = LocalTime.parse(Lifespan.DEFAULT_STOP_AT);
		}
		LocalDateTime terminateAt = null;
		if (this.lifespan.stopAt == null) {
			logger.debug("Using lifespan");
			this.waitTimeBeforeTermination = this.lifespan.lifespan;
			return;
		} else {
			logger.debug("Using stopAt");
			terminateAt = LocalDateTime.now().with(this.lifespan.stopAt);
		}
		logger.debug("Should terminate at {} local time", terminateAt);
		this.waitTimeBeforeTermination = Duration.between(LocalDateTime.now(), terminateAt);
		if (this.waitTimeBeforeTermination.isNegative() || this.waitTimeBeforeTermination.isZero()) {
			this.waitTimeBeforeTermination .plusHours(24);
		}
	}

	public static void main(String[] args) throws Exception {
		//NeoLogging.initializeDebug();
		NeoLogging.silence();
		App app = initialize(args);
		if (app != null) {
			app.run();
		}
		logger.info("Terminating");
	}
}

package psneo.logging;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Instant;
import java.util.Properties;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.BiConsumer;

import org.slf4j.LoggerFactory;
import org.slf4j.event.Level;

import ch.qos.logback.classic.LoggerContext;
import ch.qos.logback.classic.joran.JoranConfigurator;
import ch.qos.logback.core.joran.spi.JoranException;
import ch.qos.logback.core.util.StatusPrinter;
import psneo.exceptions.NeoException;
import psneo.exceptions.NeoIOException;
import psneo.exceptions.NeoInitializationException;

public class NeoLogging {
	private static final AtomicBoolean isNomTamFitsSilenced = new AtomicBoolean(false);
	private static final AtomicBoolean isHikariSilenced = new AtomicBoolean(false);
	private static final AtomicBoolean isHibernateSilenced = new AtomicBoolean(false);
	private static final AtomicBoolean isJettySilenced = new AtomicBoolean(false);

	public static final String DEFAULT_LOG_PATH = "/data/local/neo/logs";
	public static final String PICOCLI_DOC_LOG_OPTION = "Log filename. If not set, default value is "
			+ "/data/local/neo/logs/<projectName>/<projectName>-<tjd>.log. If <value> set to a directory,"
			+ "log file to <value><projectName>-<tjd>.log otherwise log to <value>";
	
	public static final Path NO_LOGGING = Path.of("/dev/null");
	
	public static final Path getDefaultLogFile(Path logOption, String projectName) throws NeoInitializationException {
		/* -9413+Instant.now().getEpochSecond()/86400 */
		if (projectName == null) {
			throw new NeoInitializationException("projectName must be set");
		}
		if (logOption == null) {
			return Path.of(DEFAULT_LOG_PATH)
					.resolve(projectName)
					.resolve(String.format("%s-%d.log", projectName, -9413+Instant.now().getEpochSecond()/86400));
		} else {
			if (!Files.exists(logOption)) {
				return logOption;
			}
			if (Files.isDirectory(logOption)) {
				return logOption.resolve(String.format("%s-%d.log", projectName, -9413+Instant.now().getEpochSecond()/86400));
			}
			return logOption;
		}
	}

	private NeoLogging() {
	}

	/*
	 * The nom-tam-fits library comes with annoying log messages to java.util.logging
	 * 
	 * This method allows silencing it.
	 */
	public static final void silenceNomTamFits() {
		if (!isNomTamFitsSilenced.getAndSet(true)) {
			for (java.util.logging.Handler handler : java.util.logging.Logger.getLogger("").getHandlers()) {
				java.util.logging.Logger.getLogger("").removeHandler(handler);
			}
		}
	}
	public static final void silenceHikari() {
		if (!isHikariSilenced.getAndSet(true)) {
			LoggerContext.class.cast(LoggerFactory.getILoggerFactory())
			.getLogger("com.zaxxer.hikari")
			.setLevel(ch.qos.logback.classic.Level.INFO);
		}
	}
	public static final void silenceHibernate() {
		if (!isHibernateSilenced.getAndSet(true)) {
			LoggerContext.class.cast(LoggerFactory.getILoggerFactory())
			.getLogger("org.hibernate")
			.setLevel(ch.qos.logback.classic.Level.INFO);
		}
	}
	public static final void silenceJetty() {
		if (!isJettySilenced.getAndSet(true)) {
			LoggerContext.class.cast(LoggerFactory.getILoggerFactory())
			.getLogger("org.eclipse.jetty")
			.setLevel(ch.qos.logback.classic.Level.INFO);
		}
	}
	
	private static ConcurrentHashMap<String, Boolean> silenced = new ConcurrentHashMap<>();
	public static void silence(String packagePath) {
		silenced.computeIfAbsent(packagePath, k -> {
			LoggerContext.class.cast(LoggerFactory.getILoggerFactory())
			.getLogger(packagePath)
			.setLevel(ch.qos.logback.classic.Level.INFO);
			return true;
		});
	}

	public static final void silence() {
		silenceNomTamFits();
		silenceHikari();
		silenceHibernate();
		silenceJetty();
	}

	/**
	 * Initialization of the logging system  with for the default configuration
	 * @throws NeoException if the logging system could not be initialized
	 */
	public static final void initialize() throws NeoInitializationException {
		try (InputStream is = Resources.class.getResourceAsStream("/logging/neo-default-logging.xml");) {
			initialize(is);
		} catch (IOException e) {
			throw new NeoInitializationException(e);
		}
	}

	/**
	 * Initialization of the logging system with for the debug configuration
	 * @throws NeoException if the logging system could not be initialized
	 */
	public static final void initializeDebug() throws NeoInitializationException {
		try (InputStream is = Resources.class.getResourceAsStream("/logging/neo-debug-logging.xml");) {
			initialize(is);
		} catch (IOException e) {
			throw new NeoInitializationException(e);
		}
	}

	public static final void initialize(Level level) throws NeoInitializationException {
		switch (level) {
		case DEBUG:
			initializeDebug();
			break;
		case INFO:
			initialize();
			break;
		default:
			System.err.println("Level " + level + " not supported. Using INFO");
			initialize();
			break;
		}
	}
	
	/**
	 * Initialization of the logging system with a user-defined configuration
	 * 
	 * @param xmlFilename XML logback configuration file
	 * 		see https://logback.qos.ch/manual/configuration.html
	 * @throws NeoException:
	 * 		either a NeoIOException If the file cannot be read
	 *  	or a NeoInitializationException if the logging system could not be initialized
	 */
	public static final void initialize(String xmlFilename) throws NeoInitializationException, NeoIOException { 
		try (FileInputStream fis = new FileInputStream(xmlFilename);) {
			initialize(fis);
		} catch (IOException e) {
			throw new NeoIOException(e);
		}
	}

	/** 
	 * Initialization of the logging system from any input stream. Useful for embedded configuration files
	 *  
	 * @param inputStream the input stream backing the configuration
	 * @throws NeoException if the logging system could not be initialized
	 */
	public static final void initialize(InputStream inputStream) throws NeoInitializationException {
		if (inputStream == null) {
			System.err.println("Can't initialize from null InputStream");
			return;
		}
		LoggerContext context = LoggerContext.class.cast(LoggerFactory.getILoggerFactory());
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default configuration. 
			// For multi-step configuration, omit calling context.reset().
			context.reset(); 
			configurator.doConfigure(inputStream);
		} catch (JoranException je) {//NOSONAR
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
			throw new NeoInitializationException(je);
		}
	}
	public static final void initializeFromJarResource(String resourceName) throws NeoInitializationException {
		try (InputStream is = ClassLoader.getSystemClassLoader().getResourceAsStream(resourceName);) {
			initialize(is);
		} catch (IOException e) {
			throw new NeoInitializationException(e);
		}
	}
	
	
	/** 
	 * Initialization of the logging system from any input stream. Useful for embedded configuration files
	 *  
	 * @param inputStream the input stream backing the configuration
	 * @throws NeoException if the logging system could not be initialized
	 */
	public static final void initialize(InputStream inputStream, Properties properties) throws NeoInitializationException {
		LoggerContext context = LoggerContext.class.cast(LoggerFactory.getILoggerFactory());
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			context.reset();
			// Call context.reset() to clear any previous configuration, e.g. default configuration. 
			// For multi-step configuration, omit calling context.reset().
			properties.forEach(new BiConsumer<Object, Object>() {
				@Override
				public void accept(Object t, Object u) {
					context.putProperty(t.toString(), u.toString());
				}
			});
			configurator.doConfigure(inputStream);
		} catch (JoranException je) {//NOSONAR
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
			throw new NeoInitializationException(je);
		}
	}

	/**
	 * Change logfile name programmatically
	 * 
	 * http://mailman.qos.ch/pipermail/logback-user/2008-November/000798.html
	 */
	//	public static final void changeLogfileName(String templateXmlFilename, String baseLogfileName)
	//			throws NeoInitializationException, NeoIOException { 
	//		LoggerContext context = LoggerContext.class.cast(LoggerFactory.getILoggerFactory());
	//		try {
	//			JoranConfigurator configurator = new JoranConfigurator();
	//			configurator.setContext(context);
	//			// Call context.reset() to clear any previous configuration, e.g. default configuration. 
	//			// For multi-step configuration, omit calling context.reset().
	//			context.putProperty("baseLogfileName", baseLogfileName);
	//			context.reset(); 
	//			configurator.doConfigure(templateXmlFilename);
	//		} catch (JoranException je) {//NOSONAR
	//			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
	//			throw new NeoInitializationException(je);
	//		}
	//	}
	public static final void changeLogfileName(InputStream inputStream, String baseLogfileName)
			throws NeoInitializationException, NeoIOException { 
		LoggerContext context = LoggerContext.class.cast(LoggerFactory.getILoggerFactory());
		try {
			JoranConfigurator configurator = new JoranConfigurator();
			configurator.setContext(context);
			// Call context.reset() to clear any previous configuration, e.g. default configuration. 
			// For multi-step configuration, omit calling context.reset().
			context.reset();
			context.putProperty("baseLogfileName", baseLogfileName);
			configurator.doConfigure(inputStream);
		} catch (JoranException je) {//NOSONAR
			StatusPrinter.printInCaseOfErrorsOrWarnings(context);
			throw new NeoInitializationException(je);
		}
	}

	public static final void log2file(String logFileName, Level logLevel) throws NeoInitializationException {
		Properties properties = new Properties();
		properties.setProperty("LOG_LEVEL", logLevel.toString());
		properties.setProperty("LOG_FILENAME", logFileName);
		try (InputStream is = Resources.class.getResourceAsStream("/logging/neo-logging2file.xml");) {
			initialize(is, properties);
		} catch (IOException e) {
			throw new NeoInitializationException(e);
		}
	}

	public static final void log2file(Path path, Level logLevel) throws NeoInitializationException {
		if (path == null) {
			initialize(logLevel);
		} else {
			if (path != NO_LOGGING) {
				LoggerFactory.getLogger(NeoLogging.class).info("Logging to [{}] at level {}", path, logLevel);
			}
			log2file(path.toString(), logLevel);
		}
	}
	
	public static final void setPackageLoggingLevel(String packageName, Level level) {
		ch.qos.logback.classic.Level logBacklevel = ch.qos.logback.classic.Level.ALL;
		switch (level) {
		case DEBUG: {
			logBacklevel = ch.qos.logback.classic.Level.DEBUG;
			break; }
		case ERROR: {
			logBacklevel = ch.qos.logback.classic.Level.ERROR;
			break; }
		case INFO: {
			logBacklevel = ch.qos.logback.classic.Level.INFO;
			break; }
		case TRACE: {
			logBacklevel = ch.qos.logback.classic.Level.TRACE;
			break; }
		case WARN: {
			logBacklevel = ch.qos.logback.classic.Level.WARN;
			break; }
		}
		ch.qos.logback.classic.LoggerContext lc = (ch.qos.logback.classic.LoggerContext) LoggerFactory.getILoggerFactory();
		lc.getLogger(packageName).setLevel(logBacklevel);
	}

	public static String format(String format, Object ... arguments) {
		String[] elements = format.split("\\{\\}");
		if (elements.length == 1) {
			if (format.contains("{}")) {
				return format.replace("{}", arguments[0] == null?"null":arguments[0].toString());
			}
			return format;
		}
		StringBuilder sb = new StringBuilder();
		int count = 0;
		for (String element : elements) {
			sb.append(element);
			if (count < arguments.length) {
				sb.append(arguments[count] == null ? "null" : arguments[count]);
			}
			count += 1;
		}
		return sb.toString();
	}


}

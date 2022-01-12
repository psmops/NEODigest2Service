package psneo.logging;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Resources for the NEOLogging project
 * 
 * @author schastel
 *
 */
public class Resources {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(Resources.class);

	public static final String PACKAGE = Resources.class.getPackage().getName();

	private Resources() {
	}

	/**
	 * From
	 * http://stackoverflow.com/questions/309424/read-convert-an-inputstream-to-a-string
	 * 
	 * @param resourceName the resource location name 
	 * @return the resource read as a String object
	 * @throws NullPointerException if the resource cannot be found
	 */
	public static String loadResourcesAsString(String resourceName) {
		try (InputStream is = Resources.class.getResourceAsStream(resourceName);) {
			if (is == null) {
				logger.warn("Cannot read from resource {}", resourceName);
				return null;
			}
			try (InputStreamReader irs = new InputStreamReader(is);
					BufferedReader br = new BufferedReader(irs);) {
				return br.lines().collect(Collectors.joining("\n"));
			} 
		} catch (IOException e) {
			logger.warn("Issue while trying to read resource [{}] as String", resourceName, e);
			return null;
		}
	}

}

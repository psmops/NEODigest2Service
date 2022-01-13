package psneo.services.digest2.threads;

import java.nio.file.Path;
import java.util.concurrent.Callable;

import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class HttpServer implements Callable<Void> {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(HttpServer.class);

	int port;
	Path digest2Executable;
	int digest2instances;
	Path digest2directory;

	Server server;
	volatile boolean terminated;
	public void close() {
		this.terminated = true;
	}
	
	public HttpServer(int port, 
			Path digest2Executable,
			int digest2instances,
			Path digest2directory) {
		this.port = port;
		this.digest2Executable = digest2Executable;
		this.digest2instances = digest2instances;
		this.digest2directory = digest2directory;
        this.terminated = false;
	}
	
	
	@Override
	public Void call() throws Exception {
		this.server = new Server(this.port);
		ServletContextHandler handler = new ServletContextHandler(this.server, "/");
        handler.addServlet(new ServletHolder(new Digest2Servlet(this)), Digest2Servlet.URL);
		logger.info("Starting http server on port {}", this.port);
		this.server.start();
        while (!this.terminated) {
        	Thread.sleep(10);
        }
		logger.info("Stopping http server");
        this.server.stop();
		return null;
	}

}

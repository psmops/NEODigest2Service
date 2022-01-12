package psneo.services.digest2.threads;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.http.HttpStatus;
import org.eclipse.jetty.server.Request;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import jakarta.servlet.MultipartConfigElement;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.Part;
import psneo.services.digest2.models.ScoresAggregator;

public class Digest2Servlet extends HttpServlet {
	/** */
	private static final long serialVersionUID = -1302919852039309078L;
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(Digest2Servlet.class);

	HttpServer digest2HttpServer;

	public static final String URL = "/digest2scores";
	public static final String INPUT_OBSERVATIONS = "obs";

	static final Gson JSON = new GsonBuilder()
			.excludeFieldsWithoutExposeAnnotation()
			.serializeSpecialFloatingPointValues()
			.create();

	public Digest2Servlet(HttpServer digest2HttpServer) {
		this.digest2HttpServer = digest2HttpServer;
	}

	/**
	 * Typically, body is built from the HttpRequest:
	 * try (BufferedReader reader = httpRequest.getReader();) {
	 *   String body = reader.lines().collect(Collectors.joining("\n"));
	 *   data = ofDataForm(body);
	 * }
	 * 
	 * @param body
	 * @return 
	 */
	static Map<String, String> ofDataForm(String body) {
		Map<String, String> data = new HashMap<>();
		String[] parameters = body.split("&");
		for (String parameter : parameters) {
			String[] elements = parameter.split("=");
			String key = URLDecoder.decode(elements[0], StandardCharsets.US_ASCII);
			String value = URLDecoder.decode(elements[1], StandardCharsets.US_ASCII);
			data.put(key, value);
		}
		return data;
	}

	private static final MultipartConfigElement MULTI_PART_CONFIG = new MultipartConfigElement("/tmp/digest2");
	@Override
	protected void doPost(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
		String contentType = req.getContentType();
		if (contentType != null && contentType.startsWith("multipart/")){
			req.setAttribute(Request.__MULTIPART_CONFIG_ELEMENT, MULTI_PART_CONFIG);
		}
		Part part = req.getPart(INPUT_OBSERVATIONS);
		String observations = null;
		logger.debug("Size = [{}]", part.getSize());
		try (BufferedInputStream bis = new BufferedInputStream(part.getInputStream());) {
			observations = new String(bis.readAllBytes());
		}
		// Dump the observations in some file
		Path obsPath = Files.createTempFile("digest2-tracklets.", "obs");
		try (PrintWriter writer = new PrintWriter(obsPath.toFile());) {
			writer.print(observations);
			writer.close();
		}
		// Now start as many digest2 instances as necessary
		ExecutorService executorService = Executors.newFixedThreadPool(this.digest2HttpServer.digest2instances);
		List<Future<String>> results = new ArrayList<>();
		for (int instance = 0; instance < this.digest2HttpServer.digest2instances; instance += 1) {
			Digest2Callable digest2Callable = new Digest2Callable(String.format("Digest2-%02d", instance),
					this.digest2HttpServer.digest2Executable,
					this.digest2HttpServer.digest2directory, 
					obsPath);
			results.add(executorService.submit(digest2Callable));
		}
		executorService.shutdown();
		try {
			executorService.awaitTermination(10, TimeUnit.MINUTES);
		} catch (InterruptedException e) {
			throw new ServletException(e);
		}
		ScoresAggregator scoresAggregator = new ScoresAggregator();
		for (var result : results) {
			try {
				scoresAggregator.addInputLines(result.get());
			} catch (InterruptedException | ExecutionException e) {
				logger.warn("Exception caught while building ScoresAggregator", e);
			}
		}

		try (PrintWriter writer = resp.getWriter();) {
			writer.println(JSON.toJson(scoresAggregator.getScores()));
			resp.setStatus(HttpStatus.OK_200);
		} 
	}
}

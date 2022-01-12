package psneo.services.digest2.models;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import psneo.exceptions.NeoAssertionException;

/**
 * Aggregates the digest2 data (only works for Pan-STAARS configuration
 * 
 * @author schastel
 *
 */
public class ScoresAggregator {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(ScoresAggregator.class);

	List<Digest2Score> digest2Scores;
	public synchronized void addInputLine(String inputLine) {
		try {
			this.digest2Scores.add(Digest2Score.build(inputLine));
		} catch (Exception e) {
			logger.warn("Ignoring line [{}] because of {}", inputLine, e.getMessage());
		}
	}
	public synchronized void addInputLines(String inputLines) {
		for (var inputLine : inputLines.split("\n")) {
			addInputLine(inputLine);
		}
	}
	
	public ScoresAggregator() {
		this.digest2Scores = new ArrayList<>();
	}
	
	public List<Digest2Score> getScores() {
		Map<String, List<Digest2Score>> byTracklet = this.digest2Scores.stream()
				.collect(Collectors.groupingBy(Digest2Score::getTracklet));
		List<Digest2Score> scores = new ArrayList<>();
		byTracklet.values().forEach(l -> {
			try {
				scores.add(Digest2Score.aggregate(l));
			} catch (NeoAssertionException e) {
				logger.info("Issues computing aggregated score: {}", e.getMessage());
			}
		});
		return scores;
	}
}

package psneo.services.digest2.models;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.annotations.Expose;

import psneo.exceptions.NeoAssertionException;

public class Digest2Score {
	/** Logging */
	private final static Logger logger = LoggerFactory.getLogger(Digest2Score.class);

	@Expose
	String inputLine;

	@Expose
	String tracklet;
	public String getTracklet() {
		return this.tracklet;
	}
	@Expose
	double rms;
	public double getRms() {
		return this.rms;
	}
	@Expose
	int neo;
	public int getNeo() {
		return this.neo;
	}

	private Digest2Score() {
	}

	public static final Digest2Score build(String inputLine) throws NeoAssertionException {
		if (inputLine.startsWith("#")) {
			throw new NeoAssertionException("Rejected comment line");
		}
		Digest2Score digest2Score = new Digest2Score();
		digest2Score.inputLine = inputLine;
		String[] elements = inputLine.split("\\s\\s*");
		if (elements.length != 3) {
			throw new NeoAssertionException("3 elements are expected. Got " + elements.length);
		}
		digest2Score.tracklet = elements[0];
		try {
			digest2Score.rms = Double.parseDouble(elements[1]);
			digest2Score.neo = Integer.parseInt(elements[2]);
		} catch (NumberFormatException e) {
			throw new NeoAssertionException(e);
		}
		return digest2Score;
	}

	public static Digest2Score aggregate(List<Digest2Score> scores) throws NeoAssertionException {
		Digest2Score aggregated = new Digest2Score();
		List<Double> rms = new ArrayList<>();
		List<Integer> neo = new ArrayList<>();
		for (Digest2Score score : scores) {
			if (aggregated.tracklet == null) {
				aggregated.tracklet = score.tracklet;
			} else {
				if (!aggregated.tracklet.equals(score.tracklet)) {
					throw new NeoAssertionException("Mixed tracklets: " + score.tracklet
							+ " is among " + aggregated.tracklet);
				}
			}
			rms.add(score.rms);
			neo.add(score.neo);
		}
		neo.sort(Integer::compareTo);
		aggregated.neo = neo.get(neo.size()/2);
		logger.info("Score: {} [{}-{}]", aggregated.neo, neo.get(0), neo.get(neo.size()-1));
		aggregated.rms = rms.stream().mapToDouble(d -> d).average().getAsDouble();
		return aggregated;
	}
}

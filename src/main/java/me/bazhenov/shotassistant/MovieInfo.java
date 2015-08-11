package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;

import java.awt.*;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;

import static com.google.common.base.Splitter.on;
import static java.nio.charset.StandardCharsets.UTF_8;
import static java.util.stream.Collectors.toList;
import static me.bazhenov.shotassistant.ExportDiagnosticFrames.parsePerspectivePoints;

public class MovieInfo {

	private List<Point> perspectivePoints;
	private List<IpscScore> scores;

	public MovieInfo(File location) throws IOException {
		List<String> lines = Files.readAllLines(location.toPath(), UTF_8);
		perspectivePoints = parsePerspectivePoints(lines.get(1));
		scores = on(',').splitToList(lines.get(2)).stream()
			.map(IpscScore::valueOf)
			.collect(toList());
	}

	public List<Point> getPerspectivePoints() {
		return perspectivePoints;
	}

	public List<IpscScore> getScores() {
		return scores;
	}
}

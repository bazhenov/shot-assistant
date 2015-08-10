package me.bazhenov.shotassistant;

import org.testng.annotations.Test;

import java.util.Set;

import static me.bazhenov.shotassistant.ExportDiagnosticFrames.parseIntervals;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.hasSize;
import static org.testng.Assert.*;

public class ExportDiagnosticFramesTest {

	@Test
	public void parseIntervalsTest() {
		Set<Integer> integers = parseIntervals("1-3,7-8,11");
		assertThat(integers, hasSize(6));
	}
}
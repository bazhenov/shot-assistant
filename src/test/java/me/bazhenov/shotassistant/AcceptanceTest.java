package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscClassicalTarget;
import me.bazhenov.shotassistant.target.IpscScore;
import org.opencv.highgui.VideoCapture;
import org.testng.annotations.Test;

import java.io.File;
import java.io.IOException;
import java.util.List;

import static com.google.common.collect.Lists.newArrayList;
import static me.bazhenov.shotassistant.OpenCV.loadOpenCv;
import static me.bazhenov.shotassistant.ProcessingChain.createProcessingChain;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;

public class AcceptanceTest {

	@Test
	public void laser7() throws IOException {
		loadOpenCv();
		String fileName = "examples/laser7.mov";

		MovieInfo info = new MovieInfo(new File(fileName + ".info"));

		IpscClassicalTarget target = new IpscClassicalTarget();
		List<IpscScore> scores = newArrayList();
		ShotDetector detector = new ShotDetector(target, scores::add);
		ProcessingChain chain = createProcessingChain(info.getPerspectivePoints(), target, detector);

		VideoProcessor processor = new VideoProcessor();
		processor.addChain(chain);
		processor.run(new VideoCapture(fileName));

		assertThat(scores, equalTo(info.getScores()));
	}
}

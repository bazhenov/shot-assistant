package me.bazhenov.shotassistant;

import org.opencv.core.Mat;

import java.util.List;
import java.util.function.Consumer;
import java.util.function.Function;

import static com.google.common.collect.Lists.newArrayList;

public class ProcessingChain implements Consumer<Mat> {

	private final List<Named<Function<Mat, Mat>>> stages = newArrayList();
	private ProcessingListener listener;

	public void addStage(String name, Consumer<Mat> stage) {
		stages.add(new Named<>(name, i -> {
			stage.accept(i);
			return i;
		}));
	}

	public void addStage(String name, Function<Mat, Mat> stage) {
		stages.add(new Named<>(name, stage));
	}

	public void setListener(ProcessingListener listener) {
		this.listener = listener;
	}

	@Override
	public void accept(Mat mat) {
		if (listener != null) {
			listener.onFrame(mat, stages.size());
		}
		int stageNo = 0;
		for (Named<Function<Mat, Mat>> stage : stages) {
			mat = stage.get().apply(mat);
			if (listener != null) {
				listener.onStage(stageNo++, stage.getName(), mat);
			}
		}
		if (listener != null) {
			listener.onFrameComplete();
		}
	}
}

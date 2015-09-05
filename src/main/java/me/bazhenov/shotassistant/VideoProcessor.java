package me.bazhenov.shotassistant;

import org.opencv.core.Mat;
import org.opencv.highgui.VideoCapture;

import java.io.Closeable;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

import static com.google.common.collect.Lists.newArrayList;
import static com.google.common.collect.Maps.newHashMap;

public class VideoProcessor implements Closeable {

	private final List<ProcessingChain<?>> chains = newArrayList();
	private boolean isClosed = false;
	private ProcessingListener listener = new NopProcessingListener();

	public void addChain(ProcessingChain<?> chain) {
		chains.add(chain);
	}

	@Override
	public void close() throws IOException {
		isClosed = true;
	}

	public void setListener(ProcessingListener listener) {
		this.listener = listener;
	}

	public void run(VideoCapture videoCapture) {
		try {
			runAsync(videoCapture).get();
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		} catch (ExecutionException e) {
			throw new RuntimeException(e);
		}
	}

	public CompletableFuture<?> runAsync(VideoCapture videoCapture) {
		CompletableFuture<?> result = new CompletableFuture<>();
		new Thread(() -> {
			Mat frame = new Mat();
			Mat copy = new Mat();
			Map<ProcessingChain<?>, Object> results = newHashMap();
			while (videoCapture.read(frame) && !isClosed) {
				frame.copyTo(copy);
				listener.onFrame(copy);
				for (ProcessingChain<?> c : chains) {
					frame.copyTo(copy);
					Object ref = c.apply(copy, (name, mat) -> listener.onStage(c, name, mat));
					results.put(c, ref);
				}
				listener.onFrameComplete(results);
			}
			result.complete(null);
		}).start();
		return result;
	}

	private class NopProcessingListener implements ProcessingListener {

		@Override
		public void onStage(ProcessingChain<?> chain, String name, Mat processingResult) {

		}

		@Override
		public void onFrame(Mat mat) {

		}

		@Override
		public void onFrameComplete(Map<ProcessingChain<?>, Object> features) {

		}
	}
}

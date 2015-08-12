package me.bazhenov.shotassistant;

import me.bazhenov.shotassistant.target.IpscScore;

import java.io.IOException;
import java.util.function.Consumer;

public class AnnounceShotListener implements Consumer<IpscScore> {

	@Override
	public void accept(IpscScore ipscScore) {
		try {
			Runtime.getRuntime().exec("say -r 220 '" + ipscScore.toString() + "'");
		} catch (IOException e) {
			e.printStackTrace(System.err);
			throw new RuntimeException(e);
		}
	}
}

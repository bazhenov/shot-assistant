package me.bazhenov.shotassistant;

import org.testng.annotations.Test;

import static me.bazhenov.shotassistant.ShotDetectingAutomata.State.*;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class ShotDetectingAutomataTest {

	@Test
	public void testAutomata() {
		ShotDetectingAutomata automata = new ShotDetectingAutomata(2, 5);
		assertThat(automata.feed(false), is(NoShot));
		assertThat(automata.feed(false), is(NoShot));

		assertThat(automata.feed(true), is(Candidate));
		assertThat(automata.feed(true), is(Candidate));

		assertThat(automata.feed(true), is(Shot));

		assertThat(automata.feed(true), is(CoolDown));
		assertThat(automata.feed(true), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));
		assertThat(automata.feed(true), is(CoolDown));
		assertThat(automata.feed(true), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));
		assertThat(automata.feed(false), is(CoolDown));

		assertThat(automata.feed(false), is(NoShot));
	}

	@Test
	public void highSpeedAutomata() {
		ShotDetectingAutomata automata = new ShotDetectingAutomata(0, 0);
		assertThat(automata.feed(false), is(NoShot));
		assertThat(automata.feed(true), is(Shot));
		assertThat(automata.feed(false), is(NoShot));
		assertThat(automata.feed(true), is(Shot));
		assertThat(automata.feed(true), is(CoolDown));
		assertThat(automata.feed(false), is(NoShot));
	}
}

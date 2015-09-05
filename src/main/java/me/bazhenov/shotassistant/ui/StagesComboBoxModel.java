package me.bazhenov.shotassistant.ui;

import me.bazhenov.shotassistant.ProcessingChain;

import javax.swing.*;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;

public class StagesComboBoxModel extends DefaultComboBoxModel<String> {

	private final Set<String> stages = newHashSet();

	public StagesComboBoxModel() {
		super();
	}

	public boolean isValid(ProcessingChain<?> c, String name) {
		return name.equals(getSelectedItem());
	}

	public void addIfNeeded(ProcessingChain<?> c, String name) {
		if (stages.add(name))
			addElement(name);
	}
}

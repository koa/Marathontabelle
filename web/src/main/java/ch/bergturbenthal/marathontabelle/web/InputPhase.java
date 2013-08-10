package ch.bergturbenthal.marathontabelle.web;

import com.vaadin.ui.FormLayout;
import com.vaadin.ui.TextField;

public class InputPhase extends FormLayout {
	TextField maxTime = new TextField("Maximum Time");
	TextField minTime = new TextField("Minimum Time");

	public InputPhase() {
		setSpacing(true);
		addComponent(maxTime);
		addComponent(minTime);
	}
}

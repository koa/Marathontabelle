package ch.bergturbenthal.marathontabelle.web.binding;

import java.time.DateTimeException;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.server.UserError;
import com.vaadin.shared.ui.ValueChangeMode;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;

public class LocalTimeField extends CustomField<LocalTime> {
	private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm");

	private final Supplier<Component> rootComponenSupplier;
	private final Supplier<LocalTime> currentValueSupplier;
	private Consumer<LocalTime> currentValueUpdater;

	public LocalTimeField(final String caption) {
		setCaption(caption);
		final TextField textField = new TextField();
		textField.addValueChangeListener(event -> {
			if (event.isUserOriginated()) {
				final String value = event.getValue();
				if (value.isEmpty())
					return;
				try {
					final LocalTime result = LocalTime.from(TIME_PATTERN.parse(value));
					textField.setValue(TIME_PATTERN.format(result));
					textField.setComponentError(null);
				} catch (final DateTimeException ex) {
					textField.setComponentError(new UserError(ex.getLocalizedMessage()));
				}
			}
		});
		textField.setValueChangeMode(ValueChangeMode.TIMEOUT);
		textField.setValueChangeTimeout(200);

		currentValueSupplier = () -> {
			final String value = textField.getValue();
			if (value.isEmpty())
				return null;

			final LocalTime result = LocalTime.from(TIME_PATTERN.parse(value));
			textField.setComponentError(null);
			return result;
		};
		rootComponenSupplier = () -> textField;
		currentValueUpdater = value -> {
			textField.setComponentError(null);
			if (value == null) {
				textField.setValue("");
				return;
			}
			final String string = TIME_PATTERN.format(value);
			textField.setValue(string);
		};
	}

	@Override
	protected void doSetValue(final LocalTime value) {
		currentValueUpdater.accept(value);
	}

	@Override
	public LocalTime getValue() {
		return currentValueSupplier.get();
	}

	@Override
	protected Component initContent() {
		return rootComponenSupplier.get();
	}

}

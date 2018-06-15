package ch.bergturbenthal.marathontabelle.web.binding;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Consumer;
import java.util.function.Supplier;

import com.vaadin.server.UserError;
import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;

public class NumberField extends CustomField<Double> {

	private final Supplier<Component> rootComponenSupplier;
	private final Supplier<Double> currentValueSupplier;
	private Consumer<Double> currentValueUpdater;

	public NumberField(final String caption, final NumberFormat format) {
		setCaption(caption);
		final TextField textField = new TextField();

		currentValueSupplier = () -> {
			try {
				final String value = textField.getValue();
				if (value.isEmpty())
					return null;
				final Number result;
				synchronized (format) {
					result = format.parse(value);
				}
				textField.setComponentError(null);
				return result.doubleValue();
			} catch (final ParseException e) {
				textField.setComponentError(new UserError(e.getMessage()));
				return null;
			}
		};
		rootComponenSupplier = () -> textField;
		currentValueUpdater = value -> {
			textField.setComponentError(null);
			if (value == null) {
				textField.setValue("");
				return;
			}
			synchronized (format) {
				textField.setValue(format.format(value));
			}
		};
	}

	@Override
	protected void doSetValue(final Double value) {
		currentValueUpdater.accept(value);
	}

	@Override
	public Double getValue() {
		return currentValueSupplier.get();
	}

	@Override
	protected Component initContent() {
		return rootComponenSupplier.get();
	}

}

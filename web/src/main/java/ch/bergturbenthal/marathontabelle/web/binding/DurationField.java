package ch.bergturbenthal.marathontabelle.web.binding;

import java.time.Duration;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoField;
import java.time.temporal.TemporalAccessor;
import java.time.temporal.TemporalField;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.regex.Pattern;

import com.vaadin.ui.Component;
import com.vaadin.ui.CustomField;
import com.vaadin.ui.TextField;

public class DurationField extends CustomField<Duration> {
	private static final Pattern DURATION_REGEX_PATTERN = Pattern.compile("([0-9]+):([0-5][0-9])");
	private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm");
	private static final DateTimeFormatter DURATION_PATTERN = DateTimeFormatter.ofPattern("mm:ss");

	private final Supplier<Component> rootComponenSupplier;
	private final Supplier<Duration> currentValueSupplier;
	private Consumer<Duration> currentValueUpdater;

	public DurationField(final String caption) {
		setCaption(caption);
		final TextField textField = new TextField();

		currentValueSupplier = () -> {
			final String value = textField.getValue();
			if (value.isEmpty())
				return null;
			final Duration result = Duration.from(DURATION_PATTERN.parse(value, r -> {
				return Duration.ofSeconds(r.get(ChronoField.HOUR_OF_DAY) * 3600 + r.get(ChronoField.MINUTE_OF_HOUR) * 60
						+ r.get(ChronoField.SECOND_OF_MINUTE));
			}));
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
			final Map<TemporalField, Supplier<Long>> fieldAccessors = new HashMap<>();
			fieldAccessors.put(ChronoField.SECOND_OF_MINUTE, () -> value.getSeconds() % 60);
			fieldAccessors.put(ChronoField.MINUTE_OF_HOUR, () -> value.toMinutes() % 60);
			fieldAccessors.put(ChronoField.HOUR_OF_DAY, () -> value.toHours());
			final TemporalAccessor temporal = new TemporalAccessor() {

				@Override
				public long getLong(final TemporalField field) {
					return fieldAccessors.get(field).get();
				}

				@Override
				public boolean isSupported(final TemporalField field) {
					return fieldAccessors.containsKey(field);
				}
			};
			final String string = DURATION_PATTERN.format(temporal);
			textField.setValue(string);
		};
	}

	@Override
	protected void doSetValue(final Duration value) {
		currentValueUpdater.accept(value);
	}

	@Override
	public Duration getValue() {
		return currentValueSupplier.get();
	}

	@Override
	protected Component initContent() {
		return rootComponenSupplier.get();
	}

}

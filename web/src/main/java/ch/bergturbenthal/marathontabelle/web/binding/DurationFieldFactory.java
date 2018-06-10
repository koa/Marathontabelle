package ch.bergturbenthal.marathontabelle.web.binding;

import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.vaadin.v7.data.Validator;
import com.vaadin.v7.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.v7.data.util.converter.Converter;
import com.vaadin.v7.ui.Field;
import com.vaadin.v7.ui.TextField;

import ch.bergturbenthal.marathontabelle.generator.FormatUtil;

public class DurationFieldFactory extends DefaultFieldGroupFieldFactory {
	private static final Pattern DURATION_REGEX_PATTERN = Pattern.compile("([0-9]+):([0-5][0-9])");
	private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm");

	@Override
	public <T extends Field> T createField(final Class<?> type, final Class<T> fieldType) {

		if (type.isAssignableFrom(Duration.class)) {
			final TextField textField = new TextField();
			textField.setConverter(new Converter<String, Duration>() {

				@Override
				public Duration convertToModel(final String value, final Class<? extends Duration> targetType,
						final Locale locale) throws ConversionException {
					if (value == null || value.trim().length() == 0)
						return null;
					final Matcher matcher = DURATION_REGEX_PATTERN.matcher(value);
					if (!matcher.matches())
						throw new Validator.InvalidValueException("Dauer muss als mm:ss eingegeben werden");
					return Duration
							.ofSeconds(Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
				}

				@Override
				public String convertToPresentation(final Duration value, final Class<? extends String> targetType,
						final Locale locale) throws ConversionException {
					if (value == null)
						return "";
					return FormatUtil.formatDuration(value);
				}

				@Override
				public Class<Duration> getModelType() {
					return Duration.class;
				}

				@Override
				public Class<String> getPresentationType() {
					return String.class;
				}
			});
			textField.setWidth("4em");
			return (T) textField;
		}
		if (type.isAssignableFrom(LocalTime.class)) {
			final TextField textField = new TextField();
			textField.setConverter(new Converter<String, LocalTime>() {

				@Override
				public LocalTime convertToModel(final String value, final Class<? extends LocalTime> targetType,
						final Locale locale) throws ConversionException {
					if (value == null || value.trim().length() == 0)
						return null;
					try {
						return LocalTime.parse(value, TIME_PATTERN);
					} catch (final IllegalArgumentException ex) {
						throw new Validator.InvalidValueException("Uhrzeit muss als hh:mm eingegeben werden");
					}
				}

				@Override
				public String convertToPresentation(final LocalTime value, final Class<? extends String> targetType,
						final Locale locale) throws ConversionException {
					if (value == null)
						return "";
					return TIME_PATTERN.format(value);
				}

				@Override
				public Class<LocalTime> getModelType() {
					return LocalTime.class;
				}

				@Override
				public Class<String> getPresentationType() {
					return String.class;
				}
			});
			textField.setWidth("4em");
			return (T) textField;
		}

		return super.createField(type, fieldType);
	}

}

package ch.bergturbenthal.marathontabelle.web.binding;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.joda.time.Duration;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import com.vaadin.data.Validator;
import com.vaadin.data.fieldgroup.DefaultFieldGroupFieldFactory;
import com.vaadin.data.util.converter.Converter;
import com.vaadin.ui.Field;
import com.vaadin.ui.TextField;

public class DurationFieldFactory extends DefaultFieldGroupFieldFactory {
	private static final Pattern DURATION_REGEX_PATTERN = Pattern.compile("([0-9]+):([0-5][0-9])");
	private static final DateTimeFormatter DURATION_PATTERN = DateTimeFormat.forPattern("mm:ss");
	private static final DateTimeFormatter TIME_PATTERN = DateTimeFormat.forPattern("HH:mm").withZoneUTC();

	@Override
	public <T extends Field> T createField(Class<?> type, Class<T> fieldType) {

		if (type.isAssignableFrom(Duration.class)) {
			TextField textField = new TextField();
			textField.setConverter(new Converter<String, Duration>() {

				@Override
				public Duration convertToModel(String value, Class<? extends Duration> targetType, Locale locale) throws ConversionException {
					if (value == null || value.trim().length() == 0)
						return null;
					Matcher matcher = DURATION_REGEX_PATTERN.matcher(value);
					if (!matcher.matches())
						throw new Validator.InvalidValueException("Dauer muss als mm:ss eingegeben werden");
					return Duration.standardSeconds(Integer.parseInt(matcher.group(1)) * 60 + Integer.parseInt(matcher.group(2)));
				}

				@Override
				public String convertToPresentation(Duration value, Class<? extends String> targetType, Locale locale) throws ConversionException {
					if (value == null)
						return "";
					return DURATION_PATTERN.print(value.getMillis());
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
			return (T) textField;
		}
		if (type.isAssignableFrom(LocalTime.class)) {
			TextField textField = new TextField();
			textField.setConverter(new Converter<String, LocalTime>() {

				@Override
				public Class<String> getPresentationType() {
					return String.class;
				}

				@Override
				public LocalTime convertToModel(String value, Class<? extends LocalTime> targetType, Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
					if (value == null || value.trim().length() == 0)
						return null;
					try {
						return LocalTime.parse(value, TIME_PATTERN);
					} catch (IllegalArgumentException ex) {
						throw new Validator.InvalidValueException("Uhrzeit muss als hh:mm eingegeben werden");
					}
				}

				@Override
				public String convertToPresentation(LocalTime value, Class<? extends String> targetType, Locale locale) throws com.vaadin.data.util.converter.Converter.ConversionException {
					if (value == null)
						return "";
					return TIME_PATTERN.print(value.getMillisOfDay());
				}

				@Override
				public Class<LocalTime> getModelType() {
					return LocalTime.class;
				}
			});
			return (T) textField;
		}

		return super.createField(type, fieldType);
	}

}

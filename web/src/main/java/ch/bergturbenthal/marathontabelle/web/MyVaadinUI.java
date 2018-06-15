package ch.bergturbenthal.marathontabelle.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.time.Duration;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.concurrent.Callable;
import java.util.concurrent.atomic.AtomicReference;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Binder;
import com.vaadin.data.ValidationException;
import com.vaadin.data.provider.DataProvider;
import com.vaadin.data.provider.ListDataProvider;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Grid;
import com.vaadin.ui.Grid.Column;
import com.vaadin.ui.HorizontalLayout;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.Upload;
import com.vaadin.ui.Upload.Receiver;
import com.vaadin.ui.VerticalLayout;
import com.vaadin.ui.components.grid.Editor;
import com.vaadin.ui.components.grid.GridRowDragger;
import com.vaadin.ui.themes.ValoTheme;

import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.DriverData;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCategory;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCompetition;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;
import ch.bergturbenthal.marathontabelle.web.binding.LocalTimeField;
import ch.bergturbenthal.marathontabelle.web.binding.NumberField;
import ch.bergturbenthal.marathontabelle.web.store.Storage;

@SpringUI
@Theme(ValoTheme.THEME_NAME)
@SuppressWarnings("serial")
public class MyVaadinUI extends UI {
	private static class BinderCollection<V> implements DataBinder<V> {

		private final List<DataBinder<V>> binders = new ArrayList<MyVaadinUI.DataBinder<V>>();
		private V currentData;

		public void appendBinder(final DataBinder<V> newBinder) {
			binders.add(newBinder);
			if (currentData != null)
				newBinder.bindData(currentData);
		}

		@Override
		public void bindData(final V data) {
			this.currentData = data;
			for (final DataBinder<V> binder : binders) {
				binder.bindData(data);

			}
		}

		@Override
		public void commitHandler(final V data) {
			for (final DataBinder<V> binder : binders) {
				binder.commitHandler(data);
			}
		}

	}

	private static interface DataBinder<V> {
		void bindData(final V data);

		void commitHandler(V data);

	}

	// @WebServlet(value = "/*", asyncSupported = true)
	// @VaadinServletConfiguration(productionMode = false, ui =
	// MyVaadinUI.class, widgetset =
	// "ch.bergturbenthal.marathontabelle.web.AppWidgetSet")
	// public static class Servlet extends VaadinServlet {
	// }

	// private static interface ShowPdfHandler {
	// void showPdf(final String driver);
	// }

	private static final DateTimeFormatter TIME_PATTERN = DateTimeFormatter.ofPattern("HH:mm");

	private final Storage storage;

	@Autowired
	public MyVaadinUI(final Storage storage) {
		this.storage = storage;
	}

	private void addSmallSheetColumn(final Grid<DriverData> table, final String caption, final Phase phase) {
		final Column<DriverData, Boolean> smallSheetColumn = table.addColumn(v -> v.getSmallSheets().contains(phase),
				b -> b ? "x" : "");
		smallSheetColumn.setCaption(caption);
		// smallSheetColumn.setEditable(true);
		final CheckBox smallSheetCheckbox = new CheckBox();
		smallSheetColumn.setEditorComponent(smallSheetCheckbox, (bean, fieldvalue) -> {
			if (fieldvalue != null && fieldvalue.booleanValue()) {
				bean.getSmallSheets().add(phase);
			} else
				bean.getSmallSheets().remove(phase);
		});
	}

	private void addStartTimeColumn(final Grid<DriverData> table, final String caption, final Phase phase) {
		final Column<DriverData, LocalTime> startTimeColumn = table.addColumn(d -> {
			return d.getStartTimes().get(phase);
		});
		startTimeColumn.setCaption(caption);
		// startTimeColumn.setEditable(true);
		startTimeColumn.setEditorComponent(new LocalTimeField(""),
				(bean, value) -> bean.getStartTimes().put(phase, value));
	}

	private <T> T defaultIfNull(final T value, final T defValue) {
		if (value == null)
			return defValue;
		return value;
	}

	private Object emptyIfNull(final Object value) {
		if (value == null)
			return null;
		return value;
	}

	private String formatDuration(final Duration duration) {
		if (duration == null)
			return "";
		final StringBuilder sb = new StringBuilder();
		final long minutes = duration.getSeconds() / 60;
		sb.append(Long.toString(minutes));
		sb.append(":");
		final long seconds = duration.getSeconds() % 60;
		if (seconds < 10)
			sb.append("0");
		sb.append(Long.toString(seconds));
		return sb.toString();
	}

	@Override
	protected void init(final VaadinRequest request) {
		final TabSheet tabLayout = new TabSheet();
		final VerticalLayout layout = new VerticalLayout();
		final Collection<String> marathonNames = new ArrayList<>();
		final ListDataProvider<String> marathonListContainer = DataProvider.ofCollection(marathonNames);
		final Runnable refreshAvailableCompetitions = () -> {
			marathonNames.clear();
			marathonNames.addAll(storage.listMarathons());
			marathonListContainer.refreshAll();
		};
		refreshAvailableCompetitions.run();
		final ComboBox<String> selectCompetionComboBox = new ComboBox<>("Veranstaltung wählen");
		selectCompetionComboBox.setDataProvider(marathonListContainer);
		selectCompetionComboBox.setTextInputAllowed(false);
		selectCompetionComboBox.setEmptySelectionAllowed(false);
		// final TextField competitionNameTextField = new TextField("Name der
		// Veranstaltung");

		layout.addComponent(new HorizontalLayout(selectCompetionComboBox
		// , competitionNameTextField
		));
		layout.addComponent(tabLayout);
		setContent(layout);

		final BinderCollection<MarathonData> binders = new BinderCollection<MarathonData>();

		binders.appendBinder(new DataBinder<MarathonData>() {

			@Override
			public void bindData(final MarathonData data) {
				refreshAvailableCompetitions.run();
				selectCompetionComboBox.setValue(data.getMarathonName());
				// competitionNameTextField.setValue(data.getMarathonName());
			}

			@Override
			public void commitHandler(final MarathonData data) {
				// data.setMarathonName(competitionNameTextField.getValue());
			}

		});

		final FormLayout competitionParameters = new FormLayout();
		binders.appendBinder(showOverviewData(competitionParameters));
		competitionParameters.setSizeFull();
		tabLayout.addTab(competitionParameters, "Übersicht");

		final FormLayout phaseATabContent = new FormLayout();
		binders.appendBinder(showPhaseData(phaseATabContent, Phase.A));
		phaseATabContent.setSizeFull();
		tabLayout.addTab(phaseATabContent, "Phase A");

		final FormLayout phaseDTabContent = new FormLayout();
		binders.appendBinder(showPhaseData(phaseDTabContent, Phase.TRANSFER));
		phaseDTabContent.setSizeFull();
		tabLayout.addTab(phaseDTabContent, "Transfer");

		final FormLayout phaseETabContent = new FormLayout();
		binders.appendBinder(showPhaseData(phaseETabContent, Phase.B));
		phaseETabContent.setSizeFull();
		tabLayout.addTab(phaseETabContent, "Phase B");

		final VerticalLayout outputLayout = new VerticalLayout();
		final FormLayout outputParameters = new FormLayout();

		final Collection<String> driverDataSource = new ArrayList<>();
		final ListDataProvider<String> driverNamesDataSet = DataProvider.ofCollection(driverDataSource);
		final ComboBox<String> selectDriverCombo = new ComboBox<>("Fahrer");
		selectDriverCombo.setDataProvider(driverNamesDataSet);
		selectDriverCombo.setTextInputAllowed(false);
		outputLayout.addComponent(selectDriverCombo);
		binders.appendBinder(new DataBinder<MarathonData>() {

			@Override
			public void bindData(final MarathonData data) {
				driverDataSource.clear();
				driverDataSource.addAll(data.getDrivers().keySet());
				driverNamesDataSet.refreshAll();
			}

			@Override
			public void commitHandler(final MarathonData data) {
			}

		});

		// new Button("Erstelle PDF");

		outputLayout.setSizeFull();
		outputLayout.addComponent(outputParameters);
		outputLayout.setExpandRatio(outputParameters, 0);

		final MarathonData marathonData = loadMarathonData("default");
		binders.bindData(marathonData);

		final StreamResource source = new StreamResource(new StreamSource() {

			@Override
			public InputStream getStream() {
				final MarathonData data = new MarathonData();
				binders.commitHandler(data);
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				new GeneratePdf().withCurrentDirectory(storage.getBlobRoot()).makePdf(os, data,
						selectDriverCombo.getValue());
				return new ByteArrayInputStream(os.toByteArray());
			}
		}, makeOutputFilename());
		source.setMIMEType("application/pdf");
		final BrowserFrame pdf = new BrowserFrame("Output", source);
		pdf.setSizeFull();
		outputLayout.addComponent(pdf);
		outputLayout.setExpandRatio(pdf, 1);
		tabLayout.addTab(outputLayout, "Resultat");
		tabLayout.setSizeFull();
		layout.setExpandRatio(tabLayout, 1);

		layout.setSizeFull();

		selectCompetionComboBox.addValueChangeListener(event -> {
			final MarathonData loadedMarathonData = loadMarathonData(selectCompetionComboBox.getValue());
			binders.bindData(loadedMarathonData);
		});

		selectDriverCombo.addValueChangeListener(event -> {
			final MarathonData data = new MarathonData();
			binders.commitHandler(data);
			saveMarathonData(data);
			source.setFilename(makeOutputFilename());
			pdf.markAsDirty();
		});

		final Button saveButton = new Button("Übernehmen", new Button.ClickListener() {
			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final MarathonData data = new MarathonData();
				binders.commitHandler(data);
				saveMarathonData(data);
				source.setFilename(makeOutputFilename());
				pdf.markAsDirty();
			}
		});
		layout.addComponent(saveButton);
	}

	private MarathonData loadMarathonData(final String name) {

		final MarathonData marathon = storage.getMarathon(name);
		if (marathon.getCompetitionPhases().isEmpty()) {
			final PhaseDataCompetition phaseA = new PhaseDataCompetition();
			phaseA.setPhaseName("Phase A");
			marathon.getCompetitionPhases().put(Phase.A, phaseA);
			final PhaseDataCompetition phaseD = new PhaseDataCompetition();
			phaseD.setPhaseName("Transfer");
			marathon.getCompetitionPhases().put(Phase.TRANSFER, phaseD);
			final PhaseDataCompetition phaseE = new PhaseDataCompetition();
			phaseE.setPhaseName("Phase B");
			marathon.getCompetitionPhases().put(Phase.B, phaseE);
		}
		storage.saveMaraton(marathon);
		return marathon;
	}

	private String makeOutputFilename() {
		return "marathon-" + System.currentTimeMillis() + ".pdf";
	}

	private Duration parseDuration(final String value) {
		if (value == null || value.isEmpty())
			return null;
		final String[] parts = value.split(":", 2);
		long secondCount;
		if (parts.length == 1) {
			secondCount = Long.parseLong(parts[0]);
		} else
			secondCount = Long.parseLong(parts[0]) * 60 + Long.parseLong(parts[1]);
		return Duration.ofSeconds(secondCount);
	}

	private void saveMarathonData(final MarathonData data) {
		storage.callInTransaction(new Callable<Void>() {

			@Override
			public Void call() throws Exception {
				storage.saveMaraton(data);
				return null;
			}
		});
	}

	private DataBinder<MarathonData> showOverviewData(final FormLayout layout) {
		layout.setMargin(true);
		final Binder<MarathonData> binder = new Binder<>();
		final TextField competitionField = new TextField("Veranstaltung");
		binder.bind(competitionField, MarathonData::getMarathonName, MarathonData::setMarathonName);
		layout.addComponent(competitionField);
		final List<String> categoryList = new ArrayList<>();
		final ListDataProvider<String> categoryListDataProvider = DataProvider.ofCollection(categoryList);

		final ListSelect<String> categoryListSelect = new ListSelect<>("Kategorieen");
		categoryListSelect.setDataProvider(categoryListDataProvider);
		layout.addComponent(categoryListSelect);
		final Button removeItemButton = new Button("Kategorie löschen");
		layout.addComponent(removeItemButton);
		removeItemButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				categoryList.remove(categoryListSelect.getValue());
				categoryListDataProvider.refreshAll();
			}
		});
		// final ListSelect categoryList = new ListSelect("Kategorieen");
		// layout.addComponent(categoryList);

		final List<DriverData> driverList = new ArrayList<>();

		final ListDataProvider<DriverData> driverListDataProvider = DataProvider.ofCollection(driverList);
		final Grid<DriverData> table = new Grid<DriverData>("Fahrer", driverListDataProvider);
		final Column<DriverData, String> nameColumn = table.addColumn(v -> v.getName());
		nameColumn.setEditorComponent(new TextField(), DriverData::setName);
		nameColumn.setCaption("Fahrer");
		final Column<DriverData, String> categoryColumn = table.addColumn(v -> v.getCategory());
		categoryColumn.setCaption("Kategorie");
		final ComboBox<String> columnEditor = new ComboBox<>();
		columnEditor.setDataProvider(categoryListDataProvider);
		columnEditor.setEmptySelectionAllowed(false);
		categoryColumn.setEditorComponent(columnEditor, DriverData::setCategory);

		addSmallSheetColumn(table, "Phase A Zettel", Phase.A);
		addSmallSheetColumn(table, "Transfer Zettel", Phase.TRANSFER);
		addSmallSheetColumn(table, "Phase B Zettel", Phase.B);

		addStartTimeColumn(table, "Phase A Start", Phase.A);
		addStartTimeColumn(table, "Transfer Start", Phase.TRANSFER);
		addStartTimeColumn(table, "Phase B Start", Phase.B);

		table.addComponentColumn(driverData -> {
			return new Button("-", clickEvent -> {
				driverList.remove(driverData);
				driverListDataProvider.refreshAll();
			});
		});
		table.setWidth(100, Unit.PERCENTAGE);

		final Editor<DriverData> editor = table.getEditor();
		editor.setEnabled(true);
		editor.addSaveListener(saveEvent -> {
			driverListDataProvider.refreshAll();
		});
		// table.setNullSelectionAllowed(true);
		layout.addComponent(table);
		final Button addDriverButton = new Button("Neuer Fahrer");
		addDriverButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final DriverData driver = new DriverData();
				driver.setName("Fahrer - " + driverList.size());
				driverList.add(driver);
				driverListDataProvider.refreshAll();
			}
		});
		layout.addComponent(addDriverButton);

		return new DataBinder<MarathonData>() {

			@Override
			public void bindData(final MarathonData data) {
				binder.readBean(data);

				categoryList.clear();
				categoryList.addAll(data.getCategories());
				categoryListDataProvider.refreshAll();

				driverList.clear();
				driverList.addAll(data.getDrivers().values());
				driverListDataProvider.refreshAll();
			}

			@Override
			public void commitHandler(final MarathonData data) {
				try {
					binder.writeBean(data);
					final List<String> categories = data.getCategories();
					categories.clear();
					categories.addAll(categoryList);
					final Map<String, DriverData> drivers = data.getDrivers();
					drivers.clear();
					for (final DriverData driver : driverList) {
						drivers.put(driver.getName(), driver);
					}
				} catch (final ValidationException e) {
					throw new RuntimeException("Cannot commit", e);
				}
			}

		};
	}

	private DataBinder<MarathonData> showPhaseData(final FormLayout layout, final Phase phase) {
		layout.setMargin(true);
		final Binder<PhaseDataCompetition> binder = new Binder<PhaseDataCompetition>(PhaseDataCompetition.class);
		// final DurationFieldFactory fieldFactory = new DurationFieldFactory();
		// binder.setFieldFactory(fieldFactory);

		// layout.addComponent(binder.buildAndBind("geplante Startzeit",
		// "startTime"));
		// layout.addComponent(binder.buildAndBind("Maximale Zeit", "maxTime"));
		// layout.addComponent(binder.buildAndBind("Minimale Zeit", "minTime"));
		final TextField phaseNameField = new TextField("Name der Phase");
		binder.bind(phaseNameField, p -> p.getPhaseName(), (p, v) -> p.setPhaseName(v));
		layout.addComponent(phaseNameField);

		final TextField lengthField = new TextField("Länge in m");
		binder.bind(lengthField, p -> {
			final Integer length = p.getLength();
			if (length == null) {
				return null;
			} else
				return Integer.toString(length);
		}, (p, v) -> {
			p.setLength(Integer.parseInt(v));
		});
		layout.addComponent(lengthField);

		final Upload fileUpload = new Upload();
		fileUpload.setCaption("Bild");
		layout.addComponent(fileUpload);
		final Button removeImageButton = new Button("Bild löschen");
		layout.addComponent(removeImageButton);
		// layout.addComponent(binder.buildAndBind("Geschwindigkeit im m/s",
		// "velocity"));
		// layout.addComponent(binder.buildAndBind("Tabelle", "entries"));

		final List<Entry<String, PhaseDataCategory>> categoryTimesList = new ArrayList<>();

		final ListDataProvider<Entry<String, PhaseDataCategory>> categoryTimesProvider = DataProvider
				.ofCollection(categoryTimesList);

		final Grid<Entry<String, PhaseDataCategory>> timesTable = new Grid<>("Zeiten", categoryTimesProvider);
		timesTable.setHeight("8em");
		timesTable.setWidth(100, Unit.PERCENTAGE);

		final Column<Entry<String, PhaseDataCategory>, String> categoryColumn = timesTable.addColumn(e -> e.getKey());
		categoryColumn.setCaption("Kategorie");

		final Column<Entry<String, PhaseDataCategory>, String> minTimeColumn = timesTable
				.addColumn(e -> formatDuration(e.getValue().getMinTime()));
		minTimeColumn.setCaption("Min. Zeit");
		minTimeColumn.setEditorComponent(new TextField(), (b, v) -> b.getValue().setMinTime(parseDuration(v)));

		final Column<Entry<String, PhaseDataCategory>, String> maxTimeColumn = timesTable
				.addColumn(e -> formatDuration(e.getValue().getMaxTime()));
		maxTimeColumn.setCaption("Max. Zeit");
		maxTimeColumn.setEditorComponent(new TextField(), (b, v) -> b.getValue().setMaxTime(parseDuration(v)));

		final NumberFormat velocityPattern = new DecimalFormat("00");

		final Column<Entry<String, PhaseDataCategory>, String> velocityColumn = timesTable
				.addColumn(e -> velocityPattern.format(defaultIfNull(e.getValue().getVelocity(), 0)));
		velocityColumn.setCaption("Geschw.");
		velocityColumn.setEditorComponent(new TextField(), (b, v) -> {
			try {
				b.getValue().setVelocity(velocityPattern.parse(v).doubleValue());
			} catch (final ParseException e1) {
				b.getValue().setVelocity(null);
			}
		});

		final Editor<Entry<String, PhaseDataCategory>> timesEditor = timesTable.getEditor();
		timesEditor.addSaveListener(saveEvent -> categoryTimesProvider.refreshAll());
		timesEditor.setEnabled(true);
		layout.addComponent(timesTable);

		final List<TimeEntry> timeEntryList = new ArrayList<>();
		final ListDataProvider<TimeEntry> timeEntryProvider = DataProvider.ofCollection(timeEntryList);

		final Grid<TimeEntry> table = new Grid<>("Strecke", timeEntryProvider);

		final Column<TimeEntry, Double> positionColumn = table.addColumn(e -> toDouble(e.getPosition()), n -> n + " m");
		positionColumn.setCaption("Position");
		positionColumn.setEditorComponent(new NumberField("", DecimalFormat.getIntegerInstance()),
				(b, n) -> b.setPosition(toInteger(n)));

		final Column<TimeEntry, String> commentColumn = table.addColumn(e -> e.getComment());
		commentColumn.setCaption("Text");
		commentColumn.setEditorComponent(new TextField(), TimeEntry::setComment);

		final Column<TimeEntry, Boolean> onlySmallSheetColumn = table.addColumn(e -> e.isOnlySmallSheet(),
				v -> v ? "x" : "");
		onlySmallSheetColumn.setEditorComponent(new CheckBox(), TimeEntry::setOnlySmallSheet);

		final Editor<TimeEntry> tableEditor = table.getEditor();
		tableEditor.addSaveListener(saveEvent -> timeEntryProvider.refreshAll());
		tableEditor.setEnabled(true);
		table.setWidth(100, Unit.PERCENTAGE);
		layout.addComponent(table);

		final GridRowDragger<TimeEntry> gridRowDragger = new GridRowDragger<>(table);
		table.getColumns().stream().forEach(col -> col.setSortable(false));

		table.addComponentColumn(entry -> {
			return new Button("Löschen", clickEvent -> {
				timeEntryList.remove(entry);
				timeEntryProvider.refreshAll();
			});
		});

		layout.addComponent(new Button("Neuer Steckenpunkt", new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				timeEntryList.add(new TimeEntry());
				timeEntryProvider.refreshAll();
			}
		}));
		final AtomicReference<PhaseDataCompetition> currentPhaseDataReference = new AtomicReference<PhaseDataCompetition>(
				null);
		removeImageButton.addClickListener(new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final PhaseDataCompetition currentData = currentPhaseDataReference.get();
				if (currentData == null)
					return;
				currentData.setImageName(null);
				removeImageButton.setEnabled(false);
			}
		});
		final AtomicReference<MarathonData> dataReference = new AtomicReference<MarathonData>();
		final DataBinder<MarathonData> phaseDataBinder = new DataBinder<MarathonData>() {

			@Override
			public void bindData(final MarathonData phaseData) {
				dataReference.set(phaseData);
				final PhaseDataCompetition dataCompetition = phaseData.getCompetitionPhases().computeIfAbsent(phase,
						k -> new PhaseDataCompetition());
				currentPhaseDataReference.set(dataCompetition);
				binder.readBean(dataCompetition);
				timeEntryList.clear();
				timeEntryList.addAll(dataCompetition.getEntries());
				timeEntryProvider.refreshAll();

				categoryTimesList.clear();
				final Map<String, PhaseDataCategory> categoryTimes = new HashMap<>(dataCompetition.getCategoryTimes());
				for (final String category : phaseData.getCategories()) {
					categoryTimes.computeIfAbsent(category, k -> new PhaseDataCategory());
					if (!categoryTimes.containsKey(category)) {
						categoryTimes.put(category, new PhaseDataCategory());
					}
					categoryTimesList.add(new Entry<String, PhaseDataCategory>() {

						@Override
						public String getKey() {
							return category;
						}

						@Override
						public PhaseDataCategory getValue() {
							return categoryTimes.get(category);
						}

						@Override
						public PhaseDataCategory setValue(final PhaseDataCategory value) {
							return categoryTimes.put(category, value);
						}
					});
				}
				categoryTimesProvider.refreshAll();
				fileUpload.setReceiver(new Receiver() {

					@Override
					public OutputStream receiveUpload(final String filename, final String mimeType) {
						final File storeFile = storage.createStoreFile(filename);
						dataCompetition.setImageName(storeFile.getName());
						removeImageButton.setEnabled(true);
						try {
							return new FileOutputStream(storeFile);
						} catch (final FileNotFoundException e) {
							throw new RuntimeException("Cannot write image to " + storeFile, e);
						}
					}
				});
				removeImageButton.setEnabled(dataCompetition.getImageName() != null);
			}

			@Override
			public void commitHandler(final MarathonData data) {
				try {
					final PhaseDataCompetition dataCompetition = data.getCompetitionPhases().computeIfAbsent(phase,
							k -> new PhaseDataCompetition());
					binder.writeBean(dataCompetition);
					dataCompetition.getEntries().clear();
					dataCompetition.getEntries().addAll(timeEntryList);
					dataCompetition.getCategoryTimes().clear();
					for (final Entry<String, PhaseDataCategory> entry : categoryTimesList) {
						dataCompetition.getCategoryTimes().put(entry.getKey(), entry.getValue());
					}
				} catch (final ValidationException e) {
					throw new RuntimeException("Cannot commit", e);
				}
			}

		};
		layout.addComponent(new Button("Reset Strecke", new Button.ClickListener() {

			@Override
			public void buttonClick(final Button.ClickEvent event) {
				final MarathonData marathonData = dataReference.get();
				final PhaseDataCompetition phaseData = marathonData.getCompetitionPhases().get(phase);
				phaseData.setDefaultPoints();

				timeEntryList.clear();
				timeEntryList.addAll(phaseData.getEntries());
				timeEntryProvider.refreshAll();
			}
		}));
		return phaseDataBinder;
	}

	private Double toDouble(final Number v) {
		if (v == null)
			return null;
		return v.doubleValue();
	}

	private Integer toInteger(final Number n) {
		if (n == null)
			return null;
		return n.intValue();
	}
}

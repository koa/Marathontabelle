package ch.bergturbenthal.marathontabelle.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.concurrent.Callable;

import org.springframework.beans.factory.annotation.Autowired;

import com.vaadin.annotations.Theme;
import com.vaadin.data.Container;
import com.vaadin.data.Item;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.IndexedContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.spring.annotation.SpringUI;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.BrowserFrame;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.CheckBox;
import com.vaadin.ui.ComboBox;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Label;
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.DriverData;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCategory;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCompetition;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;
import ch.bergturbenthal.marathontabelle.web.binding.DurationFieldFactory;
import ch.bergturbenthal.marathontabelle.web.store.Storage;

@SpringUI
@Theme("mytheme")
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
		public void commitHandler() {
			for (final DataBinder<V> binder : binders) {
				binder.commitHandler();
			}
		}

		@Override
		public V getCurrentData() {
			return currentData;
		}

	}

	private static interface DataBinder<V> {
		void bindData(final V data);

		void commitHandler();

		V getCurrentData();
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

	private final Storage storage;

	@Autowired
	public MyVaadinUI(final Storage storage) {
		this.storage = storage;
	}

	private void container2Model(final BeanItemContainer<TimeEntry> itemContainer,
			final PhaseDataCompetition phaseData) {
		final List<TimeEntry> entries = phaseData.getEntries();
		entries.clear();
		entries.addAll(itemContainer.getItemIds());
	}

	private ColumnGenerator createPhaseCheckbox(final Phase phase) {
		return new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				final Set<Phase> smallSheets = ((DriverData) itemId).getSmallSheets();
				final CheckBox checkBox = new CheckBox();
				final ObjectProperty<Boolean> dataSource = new ObjectProperty<Boolean>(
						Boolean.valueOf(smallSheets.contains(phase)));
				dataSource.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						if (dataSource.getValue().booleanValue()) {
							smallSheets.add(phase);
						} else {
							smallSheets.remove(phase);
						}
					}
				});
				checkBox.setPropertyDataSource(dataSource);
				return checkBox;
			}
		};
	}

	private ColumnGenerator createPhaseStartInput(final DurationFieldFactory fieldFactory, final Phase phase) {
		return new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				final DriverData driverData = (DriverData) itemId;
				final TextField field = fieldFactory.createField(LocalTime.class, TextField.class);
				final ObjectProperty<LocalTime> property = new ObjectProperty<LocalTime>(
						defaultIfNull(driverData.getStartTimes().get(phase), LocalTime.of(0, 0)));
				field.setPropertyDataSource(property);
				property.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						driverData.getStartTimes().put(phase, property.getValue());
					}
				});
				return field;
			}
		};
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

	@Override
	protected void init(final VaadinRequest request) {
		final TabSheet tabLayout = new TabSheet();
		final VerticalLayout layout = new VerticalLayout();
		final IndexedContainer marathonListContainer = new IndexedContainer();
		marathonListContainer.addContainerProperty("name", String.class, null);
		final Runnable refreshAvailableCompetitions = () -> {
			final Set<Object> remainingItems = new HashSet<>(marathonListContainer.getItemIds());
			for (final String marathonName : storage.listMarathons()) {

				final Item item;
				if (remainingItems.remove(marathonName))
					item = marathonListContainer.getItem(marathonName);
				else
					item = marathonListContainer.addItem(marathonName);
				item.getItemProperty("name").setValue(marathonName);

			}
			for (final Object itemId : remainingItems) {
				marathonListContainer.removeItem(itemId);
			}
		};
		refreshAvailableCompetitions.run();
		final ComboBox selectCompetionComboBox = new ComboBox("Veranstaltung wählen", marathonListContainer);
		layout.addComponent(selectCompetionComboBox);
		selectCompetionComboBox.setTextInputAllowed(false);
		selectCompetionComboBox.setNullSelectionAllowed(false);
		layout.addComponent(tabLayout);
		setContent(layout);

		final BinderCollection<MarathonData> binders = new BinderCollection<MarathonData>();

		binders.commitHandler();

		final FormLayout competitionParameters = new FormLayout();
		binders.appendBinder(showOverviewData(competitionParameters));
		tabLayout.addTab(competitionParameters, "Übersicht");

		final FormLayout phaseATabContent = new FormLayout();
		binders.appendBinder(showPhaseData(phaseATabContent, Phase.A));
		tabLayout.addTab(phaseATabContent, "Phase A");

		final FormLayout phaseDTabContent = new FormLayout();
		binders.appendBinder(showPhaseData(phaseDTabContent, Phase.TRANSFER));
		tabLayout.addTab(phaseDTabContent, "Transfer");

		final FormLayout phaseETabContent = new FormLayout();
		binders.appendBinder(showPhaseData(phaseETabContent, Phase.B));
		tabLayout.addTab(phaseETabContent, "Phase B");

		final VerticalLayout outputLayout = new VerticalLayout();
		final FormLayout outputParameters = new FormLayout();

		final BeanItemContainer<String> driverDataSource = new BeanItemContainer<String>(String.class);
		final ComboBox selectDriverCombo = new ComboBox("Fahrer", driverDataSource);
		selectDriverCombo.setNewItemsAllowed(false);
		selectDriverCombo.setImmediate(true);
		outputLayout.addComponent(selectDriverCombo);
		binders.appendBinder(new DataBinder<MarathonData>() {

			private MarathonData data;

			@Override
			public void bindData(final MarathonData data) {
				this.data = data;
				driverDataSource.removeAllItems();
				driverDataSource.addAll(data.getDrivers().keySet());
			}

			@Override
			public void commitHandler() {
			}

			@Override
			public MarathonData getCurrentData() {
				return data;
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
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				new GeneratePdf().makePdf(os, binders.getCurrentData(), (String) selectDriverCombo.getValue());
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

		selectCompetionComboBox.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				final MarathonData marathonData = loadMarathonData((String) selectCompetionComboBox.getValue());
				binders.bindData(marathonData);
			}
		});

		selectDriverCombo.addValueChangeListener(new ValueChangeListener() {

			@Override
			public void valueChange(final ValueChangeEvent event) {
				binders.commitHandler();
				saveMarathonData(binders.getCurrentData());
				source.setFilename(makeOutputFilename());
				pdf.markAsDirty();
			}
		});

		final Button saveButton = new Button("Übernehmen", new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				binders.commitHandler();
				saveMarathonData(binders.getCurrentData());
				source.setFilename(makeOutputFilename());
				pdf.markAsDirty();
				refreshAvailableCompetitions.run();
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

	private void model2Container(final PhaseDataCompetition phaseData,
			final BeanItemContainer<TimeEntry> itemContainer) {
		itemContainer.removeAllItems();
		itemContainer.addAll(phaseData.getEntries());
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
		final BeanFieldGroup<MarathonData> binder = new BeanFieldGroup<MarathonData>(MarathonData.class);
		layout.addComponent(binder.buildAndBind("Veranstaltung", "marathonName"));
		final BeanItemContainer<String> categoryListContainer = new BeanItemContainer<String>(String.class);

		final ListSelect categoryListSelect = new ListSelect("Kategorieen", categoryListContainer);
		categoryListSelect.setNewItemsAllowed(true);
		categoryListSelect.setNullSelectionAllowed(false);
		categoryListSelect.setMultiSelect(false);
		layout.addComponent(categoryListSelect);
		final Button removeItemButton = new Button("Kategorie löschen");
		layout.addComponent(removeItemButton);
		removeItemButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				categoryListContainer.removeItem(categoryListSelect.getValue());
			}
		});
		// final ListSelect categoryList = new ListSelect("Kategorieen");
		// layout.addComponent(categoryList);

		final BeanItemContainer<DriverData> driverContainer = new BeanItemContainer<DriverData>(DriverData.class);

		final Table table = new Table("Fahrer");
		table.setContainerDataSource(driverContainer);
		table.removeContainerProperty("smallSheets");
		table.removeContainerProperty("startTimes");
		table.removeContainerProperty("category");
		table.addGeneratedColumn("Kategorie", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				final ComboBox comboBox = new ComboBox();
				comboBox.setContainerDataSource(categoryListContainer);
				final DriverData driverData = (DriverData) itemId;
				final String category = driverData.getCategory() == null ? "" : driverData.getCategory();

				final ObjectProperty<String> property = new ObjectProperty<String>(category);
				comboBox.setPropertyDataSource(property);
				property.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						driverData.setCategory(property.getValue());
					}
				});
				return comboBox;
			}
		});
		table.addGeneratedColumn("Generieren", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				final DriverData driverData = (DriverData) itemId;
				final Button button = new Button("PDF");
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						// showPdfHandler.showPdf(driverData.getName());
					}
				});
				return button;
			}
		});
		table.addGeneratedColumn("Phase A Zettel", createPhaseCheckbox(Phase.A));
		table.addGeneratedColumn("Transfer Zettel", createPhaseCheckbox(Phase.TRANSFER));
		table.addGeneratedColumn("Phase B Zettel", createPhaseCheckbox(Phase.B));
		final DurationFieldFactory fieldFactory = new DurationFieldFactory();
		table.addGeneratedColumn("Phase A Start", createPhaseStartInput(fieldFactory, Phase.A));
		table.addGeneratedColumn("Transfer Start", createPhaseStartInput(fieldFactory, Phase.TRANSFER));
		table.addGeneratedColumn("Phase D Start", createPhaseStartInput(fieldFactory, Phase.B));
		table.addGeneratedColumn("Fahrer Löschen", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				final Button button = new Button("-");
				button.addClickListener(new ClickListener() {

					@Override
					public void buttonClick(final ClickEvent event) {
						driverContainer.removeItem(itemId);
					}
				});
				return button;
			}
		});

		table.setEditable(true);
		table.setSortEnabled(false);
		// table.setNullSelectionAllowed(true);
		layout.addComponent(table);
		final Button addDriverButton = new Button("Neuer Fahrer");
		addDriverButton.addClickListener(new ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				final DriverData driver = new DriverData();
				driver.setName("Fahrer - " + driverContainer.size());
				driverContainer.addBean(driver);
			}
		});
		layout.addComponent(addDriverButton);

		return new DataBinder<MarathonData>() {

			private MarathonData data;

			@Override
			public void bindData(final MarathonData data) {
				this.data = data;
				binder.setItemDataSource(data);
				categoryListContainer.removeAllItems();
				for (final String category : data.getCategories()) {
					categoryListContainer.addBean(category);
				}
				driverContainer.removeAllItems();
				for (final DriverData driver : data.getDrivers().values()) {
					driverContainer.addBean(driver);
				}
			}

			@Override
			public void commitHandler() {
				try {
					binder.commit();
					final List<String> categories = data.getCategories();
					categories.clear();
					categories.addAll(categoryListContainer.getItemIds());
					final Map<String, DriverData> drivers = data.getDrivers();
					drivers.clear();
					for (final DriverData driver : driverContainer.getItemIds()) {
						drivers.put(driver.getName(), driver);
					}
				} catch (final CommitException e) {
					throw new RuntimeException("Cannot commit", e);
				}
			}

			@Override
			public MarathonData getCurrentData() {
				return data;
			}
		};
	}

	private DataBinder<MarathonData> showPhaseData(final FormLayout layout, final Phase phase) {
		layout.setMargin(true);
		final BeanFieldGroup<PhaseDataCompetition> binder = new BeanFieldGroup<PhaseDataCompetition>(
				PhaseDataCompetition.class);
		final DurationFieldFactory fieldFactory = new DurationFieldFactory();
		binder.setFieldFactory(fieldFactory);

		// layout.addComponent(binder.buildAndBind("geplante Startzeit",
		// "startTime"));
		// layout.addComponent(binder.buildAndBind("Maximale Zeit", "maxTime"));
		// layout.addComponent(binder.buildAndBind("Minimale Zeit", "minTime"));
		layout.addComponent(binder.buildAndBind("Name der Phase", "phaseName"));
		layout.addComponent(binder.buildAndBind("Länge in m", "length"));
		// layout.addComponent(binder.buildAndBind("Geschwindigkeit im m/s",
		// "velocity"));
		// layout.addComponent(binder.buildAndBind("Tabelle", "entries"));

		final BeanItemContainer<Entry<String, PhaseDataCategory>> categoryTimesItemContainer = new BeanItemContainer<Map.Entry<String, PhaseDataCategory>>(
				Map.Entry.class);
		final Table timesTable = new Table("Zeiten");
		timesTable.setContainerDataSource(categoryTimesItemContainer);
		timesTable.setHeight("8em");
		timesTable.removeContainerProperty("key");
		timesTable.removeContainerProperty("value");
		timesTable.addGeneratedColumn("Kategorie", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				@SuppressWarnings("unchecked")
				final Entry<String, PhaseDataCategory> entry = (Entry<String, PhaseDataCategory>) itemId;
				return new Label(entry.getKey());
			}
		});
		timesTable.addGeneratedColumn("Min. Zeit", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				@SuppressWarnings("unchecked")
				final Entry<String, PhaseDataCategory> entry = (Entry<String, PhaseDataCategory>) itemId;
				final TextField field = fieldFactory.createField(Duration.class, TextField.class);
				final PhaseDataCategory phaseDataCategory = entry.getValue();
				final ObjectProperty<Duration> property = new ObjectProperty<Duration>(
						defaultIfNull(phaseDataCategory.getMinTime(), Duration.ZERO));
				field.setPropertyDataSource(property);
				property.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						phaseDataCategory.setMinTime(property.getValue());
					}
				});
				return field;
			}
		});
		timesTable.addGeneratedColumn("Max. Zeit", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				@SuppressWarnings("unchecked")
				final Entry<String, PhaseDataCategory> entry = (Entry<String, PhaseDataCategory>) itemId;
				final TextField field = fieldFactory.createField(Duration.class, TextField.class);
				final PhaseDataCategory phaseDataCategory = entry.getValue();
				final ObjectProperty<Duration> property = new ObjectProperty<Duration>(
						defaultIfNull(phaseDataCategory.getMaxTime(), Duration.ZERO));
				field.setPropertyDataSource(property);
				property.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						phaseDataCategory.setMaxTime(property.getValue());
					}
				});
				return field;
			}
		});
		timesTable.addGeneratedColumn("Geschw.", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				@SuppressWarnings("unchecked")
				final Entry<String, PhaseDataCategory> entry = (Entry<String, PhaseDataCategory>) itemId;
				final PhaseDataCategory phaseDataCategory = entry.getValue();
				phaseDataCategory.getVelocity();
				final TextField field = fieldFactory.createField(Double.class, TextField.class);
				final ObjectProperty<Double> property = new ObjectProperty<Double>(
						defaultIfNull(phaseDataCategory.getVelocity(), Double.valueOf(0)));
				field.setPropertyDataSource(property);
				property.addValueChangeListener(new ValueChangeListener() {

					@Override
					public void valueChange(final ValueChangeEvent event) {
						phaseDataCategory.setVelocity(property.getValue());
					}
				});
				return field;
			}
		});

		layout.addComponent(timesTable);

		final BeanItemContainer<TimeEntry> timeEntryItemContainer = new BeanItemContainer<TimeEntry>(TimeEntry.class);

		final Table table = new Table("Strecke");
		table.setContainerDataSource(timeEntryItemContainer);
		table.setEditable(true);
		table.setSortEnabled(false);
		table.setNullSelectionAllowed(true);
		layout.addComponent(table);
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {

			@Override
			public void drop(final DragAndDropEvent dropEvent) {
				final DataBoundTransferable t = (DataBoundTransferable) dropEvent.getTransferable();
				final TimeEntry sourceItemId = (TimeEntry) t.getItemId(); // returns
																			// our
																			// Bean

				final AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent
						.getTargetDetails());
				final TimeEntry targetItemId = (TimeEntry) dropData.getItemIdOver(); // returns
																						// our
																						// Bean

				// No move if source and target are the same, or there is no
				// target
				if (sourceItemId == targetItemId || targetItemId == null)
					return;

				// Let's remove the source of the drag so we can add it back
				// where requested...
				timeEntryItemContainer.removeItem(sourceItemId);

				if (dropData.getDropLocation() == VerticalDropLocation.BOTTOM) {
					timeEntryItemContainer.addItemAfter(targetItemId, sourceItemId);
				} else {
					final Object prevItemId = timeEntryItemContainer.prevItemId(targetItemId);
					timeEntryItemContainer.addItemAfter(prevItemId, sourceItemId);
				}
			}

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return new SourceIs(table);
			}
		});
		table.addGeneratedColumn("", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, final Object columnId) {
				final Button button = new Button("Löschen");
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(final ClickEvent event) {
						source.getContainerDataSource().removeItem(itemId);
					}
				});
				return button;
			}
		});
		final TableFieldFactory tableFieldFactory = new TableFieldFactory() {

			@Override
			public Field<?> createField(final Container container, final Object itemId, final Object propertyId,
					final Component uiContext) {
				final Class<?> type = container.getType(propertyId);
				final Field field = fieldFactory.createField(type, Field.class);
				if (field instanceof AbstractTextField) {
					final AbstractTextField abstractTextField = (AbstractTextField) field;
					abstractTextField.setNullRepresentation("");
					abstractTextField.setValidationVisible(true);
				}
				return field;
			}
		};
		table.setTableFieldFactory(tableFieldFactory);

		layout.addComponent(new Button("Neuer Steckenpunkt", new ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				timeEntryItemContainer.addBean(new TimeEntry());
			}
		}));

		final DataBinder<MarathonData> phaseDataBinder = new DataBinder<MarathonData>() {

			private MarathonData data;

			@Override
			public void bindData(final MarathonData phaseData) {
				this.data = phaseData;
				final PhaseDataCompetition dataCompetition = data.getCompetitionPhases().get(phase);
				binder.setItemDataSource(dataCompetition);
				timeEntryItemContainer.removeAllItems();
				timeEntryItemContainer.addAll(dataCompetition.getEntries());
				categoryTimesItemContainer.removeAllItems();
				final Map<String, PhaseDataCategory> categoryTimes = dataCompetition.getCategoryTimes();
				for (final String category : data.getCategories()) {
					if (!categoryTimes.containsKey(category)) {
						categoryTimes.put(category, new PhaseDataCategory());
					}
					categoryTimesItemContainer.addBean(new Entry<String, PhaseDataCategory>() {

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
			}

			@Override
			public void commitHandler() {
				try {
					binder.commit();
					final PhaseDataCompetition dataCompetition = data.getCompetitionPhases().get(phase);
					container2Model(timeEntryItemContainer, dataCompetition);
					for (final Entry<String, PhaseDataCategory> entry : categoryTimesItemContainer.getItemIds()) {
						dataCompetition.getCategoryTimes().put(entry.getKey(), entry.getValue());
					}
				} catch (final CommitException e) {
					throw new RuntimeException("Cannot commit", e);
				}
			}

			@Override
			public MarathonData getCurrentData() {
				return data;
			}
		};
		layout.addComponent(new Button("Reset Strecke", new ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				phaseDataBinder.commitHandler();

				final PhaseDataCompetition phaseData = phaseDataBinder.getCurrentData().getCompetitionPhases()
						.get(phase);
				phaseData.setDefaultPoints();

				model2Container(phaseData, timeEntryItemContainer);
				System.out.println(phaseData);
			}
		}));
		return phaseDataBinder;
	}
}

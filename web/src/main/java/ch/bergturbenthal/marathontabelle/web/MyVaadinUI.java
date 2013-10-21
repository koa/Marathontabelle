package ch.bergturbenthal.marathontabelle.web;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;

import javax.servlet.annotation.WebServlet;

import org.joda.time.LocalTime;

import ch.bergturbenthal.filestore.core.FileStorage.ReadPolicy;
import ch.bergturbenthal.marathontabelle.generator.GeneratePdf;
import ch.bergturbenthal.marathontabelle.model.DriverData;
import ch.bergturbenthal.marathontabelle.model.MarathonData;
import ch.bergturbenthal.marathontabelle.model.Phase;
import ch.bergturbenthal.marathontabelle.model.PhaseDataCompetition;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;
import ch.bergturbenthal.marathontabelle.web.binding.DurationFieldFactory;
import ch.bergturbenthal.marathontabelle.web.store.Storage;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.joda.JodaModule;
import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container;
import com.vaadin.data.Property.ValueChangeEvent;
import com.vaadin.data.Property.ValueChangeListener;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.data.util.ObjectProperty;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.server.StreamResource;
import com.vaadin.server.StreamResource.StreamSource;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
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
import com.vaadin.ui.ListSelect;
import com.vaadin.ui.TabSheet;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.TextField;
import com.vaadin.ui.UI;
import com.vaadin.ui.VerticalLayout;

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

	private static class DelegatingDataBinder implements DataBinder<MarathonData> {

		private final DataBinder<PhaseDataCompetition> binder;
		private final Phase phase;
		private MarathonData data;

		public DelegatingDataBinder(final DataBinder<PhaseDataCompetition> binder, final Phase phase) {
			this.binder = binder;
			this.phase = phase;
		}

		@Override
		public void bindData(final MarathonData data) {
			this.data = data;
			binder.bindData(data.getCompetitionPhases().get(phase));
		}

		@Override
		public void commitHandler() {
			binder.commitHandler();
		}

		@Override
		public MarathonData getCurrentData() {
			return data;
		}
	}

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "ch.bergturbenthal.marathontabelle.web.AppWidgetSet")
	public static class Servlet extends VaadinServlet {
	}

	private final Storage storage = new Storage();

	private final ObjectMapper mapper = new ObjectMapper();
	private final File file = new File(System.getProperty("user.home"), "marathon.json");

	public MyVaadinUI() {
		mapper.registerModule(new JodaModule());
	}

	private void container2Model(final BeanItemContainer<TimeEntry> itemContainer, final PhaseDataCompetition phaseData) {
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
				final ObjectProperty<Boolean> dataSource = new ObjectProperty<Boolean>(Boolean.valueOf(smallSheets.contains(phase)));
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
				final ObjectProperty<LocalTime> property = new ObjectProperty<LocalTime>(defaultIfNull(driverData.getStartTimes().get(phase), new LocalTime(0, 0)));
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
		layout.addComponent(tabLayout);
		setContent(layout);

		final BinderCollection<MarathonData> binders = new BinderCollection<MarathonData>();

		final FormLayout competitionParameters = new FormLayout();
		binders.appendBinder(showOverviewData(competitionParameters));
		tabLayout.addTab(competitionParameters, "Übersicht");

		final FormLayout phaseATabContent = new FormLayout();
		binders.appendBinder(new DelegatingDataBinder(showPhaseData(phaseATabContent, Phase.A), Phase.A));
		tabLayout.addTab(phaseATabContent, "Phase A");

		final FormLayout phaseDTabContent = new FormLayout();
		binders.appendBinder(new DelegatingDataBinder(showPhaseData(phaseDTabContent, Phase.D), Phase.D));
		tabLayout.addTab(phaseDTabContent, "Phase D");

		final FormLayout phaseETabContent = new FormLayout();
		binders.appendBinder(new DelegatingDataBinder(showPhaseData(phaseETabContent, Phase.E), Phase.E));
		tabLayout.addTab(phaseETabContent, "Phase E");

		final VerticalLayout outputLayout = new VerticalLayout();
		final FormLayout outputParameters = new FormLayout();

		final CheckBox phaseAss = new CheckBox("Phase A");
		phaseAss.setValue(Boolean.TRUE);
		outputParameters.addComponent(phaseAss);

		final CheckBox phaseDss = new CheckBox("Phase D");
		phaseDss.setValue(Boolean.FALSE);
		outputParameters.addComponent(phaseDss);

		final CheckBox phaseEss = new CheckBox("Phase E");
		phaseEss.setValue(Boolean.TRUE);
		outputParameters.addComponent(phaseEss);

		outputLayout.setSizeFull();
		outputLayout.addComponent(outputParameters);
		outputLayout.setExpandRatio(outputParameters, 0);

		final MarathonData marathonData = loadMarathonData("default");
		binders.bindData(marathonData);

		final StreamResource source = new StreamResource(new StreamSource() {

			@Override
			public InputStream getStream() {
				final ByteArrayOutputStream os = new ByteArrayOutputStream();
				new GeneratePdf().makePdf(os, binders.getCurrentData(), "nobody");
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

		final Button saveButton = new Button("Übernehmen", new ClickListener() {
			@Override
			public void buttonClick(final ClickEvent event) {
				binders.commitHandler();
				saveMarathonData(binders.getCurrentData());
				source.setFilename(makeOutputFilename());
				pdf.markAsDirty();
			}
		});
		layout.addComponent(saveButton);
	}

	private MarathonData loadMarathonData(final String name) {
		return storage.callInTransaction(new Callable<MarathonData>() {

			@Override
			public MarathonData call() throws Exception {
				final MarathonData marathon = storage.getMarathon(name, ReadPolicy.READ_OR_CREATE);
				if (marathon.getCompetitionPhases().isEmpty()) {
					final PhaseDataCompetition phaseA = new PhaseDataCompetition();
					phaseA.setPhaseName("Phase A");
					marathon.getCompetitionPhases().put(Phase.A, phaseA);
					final PhaseDataCompetition phaseD = new PhaseDataCompetition();
					phaseD.setPhaseName("Phase D");
					marathon.getCompetitionPhases().put(Phase.D, phaseD);
					final PhaseDataCompetition phaseE = new PhaseDataCompetition();
					phaseE.setPhaseName("Phase E");
					marathon.getCompetitionPhases().put(Phase.E, phaseE);
				}
				return marathon;
			}
		});
	}

	private String makeOutputFilename() {
		return "marathon-" + System.currentTimeMillis() + ".pdf";
	}

	private void model2Container(final PhaseDataCompetition phaseData, final BeanItemContainer<TimeEntry> itemContainer) {
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

				return new Button("PDF");
			}
		});
		table.addGeneratedColumn("Phase A Zettel", createPhaseCheckbox(Phase.A));
		table.addGeneratedColumn("Phase D Zettel", createPhaseCheckbox(Phase.D));
		table.addGeneratedColumn("Phase E Zettel", createPhaseCheckbox(Phase.E));
		final DurationFieldFactory fieldFactory = new DurationFieldFactory();
		table.addGeneratedColumn("Phase A Start", createPhaseStartInput(fieldFactory, Phase.A));
		table.addGeneratedColumn("Phase D Start", createPhaseStartInput(fieldFactory, Phase.D));
		table.addGeneratedColumn("Phase E Start", createPhaseStartInput(fieldFactory, Phase.E));
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

	private DataBinder<PhaseDataCompetition> showPhaseData(final FormLayout layout, final Phase phase) {
		layout.setMargin(true);
		final BeanFieldGroup<PhaseDataCompetition> binder = new BeanFieldGroup<PhaseDataCompetition>(PhaseDataCompetition.class);
		final DurationFieldFactory fieldFactory = new DurationFieldFactory();
		binder.setFieldFactory(fieldFactory);

		// layout.addComponent(binder.buildAndBind("geplante Startzeit", "startTime"));
		// layout.addComponent(binder.buildAndBind("Maximale Zeit", "maxTime"));
		// layout.addComponent(binder.buildAndBind("Minimale Zeit", "minTime"));
		layout.addComponent(binder.buildAndBind("Name der Phase", "phaseName"));
		layout.addComponent(binder.buildAndBind("Länge in m", "length"));
		// layout.addComponent(binder.buildAndBind("Geschwindigkeit im m/s", "velocity"));
		// layout.addComponent(binder.buildAndBind("Tabelle", "entries"));

		final BeanItemContainer<TimeEntry> itemContainer = new BeanItemContainer<TimeEntry>(TimeEntry.class);

		final Table table = new Table("Strecke");
		table.setContainerDataSource(itemContainer);
		table.setEditable(true);
		table.setSortEnabled(false);
		table.setNullSelectionAllowed(true);
		layout.addComponent(table);
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {

			@Override
			public void drop(final DragAndDropEvent dropEvent) {
				final DataBoundTransferable t = (DataBoundTransferable) dropEvent.getTransferable();
				final TimeEntry sourceItemId = (TimeEntry) t.getItemId(); // returns our Bean

				final AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
				final TimeEntry targetItemId = (TimeEntry) dropData.getItemIdOver(); // returns our Bean

				// No move if source and target are the same, or there is no target
				if (sourceItemId == targetItemId || targetItemId == null)
					return;

				// Let's remove the source of the drag so we can add it back where requested...
				itemContainer.removeItem(sourceItemId);

				if (dropData.getDropLocation() == VerticalDropLocation.BOTTOM) {
					itemContainer.addItemAfter(targetItemId, sourceItemId);
				} else {
					final Object prevItemId = itemContainer.prevItemId(targetItemId);
					itemContainer.addItemAfter(prevItemId, sourceItemId);
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
			public Field<?> createField(final Container container, final Object itemId, final Object propertyId, final Component uiContext) {
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
				itemContainer.addBean(new TimeEntry());
			}
		}));

		final DataBinder<PhaseDataCompetition> phaseDataBinder = new DataBinder<PhaseDataCompetition>() {

			private PhaseDataCompetition phaseData;

			@Override
			public void bindData(final PhaseDataCompetition phaseData) {
				this.phaseData = phaseData;
				binder.setItemDataSource(phaseData);
				itemContainer.removeAllItems();
				itemContainer.addAll(phaseData.getEntries());
			}

			@Override
			public void commitHandler() {
				try {
					binder.commit();
					container2Model(itemContainer, phaseData);
				} catch (final CommitException e) {
					throw new RuntimeException("Cannot commit", e);
				}
			}

			@Override
			public PhaseDataCompetition getCurrentData() {
				return phaseData;
			}
		};
		layout.addComponent(new Button("Reset Strecke", new ClickListener() {

			@Override
			public void buttonClick(final ClickEvent event) {
				phaseDataBinder.commitHandler();

				final PhaseDataCompetition phaseData = phaseDataBinder.getCurrentData();
				phaseData.setDefaultPoints();

				model2Container(phaseData, itemContainer);
				System.out.println(phaseData);
			}
		}));
		return phaseDataBinder;
	}
}

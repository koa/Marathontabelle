package ch.bergturbenthal.marathontabelle.web;

import java.util.List;

import javax.servlet.annotation.WebServlet;

import org.joda.time.Duration;

import ch.bergturbenthal.marathontabelle.model.PhaseData;
import ch.bergturbenthal.marathontabelle.model.TimeEntry;
import ch.bergturbenthal.marathontabelle.web.binding.DurationFieldFactory;

import com.vaadin.annotations.Theme;
import com.vaadin.annotations.VaadinServletConfiguration;
import com.vaadin.data.Container;
import com.vaadin.data.fieldgroup.BeanFieldGroup;
import com.vaadin.data.fieldgroup.FieldGroup.CommitException;
import com.vaadin.data.util.BeanItemContainer;
import com.vaadin.event.DataBoundTransferable;
import com.vaadin.event.dd.DragAndDropEvent;
import com.vaadin.event.dd.DropHandler;
import com.vaadin.event.dd.acceptcriteria.AcceptCriterion;
import com.vaadin.event.dd.acceptcriteria.SourceIs;
import com.vaadin.server.VaadinRequest;
import com.vaadin.server.VaadinServlet;
import com.vaadin.shared.ui.dd.VerticalDropLocation;
import com.vaadin.ui.AbstractSelect.AbstractSelectTargetDetails;
import com.vaadin.ui.AbstractTextField;
import com.vaadin.ui.Button;
import com.vaadin.ui.Button.ClickEvent;
import com.vaadin.ui.Button.ClickListener;
import com.vaadin.ui.Component;
import com.vaadin.ui.Field;
import com.vaadin.ui.FormLayout;
import com.vaadin.ui.Table;
import com.vaadin.ui.Table.ColumnGenerator;
import com.vaadin.ui.Table.TableDragMode;
import com.vaadin.ui.TableFieldFactory;
import com.vaadin.ui.UI;

@Theme("mytheme")
@SuppressWarnings("serial")
public class MyVaadinUI extends UI {

	@WebServlet(value = "/*", asyncSupported = true)
	@VaadinServletConfiguration(productionMode = false, ui = MyVaadinUI.class, widgetset = "ch.bergturbenthal.marathontabelle.web.AppWidgetSet")
	public static class Servlet extends VaadinServlet {
	}

	@Override
	protected void init(VaadinRequest request) {
		final FormLayout layout = new FormLayout();
		layout.setMargin(true);
		setContent(layout);

		// Button button = new Button("Click Me");
		// button.addClickListener(new Button.ClickListener() {
		// public void buttonClick(ClickEvent event) {
		// layout.addComponent(new Label("Thank you for clicking"));
		// }
		// });
		// layout.addComponent(button);

		final BeanFieldGroup<PhaseData> binder = new BeanFieldGroup<PhaseData>(PhaseData.class);
		final DurationFieldFactory fieldFactory = new DurationFieldFactory();
		binder.setFieldFactory(fieldFactory);
		final PhaseData phaseData = new PhaseData();
		phaseData.setLength(Integer.valueOf(5200));
		phaseData.setVelocity(Double.valueOf(13));
		phaseData.setMaxTime(Duration.standardMinutes(5));
		phaseData.setDefaultPoints();
		binder.setItemDataSource(phaseData);

		layout.addComponent(binder.buildAndBind("geplante Startzeit", "startTime"));
		layout.addComponent(binder.buildAndBind("Maximale Zeit", "maxTime"));
		layout.addComponent(binder.buildAndBind("Minimale Zeit", "minTime"));
		layout.addComponent(binder.buildAndBind("Länge in m", "length"));
		layout.addComponent(binder.buildAndBind("Geschwindigkeit im m/s", "velocity"));
		// layout.addComponent(binder.buildAndBind("Tabelle", "entries"));

		final BeanItemContainer<TimeEntry> itemContainer = new BeanItemContainer<TimeEntry>(TimeEntry.class);
		itemContainer.addAll(phaseData.getEntries());

		final Table table = new Table("Strecke");
		table.setContainerDataSource(itemContainer);
		table.setEditable(true);
		table.setSortEnabled(false);
		table.setNullSelectionAllowed(true);
		layout.addComponent(table);
		table.setDragMode(TableDragMode.ROW);
		table.setDropHandler(new DropHandler() {

			@Override
			public AcceptCriterion getAcceptCriterion() {
				return new SourceIs(table);
			}

			@Override
			public void drop(DragAndDropEvent dropEvent) {
				DataBoundTransferable t = (DataBoundTransferable) dropEvent.getTransferable();
				TimeEntry sourceItemId = (TimeEntry) t.getItemId(); // returns our Bean

				AbstractSelectTargetDetails dropData = ((AbstractSelectTargetDetails) dropEvent.getTargetDetails());
				TimeEntry targetItemId = (TimeEntry) dropData.getItemIdOver(); // returns our Bean

				// No move if source and target are the same, or there is no target
				if (sourceItemId == targetItemId || targetItemId == null)
					return;

				// Let's remove the source of the drag so we can add it back where requested...
				itemContainer.removeItem(sourceItemId);

				if (dropData.getDropLocation() == VerticalDropLocation.BOTTOM) {
					itemContainer.addItemAfter(targetItemId, sourceItemId);
				} else {
					Object prevItemId = itemContainer.prevItemId(targetItemId);
					itemContainer.addItemAfter(prevItemId, sourceItemId);
				}
			}
		});
		table.addGeneratedColumn("", new ColumnGenerator() {

			@Override
			public Object generateCell(final Table source, final Object itemId, Object columnId) {
				Button button = new Button("Löschen");
				button.addClickListener(new ClickListener() {
					@Override
					public void buttonClick(ClickEvent event) {
						source.getContainerDataSource().removeItem(itemId);
					}
				});
				return button;
			}
		});
		TableFieldFactory tableFieldFactory = new TableFieldFactory() {

			@Override
			public Field<?> createField(Container container, Object itemId, Object propertyId, Component uiContext) {
				Class<?> type = container.getType(propertyId);
				Field field = fieldFactory.createField(type, Field.class);
				if (field instanceof AbstractTextField) {
					AbstractTextField abstractTextField = (AbstractTextField) field;
					abstractTextField.setNullRepresentation("");
					abstractTextField.setValidationVisible(true);
				}
				return field;
			}
		};
		table.setTableFieldFactory(tableFieldFactory);

		layout.addComponent(new Button("Neuer Steckenpunkt", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				itemContainer.addBean(new TimeEntry());
			}
		}));

		layout.addComponent(new Button("Reset Strecke", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					container2Model(itemContainer, phaseData);

					phaseData.setDefaultPoints();

					model2Container(phaseData, itemContainer);
					System.out.println(phaseData);
				} catch (CommitException e) {
				}
			}
		}));
		layout.addComponent(new Button("Ok", new ClickListener() {

			@Override
			public void buttonClick(ClickEvent event) {
				try {
					binder.commit();
					container2Model(itemContainer, phaseData);
					System.out.println(phaseData);
					for (TimeEntry component : phaseData.getEntries()) {
						System.out.println("- " + emptyIfNull(component.getComment()) + ": " + emptyIfNull(component.getPosition()));
					}
				} catch (CommitException e) {
				}
			}

		}));

	}

	private void container2Model(final BeanItemContainer<TimeEntry> itemContainer, final PhaseData phaseData) {
		List<TimeEntry> entries = phaseData.getEntries();
		entries.clear();
		entries.addAll(itemContainer.getItemIds());
	}

	private void model2Container(final PhaseData phaseData, final BeanItemContainer<TimeEntry> itemContainer) {
		itemContainer.removeAllItems();
		itemContainer.addAll(phaseData.getEntries());
	}

	private Object emptyIfNull(Object value) {
		if (value == null)
			return null;
		return value;
	}
}

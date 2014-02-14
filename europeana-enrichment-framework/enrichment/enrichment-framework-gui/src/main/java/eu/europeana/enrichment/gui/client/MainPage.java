package eu.europeana.enrichment.gui.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.europeana.enrichment.gui.shared.EntityWrapperDTO;
import eu.europeana.enrichment.gui.shared.InputValueDTO;

public class MainPage implements EntryPoint {

	private RootPanel rootPanel;
	private VerticalPanel sp;
	final EnrichmentServiceAsync enrichmentService = GWT
			.create(EnrichmentService.class);
	final List<InputValueDTO> inputValueDTOs = new ArrayList<InputValueDTO>();
	final TextArea area = new TextArea();
	final TextArea enrichment = new TextArea();

	@Override
	public void onModuleLoad() {
		initialize();

	}

	private void initialize() {
		rootPanel = RootPanel.get();
		sp = new VerticalPanel();
		sp.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		sp.add(createInputArea());
		sp.add(createEnrichmentTable());
		sp.add(createEnrichmentArea());
		rootPanel.setWidth("768px");
		rootPanel.setHeight("1024px");

		rootPanel.add(sp);

	}

	private Widget createEnrichmentArea() {
		final DecoratorPanel headerPanel = new DecoratorPanel();
		final FlexTable headerTable = new FlexTable();
		headerTable.setWidget(0,0,enrichment);
		headerPanel.add(headerTable);
		return headerTable;
	}

	private Widget createEnrichmentTable() {
		Button enrichButton = new Button();
		enrichButton.setText("Enrich values");

		enrichButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				enrichmentService.enrich(inputValueDTOs,
						new AsyncCallback<List<EntityWrapperDTO>>() {

							@Override
							public void onSuccess(List<EntityWrapperDTO> arg0) {
								inputValueDTOs.clear();
								area.setText("");
								enrichment.setText("");
								for (EntityWrapperDTO entityWrapper : arg0) {
									enrichment.setText("Original Field: "
											+ entityWrapper.getOriginalField()
											+ "\nClassName: "
											+ entityWrapper.getClassName()
											+ "\nContextual Entity:"
											+ entityWrapper
													.getContextualEntity());
								}

							}

							@Override
							public void onFailure(Throwable arg0) {
								Window.alert(arg0.getMessage());
							}
						});
			}
		});
		final DecoratorPanel headerPanel = new DecoratorPanel();
		final FlexTable headerTable = new FlexTable();
		headerTable.setWidget(0, 0, area);
		headerTable.setWidget(0, 1, enrichButton);
		headerPanel.add(headerTable);
		return headerPanel;
	}

	private Widget createInputArea() {
		final DecoratorPanel headerPanel = new DecoratorPanel();
		final FlexTable headerTable = new FlexTable();
		final Label lblOriginal = new Label("Original Value");
		final Label lblValue = new Label("Value to enrich");
		final Label lblVocabulary = new Label("Vocabulary to use");
		final TextBox txtOriginal = new TextBox();
		final TextBox txtValue = new TextBox();
		final TextBox txtHidden = new TextBox();
		final ListBox lstSelection = new ListBox(false);
		lstSelection.addItem("CONCEPT");
		lstSelection.addItem("AGENT");
		lstSelection.addItem("TIMESPAN");
		lstSelection.addItem("PLACE");
		lstSelection.addChangeHandler(new ChangeHandler() {

			@Override
			public void onChange(ChangeEvent arg0) {
				txtHidden.setText(lstSelection.getValue(lstSelection
						.getSelectedIndex()));
			}
		});
		Button addButton = new Button("Add for enrichment");
		addButton.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				if (txtValue.getText() != null
						|| txtValue.getText().trim().length() > 0) {
					InputValueDTO inputDTO = new InputValueDTO();
					inputDTO.setOriginalField(txtOriginal.getText());
					inputDTO.setValue(txtValue.getText());
					inputDTO.setVocabulary(txtHidden.getText());
					txtOriginal.setText("");
					txtValue.setText("");
					area.setText(area.getText() + inputDTO.getValue() + " "
							+ inputDTO.getOriginalField() + " "
							+ inputDTO.getVocabulary() + "\n");
					inputValueDTOs.add(inputDTO);

				} else {
					Window.alert("No text provided");
				}

			}
		});

		headerTable.setWidget(0, 0, lblOriginal);
		headerTable.setWidget(0, 1, txtOriginal);
		headerTable.setWidget(1, 0, lblValue);
		headerTable.setWidget(1, 1, txtValue);
		headerTable.setWidget(2, 0, lblVocabulary);
		headerTable.setWidget(2, 1, lstSelection);
		headerTable.setWidget(3, 0, addButton);
		headerPanel.add(headerTable);
		return headerPanel;
	}

}

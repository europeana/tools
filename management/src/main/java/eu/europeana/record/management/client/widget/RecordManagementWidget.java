/*
 * Copyright 2007-2012 The Europeana Foundation
 *
 *  Licenced under the EUPL, Version 1.1 (the "Licence") and subsequent versions as approved
 *  by the European Commission;
 *  You may not use this work except in compliance with the Licence.
 * 
 *  You may obtain a copy of the Licence at:
 *  http://joinup.ec.europa.eu/software/page/eupl
 *
 *  Unless required by applicable law or agreed to in writing, software distributed under
 *  the Licence is distributed on an "AS IS" basis, without warranties or conditions of
 *  any kind, either express or implied.
 *  See the Licence for the specific language governing permissions and limitations under
 *  the Licence.
 */
package eu.europeana.record.management.client.widget;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.DataGrid;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.DOM;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FileUpload;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FormPanel;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitCompleteHandler;
import com.google.gwt.user.client.ui.FormPanel.SubmitEvent;
import com.google.gwt.user.client.ui.FormPanel.SubmitHandler;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.europeana.record.management.client.Messages;
import eu.europeana.record.management.client.RecordService;
import eu.europeana.record.management.client.RecordServiceAsync;
import eu.europeana.record.management.client.SystemService;
import eu.europeana.record.management.client.SystemServiceAsync;
import eu.europeana.record.management.shared.dto.Record;
import eu.europeana.record.management.shared.dto.SystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;
import eu.europeana.record.management.shared.exceptions.UniqueRecordException;

/**
 * Record Management widget to remove a single record, a list of records or a
 * collection of records
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class RecordManagementWidget implements AbstractWidget {
	RecordServiceAsync recordService = GWT.create(RecordService.class);
	final SystemServiceAsync systemService = GWT.create(SystemService.class);

	final List<Record> recordsToRemove = new ArrayList<Record>();
	AsyncDataProvider<Record> recordDataProvider;
	AsyncDataProvider<SystemDTO> systemDP;
	DataGrid<Record> records;
	UserDTO user;

	public Widget createWidget(Object... obj) {
		user = (UserDTO) obj[0];
		recordDataProvider = new AsyncDataProvider<Record>() {

			@Override
			protected void onRangeChanged(HasData<Record> arg0) {
				// TODO Auto-generated method stub

			}
		};

		VerticalPanel vp = new VerticalPanel();
		vp.add(createRecordRemovalPanel());
		vp.add(createRecordPreview());
		vp.add(createCollectionRemovalPanel());
		//vp.add(createOptimizeSolrPanel());
		return vp;
	}

	private Widget createRecordPreview() {
		VerticalPanel vp = new VerticalPanel();

		ProvidesKey<Record> key = new ProvidesKey<Record>() {

			@Override
			public Object getKey(Record arg0) {
				// TODO Auto-generated method stub
				return arg0.getValue();
			}
		};
		records = new DataGrid<Record>();

		TextColumn<Record> recordColumn = new TextColumn<Record>() {

			@Override
			public String getValue(Record arg0) {
				// TODO Auto-generated method stub
				return arg0.getValue();
			}
		};
		ButtonCell clear = new ButtonCell();
		Column<Record, String> clearColumn = new Column<Record, String>(clear) {

			@Override
			public String getValue(Record arg0) {
				return "Clear";
			}
		};
		clearColumn.setFieldUpdater(new FieldUpdater<Record, String>() {

			public void update(int arg0, Record arg1, String arg2) {
				recordsToRemove.remove(arg0);
				recordDataProvider.updateRowCount(recordsToRemove.size(), true);
				recordDataProvider.updateRowData(0, recordsToRemove);
			}
		});

		recordDataProvider.addDataDisplay(records);
		recordDataProvider.updateRowCount(recordsToRemove.size(), true);
		recordDataProvider.updateRowData(0, recordsToRemove);
		// recordDataProvider.setList(recordsToRemove);
		SingleSelectionModel<Record> selectionModel = new SingleSelectionModel<Record>(
				key);
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

					public void onSelectionChange(SelectionChangeEvent arg0) {

					}
				});
		records.setSelectionModel(selectionModel);
		records.addColumn(recordColumn, "Record URL");
		records.addColumn(clearColumn, "Clear Record");
		// vp.add(records);
		records.setSize("1000px", "600px");

		vp.add(records);
		vp.add(InlineHTML.wrap(Document.get().createHRElement()));
		Button deleteRecords = new Button(Messages.DELETERECORDS);
		deleteRecords.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {

				if (Window.confirm("Are you sure you want to delete "
						+ recordsToRemove.size()
						+ " records?\n This operation cannot be undone.")) {
					recordService.delete(recordsToRemove, user,
							new AsyncCallback<Void>() {

								public void onFailure(Throwable arg0) {
									// TODO Auto-generated method stub
									if (arg0 instanceof UniqueRecordException) {
										Window.alert("Record was not unique");
									} else {
										Window.alert("No record was found");
										recordsToRemove.clear();
										recordDataProvider.updateRowCount(
												recordsToRemove.size(), true);
										recordDataProvider.updateRowData(0,
												recordsToRemove);
										createRecordPreview();
									}
								}

								public void onSuccess(Void arg0) {
									// TODO Auto-generated method stub
									Window.alert("Records were removed successfully");
									recordsToRemove.clear();
									recordDataProvider.updateRowCount(
											recordsToRemove.size(), true);
									recordDataProvider.updateRowData(0,
											recordsToRemove);
									createRecordPreview();
								}
							}

					);
				}

			}
		});
		vp.add(deleteRecords);
		return vp;
	}

	private Widget createCollectionRemovalPanel() {
		DecoratorPanel dp = new DecoratorPanel();
		FlexTable ft = new FlexTable();
		Label collectionLabel = new Label(Messages.COLLECTIONNAME);
		final TextBox collectionValue = new TextBox();
		final Button deleteButton = new Button(Messages.REMOVECOLLECTION);
		deleteButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				if (Window
						.confirm("Are you sure you want to remove collection "
								+ collectionValue.getValue())) {
					try {
						recordService.deleteCollection(
								collectionValue.getValue(), user,
								new AsyncCallback<Void>() {

									public void onSuccess(Void arg0) {
										Window.alert("Collection Deleted");

									}

									public void onFailure(Throwable arg0) {
										// TODO Auto-generated method stub
										Window.alert(arg0.getMessage());
									}
								});
					} catch (Exception e) {
						Window.alert(e.getMessage());
					}
				}

			}
		});
		ft.setWidget(0, 0, collectionLabel);
		ft.setWidget(0, 1, collectionValue);
		ft.setWidget(1, 1, deleteButton);
		dp.add(ft);

		return dp;
	}

	private Widget createRecordRemovalPanel() {
		FlexTable table = new FlexTable();
		DecoratorPanel dp = new DecoratorPanel();

		Label fieldLabel = new Label(Messages.FIELD);
		final RadioButton recordLabel = new RadioButton("record",
				Messages.VALUE);
		final RadioButton batchUpload = new RadioButton("batch",
				Messages.BATCHREMOVE);
		recordLabel.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				batchUpload.setValue(false);
				recordLabel.setValue(true);
			}
		});

		batchUpload.addClickHandler(new ClickHandler() {

			@Override
			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				recordLabel.setValue(false);
				batchUpload.setValue(true);
			}
		});
		final TextBox fieldValue = new TextBox();

		final TextBox recordValue = new TextBox();
		final FileUpload fileUpload = new FileUpload();

		final FormPanel formPanel = new FormPanel();
		formPanel.add(fileUpload);
		fileUpload.ensureDebugId("local");
		fileUpload.setName("local");
		setDOMID(fileUpload, "local");
		formPanel.setAction(GWT.getModuleBaseURL() + "upload");
		formPanel.setMethod(FormPanel.METHOD_POST);
		formPanel.setEncoding(FormPanel.ENCODING_MULTIPART);
		final Button add = new Button("Add record(s)");
		add.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				if (recordLabel.getValue()) {
					Record record = new Record();
					record.setField(fieldValue.getValue());
					record.setValue(recordValue.getText());
					recordsToRemove.add(record);
					recordDataProvider.updateRowCount(recordsToRemove.size(),
							true);
					recordDataProvider.updateRowData(0, recordsToRemove);
					createRecordPreview();
				} else if (batchUpload.getValue()) {
					formPanel.submit();
				}
			}
		});
		formPanel.addSubmitHandler(new SubmitHandler() {

			@Override
			public void onSubmit(SubmitEvent arg0) {
				Window.alert("Uploading file");

			}
		});

		formPanel.addSubmitCompleteHandler(new SubmitCompleteHandler() {

			@Override
			public void onSubmitComplete(SubmitCompleteEvent arg0) {
				String[] records = arg0.getResults().split("\n");
				createRecordsFromStringArray(records);
			}

			private void createRecordsFromStringArray(String[] records) {
				for (String record : records) {
					Record recordObj = new Record();
					recordObj.setField(fieldValue.getValue());
					recordObj.setValue(record);
					recordsToRemove.add(recordObj);
					recordDataProvider.updateRowCount(recordsToRemove.size(),
							true);
					recordDataProvider.updateRowData(0, recordsToRemove);
					createRecordPreview();
				}

			}
		});
		table.setWidget(0, 0, fieldLabel);
		table.setWidget(0, 1, fieldValue);
		table.setWidget(1, 0, recordLabel);
		table.setWidget(1, 1, recordValue);
		table.setWidget(2, 0, batchUpload);
		table.setWidget(2, 1, fileUpload);
		table.setWidget(3, 1, add);
		dp.add(table);
		formPanel.add(dp);
		return formPanel;
	}

	private void setDOMID(Widget widg, String id) {
		DOM.setElementProperty(widg.getElement(), "id", id);
	}

	private Widget createOptimizeSolrPanel() {
		DecoratorPanel dp = new DecoratorPanel();
		final List<SystemDTO> solrList = new ArrayList<SystemDTO>();
		systemService.showAllSystems(user,
				new AsyncCallback<List<SystemDTO>>() {

					@Override
					public void onFailure(Throwable arg0) {
						Window.alert("Error retrieving solrs");

					}

					@Override
					public void onSuccess(List<SystemDTO> arg0) {
						for (SystemDTO system : arg0) {
							if (system.getType().equals("SOLR")) {
								solrList.add(system);
							}
						}

					}

				});

		ProvidesKey<SystemDTO> key = new ProvidesKey<SystemDTO>() {

			@Override
			public Object getKey(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getUrl();
			}
		};
		TextColumn<SystemDTO> solrListColumn = new TextColumn<SystemDTO>() {

			@Override
			public String getValue(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getUrl();
			}
		};
		CellTable<SystemDTO> systems = new CellTable<SystemDTO>(key);
		systems.setTitle(Messages.AVAILABLESYSTEMS);
		ButtonCell optimize = new ButtonCell();
		Column<SystemDTO, String> optimizeColumn = new Column<SystemDTO, String>(
				optimize) {

			@Override
			public String getValue(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return "Optimize";

			}
		};

		optimizeColumn.setFieldUpdater(new FieldUpdater<SystemDTO, String>() {

			public void update(int arg0, SystemDTO arg1, String arg2) {
				if (Window
						.confirm("This can take a long time and will make this server unresponsive.\n Are you sure you want to proceed?")) {
					final long now = new Date().getTime();
					systemService.optimize(arg1, user,
							new AsyncCallback<Boolean>() {

								@Override
								public void onSuccess(Boolean arg0) {
									Window.alert("Server optimized. Optimization took "+ (new Date().getTime() - now) +" ms");
								}

								@Override
								public void onFailure(Throwable arg0) {
									Window.alert("Server optimization failed");

								}
							});
				}
			}
		});
		systems.addColumn(solrListColumn,"Solr Servers");
		systems.addColumn(optimizeColumn,"Optimize");
		systemDP.addDataDisplay(systems);
		
		dp.add(systems);
		return dp;
	}
}

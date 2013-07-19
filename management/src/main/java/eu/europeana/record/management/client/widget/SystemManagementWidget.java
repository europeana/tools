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
import java.util.List;

import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.europeana.record.management.client.Messages;
import eu.europeana.record.management.client.SystemService;
import eu.europeana.record.management.client.SystemServiceAsync;
import eu.europeana.record.management.shared.dto.SystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * System Management widget
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class SystemManagementWidget implements AbstractWidget {

	final SystemServiceAsync systemService = GWT.create(SystemService.class);
	final Label systemUrlLabel = new Label(Messages.SYSTEMURL);
	final Label systemTypeLabel = new Label(Messages.SYSTEMTYPE);
	final TextBox systemUrlValue = new TextBox();
	final ListBox systemTypeValue = new ListBox();
	UserDTO user;
	final List<SystemDTO> systemDTOs = new ArrayList<SystemDTO>();
	AsyncDataProvider<SystemDTO> systemDP;

	/**
	 * Instantiate the Widget with a User (The information contained here are
	 * only editable by users with GOD or USERADMIN roles)
	 * 
	 * @param The user to create the widget for
	 */
	public SystemManagementWidget(UserDTO user) {
		this.user = user;
		systemDP = new AsyncDataProvider<SystemDTO>() {

			@Override
			protected void onRangeChanged(HasData<SystemDTO> arg0) {
				// TODO Auto-generated method stub
				getData();

			}

		};
		systemTypeValue.addItem("SOLR");
		systemTypeValue.addItem("MONGO");

	}

	public Widget createWidget(Object... obj) {
		HorizontalPanel sp = new HorizontalPanel();
		sp.add(createNewSystem());
		sp.add(showSystems());
		return sp;
	}

	private Widget showSystems() {
		DecoratorPanel vp = new DecoratorPanel();
		ProvidesKey<SystemDTO> key = new ProvidesKey<SystemDTO>() {

			@Override
			public Object getKey(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getId();
			}
		};
		CellTable<SystemDTO> systems = new CellTable<SystemDTO>(key);
		systems.setTitle(Messages.AVAILABLESYSTEMS);
		TextColumn<SystemDTO> systemName = new TextColumn<SystemDTO>() {

			@Override
			public String getValue(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getUrl();
			}

		};
		TextColumn<SystemDTO> systemType = new TextColumn<SystemDTO>() {

			@Override
			public String getValue(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getType();
			}
		};
		ButtonCell delete = new ButtonCell();
		Column<SystemDTO, String> deleteColumn = new Column<SystemDTO, String>(
				delete) {

			@Override
			public String getValue(SystemDTO arg0) {
				// TODO Auto-generated method stub
				return "Delete";

			}
		};

		deleteColumn.setFieldUpdater(new FieldUpdater<SystemDTO, String>() {

			public void update(int arg0, SystemDTO arg1, String arg2) {
				systemService.deleteSystem(arg1, user,
						new AsyncCallback<Void>() {

							public void onFailure(Throwable arg0) {
								// TODO Auto-generated method stub

							}

							public void onSuccess(Void arg0) {
								// TODO Auto-generated method stub
								Window.alert("System deleted successfully");

								getData();
							}
						});

			}
		});
		systems.addColumn(systemName, Messages.SYSTEMURL);
		systems.addColumn(systemType, Messages.SYSTEMTYPE);
		systems.addColumn(deleteColumn, Messages.REMOVESYSTEM);

		systemDP.addDataDisplay(systems);

		final SingleSelectionModel<SystemDTO> selectionModel = new SingleSelectionModel<SystemDTO>(
				key);
		systems.setSelectionModel(selectionModel);
		selectionModel
				.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

					public void onSelectionChange(SelectionChangeEvent arg0) {
						SystemDTO selectedSystem = selectionModel
								.getSelectedObject();
						systemUrlValue.setText(selectedSystem.getUrl());
						systemTypeValue
								.setSelectedIndex(retrieveSelectedIndex(selectedSystem
										.getType()));
					}

					private int retrieveSelectedIndex(String type) {
						for (int i = 0; i < systemTypeValue.getItemCount(); i++) {
							if (systemTypeValue.getItemText(i).equals(
									type.toString())) {
								return i;
							}
						}
						return 0;
					}
				});
		vp.add(systems);
		return vp;
	}

	private void getData() {
		systemService.showAllSystems(user,
				new AsyncCallback<List<SystemDTO>>() {

					public void onSuccess(List<SystemDTO> arg0) {
						// TODO Auto-generated method stub

						systemDTOs.clear();
						systemDTOs.addAll(arg0);
						systemDP.updateRowCount(systemDTOs.size(), true);
						systemDP.updateRowData(0, systemDTOs);
					}

					public void onFailure(Throwable arg0) {
						// TODO Auto-generated method stub
						Window.alert(arg0.getMessage());
					}
				});
	}

	private Widget createNewSystem() {
		DecoratorPanel vp = new DecoratorPanel();
		FlexTable ft = new FlexTable();
		ft.setWidget(0, 0, systemUrlLabel);
		ft.setWidget(0, 1, systemUrlValue);
		ft.setWidget(1, 0, systemTypeLabel);
		ft.setWidget(1, 1, systemTypeValue);
		Button save = new Button(Messages.SAVE);
		save.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				final SystemDTO systemDTO = new SystemDTO();
				systemDTO.setUrl(systemUrlValue.getValue());
				systemDTO.setType(systemTypeValue.getItemText(systemTypeValue
						.getSelectedIndex()));
				systemService.createSystem(systemDTO, user,
						new AsyncCallback<Void>() {

							public void onSuccess(Void arg0) {
								Window.alert("System saved successfully");
								getData();
							}

							public void onFailure(Throwable arg0) {
								Window.alert(arg0.getMessage());

							}
						});
			}
		});
		ft.setWidget(2, 1, save);
		vp.add(ft);
		return vp;
	}

}

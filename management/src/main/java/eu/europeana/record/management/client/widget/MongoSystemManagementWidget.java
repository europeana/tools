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

import org.postgresql.translation.messages_bg;

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
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;
import com.google.gwt.view.client.AsyncDataProvider;
import com.google.gwt.view.client.HasData;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionChangeEvent;
import com.google.gwt.view.client.SingleSelectionModel;

import eu.europeana.record.management.client.Messages;
import eu.europeana.record.management.client.MongoSystemService;
import eu.europeana.record.management.client.MongoSystemServiceAsync;
import eu.europeana.record.management.database.enums.ProfileType;
import eu.europeana.record.management.shared.dto.MongoSystemDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * System Management widget
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class MongoSystemManagementWidget implements AbstractWidget {

	final MongoSystemServiceAsync systemService = GWT.create(MongoSystemService.class);
	final Label systemUrlsLabel = new Label(Messages.SYSTEMURL);
	final Label mongoDBNameLabel = new Label(Messages.MONGODBNAME);

	final Label profileTypeLabel = new Label(Messages.PROFILETYPE);

	final Label systemUserNameLabel = new Label(Messages.SYSTEMUSERNAME);
	final Label systemPasswordLabel = new Label(Messages.SYSTEMPASSWORD);

	final TextBox systemUrlsValue = new TextBox();
	final TextBox mongoDBNameValue = new TextBox();

	final ListBox profileTypeValue = new ListBox();

	final TextBox systemUserNameValue = new TextBox();
	final PasswordTextBox systemPasswordValue = new PasswordTextBox();

	UserDTO user;
	final List<MongoSystemDTO> systemDTOs = new ArrayList<MongoSystemDTO>();
	AsyncDataProvider<MongoSystemDTO> systemDP;

	/**
	 * Instantiate the Widget with a User (The information contained here are
	 * only editable by users with GOD or USERADMIN roles)
	 * 
	 * @param The
	 *            user to create the widget for
	 */
	public MongoSystemManagementWidget(UserDTO user) {
		this.user = user;
		systemDP = new AsyncDataProvider<MongoSystemDTO>() {

			@Override
			protected void onRangeChanged(HasData<MongoSystemDTO> arg0) {
				// TODO Auto-generated method stub
				getData();

			}

		};

		profileTypeValue.addItem(Messages.PROFILETYPE_ACCEPTANCE_PORTAL, "ACCEPTANCE_PORTAL");
		profileTypeValue.addItem(Messages.PROFILETYPE_LIVE_PORTAL, "LIVE_PORTAL");

	}

	public Widget createWidget(Object... obj) {
		VerticalPanel sp = new VerticalPanel();
		sp.add(showSystems());
		sp.add(createNewSystem());
		sp.setWidth("800px");
		return sp;
	}

	private Widget showSystems() {
		DecoratorPanel vp = new DecoratorPanel();
		ProvidesKey<MongoSystemDTO> key = new ProvidesKey<MongoSystemDTO>() {

			@Override
			public Object getKey(MongoSystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getId();
			}
		};
		CellTable<MongoSystemDTO> systems = new CellTable<MongoSystemDTO>(key);
		systems.setTitle(Messages.AVAILABLESYSTEMS);
		TextColumn<MongoSystemDTO> systemName = new TextColumn<MongoSystemDTO>() {

			@Override
			public String getValue(MongoSystemDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getUrls();
			}

		};

		TextColumn<MongoSystemDTO> systemProfileType = new TextColumn<MongoSystemDTO>() {

			@Override
			public String getValue(MongoSystemDTO arg0) {
				return "ACCEPTANCE_PORTAL".toString().equals(arg0.getProfileType())? Messages.PROFILETYPE_ACCEPTANCE_PORTAL : Messages.PROFILETYPE_LIVE_PORTAL ;
			}
		};

		ButtonCell delete = new ButtonCell();
		Column<MongoSystemDTO, String> deleteColumn = new Column<MongoSystemDTO, String>(delete) {

			@Override
			public String getValue(MongoSystemDTO arg0) {
				// TODO Auto-generated method stub
				return "Delete";

			}
		};

		deleteColumn.setFieldUpdater(new FieldUpdater<MongoSystemDTO, String>() {

			public void update(int arg0, MongoSystemDTO arg1, String arg2) {

				systemService.deleteMongoSystem(arg1, user, new AsyncCallback<Void>() {

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
		systems.addColumn(systemProfileType, Messages.PROFILETYPE);
		systems.addColumn(deleteColumn, Messages.REMOVESYSTEM);

		systemDP.addDataDisplay(systems);

		final SingleSelectionModel<MongoSystemDTO> selectionModel = new SingleSelectionModel<MongoSystemDTO>(key);
		systems.setSelectionModel(selectionModel);
		selectionModel.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			public void onSelectionChange(SelectionChangeEvent arg0) {
				MongoSystemDTO selectedSystem = selectionModel.getSelectedObject();
				systemUrlsValue.setText(selectedSystem.getUrls());
				profileTypeValue.setSelectedIndex(retrieveSelectedIndex(selectedSystem.getProfileType()));
			}

			private int retrieveSelectedIndex(String type) {
				for (int i = 0; i < profileTypeValue.getItemCount(); i++) {
					if (profileTypeValue.getItemText(i).equals(type.toString())) {
						return i;
					}
				}
				return 0;
			}
		});
		vp.add(systems);
		systems.setWidth("800px");
		return vp;
	}

	private void getData() {
		systemService.showAllMongoSystems(user, new AsyncCallback<List<MongoSystemDTO>>() {

			public void onSuccess(List<MongoSystemDTO> arg0) {
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
		
		systemUrlsValue.setWidth("500px");

		ft.setWidget(0, 0, systemUrlsLabel);
		ft.setWidget(0, 1, systemUrlsValue);
		ft.setWidget(1, 0, mongoDBNameLabel);
		ft.setWidget(1, 1, mongoDBNameValue);
		ft.setWidget(2, 0, profileTypeLabel);
		ft.setWidget(2, 1, profileTypeValue);
		ft.setWidget(3, 0, systemUserNameLabel);
		ft.setWidget(3, 1, systemUserNameValue);
		ft.setWidget(4, 0, systemPasswordLabel);
		ft.setWidget(4, 1, systemPasswordValue);

		Button save = new Button(Messages.SAVE);
		save.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				// TODO Auto-generated method stub
				final MongoSystemDTO systemDTO = new MongoSystemDTO();
				systemDTO.setUrls(systemUrlsValue.getValue());
				systemDTO.setProfileType(profileTypeValue.getValue(profileTypeValue.getSelectedIndex()));
				systemDTO.setMongoDBName(mongoDBNameValue.getValue());
				systemDTO.setUserName(systemUserNameValue.getValue());
				systemDTO.setPassword(systemPasswordValue.getValue());
				systemService.createMongoSystem(systemDTO, user, new AsyncCallback<Void>() {

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
		ft.setWidget(5, 1, save);
		ft.setWidth("700px");
		vp.add(ft);
		vp.setWidth("800px");
		return vp;
	}

}

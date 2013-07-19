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
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.TextColumn;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HorizontalPanel;
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
import eu.europeana.record.management.client.UserService;
import eu.europeana.record.management.client.UserServiceAsync;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * Main widget for managing users
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class UserManagementWidget implements AbstractWidget {

	final Label nameLabel = new Label(Messages.NAME);
	final Label surnameLabel = new Label(Messages.SURNAME);
	final Label usernameLabel = new Label(Messages.USERNAME);
	final Label passwordLabel = new Label(Messages.PASSWORD);
	final Label roleLabel = new Label(Messages.ROLE);
	final TextBox nameValue = new TextBox();
	final TextBox surnameValue = new TextBox();
	final TextBox usernameValue = new TextBox();
	final PasswordTextBox passwordValue = new PasswordTextBox();
	final ListBox roleListBox = new ListBox();
	final Button saveUserButton = new Button(Messages.SAVE);
	UserServiceAsync userService = GWT.create(UserService.class);
	UserDTO user;
	final List<UserDTO> userDTOs = new ArrayList<UserDTO>();
	AsyncDataProvider<UserDTO> dataProvider;
	SimplePager pager = new SimplePager();

	/**
	 * Constructor for this Widget. The information depicted in this Widget are
	 * not editable by all users
	 * 
	 * @param The
	 *            user for who the widget is created
	 */
	public UserManagementWidget(UserDTO user) {
		this.user = user;
		dataProvider = new AsyncDataProvider<UserDTO>() {

			@Override
			protected void onRangeChanged(HasData<UserDTO> arg0) {
				// TODO Auto-generated method stub
				getData();
			}
		};
	}

	public Widget createWidget(Object... obj) {
		HorizontalPanel sp = new HorizontalPanel();
		sp.add(createNewUserWidget());
		sp.add(showUsersWidget());

		return sp;
	}

	private Widget showUsersWidget() {

		DecoratorPanel vp = new DecoratorPanel();
		ProvidesKey<UserDTO> key = new ProvidesKey<UserDTO>() {
			@Override
			public Object getKey(UserDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getId();
			}
		};
		CellTable<UserDTO> users = new CellTable<UserDTO>();
		users.setTitle(Messages.AVAILABLEUSERS);
		TextColumn<UserDTO> name = new TextColumn<UserDTO>() {

			@Override
			public String getValue(UserDTO arg0) {

				return arg0.getName();
			}
		};
		name.setSortable(true);

		TextColumn<UserDTO> surname = new TextColumn<UserDTO>() {

			@Override
			public String getValue(UserDTO arg0) {
				return arg0.getSurname();
			}
		};
		surname.setSortable(true);
		TextColumn<UserDTO> username = new TextColumn<UserDTO>() {

			@Override
			public String getValue(UserDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getUsername();
			}
		};
		username.setSortable(true);
		TextColumn<UserDTO> role = new TextColumn<UserDTO>() {

			@Override
			public String getValue(UserDTO arg0) {
				return arg0.getRole();
			}
		};
		role.setSortable(true);

		TextColumn<UserDTO> lastLogin = new TextColumn<UserDTO>() {

			@Override
			public String getValue(UserDTO arg0) {
				// TODO Auto-generated method stub
				return arg0.getDate();
			}
		};

		users.addColumn(name, "Name");
		users.addColumn(surname, "Surname");
		users.addColumn(username, "Username");
		users.addColumn(role, "Role");
		users.addColumn(lastLogin, "Last Login");

		if (!user.getRole().equals("USER")) {
			ButtonCell removeUser = new ButtonCell();
			Column<UserDTO, String> removeUserColumn = new Column<UserDTO, String>(
					removeUser) {

				@Override
				public String getValue(UserDTO arg0) {
					// TODO Auto-generated method stub
					return "Remove User";
				}
			};

			removeUserColumn
					.setFieldUpdater(new FieldUpdater<UserDTO, String>() {

						public void update(int arg0, UserDTO arg1, String arg2) {
							// TODO Auto-generated method stub
							userService.deleteUser(arg1, user,
									new AsyncCallback<Void>() {

										public void onFailure(Throwable arg0) {
											// TODO Auto-generated method stub

										}

										public void onSuccess(Void arg0) {
											Window.alert("User removed successfully");
											getData();
										}
									});
						}
					});
			users.addColumn(removeUserColumn, "Remove User");
		}

		dataProvider.addDataDisplay(users);

		pager.setDisplay(users);

		final SingleSelectionModel<UserDTO> model = new SingleSelectionModel<UserDTO>();

		users.setSelectionModel(model);
		model.addSelectionChangeHandler(new SelectionChangeEvent.Handler() {

			public void onSelectionChange(SelectionChangeEvent arg0) {
				// TODO Auto-generated method stub
				final UserDTO selection = model.getSelectedObject();
				nameValue.setValue(selection.getName());
				surnameValue.setValue(selection.getSurname());
				usernameValue.setValue(selection.getUsername());
				passwordValue.setValue(selection.getPassword());
				roleListBox.setSelectedIndex(retrieveSelectedRole(selection
						.getRole()));

			}

			private int retrieveSelectedRole(String role) {
				for (int i = 0; i < roleListBox.getItemCount(); i++) {
					if (role.toString().equals(roleListBox.getValue(i))) {
						return i;
					}
				}
				return 0;
			}
		});

		vp.add(users);

		return vp;
	}

	private Widget createNewUserWidget() {
		DecoratorPanel vp = new DecoratorPanel();
		FlexTable ft = new FlexTable();
		ft.setWidget(0, 0, nameLabel);
		ft.setWidget(0, 1, nameValue);
		ft.setWidget(1, 0, surnameLabel);
		ft.setWidget(1, 1, surnameValue);
		ft.setWidget(2, 0, usernameLabel);
		ft.setWidget(2, 1, usernameValue);
		ft.setWidget(3, 0, passwordLabel);
		ft.setWidget(3, 1, passwordValue);
		ft.setWidget(4, 0, roleLabel);
		roleListBox.insertItem("USER", 0);
		roleListBox.insertItem("USERADMIN", 1);
		roleListBox.insertItem("GOD", 2);
		ft.setWidget(4, 1, roleListBox);
		ft.setWidget(5, 0, saveUserButton);
		saveUserButton.setEnabled(!this.user.getRole().equals("USER"));
		saveUserButton.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				final UserDTO newUser = new UserDTO();
				newUser.setName(nameValue.getValue());
				newUser.setSurname(surnameValue.getValue());
				newUser.setUsername(usernameValue.getValue());
				newUser.setPassword(passwordValue.getValue());
				newUser.setRole(roleListBox.getItemText(roleListBox
						.getSelectedIndex()));
//				newUser.setId(user.getId());
				userService.createUser(newUser, user,
						new AsyncCallback<Void>() {

							public void onFailure(Throwable arg0) {
								Window.alert(arg0.getMessage());

							}

							public void onSuccess(Void arg0) {
								Window.alert("User saved");

								getData();
							}
						});
			}
		});
		vp.add(ft);
		return vp;
	}

	private void getData() {
		userService.showUsers(user, new AsyncCallback<List<UserDTO>>() {

			public void onFailure(Throwable arg0) {
				// TODO Auto-generated method stub
				Window.alert(arg0.getMessage());
			}

			public void onSuccess(List<UserDTO> arg0) {
				userDTOs.clear();
				userDTOs.addAll(arg0);
				dataProvider.updateRowCount(userDTOs.size(), true);
				pager.setPageStart(0);
				dataProvider.updateRowData(0, userDTOs);
			}
		});
	}
}

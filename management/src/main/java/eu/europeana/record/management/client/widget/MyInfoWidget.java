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

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.Widget;

import eu.europeana.record.management.client.Messages;
import eu.europeana.record.management.client.UserService;
import eu.europeana.record.management.client.UserServiceAsync;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * Widget to manage the user's own information as password, name and surname
 * More information can be edited in the UserManagement Tab, provided that the
 * user is a user admin
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class MyInfoWidget implements AbstractWidget {

	final Label username = new Label(Messages.USERNAME);
	final Label password = new Label(Messages.PASSWORD);
	final Label role = new Label(Messages.ROLE);
	final Label name = new Label(Messages.NAME);
	final Label surname = new Label(Messages.SURNAME);
	final Label lastLogin = new Label(Messages.LASTLOGIN);
	final TextBox nameValueBox = new TextBox();
	final TextBox surnameValueBox = new TextBox();
	final PasswordTextBox passwordTextBox = new PasswordTextBox();
	final Label nameValue = new Label("");
	final Label surnameValue = new Label("");
	final Label usernameValue = new Label("");
	final FlexTable ft = new FlexTable();
	final Label passwordValue = new Label("");

	public Widget createWidget(Object... obj) {
		DecoratorPanel vp = new DecoratorPanel();

		final UserServiceAsync userService = GWT.create(UserService.class);
		final UserDTO userDTO = (UserDTO) obj[0];
		updateFields(userDTO);

		final Button modify = new Button(Messages.MODIFY);
		final Button save = new Button(Messages.SAVE);
		save.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				final UserDTO updateUser = new UserDTO();
				updateUser.setId(userDTO.getId());
				updateUser.setDate(userDTO.getDate());
				updateUser.setName(nameValueBox.getValue());
				updateUser.setPassword(passwordTextBox.getValue());
				updateUser.setRole(userDTO.getRole());
				updateUser.setSurname(surnameValueBox.getValue());
				updateUser.setUsername(userDTO.getUsername());
				userService.updateUser(updateUser, updateUser,
						new AsyncCallback<Void>() {

							@Override
							public void onFailure(Throwable arg0) {
								Window.alert(arg0.getMessage());

							}

							@Override
							public void onSuccess(Void arg0) {
								userDTO.setName(nameValueBox.getValue());
								userDTO.setPassword(passwordTextBox.getValue());
								userDTO.setSurname(surnameValueBox.getValue());
								Window.alert("user updated successfully");

							}
						});
				ft.setWidget(1, 1, passwordValue);
				ft.setWidget(2, 1, nameValue);
				ft.setWidget(3, 1, surnameValue);
				ft.setWidget(6, 1, modify);
			}
		});
		if (userDTO != null) {
			ft.setWidget(0, 0, username);
			ft.setWidget(1, 0, password);
			ft.setWidget(2, 0, name);
			ft.setWidget(3, 0, surname);
			ft.setWidget(4, 0, role);
			ft.setWidget(5, 0, lastLogin);
			ft.setWidget(6, 1, modify);

			nameValue.setText(userDTO.getName());
			surnameValue.setText(userDTO.getSurname());
			usernameValue.setText(userDTO.getUsername());

			final Label lastLoginValue = new Label(userDTO.getDate());
			final Label roleValue = new Label(userDTO.getRole());

			nameValueBox.setText(userDTO.getName());
			surnameValueBox.setText(userDTO.getSurname());
			passwordTextBox.setText("");
			ft.setWidget(0, 1, usernameValue);
			ft.setWidget(1, 1, passwordValue);
			ft.setWidget(2, 1, nameValue);
			ft.setWidget(3, 1, surnameValue);
			ft.setWidget(4, 1, roleValue);
			ft.setWidget(5, 1, lastLoginValue);
			modify.addClickHandler(new ClickHandler() {

				public void onClick(ClickEvent arg0) {
					if (Window.confirm(userDTO.getName()
							+ " are you sure you want to modify your information?")) {
						generateEditableFields();
					}

				}

				private void generateEditableFields() {
					ft.setWidget(1, 1, passwordTextBox);
					ft.setWidget(2, 1, nameValueBox);
					ft.setWidget(3, 1, surnameValueBox);
					ft.setWidget(6, 1, save);
				}
			});

		}

		vp.add(ft);
		return vp;
	}

	protected void updateFields(UserDTO userDTO) {
		nameValueBox.setText(userDTO.getName());
		surnameValueBox.setText(userDTO.getSurname());
		passwordTextBox.setText("");
		nameValue.setText(userDTO.getName());
		surnameValue.setText(userDTO.getSurname());
		usernameValue.setText(userDTO.getUsername());

	}

}

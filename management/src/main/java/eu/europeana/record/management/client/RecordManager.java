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
package eu.europeana.record.management.client;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.dom.client.Document;
import com.google.gwt.dom.client.HRElement;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedTabPanel;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.InlineHTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PasswordTextBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

import eu.europeana.record.management.client.widget.LogEntryWidget;
import eu.europeana.record.management.client.widget.MyInfoWidget;
import eu.europeana.record.management.client.widget.RecordManagementWidget;
import eu.europeana.record.management.client.widget.SystemManagementWidget;
import eu.europeana.record.management.client.widget.UserManagementWidget;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * Entry point for the UI of the application
 * @author Yorgos.Mamakis@ kb.nl
 */
public class RecordManager implements EntryPoint {
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting
	 * service.
	 */
	private LoginServiceAsync loginService;

	private RootPanel rootPanel;
	private VerticalPanel sp;
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		initialize();

	}

	private void initialize() {
		rootPanel = RootPanel.get();
		loginService = GWT.create(LoginService.class);
		sp = new VerticalPanel();
		sp.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		sp.add(createHeader());
		sp.add(createLoginPage());
		sp.add(createFooter());
		rootPanel.setWidth("768px");
		rootPanel.setHeight("1024px");
		
		rootPanel.add(sp);
		
	}

	private Widget createFooter() {
		VerticalPanel vertP = new VerticalPanel();
		//vertP.add(new Image("vertical.png"));
		HRElement hr = Document.get().createHRElement();
		hr.setAttribute("width", "1024px");
		vertP.add(InlineHTML.wrap(hr));
		HTML lbl = new HTML();
		lbl.setHTML("<p align='center'><font size='2'>Europeana 2013 (C)</font></p>");
		vertP.add(lbl);
		vertP.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		return vertP;
	}

	private Widget createHeader() {
		VerticalPanel vertP = new VerticalPanel();
		HorizontalPanel vp = new HorizontalPanel();
		final HTML label = new HTML("<p><font size='4'><b>"+Messages.MANAGERECORDS+"</b></font></p>");
		
		vp.add(new Image("europeana-logo-en.png"));
		vp.add(label);
		vp.setVerticalAlignment(HasAlignment.ALIGN_MIDDLE);
		vertP.add(vp);
		HRElement hr = Document.get().createHRElement();
		hr.setAttribute("width", "1024px");
		vertP.add(InlineHTML.wrap(hr));
		//vertP.add(new Image("vertical.png"));
		vertP.setHorizontalAlignment(HasAlignment.ALIGN_CENTER);
		return vertP;
	}

	private Widget createLoginPage() {
		final DecoratorPanel loginPanel = new DecoratorPanel();
		final FlexTable loginTable = new FlexTable();
		
		Label username = new Label(Messages.USERNAME);
		Label password = new Label(Messages.PASSWORD);
		final TextBox unameVal = new TextBox();
		final PasswordTextBox passwordVal = new PasswordTextBox();
		Button login = new Button(Messages.LOGIN);
		login.addClickHandler(new ClickHandler() {

			public void onClick(ClickEvent arg0) {
				loginService.userExists(unameVal.getValue(),
						passwordVal.getValue(), new AsyncCallback<UserDTO>() {

							public void onSuccess(UserDTO arg0) {
								if(arg0!=null){
									sp.remove(loginPanel);
									sp.remove(1);
									sp.add(createRecordPanel(arg0));
									sp.add(createFooter());
								}
								else {
									Window.alert("User does not exist/Password is wrong");
								}
							}

							public void onFailure(Throwable arg0) {
								Window.alert(arg0.getMessage());

							}
						});

			}

		});
		loginTable.setWidget(0, 0, username);
		loginTable.setWidget(0, 1, unameVal);
		loginTable.setWidget(1, 0, password);
		loginTable.setWidget(1, 1, passwordVal);
		loginTable.setWidget(2, 1, login);
		
		loginPanel.add(loginTable);
		
		return loginPanel;
		
	}

	private Widget createRecordPanel(UserDTO user) {
		DecoratedTabPanel panel = new DecoratedTabPanel();
		panel.setWidth("768px");
		panel.setHeight("1024px");
		panel.add(new MyInfoWidget().createWidget(user),Messages.MYINFORMATION);
		panel.add(new LogEntryWidget().createWidget(user),Messages.LOGS);
		panel.add(new RecordManagementWidget().createWidget(user),Messages.RECORDMANAGEMENT);
		panel.add(new SystemManagementWidget(user).createWidget(),Messages.SYSTEMMANAGEMENT);
		panel.add(new UserManagementWidget(user).createWidget(),Messages.USERMANAGEMENT);
		panel.setHeight("100%");
		panel.selectTab(0);

		return panel;
	}
}

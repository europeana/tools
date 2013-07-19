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

import java.util.List;

import com.google.gwt.core.shared.GWT;
import com.google.gwt.event.dom.client.ChangeEvent;
import com.google.gwt.event.dom.client.ChangeHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.DecoratorPanel;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.Widget;

import eu.europeana.record.management.client.LogEntryService;
import eu.europeana.record.management.client.LogEntryServiceAsync;
import eu.europeana.record.management.client.Messages;
import eu.europeana.record.management.shared.dto.LogEntryDTO;
import eu.europeana.record.management.shared.dto.UserDTO;

/**
 * The Log Entry presentation Widget
 * @author Yorgos.Mamakis@ kb.nl
 *
 */
public class LogEntryWidget implements AbstractWidget {

	
/**
 * @see LogEntryWidget.java#createWidget()
 */
	public Widget createWidget(Object... obj) {
		DecoratorPanel vp = new DecoratorPanel();
		Label label = new Label(Messages.LOGSFORUSER);
		final ListBox listBox = new ListBox();
		
		listBox.addItem("---------");
		listBox.addItem("By me");
		listBox.addItem("By anyone");
		final LogEntryServiceAsync logEntryService = GWT
				.create(LogEntryService.class);
		final UserDTO user = (UserDTO) obj[0];
		FlexTable ft = new FlexTable();
		ft.setWidget(0, 0, label);
		ft.setWidget(0, 1, listBox);
		final TextArea ta = new TextArea();
		ta.setSize("800px", "600px");
		ft.setWidget(1, 0, ta);
		listBox.setSelectedIndex(0);
		listBox.addChangeHandler(new ChangeHandler() {

			public void onChange(ChangeEvent arg0) {
				if (listBox.getSelectedIndex() == 1) {
					logEntryService.findEntryByUser(user,
							new AsyncCallback<List<LogEntryDTO>>() {

								@Override
								public void onFailure(Throwable arg0) {
									// TODO Auto-generated method stub
									Window.alert(arg0.getMessage());
								}

								@Override
								public void onSuccess(List<LogEntryDTO> arg0) {
									// TODO Auto-generated method stub
									ta.setText(formatAsText(arg0));
								}
							});
				} else if (listBox.getSelectedIndex() == 2) {
					logEntryService
							.findAllEntries(user,new AsyncCallback<List<LogEntryDTO>>() {

								@Override
								public void onFailure(Throwable arg0) {
									// TODO Auto-generated method stub
									Window.alert(arg0.getMessage());
								}

								@Override
								public void onSuccess(List<LogEntryDTO> arg0) {
									// TODO Auto-generated method stub
									ta.setText(formatAsText(arg0));
								}
							});
				}
			}

			private String formatAsText(List<LogEntryDTO> logEntries) {
				String str = "";

				for (LogEntryDTO le : logEntries) {
					str += "[" + le.getTimestamp() + "]: User " + le.getUser().getName()+" "
							+ le.getUser().getSurname() + " (" + le.getUser().getUsername()+ ") performed "
							+ le.getType() + " with message " + le.getMessage()
							+ "\n";
				}
				return str;
				
			}
		});

		vp.add(ft);

		return vp;
	}
}

package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.IOException;
import java.net.ConnectException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;

public class DBPediaEndpointChecks {

	public static void main(String[] args) {
		try {
			List<String> endpoints = FileUtils.readLines(new File(
					"/home/gmamakis/dbpediaEntries"));
			List<String> valid = new ArrayList<String>();
			for (String endpoint : endpoints) {
				try {
					System.out.println("trying endpoint: " + endpoint);
					HttpURLConnection connection = (HttpURLConnection) new URL(
							endpoint).openConnection();
					connection.setConnectTimeout(30000);
					connection.connect();
					if (connection.getResponseCode() < 400) {
						valid.add(endpoint);
						System.out.println("endpoint " + endpoint
								+ " successful");
					}
				} catch (UnknownHostException e) {
					System.out.println("endpoint " + endpoint
							+ " not successful");
				} catch (SocketTimeoutException e) {
					System.out.println(e.getMessage());
				}
			}
			FileUtils.writeLines(new File(
					"/home/gmamakis/validDbpediaEndpoints"), valid);
		} catch (Exception e) {
			e.printStackTrace();
		}

	}

}

package eu.europeana.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

import org.apache.commons.io.IOUtils;

public class URLHeaderTest {

	public static void main(String[] args){
		try {
			URLConnection urlConnection = new URL("http://data.europeana.eu/concept/loc/sh85148236").openConnection();
			urlConnection.setRequestProperty("accept", "application/rdf+xml");
			InputStream inputStream = urlConnection.getInputStream();
			StringWriter writer = new StringWriter();
			IOUtils.copy(inputStream, writer, "UTF-8");
			System.out.println(writer.toString());
			System.err.println(urlConnection.getURL());
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}

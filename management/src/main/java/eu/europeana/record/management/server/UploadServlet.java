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
package eu.europeana.record.management.server;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.FileUploadException;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;
import org.apache.commons.fileupload.servlet.ServletFileUpload;
import org.apache.commons.io.IOUtils;

/**
 * Servlet with upload capabilities for local files
 * 
 * @author Yorgos.Mamakis@ kb.nl
 * 
 */
public class UploadServlet extends HttpServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 8392834299256226392L;

	/**
	 * Initialization method
	 */
	public void init(ServletConfig config) throws ServletException {
		super.init(config);

	}

	/**
	 * Method for handling GET requests. Left here for debugging reasons, will
	 * be removed in the final version as FileUploadServlet does NOT support GET
	 * requiests
	 */
	protected void doGet(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {
		doPost(request, response);
	}

	/**
	 * Method for handling POST requests. It uses two repositories (a temp and a
	 * produxation)
	 */
	protected void doPost(HttpServletRequest request,
			HttpServletResponse response) throws ServletException, IOException {

		DiskFileItemFactory fileItemFactory = new DiskFileItemFactory();
		/*
		 * Set the size threshold, above which content will be stored on disk.
		 */
		fileItemFactory.setSizeThreshold(1024 * 1024); // 1 MB
		/*
		 * Set the temporary directory to store the uploaded files of size above
		 * threshold.
		 */

		ServletFileUpload uploadHandler = new ServletFileUpload(fileItemFactory);
		try {

			@SuppressWarnings("unchecked")
			List<FileItem> items = uploadHandler.parseRequest(request);
			for (FileItem item : items) {

				if (!item.isFormField()) {
					System.out.println("In file upload3");
					List<String> lines = IOUtils.readLines(item
							.getInputStream());
					response.setStatus(HttpServletResponse.SC_OK);
					for (String line : lines) {
						response.getWriter().print(line + "\n");
					}
					System.out.println(response.toString());
					response.flushBuffer();
				}

			}
		} catch (FileUploadException ex) {
			ex.printStackTrace();
			response.setStatus(HttpServletResponse.SC_UNSUPPORTED_MEDIA_TYPE);
			response.getWriter().print("The file was not created");
			response.flushBuffer();
		} catch (Exception ex) {
			ex.printStackTrace();
			response.setStatus(HttpServletResponse.SC_SERVICE_UNAVAILABLE);
			response.getWriter().print("The file was not created");
			response.flushBuffer();
		}

	}

}

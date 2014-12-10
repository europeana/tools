package eu.europeana.corelib.validation;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.StringReader;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.codehaus.plexus.archiver.AbstractUnArchiver;
import org.codehaus.plexus.archiver.ArchiverException;
import org.codehaus.plexus.archiver.tar.TarGZipUnArchiver;
import org.codehaus.plexus.archiver.zip.ZipUnArchiver;
import org.codehaus.plexus.logging.console.ConsoleLogger;
import org.codehaus.plexus.util.StringUtils;
import org.jibx.runtime.BindingDirectory;
import org.jibx.runtime.IBindingFactory;
import org.jibx.runtime.IUnmarshallingContext;
import org.jibx.runtime.JiBXException;

import eu.europeana.corelib.definitions.jibx.RDF;

public class EDMValidator {
	private static IBindingFactory bfact;
	private final static Logger LOG = Logger.getLogger("EDM-Validator");

	public void validate(String fileName) {
		AbstractUnArchiver unzip = null;
		if (fileName.endsWith("tar.gz") || fileName.endsWith("tgz")) {
			unzip = new TarGZipUnArchiver();
		} else if (fileName.endsWith("zip")) {
			unzip = new ZipUnArchiver();
		}
		if (unzip != null) {
			unzip.enableLogging(new ConsoleLogger(
					org.codehaus.plexus.logging.Logger.LEVEL_INFO, "UnArchiver"));
			try {
				bfact = BindingDirectory.getFactory(RDF.class);
				FileUtils.forceMkdir(new File(FileUtils.getTempDirectoryPath()
						+ "/archivetmp"));

				unzip.setDestDirectory(new File(FileUtils
						.getTempDirectoryPath() + "/archivetmp"));
				unzip.extract(fileName,
						new File(FileUtils.getTempDirectoryPath()
								+ "/archivetmp"));
				validateInternal(new File(FileUtils.getTempDirectoryPath()
						+ "/archivetmp"));
			} catch (ArchiverException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			} catch (JiBXException e) {
				e.printStackTrace();
			}
		} else {
			try {
				FileUtils
						.writeStringToFile(new File("report.txt"),
								"Unidentified zipped file provided.\nExpected zip, tar.gz or tgz as input");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	private void validateInternal(File folder) {
		try {
			String log = "";
			for (File f : folder.listFiles()) {
				IUnmarshallingContext uctx = bfact.createUnmarshallingContext();
				try {
					RDF rdf = (RDF) uctx.unmarshalDocument(new StringReader(
							IOUtils.toString(new FileInputStream(f))));
				} catch (JiBXException e) {
					LOG.log(Level.SEVERE, e.getMessage());
					log = f.getName() + ": " + log + "\n" + e.getMessage();
				}

			}
			if (StringUtils.isEmpty(log)) {
				log = "Everything was fine.";
			}
			FileUtils.writeStringToFile(new File("report.txt"), log);
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JiBXException e1) {
			e1.printStackTrace();
		}
	}
}

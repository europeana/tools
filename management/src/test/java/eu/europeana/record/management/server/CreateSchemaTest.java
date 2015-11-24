package eu.europeana.record.management.server;

import org.hibernate.cfg.Configuration;
import org.hibernate.tool.hbm2ddl.SchemaExport;
import org.junit.Test;

import eu.europeana.record.management.database.entity.LogEntry;
import eu.europeana.record.management.database.entity.MongoSystemObj;
import eu.europeana.record.management.database.entity.Session;
import eu.europeana.record.management.database.entity.SolrSystemObj;
import eu.europeana.record.management.database.entity.SystemObj;
import eu.europeana.record.management.database.entity.UserObj;
import eu.europeana.record.management.server.util.PasswordUtils;


public class CreateSchemaTest {

	@Test
	public void schemaExportTest() {
			Configuration cfg = new Configuration();
			cfg.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
			cfg.setProperty("hibernate.hbm2ddl.auto", "create");
		
			cfg.setProperty("hibernate.ejb.naming_strategy", "org.hibernate.cfg.ImprovedNamingStrategy");
			

			cfg.addAnnotatedClass(UserObj.class);
			cfg.addAnnotatedClass(LogEntry.class);
			cfg.addAnnotatedClass(SystemObj.class);
			cfg.addAnnotatedClass(SolrSystemObj.class);
			cfg.addAnnotatedClass(MongoSystemObj.class);
			cfg.addAnnotatedClass(Session.class);

			
			SchemaExport schemaExport = new SchemaExport(cfg);
			schemaExport.setOutputFile("schema.sql");
			schemaExport.create(false, false);
	    }
	
	@Test
	public void testPassword() {
		System.out.println(PasswordUtils.generateMD5("hgeorgiadis"));
	}

}

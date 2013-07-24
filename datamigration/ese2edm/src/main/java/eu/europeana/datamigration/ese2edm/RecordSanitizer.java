package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.solr.client.solrj.SolrServerException;
import org.apache.solr.client.solrj.impl.HttpSolrServer;
import org.apache.solr.common.SolrDocument;
import org.apache.solr.common.SolrDocumentList;
import org.apache.solr.common.params.ModifiableSolrParams;

import com.mongodb.Mongo;
import com.mongodb.MongoException;

import eu.europeana.corelib.definitions.solr.beans.FullBean;
import eu.europeana.corelib.solr.exceptions.MongoDBException;
import eu.europeana.corelib.solr.server.EdmMongoServer;
import eu.europeana.corelib.solr.server.impl.EdmMongoServerImpl;
import eu.europeana.corelib.tools.lookuptable.CollectionMongoServer;
import eu.europeana.corelib.tools.lookuptable.impl.CollectionMongoServerImpl;
import eu.europeana.datamigration.ese2edm.helpers.HashIncrementor;
import eu.europeana.datamigration.ese2edm.sanitizers.Sanitizer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

public class RecordSanitizer implements Sanitizer {

	
	@Override
	public void sanitize() {
		RecordSanitizer2 r1 = new RecordSanitizer2();
		r1.setHash("499");
		r1.setEndHash("500");
		r1.setName("31");
		Thread t1 = new Thread(r1);
		t1.start();
		RecordSanitizer2 r2 = new RecordSanitizer2();
		r2.setHash("997");
		r2.setEndHash("A00");
		r2.setName("32");
		Thread t2 = new Thread(r2);
		t2.start();
		RecordSanitizer2 r3 = new RecordSanitizer2();
		r3.setHash("E97");
		r3.setEndHash("G00");
		r3.setName("33");
		Thread t3 = new Thread(r3);
		t3.start();
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
//		hash=args[0];
//		endHash=args[1];
		new RecordSanitizer().sanitize();

	}

	private class RecordSanitizer2 implements Runnable{
		private String hash;
		private String endHash;
		private String name;
		
		public String getName() {
			return name;
		}

		public void setName(String name) {
			this.name = name;
		}

		public String getHash() {
			return hash;
		}

		public void setHash(String hash) {
			this.hash = hash;
		}

		public String getEndHash() {
			return endHash;
		}

		public void setEndHash(String endHash) {
			this.endHash = endHash;
		}

		@Override
		public void run() {
			// TODO Auto-generated method stub
			try {
				HttpSolrServer edmSolrServer = new HttpSolrServer(PropertyUtils.getWriteServerUrl());
				CollectionMongoServer collectionServer;
				
					collectionServer = new CollectionMongoServerImpl(new Mongo(
							PropertyUtils.getMongoServer(), PropertyUtils.getMongoPort()),
							PropertyUtils.getCollectionDB());
				
				EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
						PropertyUtils.getMongoServer(),
						PropertyUtils.getMongoPort()),
						PropertyUtils.getEuropeanaDB(), "", "");
				
				
				 
				
				int finished=0;
				long started = new Date().getTime();
				while(!StringUtils.equals(hash, "finished")|| !StringUtils.equals(hash, endHash)){
					List<String> idList = new ArrayList<String>();
					System.out.println("Using hash: " + hash +". Found " +idList.size()+" records so far");
					ModifiableSolrParams params = new ModifiableSolrParams();
					params.add("q", "id3hash:"+hash);
					params.add("rows","200000");
					SolrDocumentList docList = edmSolrServer.query(params).getResults();
					int i =0;
					for (SolrDocument doc:docList){
						String about = doc.getFieldValue("europeana_id").toString();
						String collectionNew = StringUtils.split(about, "/")[1];
						String collectionOld = collectionServer.findOldCollectionId(collectionNew);
						String collection =collectionOld!=null?collectionOld:collectionNew;
						
						try{
							FullBean fb = mongoServer.getFullBean(about);
							if(fb.getProvidedCHOs().size()!=1||fb.getAggregations().size()!=1||fb.getEuropeanaAggregation()==null||fb.getProxies().size()!=2){
								idList.add(StringUtils.replace(about, "/"+collectionNew+"/", "/"+collection+"/"));
								System.out.println("Added id: " + about + "("+i+"/"+ docList.size()+") because of incorrect resources");
							}
						} catch(Exception e){
							idList.add(StringUtils.replace(about, "/"+collectionNew+"/", "/"+collection+"/"));
							System.out.println("Added id: " + about + "("+i+"/"+ docList.size()+") because object could not be retrieved");
						}
					i++;	
					}
					hash = HashIncrementor.incrementHash(hash);
					finished += docList.size();
					System.out.println("Went through " + finished +" records in "+ (new Date().getTime() - started) + " ms");
					FileUtils.writeLines(new File("records_to_fix"+name), idList,true);
					
				}
				
				
				//RecordFixer.main(new String[]{"records_to_fix"});
				} catch (UnknownHostException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MongoException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (MongoDBException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (SolrServerException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
		}
		
	}
	
}

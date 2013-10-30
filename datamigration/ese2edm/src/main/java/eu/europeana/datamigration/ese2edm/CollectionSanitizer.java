package eu.europeana.datamigration.ese2edm;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
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
import eu.europeana.datamigration.ese2edm.sanitizers.Sanitizer;
import eu.europeana.datamigration.ese2edm.utils.PropertyUtils;

public class CollectionSanitizer implements Sanitizer {

	@Override
	public void sanitize() {
		CollectionSanitizer2 sanitizer = new CollectionSanitizer2();
		sanitizer.setName("2022313_");
		Thread t1 = new Thread(sanitizer);
		t1.start();
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		new CollectionSanitizer().sanitize();

	}

	private class CollectionSanitizer2 implements Runnable {
		private String name;

		

		public void setName(String name) {
			this.name = name;
		}

		

		@Override
		public void run() {
			try {
				HttpSolrServer edmSolrServer = new HttpSolrServer(
						PropertyUtils.getWriteServerUrl());

				

				EdmMongoServer mongoServer = new EdmMongoServerImpl(new Mongo(
						PropertyUtils.getMongoServer(),
						PropertyUtils.getMongoPort()),
						PropertyUtils.getEuropeanaDB(), "", "");

				int i = 0;
				int k = 0;
				List<String> idList = new ArrayList<String>();
				while (i < 17000) {
					ModifiableSolrParams params = new ModifiableSolrParams();
					params.add("q", "europeana_collectionName:2022313_*");
					params.add("rows", "1000");
					params.add("start", Integer.toString(i));
					SolrDocumentList docList = edmSolrServer.query(params)
							.getResults();
					
					for (SolrDocument doc : docList) {
						String about = doc.getFieldValue("europeana_id")
								.toString();
						//
						try {
							FullBean fb = mongoServer.getFullBean(about);
							if (fb.getProvidedCHOs().size() != 1
									|| fb.getAggregations().size() != 1
									|| fb.getEuropeanaAggregation() == null
									|| fb.getProxies().size() != 2) {
								idList.add(about);
								System.out.println("Added id: " + about + "("
										+ k + "/" + docList.size()
										+ ") because of incorrect resources");
							}
						} catch (Exception e) {
							idList.add(about);
							System.out
									.println("Added id: "
											+ about
											+ "("
											+ k
											+ "/"
											+ docList.size()
											+ ") because object could not be retrieved");
						}
						k++;
					}
					i += 1000;
				}
				FileUtils.writeLines(new File("records_to_fix"+name),idList,true);
				// while(!StringUtils.equals(hash, "finished")||
				// !StringUtils.equals(hash, endHash)){
				// List<String> idList = new ArrayList<String>();
				// System.out.println("Using hash: " + hash +". Found "
				// +idList.size()+" records so far");
				// // ModifiableSolrParams params = new ModifiableSolrParams();
				// params.add("q", "id3hash:"+hash);
				// params.add("rows","200000");
				// SolrDocumentList docList =
				// edmSolrServer.query(params).getResults();
				// int i =0;
				// for (SolrDocument doc:docList){
				// String about = doc.getFieldValue("europeana_id").toString();
				// String collectionNew = StringUtils.split(about, "/")[1];
				// String collectionOld =
				// collectionServer.findOldCollectionId(collectionNew);
				// String collection
				// =collectionOld!=null?collectionOld:collectionNew;
				//
				// try{
				// FullBean fb = mongoServer.getFullBean(about);
				// if(fb.getProvidedCHOs().size()!=1||fb.getAggregations().size()!=1||fb.getEuropeanaAggregation()==null||fb.getProxies().size()!=2){
				// idList.add(StringUtils.replace(about, "/"+collectionNew+"/",
				// "/"+collection+"/"));
				// System.out.println("Added id: " + about + "("+i+"/"+
				// docList.size()+") because of incorrect resources");
				// }
				// } catch(Exception e){
				// idList.add(StringUtils.replace(about, "/"+collectionNew+"/",
				// "/"+collection+"/"));
				// System.out.println("Added id: " + about + "("+i+"/"+
				// docList.size()+") because object could not be retrieved");
				// }
				// i++;
				// }
				// hash = HashIncrementor.incrementHash(hash);
				// finished += docList.size();
				// System.out.println("Went through " + finished
				// +" records in "+ (new Date().getTime() - started) + " ms");
				// FileUtils.writeLines(new File("records_to_fix"+name),
				// idList,true);
				//
				// }

				// RecordFixer.main(new String[]{"records_to_fix"});
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

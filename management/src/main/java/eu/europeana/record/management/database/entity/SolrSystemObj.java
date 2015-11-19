package eu.europeana.record.management.database.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("SOLR")
@NamedQueries({ @NamedQuery(name = "findSolrSystems", query = "Select s from SolrSystemObj s"),
@NamedQuery(name = "findOneSolrSystem", query = "Select s from SolrSystemObj s where s.urls = ?1") })
public class SolrSystemObj extends SystemObj{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private String solrCore;
	
	private String zookeeperURL;
	

	@Column(name = "SOLRCORE")
	public String getSolrCore() {
		return solrCore;
	}

	public void setSolrCore(String solrCore) {
		this.solrCore = solrCore;
	}

	@Column(name = "ZOOKEEPERURL")
	public String getZookeeperURL() {
		return zookeeperURL;
	}

	public void setZookeeperURL(String zookeeperURL) {
		this.zookeeperURL = zookeeperURL;
	}
	
	

}

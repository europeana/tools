package eu.europeana.record.management.database.entity;

import javax.persistence.Column;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;

@Entity
@DiscriminatorValue("MONGO")
@NamedQueries({ @NamedQuery(name = "findMongoSystems", query = "Select s from MongoSystemObj s"),
@NamedQuery(name = "findOneMongoSystem", query = "Select s from MongoSystemObj s where s.urls = ?1") })
public class MongoSystemObj extends SystemObj{
	
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private String mongoDBName;

	@Column(name = "MONGODBNAME")
	public String getMongoDBName() {
		return mongoDBName;
	}

	public void setMongoDBName(String mongoDBName) {
		this.mongoDBName = mongoDBName;
	}
	

}

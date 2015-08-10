package eu.europeana.corelib.ordering.model;

public class NaturalOrderNode implements Comparable<NaturalOrderNode> {

	private String id;
	private String title;
	private String description;
	private String date;
	private String created;
	private String issued;

	public String getDate() {
		return date;
	}

	public void setDate(String date) {
		this.date = date;
	}

	public String getCreated() {
		return created;
	}

	public void setCreated(String created) {
		this.created = created;
	}

	public String getIssued() {
		return issued;
	}

	public void setIssued(String issued) {
		this.issued = issued;
	}

	public long getNodeId() {
		return nodeId;
	}

	public void setNodeId(long nodeId) {
		this.nodeId = nodeId;
	}

	private long nodeId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getTitle() {
		return title;
	}

	public void setTitle(String title) {
		this.title = title;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public int compareTo(NaturalOrderNode o) {
		if (this.title != null && o.title != null) {
			return title.compareToIgnoreCase(o.title);
		} else if (this.issued != null && o.issued != null) {
			return issued.compareToIgnoreCase(o.issued);
		} else if (this.created != null && o.created != null) {
			return created.compareToIgnoreCase(o.created);
		} else if (this.date != null && o.date != null) {
			return date.compareToIgnoreCase(o.date);
		} else if(this.description!=null && o.description!=null) {
			return description.compareToIgnoreCase(o.description);
		} else {
			return id.compareTo(o.id);
		}
	}
}

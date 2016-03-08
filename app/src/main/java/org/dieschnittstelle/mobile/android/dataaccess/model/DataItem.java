package org.dieschnittstelle.mobile.android.dataaccess.model;

import java.io.Serializable;

public class DataItem implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 3731856366358220228L;
	private long id;
	private String name;
	private String description;

	public DataItem(long id, String name, String description) {
		this.id = id;
		this.name = name;
		this.description = description;
	}

	// a default constructor
	public DataItem() {
	}

	public long getId() {
		return this.id;
	}

	public String getName() {
		return this.name;
	}

	public String getDescription() {
		return this.description;
	}

	public void setId(long id) {
		this.id = id;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public boolean equals(Object other) {
		return this.getId() == ((DataItem) other).getId();
	}

	public DataItem updateFrom(DataItem item) {
		this.setDescription(item.getDescription());
		this.setName(item.getName());
		
		return this;
	}

}

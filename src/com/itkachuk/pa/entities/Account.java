package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Account {
	
	@DatabaseField(id = true, unique = true)
	private String name;
	
	@DatabaseField
	private String description;
	
	public Account() {
		// needed by ormlite
	}

	public Account(String name, String description) {
		super();
		this.name = name;
		this.description = description;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return "Account [name=" + name + ", description=" + description + "]";
	}
	
	
}

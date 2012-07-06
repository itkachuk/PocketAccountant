package com.itkachuk.pa.entities;

import com.itkachuk.pa.sectionedList.SectionedListItem;
import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Account implements SectionedListItem {
	
	public static final String ACCOUNT_ID_FIELD_NAME = "id";
	public static final String NAME_FIELD_NAME = "name";
	public static final String CURRENCY_FIELD_NAME = "currency";
	public static final String DESCRIPTION_FIELD_NAME = "description";
	public static final String IS_REMOVABLE_FIELD_NAME = "isRemovable";	
	
	@DatabaseField(generatedId = true, columnName = ACCOUNT_ID_FIELD_NAME)
	private int id;
	
	@DatabaseField(unique = true, columnName = NAME_FIELD_NAME)
	private String name;
	
	@DatabaseField(columnName = CURRENCY_FIELD_NAME)
	private String currency;
	
	@DatabaseField(columnName = DESCRIPTION_FIELD_NAME)
	private String description;
	
	public Account() {
		// needed by ormlite
	}

	public Account(int id, String name, String currency, String description) {
		super();
		this.id = id;
		this.name = name;
		this.currency = currency;
		this.description = description;
	}
	
	public Account(String name, String currency, String description) {
		super();
		this.name = name;
		this.currency = currency;
		this.description = description;
	}

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}
	
	public String getCurrency() {
		return currency;
	}

	public void setCurrency(String currency) {
		this.currency = currency;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public String toString() {
		return name == null ? "" : name;
	}
	
	public boolean equals(Account account) {
		return (id == account.getId());
	}

	@Override
	public boolean isSection() {
		return false;
	}
		
}

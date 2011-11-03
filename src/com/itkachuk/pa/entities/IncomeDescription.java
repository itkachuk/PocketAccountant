package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class IncomeDescription {
	
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(uniqueCombo=true)
	private String description; // it can be subcategory name, or any note for expense/income record

	@DatabaseField(foreign = true, uniqueCombo=true)
	private IncomeCategory parentCategory;

	IncomeDescription() {
		// needed by ormlite
	}
	
	public IncomeDescription(String description, IncomeCategory parentCategory) {
		super();
		this.description = description;
		this.parentCategory = parentCategory;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public IncomeCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(IncomeCategory parentCategory) {
		this.parentCategory = parentCategory;
	}

	@Override
	public String toString() {
		return "IncomeDescription [id=" + id + ", description=" + description
				+ ", parentCategory=" + parentCategory + "]";
	}		
}

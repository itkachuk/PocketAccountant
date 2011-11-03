package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class ExpenseDescription {
	
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(uniqueCombo=true)
	private String description; // it can be subcategory name, or any note for expense/income record

	@DatabaseField(foreign = true, uniqueCombo=true)
	private ExpenseCategory parentCategory;

	ExpenseDescription() {
		// needed by ormlite
	}

	public ExpenseDescription(String description, ExpenseCategory parentCategory) {
		super();
		this.description = description;
		this.parentCategory = parentCategory;
	}

	public int getId() {
		return id;
	}
	
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public ExpenseCategory getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(ExpenseCategory parentCategory) {
		this.parentCategory = parentCategory;
	}

	@Override
	public String toString() {
		return "SubCategory [description=" + description + ", parentCategory="
				+ parentCategory + "]";
	}
	
	
}

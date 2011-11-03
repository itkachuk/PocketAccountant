package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Description {
	
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(uniqueCombo=true)
	private String description; // it can be subcategory name, or any note for expense/income record

	@DatabaseField(canBeNull = false, foreign = true, uniqueCombo=true)
	private Category parentCategory;

	Description() {
		// needed by ormlite
	}

	public Description(String description, Category parentCategory) {
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

	public Category getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(Category parentCategory) {
		this.parentCategory = parentCategory;
	}

	@Override
	public String toString() {
		return "SubCategory [description=" + description + ", parentCategory="
				+ parentCategory + "]";
	}
	
	
}

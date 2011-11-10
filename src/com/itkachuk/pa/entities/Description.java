package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Description {
	public static final String DESCRIPTION_FIELD_NAME = "description";
	public static final String PARENT_CATEGORY_FIELD_NAME = "parentCategory";
	public static final String IS_EXPENSE_FIELD_NAME = "isExpense";
	
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(uniqueCombo = true, columnName = DESCRIPTION_FIELD_NAME)
	private String description; // it can be subcategory name, or any note for expense/income record

	@DatabaseField(canBeNull = false, uniqueCombo = true, columnName = PARENT_CATEGORY_FIELD_NAME)
	private String parentCategory;
	
	@DatabaseField(uniqueCombo = true, columnName = IS_EXPENSE_FIELD_NAME)
	private boolean isExpense; // Expense - true, Income - false

	Description() {
		// needed by ormlite
	}

	public Description(int id, String description, String parentCategory,
			boolean isExpense) {
		super();
		this.description = description;
		this.parentCategory = parentCategory;
		this.isExpense = isExpense;
	}

	public void setId(int id) {
		this.id = id;
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

	public String getParentCategory() {
		return parentCategory;
	}

	public void setParentCategory(String parentCategory) {
		this.parentCategory = parentCategory;
	}
	
	public boolean isExpense() {
		return isExpense;
	}

	public void setExpense(boolean isExpense) {
		this.isExpense = isExpense;
	}

	@Override
	public String toString() {
		return description == null ? "" : description;
	}		
}

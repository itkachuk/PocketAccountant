package com.itkachuk.pa.entities;

import com.j256.ormlite.field.DatabaseField;
import com.j256.ormlite.table.DatabaseTable;

@DatabaseTable
public class Description {
	public static final String DESCRIPTION_FIELD_NAME = "description";
	public static final String CATEGORY_FIELD_NAME = "category";
//	public static final String IS_EXPENSE_FIELD_NAME = "isExpense";
	
	@DatabaseField(generatedId = true)
	private int id;
	
	@DatabaseField(uniqueCombo = true, columnName = DESCRIPTION_FIELD_NAME)
	private String description; // it can be subcategory name, or any note for expense/income record

	@DatabaseField(uniqueCombo = true, foreign = true, canBeNull = false, columnName = CATEGORY_FIELD_NAME)
	private Category category;
	
//	@DatabaseField(uniqueCombo = true, columnName = IS_EXPENSE_FIELD_NAME)
//	private boolean isExpense; // Expense - true, Income - false

	Description() {
		// needed by ormlite
	}

	public Description(int id, String description, Category category) {
		super();
		this.description = description;
		this.category = category;
//		this.isExpense = isExpense;
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

	public Category getCategory() {
		return category;
	}

	public void setCategory(Category category) {
		this.category = category;
	}
	
//	public boolean isExpense() {
//		return isExpense;
//	}
//
//	public void setExpense(boolean isExpense) {
//		this.isExpense = isExpense;
//	}

	@Override
	public String toString() {
		return description == null ? "" : description;
	}		
}
